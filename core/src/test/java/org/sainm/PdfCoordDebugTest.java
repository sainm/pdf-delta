package org.sainm;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

class PdfCoordDebugTest {

    @Test
    void printCoords() throws Exception {
        byte[] pdf = ContractPdfFactory.contractV1();
        try (var doc = Loader.loadPDF(pdf)) {
            PDRectangle box = doc.getPage(0).getMediaBox();
            System.out.printf("Page: w=%.1f h=%.1f%n", box.getWidth(), box.getHeight());

            var stripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> positions) throws java.io.IOException {
                    if (text.isBlank() || positions.isEmpty()) { super.writeString(text, positions); return; }
                    var f = positions.get(0);
                    var l = positions.get(positions.size() - 1);
                    System.out.printf("%-55s getX=%.1f getY=%.1f  XAdj=%.1f YAdj=%.1f  h=%.1f%n",
                        "\"" + text.strip() + "\"",
                        f.getX(), f.getY(),
                        f.getXDirAdj(), f.getYDirAdj(),
                        f.getHeightDir());
                    super.writeString(text, positions);
                }
            };
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            stripper.getText(doc);
        }
    }
}
