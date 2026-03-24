package org.sainm.exception;

public class OcrTimeoutException extends OcrException {
    public OcrTimeoutException(String message) { super(message); }
    public OcrTimeoutException(String message, Throwable cause) { super(message, cause); }
}
