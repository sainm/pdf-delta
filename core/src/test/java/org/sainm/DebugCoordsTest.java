package org.sainm;

import org.junit.jupiter.api.Test;
import org.sainm.report.TextDiffRenderer;

import java.nio.file.Files;
import java.nio.file.Path;

public class DebugCoordsTest {

    @Test
    void printCoords() throws Exception {
        byte[] pdfA = PersonalInfoPdfFactory.generateV1();

        
        try (var doc = org.apache.pdfbox.Loader.loadPDF(pdfA)) {
            var box = doc.getPage(0).getMediaBox();
            System.out.printf("PDF page size: w=%.1f h=%.1f%n", box.getWidth(), box.getHeight());
        }

        var lines = new TextDiffRenderer().extractLines(pdfA);
        System.out.println("=== First 20 lines (x, y, w, h, text) ===");
        for (int i = 0; i < Math.min(20, lines.size()); i++) {
            var l = lines.get(i);
            System.out.printf("  x=%.1f y=%.1f w=%.1f h=%.1f  [%s]%n",
                l.x(), l.y(), l.w(), l.h(), l.text());
        }
    }
}
