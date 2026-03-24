package org.sainm.ocr.remote.exception;

import org.sainm.exception.OcrException;

public class OcrNetworkException extends OcrException {
    private final int statusCode;

    public OcrNetworkException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
