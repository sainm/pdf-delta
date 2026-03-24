package org.sainm.pipeline;

import org.junit.jupiter.api.Test;
import org.sainm.ColumnChangePdfFactory;
import org.sainm.report.DiffReportPdfGenerator;
import org.sainm.report.ReportMode;

import java.nio.file.Files;
import java.nio.file.Path;




class ColumnDeleteTest {

    private static final Path OUT = Path.of("build/contracts");

    @Test
    void generateAllModes() throws Exception {
        Files.createDirectories(OUT);

        byte[] pdfA = ColumnChangePdfFactory.buildV1();
        byte[] pdfB = ColumnChangePdfFactory.buildDeleteOnly();

        Files.write(OUT.resolve("col-delete-v1.pdf"), pdfA);
        Files.write(OUT.resolve("col-delete-v2.pdf"), pdfB);

        Files.write(OUT.resolve("col-delete-mode0.pdf"),
            new DiffReportPdfGenerator().mode(ReportMode.TEXT_DIFF).generate(null, pdfA, pdfB));
        Files.write(OUT.resolve("col-delete-modeA.pdf"),
            new DiffReportPdfGenerator().mode(ReportMode.SIDE_BY_SIDE).generate(null, pdfA, pdfB));
        Files.write(OUT.resolve("col-delete-modeB.pdf"),
            new DiffReportPdfGenerator().mode(ReportMode.SINGLE_PAGE).generate(null, pdfA, pdfB));
        Files.write(OUT.resolve("col-delete-modeC.pdf"),
            new DiffReportPdfGenerator().mode(ReportMode.DUAL_PAGE).generate(null, pdfA, pdfB));
        Files.write(OUT.resolve("col-delete-modeD.html"),
            new DiffReportPdfGenerator().mode(ReportMode.HTML).generate(null, pdfA, pdfB));

        System.out.println("列削除テスト出力完了: " + OUT.toAbsolutePath());
    }
}
