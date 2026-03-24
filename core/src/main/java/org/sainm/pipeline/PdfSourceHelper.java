package org.sainm.pipeline;

import org.sainm.exception.PdfParseException;
import org.sainm.model.PdfSource;

import java.io.IOException;
import java.nio.file.Files;

final class PdfSourceHelper {
    private PdfSourceHelper() {}

    static byte[] toBytes(PdfSource source) {
        try {
            if (source instanceof PdfSource.Bytes bytes) return bytes.data();
            if (source instanceof PdfSource.FilePath filePath) return Files.readAllBytes(filePath.path());
            if (source instanceof PdfSource.Stream stream) return stream.stream().readAllBytes();
            throw new PdfParseException("Unknown PdfSource type: " + source.getClass());
        } catch (IOException e) {
            throw new PdfParseException("Failed to read PDF source bytes", e);
        }
    }
}
