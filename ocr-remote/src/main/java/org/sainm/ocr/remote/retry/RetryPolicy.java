package org.sainm.ocr.remote.retry;

import org.sainm.exception.OcrException;
import org.sainm.ocr.remote.exception.OcrNetworkException;
import org.sainm.ocr.remote.exception.OcrRateLimitException;

import java.util.function.Supplier;

public class RetryPolicy {
    private final int maxAttempts;
    private final long initialDelayMs;
    private final double backoffMultiplier;

    public RetryPolicy(int maxAttempts, long initialDelayMs, double backoffMultiplier) {
        this.maxAttempts = maxAttempts;
        this.initialDelayMs = initialDelayMs;
        this.backoffMultiplier = backoffMultiplier;
    }

    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(3, 1000, 2.0);
    }

    public <T> T execute(Supplier<T> operation) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (OcrNetworkException e) {
                lastException = e;
                if (isRetryable(e.getStatusCode())) {
                    if (attempt < maxAttempts) {
                        sleep(calculateDelay(attempt));
                        continue;
                    }
                    
                    throw new OcrException("OCR failed after " + attempt + " attempts", e);
                }
                
                throw e;
            } catch (OcrRateLimitException e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    sleep(e.getRetryAfterMs());
                    continue;
                }
                
                throw new OcrException("OCR failed after " + attempt + " attempts", e);
            } catch (Exception e) {
                throw new OcrException("OCR failed after " + attempt + " attempts", e);
            }
        }

        throw new OcrException("OCR failed after " + maxAttempts + " attempts", lastException);
    }

    private boolean isRetryable(int statusCode) {
        return statusCode >= 500 || statusCode == 408 || statusCode == 429;
    }

    private long calculateDelay(int attempt) {
        return (long) (initialDelayMs * Math.pow(backoffMultiplier, attempt - 1));
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OcrException("Retry interrupted", e);
        }
    }
}
