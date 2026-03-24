package org.sainm.pipeline;

import org.junit.jupiter.api.Test;
import org.sainm.ColumnChangePdfFactory;
import org.sainm.report.DiffReportPdfGenerator;
import org.sainm.report.ReportMode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

class ColumnChangeAllModesTest {

    private static final Path OUT = Path.of("build/contracts");

    @Test
    void generateAllModes() throws Exception {
        Files.createDirectories(OUT);

        byte[] pdfA = ColumnChangePdfFactory.buildV1();
        byte[] pdfB = ColumnChangePdfFactory.buildV2();

        Files.write(OUT.resolve("column-change-v1.pdf"), pdfA);
        Files.write(OUT.resolve("column-change-v2.pdf"), pdfB);

        
        Files.write(OUT.resolve("column-change-mode0-textdiff.pdf"),
            new DiffReportPdfGenerator().mode(ReportMode.TEXT_DIFF).generate(null, pdfA, pdfB));

        
        Files.write(OUT.resolve("column-change-modeA.pdf"),
            new DiffReportPdfGenerator().mode(ReportMode.SIDE_BY_SIDE).generate(null, pdfA, pdfB));

        
        Files.write(OUT.resolve("column-change-modeB.pdf"),
            new DiffReportPdfGenerator().mode(ReportMode.SINGLE_PAGE).generate(null, pdfA, pdfB));

        
        Files.write(OUT.resolve("column-change-modeC.pdf"),
            new DiffReportPdfGenerator().mode(ReportMode.DUAL_PAGE).generate(null, pdfA, pdfB));

        
        Files.write(OUT.resolve("column-change-modeD.html"),
            new DiffReportPdfGenerator().mode(ReportMode.HTML).generate(null, pdfA, pdfB));

        System.out.println("Generated to: " + OUT.toAbsolutePath());
    }
}
