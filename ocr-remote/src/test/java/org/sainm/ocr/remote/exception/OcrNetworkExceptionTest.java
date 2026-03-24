package org.sainm.ocr.remote.exception;

import org.sainm.exception.OcrException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OcrNetworkExceptionTest {
    @Test void extendsOcrException() {
        var ex = new OcrNetworkException(500, "Server error");
        assertThat(ex).isInstanceOf(OcrException.class);
    }

    @Test void storesStatusCode() {
        var ex = new OcrNetworkException(404, "Not found");
        assertThat(ex.getStatusCode()).isEqualTo(404);
        assertThat(ex.getMessage()).isEqualTo("Not found");
    }
}

class OcrRateLimitExceptionTest {
    @Test void extendsOcrException() {
        var ex = new OcrRateLimitException(5000);
        assertThat(ex).isInstanceOf(OcrException.class);
    }

    @Test void storesRetryAfterMs() {
        var ex = new OcrRateLimitException(3000);
        assertThat(ex.getRetryAfterMs()).isEqualTo(3000);
    }

    @Test void generatesCorrectMessageFormat() {
        var ex = new OcrRateLimitException(5000);
        assertThat(ex.getMessage()).isEqualTo("Rate limit exceeded, retry after 5000ms");
    }
}
