package org.sainm;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;





public class ContractPdfFactory {

    






    public static byte[] contractV1() throws IOException {
        List<String> lines = List.of(
            "SERVICE AGREEMENT",
            "",
            "Version: 1.0    Date: 2026-01-01",
            "",
            "PARTIES",
            "Client: Acme Corporation",
            "Vendor: TechSolutions Ltd.",
            "",
            "1. SCOPE OF SERVICES",
            "Vendor shall provide software development and consulting services",
            "as described in Schedule A attached hereto.",
            "",
            "2. PAYMENT TERMS",
            "Client shall pay Vendor a total fee of $100,000.",
            "Payment is due within 30 days of invoice.",
            "Late payments incur a penalty of 1.5% per month.",
            "",
            "3. TERM",
            "This Agreement commences on 2026-01-01 and continues",
            "for a period of 12 months unless terminated earlier.",
            "",
            "4. CONFIDENTIALITY",
            "Both parties agree to maintain strict confidentiality",
            "of all proprietary information exchanged hereunder.",
            "",
            "5. LIABILITY",
            "Vendor liability shall not exceed $500,000 in aggregate.",
            "Neither party shall be liable for indirect damages.",
            "",
            "6. GOVERNING LAW",
            "This Agreement shall be governed by the laws of New York.",
            "",
            "SIGNATURES",
            "Client: ____________________  Date: __________",
            "Vendor: ____________________  Date: __________"
        );
        return buildPdf(lines);
    }

    








    public static byte[] contractV2() throws IOException {
        List<String> lines = List.of(
            "SERVICE AGREEMENT",
            "",
            "Version: 2.0    Date: 2026-03-01",
            "",
            "PARTIES",
            "Client: Acme Corporation",
            "Vendor: TechSolutions Ltd.",
            "",
            "1. SCOPE OF SERVICES",
            "Vendor shall provide software development and consulting services",
            "as described in Schedule A attached hereto.",
            "",
            "2. PAYMENT TERMS",
            "Client shall pay Vendor a total fee of $150,000.",
            "Payment is due within 60 days of invoice.",
            "Late payments incur a penalty of 1.5% per month.",
            "",
            "3. TERM",
            "This Agreement commences on 2026-03-01 and continues",
            "for a period of 24 months unless terminated earlier.",
            "",
            "4. CONFIDENTIALITY",
            "Both parties agree to maintain strict confidentiality",
            "of all proprietary information exchanged hereunder.",
            "",
            "5. ARBITRATION",
            "Any dispute arising under this Agreement shall be resolved",
            "by binding arbitration under AAA Commercial Rules.",
            "",
            "6. GOVERNING LAW",
            "This Agreement shall be governed by the laws of New York.",
            "",
            "SIGNATURES",
            "Client: ____________________  Date: __________",
            "Vendor: ____________________  Date: __________"
        );
        return buildPdf(lines);
    }

    private static byte[] buildPdf(List<String> lines) throws IOException {
        try (var doc = new PDDocument()) {
            var page = new PDPage();
            doc.addPage(page);
            var font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            var fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            try (var stream = new PDPageContentStream(doc, page)) {
                float y = 750;
                float margin = 60;
                float lineHeight = 16;
                for (String line : lines) {
                    stream.beginText();
                    boolean isHeader = line.equals("SERVICE AGREEMENT") || line.matches("\\d+\\..*") || line.equals("PARTIES") || line.equals("SIGNATURES");
                    stream.setFont(isHeader ? fontBold : font, isHeader ? 13 : 11);
                    stream.newLineAtOffset(margin, y);
                    stream.showText(line);
                    stream.endText();
                    y -= lineHeight;
                    if (y < 50) break;
                }
            }
            var out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }
}
