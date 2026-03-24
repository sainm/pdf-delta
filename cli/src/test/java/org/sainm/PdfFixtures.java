package org.sainm;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PdfFixtures {
    public static byte[] singlePageWithText(String text) throws IOException {
        try (var doc = new PDDocument()) {
            var page = new PDPage();
            doc.addPage(page);
            try (var stream = new PDPageContentStream(doc, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(100, 700);
                stream.showText(text);
                stream.endText();
            }
            var out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    public static byte[] singlePageImageOnly() throws IOException {
        try (var doc = new PDDocument()) {
            doc.addPage(new PDPage());
            var out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }
}
