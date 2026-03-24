package org.sainm;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.*;
import java.awt.image.BufferedImage;
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
                stream.setLeading(16f);
                stream.newLineAtOffset(50, 750);
                
                String line = text + " ".repeat(Math.max(1, 50 - text.length()));
                for (int i = 0; i < 20; i++) {
                    stream.showText(line);
                    stream.newLine();
                }
                stream.endText();
            }
            var out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    public static byte[] singlePageImageOnly() throws IOException {
        return singlePageWithImageColor(Color.BLUE);
    }

    public static byte[] singlePageWithImageColor(Color color) throws IOException {
        try (var doc = new PDDocument()) {
            var page = new PDPage();
            doc.addPage(page);
            BufferedImage image = new BufferedImage(220, 220, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.setColor(color);
            g.fillRect(40, 40, 140, 140);
            g.dispose();

            var pdImage = LosslessFactory.createFromImage(doc, image);
            try (var stream = new PDPageContentStream(doc, page)) {
                stream.drawImage(pdImage, 120, 420, 220, 220);
            }
            var out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    public static byte[] singlePageMixedTextAndImage(String text, Color color) throws IOException {
        try (var doc = new PDDocument()) {
            var page = new PDPage();
            doc.addPage(page);
            BufferedImage image = new BufferedImage(160, 120, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.setColor(color);
            g.fillOval(20, 20, 100, 70);
            g.dispose();

            var pdImage = LosslessFactory.createFromImage(doc, image);
            try (var stream = new PDPageContentStream(doc, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(100, 700);
                stream.showText(text);
                stream.endText();
                stream.drawImage(pdImage, 100, 430, 180, 130);
            }
            var out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }
}
