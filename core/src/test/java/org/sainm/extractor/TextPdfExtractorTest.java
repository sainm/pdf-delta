package org.sainm.extractor;

import org.sainm.PdfFixtures;
import org.sainm.model.CompareOptions;
import org.sainm.model.PageType;
import org.sainm.model.PdfSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextPdfExtractorTest {
    @Test void extractsTextBlocksFromTextPdf() throws Exception {
        byte[] pdf = PdfFixtures.singlePageWithText("Hello World");
        var extractor = new TextPdfExtractor();
        var pages = extractor.extract(new PdfSource.Bytes(pdf), CompareOptions.defaults());
        assertThat(pages).hasSize(1);
        assertThat(pages.get(0).getType()).isIn(PageType.TEXT, PageType.MIXED);
        assertThat(pages.get(0).getTextBlocks()).anySatisfy(b ->
            assertThat(b.getText()).contains("Hello World"));
    }

    @Test void pageTypeIsImageWhenNoExtractableText() throws Exception {
        byte[] pdf = PdfFixtures.singlePageImageOnly();
        var extractor = new TextPdfExtractor();
        var pages = extractor.extract(new PdfSource.Bytes(pdf), CompareOptions.defaults());
        assertThat(pages.get(0).getType()).isEqualTo(PageType.IMAGE);
    }
}
