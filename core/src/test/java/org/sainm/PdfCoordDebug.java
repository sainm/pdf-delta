package org.sainm;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.util.List;




public class PdfCoordDebug {

    public static void dump(byte[] pdf) throws Exception {
        try (var doc = Loader.loadPDF(pdf)) {
            PDRectangle box = doc.getPage(0).getMediaBox();
            System.out.printf("Page size: w=%.1f h=%.1f%n", box.getWidth(), box.getHeight());

            var stripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> positions) throws java.io.IOException {
                    if (text.isBlank() || positions.isEmpty()) { super.writeString(text, positions); return; }
                    var first = positions.get(0);
                    
                    
                    System.out.printf("text=%-50s  getX=%.1f getY=%.1f  getXDirAdj=%.1f getYDirAdj=%.1f  h=%.1f%n",
                        "\"" + text.strip() + "\"",
                        first.getX(), first.getY(),
                        first.getXDirAdj(), first.getYDirAdj(),
                        first.getHeightDir());
                    super.writeString(text, positions);
                }
            };
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            stripper.getText(doc);
        }
    }
}
