package org.sainm.extractor;

import org.sainm.exception.PdfParseException;
import org.sainm.model.*;
import org.sainm.spi.OcrProvider;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

final class ImagePdfExtractor implements ContentExtractor {
    private final List<OcrProvider> providers;

    public ImagePdfExtractor(List<OcrProvider> providers) {
        this.providers = providers;
    }

    public static ImagePdfExtractor fromSpi() {
        var providers = ServiceLoader.load(OcrProvider.class).stream()
            .map(ServiceLoader.Provider::get).toList();
        return new ImagePdfExtractor(providers);
    }

    @Override
    public List<PageContent> extract(PdfSource source, CompareOptions options) {
        var provider = selectProvider(options.getOcrOptions());
        try (PDDocument doc = TextPdfExtractor.loadDocument(source)) {
            var renderer = new PDFRenderer(doc);
            List<PageContent> pages = new ArrayList<>();
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                var page = new PageContent();
                page.setPageNumber(i + 1);
                var pdPage = doc.getPage(i);
                page.setWidth(pdPage.getMediaBox().getWidth());
                page.setHeight(pdPage.getMediaBox().getHeight());

                BufferedImage img = renderer.renderImageWithDPI(i, options.getRenderDpi());
                page.setImageBlocks(List.of(fullPageImageBlock(img, i + 1, page.getWidth(), page.getHeight())));
                if (provider == null) {
                    page.setType(PageType.OCR_FAILED);
                    page.setTextBlocks(List.of());
                    pages.add(page);
                    continue;
                }

                try {
                    List<TextBlock> blocks = new ArrayList<>(provider.recognize(img, options.getOcrOptions()));
                    blocks.forEach(b -> b.setSource(TextBlock.Source.OCR));
                    page.setType(PageType.IMAGE);
                    page.setTextBlocks(blocks);
                } catch (RuntimeException e) {
                    page.setType(PageType.OCR_FAILED);
                    page.setTextBlocks(List.of());
                }
                pages.add(page);
            }
            return pages;
        } catch (IOException e) {
            throw new PdfParseException("Failed to render PDF for OCR", e);
        }
    }

    private ImageBlock fullPageImageBlock(BufferedImage image, int pageNumber, double pageWidth, double pageHeight) throws IOException {
        var block = new ImageBlock();
        block.setBbox(new BoundingBox(0, 0, pageWidth, pageHeight, pageNumber));
        var out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        block.setImageData(out.toByteArray());
        return block;
    }

    private OcrProvider selectProvider(OcrOptions options) {
        try {
            return OcrProviderResolver.resolve(providers, options);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
