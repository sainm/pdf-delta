package org.sainm.pipeline;

import org.sainm.MinimalPdfFixture;
import org.sainm.model.CompareRequest;
import org.sainm.model.PdfSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PdfComparatorTest {
    @Test void rejectsNullRequest() {
        var comparator = new PdfComparator();
        assertThatThrownBy(() -> comparator.compare(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test void returnsResultWithJobId() throws Exception {
        var src = new PdfSource.Bytes(MinimalPdfFixture.bytes());
        var request = new CompareRequest(src, src);
        var result = new PdfComparator().compare(request);
        assertThat(result.getJobId()).isNotBlank();
        assertThat(result.getItems()).isEmpty();
    }
}
