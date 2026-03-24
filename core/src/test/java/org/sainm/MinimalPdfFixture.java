package org.sainm;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MinimalPdfFixture {
    public static byte[] bytes() throws IOException {
        try (var doc = new PDDocument()) {
            doc.addPage(new PDPage());
            var out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }
}
