package org.sainm.exception;

public class OcrProviderUnavailableException extends OcrException {
    public OcrProviderUnavailableException(String message) { super(message); }
    public OcrProviderUnavailableException(String message, Throwable cause) { super(message, cause); }
}
