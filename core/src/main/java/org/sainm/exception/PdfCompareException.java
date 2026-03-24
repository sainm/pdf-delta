package org.sainm.exception;

public class PdfCompareException extends RuntimeException {
    public PdfCompareException(String message) { super(message); }
    public PdfCompareException(String message, Throwable cause) { super(message, cause); }
}
