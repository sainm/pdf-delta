package org.sainm.ocr.remote.exception;

import org.sainm.exception.OcrException;

public class OcrRateLimitException extends OcrException {
    private final long retryAfterMs;

    public OcrRateLimitException(long retryAfterMs) {
        super("Rate limit exceeded, retry after " + retryAfterMs + "ms");
        this.retryAfterMs = retryAfterMs;
    }

    public long getRetryAfterMs() {
        return retryAfterMs;
    }
}
