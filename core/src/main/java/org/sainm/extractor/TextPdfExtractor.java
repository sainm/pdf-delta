package org.sainm.extractor;

import org.sainm.exception.PdfParseException;
import org.sainm.model.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

final class TextPdfExtractor implements ContentExtractor {
    private static final int TEXT_CHAR_THRESHOLD = 50;
    private static final double TEXT_COVERAGE_THRESHOLD = 0.30;

    @Override
    public List<PageContent> extract(PdfSource source, CompareOptions options) {
        try (PDDocument doc = loadDocument(source)) {
            List<PageContent> pages = new ArrayList<>();
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                pages.add(extractPage(doc, i));
            }
            return pages;
        } catch (IOException e) {
            throw new PdfParseException("Failed to extract text from PDF", e);
        }
    }

    private PageContent extractPage(PDDocument doc, int pageIndex) throws IOException {
        var page = new PageContent();
        page.setPageNumber(pageIndex + 1);

        var pdPage = doc.getPage(pageIndex);
        page.setWidth(pdPage.getMediaBox().getWidth());
        page.setHeight(pdPage.getMediaBox().getHeight());

        List<TextBlock> blocks = new ArrayList<>();
        List<TextPosition[]> linePositions = new ArrayList<>();

        var stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> positions) throws IOException {
                if (text.isBlank()) return;
                if (!positions.isEmpty()) {
                    var first = positions.get(0);
                    var last = positions.get(positions.size() - 1);
                    double x = first.getXDirAdj();
                    double y = first.getYDirAdj();
                    double w = last.getXDirAdj() + last.getWidthDirAdj() - x;
                    double h = first.getHeightDir();
                    var block = new TextBlock();
                    block.setText(text);
                    block.setBbox(new BoundingBox(x, y, w, h, getStartPage()));
                    blocks.add(block);
                }
                super.writeString(text, positions);
            }
        };
        stripper.setStartPage(pageIndex + 1);
        stripper.setEndPage(pageIndex + 1);
        stripper.getText(doc);

        page.setTextBlocks(blocks);

        
        
        
        
        int totalChars = blocks.stream().mapToInt(b -> b.getText().length()).sum();
        double pageArea = page.getWidth() * page.getHeight();
        double textArea = blocks.stream().mapToDouble(b -> {
            var bb = b.getBbox();
            return bb == null ? 0 : bb.width() * bb.height();
        }).sum();
        double coverage = pageArea > 0 ? textArea / pageArea : 0;
        PageType type;
        if (totalChars >= TEXT_CHAR_THRESHOLD && coverage >= TEXT_COVERAGE_THRESHOLD) {
            type = PageType.TEXT;
        } else if (totalChars > 0) {
            type = PageType.MIXED;
        } else {
            type = PageType.IMAGE;
        }
        page.setType(type);

        return page;
    }

    static PDDocument loadDocument(PdfSource source) throws IOException {
        if (source instanceof PdfSource.FilePath fp) return Loader.loadPDF(fp.path().toFile());
        if (source instanceof PdfSource.Bytes b)    return Loader.loadPDF(b.data());
        if (source instanceof PdfSource.Stream s)   return Loader.loadPDF(s.stream().readAllBytes());
        throw new PdfParseException("Unknown PdfSource type: " + source.getClass());
    }
}
