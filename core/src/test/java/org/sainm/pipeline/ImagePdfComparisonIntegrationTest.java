package org.sainm.pipeline;

import org.junit.jupiter.api.Test;
import org.sainm.PdfFixtures;
import org.sainm.model.CompareOptions;
import org.sainm.model.CompareRequest;
import org.sainm.model.PdfSource;

import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;

class ImagePdfComparisonIntegrationTest {
    @Test void imageOnlyPdfProducesVisualDiffsWithoutChangingTextPath() throws Exception {
        byte[] pdfA = PdfFixtures.singlePageWithImageColor(Color.BLUE);
        byte[] pdfB = PdfFixtures.singlePageWithImageColor(Color.RED);

        var options = CompareOptions.defaults();
        options.setEnableVisualDiff(true);
        var result = new PdfComparator().compare(new CompareRequest(new PdfSource.Bytes(pdfA), new PdfSource.Bytes(pdfB), options));

        assertThat(result.getVisualDiffItems()).isNotEmpty();
        assertThat(result.getImageComparisonSummary()).isNotNull();
        assertThat(result.getImageComparisonSummary().getVisualDiffItems()).isGreaterThan(0);
    }

    @Test void mixedPdfKeepsTextDiffsAndCanAddVisualDiffs() throws Exception {
        byte[] pdfA = PdfFixtures.singlePageMixedTextAndImage("Shared text", Color.BLUE);
        byte[] pdfB = PdfFixtures.singlePageMixedTextAndImage("Shared text", Color.RED);

        var result = new PdfComparator().compare(
            new CompareRequest(new PdfSource.Bytes(pdfA), new PdfSource.Bytes(pdfB), CompareOptions.defaults()));

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getVisualDiffItems()).isNotEmpty();
        assertThat(result.getPageSummaries()).anySatisfy(page -> {
            assertThat(page.getPageNumber()).isEqualTo(1);
            assertThat(page.isHasVisualDiffs()).isTrue();
        });
    }
}
