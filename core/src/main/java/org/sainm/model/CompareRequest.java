package org.sainm.model;

public record CompareRequest(PdfSource sourceA, PdfSource sourceB, CompareOptions options) {
    public CompareRequest(PdfSource sourceA, PdfSource sourceB) {
        this(sourceA, sourceB, CompareOptions.defaults());
    }
}
