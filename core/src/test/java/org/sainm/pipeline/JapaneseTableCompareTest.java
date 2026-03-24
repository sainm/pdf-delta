package org.sainm.pipeline;

import org.junit.jupiter.api.Test;
import org.sainm.JapaneseTablePdfFactory;
import org.sainm.model.CompareOptions;
import org.sainm.model.CompareRequest;
import org.sainm.model.PdfSource;
import org.sainm.report.DiffReportPdfGenerator;

import java.nio.file.Files;
import java.nio.file.Path;

public class JapaneseTableCompareTest {

    @Test
    void generateJapaneseTableDiffReport() throws Exception {
        byte[] pdfA = JapaneseTablePdfFactory.generateV1();
        byte[] pdfB = JapaneseTablePdfFactory.generateV2();

        Path outDir = Path.of("build/contracts");
        Files.createDirectories(outDir);
        Files.write(outDir.resolve("table-v1.pdf"), pdfA);
        Files.write(outDir.resolve("table-v2.pdf"), pdfB);

        var request = new CompareRequest(new PdfSource.Bytes(pdfA), new PdfSource.Bytes(pdfB), new CompareOptions());
        var comparator = new org.sainm.pipeline.PdfComparator();
        var result = comparator.compare(request);

        var report = new DiffReportPdfGenerator().generate(result, pdfA, pdfB);
        Files.write(outDir.resolve("table-diff-report.pdf"), report);

        System.out.println("Output: " + outDir.toAbsolutePath());
    }
}
