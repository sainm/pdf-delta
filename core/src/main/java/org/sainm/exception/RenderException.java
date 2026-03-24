package org.sainm.exception;

public class RenderException extends PdfCompareException {
    public RenderException(String message) { super(message); }
    public RenderException(String message, Throwable cause) { super(message, cause); }
}
