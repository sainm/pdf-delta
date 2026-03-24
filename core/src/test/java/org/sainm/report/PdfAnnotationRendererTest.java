package org.sainm.report;

import org.sainm.PdfFixtures;
import org.sainm.model.*;
import org.apache.pdfbox.Loader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PdfAnnotationRendererTest {
    @Test void formatIdIsPdfAnnotated() {
        assertThat(new PdfAnnotationRenderer().formatId()).isEqualTo("pdf-annotated");
    }

    @Test void rendersAnnotatedPdf() throws Exception {
        byte[] pdfA = PdfFixtures.singlePageWithText("Original");
        var result = new CompareResult();
        result.setJobId(UUID.randomUUID().toString());
        result.setItems(List.of());
        result.setSummary(new DiffSummary(0, 0, 0, 0, 0));
        result.setSourceBytesA(pdfA);

        byte[] output = new PdfAnnotationRenderer().render(result, CompareOptions.defaults());
        assertThat(output).isNotEmpty();
        try (var doc = Loader.loadPDF(output)) {
            assertThat(doc.getNumberOfPages()).isGreaterThan(0);
        }
    }

    @Test void imageMarkedRendererProducesPdf() throws Exception {
        byte[] pdfA = PdfFixtures.singlePageWithText("Original");
        byte[] pdfB = PdfFixtures.singlePageWithText("Modified");
        var result = new CompareResult();
        result.setJobId(UUID.randomUUID().toString());
        result.setItems(List.of());
        result.setSummary(new DiffSummary(0, 0, 0, 0, 0));
        result.setSourceBytesA(pdfA);
        result.setSourceBytesB(pdfB);

        byte[] output = new ImageMarkedPdfRenderer().render(result, CompareOptions.defaults());
        assertThat(output).isNotEmpty();
        try (var doc = Loader.loadPDF(output)) {
            assertThat(doc.getNumberOfPages()).isGreaterThan(0);
        }
    }
}
