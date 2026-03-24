package org.sainm.exception;

public class PdfParseException extends PdfCompareException {
    public PdfParseException(String message) { super(message); }
    public PdfParseException(String message, Throwable cause) { super(message, cause); }
}
