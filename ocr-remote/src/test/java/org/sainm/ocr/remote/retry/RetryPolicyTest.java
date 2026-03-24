package org.sainm.ocr.remote.retry;

import org.sainm.exception.OcrException;
import org.sainm.ocr.remote.exception.OcrNetworkException;
import org.sainm.ocr.remote.exception.OcrRateLimitException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

class RetryPolicyTest {
    @Test void successOnFirstAttempt() {
        var policy = RetryPolicy.defaultPolicy();
        String result = policy.execute(() -> "success");
        assertThat(result).isEqualTo("success");
    }

    @Test void retryOnNetworkError() {
        var policy = new RetryPolicy(3, 10, 1.5);
        var attempts = new AtomicInteger(0);

        String result = policy.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new OcrNetworkException(500, "Server error");
            }
            return "success";
        });

        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test void noRetryOnClientError() {
        var policy = RetryPolicy.defaultPolicy();

        assertThatThrownBy(() -> policy.execute(() -> {
            throw new OcrNetworkException(400, "Bad request");
        })).isInstanceOf(OcrNetworkException.class)
          .hasMessageContaining("Bad request");
    }

    @Test void retryOnRateLimit() {
        var policy = new RetryPolicy(3, 10, 1.5);
        var attempts = new AtomicInteger(0);

        String result = policy.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 2) {
                throw new OcrRateLimitException(100);
            }
            return "success";
        });

        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test void failAfterMaxAttempts() {
        var policy = new RetryPolicy(2, 10, 1.5);

        assertThatThrownBy(() -> policy.execute(() -> {
            throw new OcrNetworkException(500, "Server error");
        })).isInstanceOf(OcrException.class)
          .hasMessageContaining("failed after 2 attempts");
    }
}
