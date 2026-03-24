package org.sainm.pipeline;

import org.junit.jupiter.api.Test;
import org.sainm.PersonalInfoPdfFactory;
import org.sainm.model.CompareRequest;
import org.sainm.model.PdfSource;
import org.sainm.report.DiffReportPdfGenerator;
import org.sainm.report.ReportMode;

import java.nio.file.Files;
import java.nio.file.Path;

public class PersonalInfoAllModesTest {

    @Test
    void generateAllModes() throws Exception {
        byte[] pdfA = PersonalInfoPdfFactory.generateV1();
        byte[] pdfB = PersonalInfoPdfFactory.generateV2();

        Path outDir = Path.of("build/contracts");
        Files.createDirectories(outDir);
        Files.write(outDir.resolve("personal-v1.pdf"), pdfA);
        Files.write(outDir.resolve("personal-v2.pdf"), pdfB);

        var result = new PdfComparator().compare(
            new CompareRequest(new PdfSource.Bytes(pdfA), new PdfSource.Bytes(pdfB)));

        
        Files.write(outDir.resolve("report-mode0-textdiff.pdf"),
            new DiffReportPdfGenerator().generate(result, pdfA, pdfB));

        
        Files.write(outDir.resolve("report-modeA-sidebyside.pdf"),
            new DiffReportPdfGenerator().mode(ReportMode.SIDE_BY_SIDE).generate(result, pdfA, pdfB));

        
        Files.write(outDir.resolve("report-modeB-singlepage.pdf"),
            new DiffReportPdfGenerator().mode(ReportMode.SINGLE_PAGE).generate(result, pdfA, pdfB));

        
        Files.write(outDir.resolve("report-modeC-dualpage.pdf"),
            new DiffReportPdfGenerator().mode(ReportMode.DUAL_PAGE).generate(result, pdfA, pdfB));

        
        Files.write(outDir.resolve("report-modeD.html"),
            new DiffReportPdfGenerator().mode(ReportMode.HTML).generate(result, pdfA, pdfB));

        System.out.println("全方式出力完了: " + outDir.toAbsolutePath());
    }
}
