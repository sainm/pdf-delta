package org.sainm.extractor;

import org.sainm.PdfFixtures;
import org.sainm.model.CompareOptions;
import org.sainm.model.PageType;
import org.sainm.model.PdfSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PdfExtractorFactoryTest {
    @Test void selectsTextExtractorForTextPdf() throws Exception {
        byte[] pdf = PdfFixtures.singlePageWithText("Hello World");
        var factory = PdfExtractorFactory.withNoOcr();
        var pages = factory.extract(new PdfSource.Bytes(pdf), CompareOptions.defaults());
        assertThat(pages.get(0).getType()).isIn(PageType.TEXT, PageType.MIXED);
    }

    @Test void streamingModeActivatesAbove500Pages() {
        assertThat(PdfExtractorFactory.STREAMING_THRESHOLD).isEqualTo(500);
    }
}
