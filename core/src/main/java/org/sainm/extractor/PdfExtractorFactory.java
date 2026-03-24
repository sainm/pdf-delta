package org.sainm.extractor;

import org.sainm.exception.PdfParseException;
import org.sainm.model.CompareOptions;
import org.sainm.model.PageContent;
import org.sainm.model.PdfSource;
import org.sainm.spi.OcrProvider;
import org.apache.pdfbox.Loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public final class PdfExtractorFactory {
    public static final int STREAMING_THRESHOLD = 500;
    private static final int BATCH_SIZE = 50;

    private final List<OcrProvider> ocrProviders;

    private PdfExtractorFactory(List<OcrProvider> ocrProviders) {
        this.ocrProviders = ocrProviders;
    }

    public static PdfExtractorFactory withNoOcr() {
        return new PdfExtractorFactory(List.of());
    }

    public static PdfExtractorFactory fromSpi() {
        var providers = ServiceLoader.load(OcrProvider.class).stream()
            .map(ServiceLoader.Provider::get).toList();
        return new PdfExtractorFactory(providers);
    }

    public List<PageContent> extract(PdfSource source, CompareOptions options) {
        int pageCount = getPageCount(source);
        if (pageCount > STREAMING_THRESHOLD) {
            return extractStreaming(source, options, pageCount);
        }
        return extractAll(source, options);
    }

    private List<PageContent> extractAll(PdfSource source, CompareOptions options) {
        var textExtractor = new TextPdfExtractor();
        var pages = textExtractor.extract(source, options);

        var imageExtractor = new ImagePdfExtractor(ocrProviders);
        var ocrPages = imageExtractor.extract(source, options);

        for (int i = 0; i < pages.size(); i++) {
            var page = pages.get(i);
            page.setImageBlocks(ocrPages.get(i).getImageBlocks());
            if (page.getType() == org.sainm.model.PageType.IMAGE) {
                pages.set(i, ocrPages.get(i));
            } else if (page.getType() == org.sainm.model.PageType.MIXED) {
                
                if (ocrPages.get(i).getType() == org.sainm.model.PageType.OCR_FAILED) {
                    page.setType(org.sainm.model.PageType.MIXED);
                }
            }
        }
        return pages;
    }

    private List<PageContent> extractStreaming(PdfSource source, CompareOptions options, int total) {
        
        
        
        return extractAll(source, options);
    }

    private int getPageCount(PdfSource source) {
        try (var doc = TextPdfExtractor.loadDocument(source)) {
            return doc.getNumberOfPages();
        } catch (IOException e) {
            throw new PdfParseException("Failed to read page count", e);
        }
    }
}
