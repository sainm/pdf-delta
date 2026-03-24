package org.sainm.exception;

public class OcrException extends PdfCompareException {
    public OcrException(String message) { super(message); }
    public OcrException(String message, Throwable cause) { super(message, cause); }
}
