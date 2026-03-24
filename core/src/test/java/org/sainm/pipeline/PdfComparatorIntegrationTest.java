package org.sainm.pipeline;

import org.sainm.PdfFixtures;
import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PdfComparatorIntegrationTest {
    @Test void identicalPdfsProduceNoDiffs() throws Exception {
        byte[] pdf = PdfFixtures.singlePageWithText("Hello World");
        var src = new PdfSource.Bytes(pdf);
        var result = new PdfComparator().compare(new CompareRequest(src, src));
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getSummary().totalDiffs()).isEqualTo(0);
        assertThat(result.getVisualDiffItems()).isEmpty();
    }

    @Test void differentPdfsProduceDiffs() throws Exception {
        byte[] pdfA = PdfFixtures.singlePageWithText("Original text content here.");
        byte[] pdfB = PdfFixtures.singlePageWithText("Modified text content here.");
        var result = new PdfComparator().compare(
            new CompareRequest(new PdfSource.Bytes(pdfA), new PdfSource.Bytes(pdfB)));
        assertThat(result.getItems()).isNotEmpty();
    }
}
