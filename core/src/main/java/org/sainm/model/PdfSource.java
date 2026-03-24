package org.sainm.model;

public sealed interface PdfSource permits PdfSource.FilePath, PdfSource.Stream, PdfSource.Bytes {
    record FilePath(java.nio.file.Path path) implements PdfSource {}
    record Stream(java.io.InputStream stream) implements PdfSource {}
    record Bytes(byte[] data) implements PdfSource {}
}
