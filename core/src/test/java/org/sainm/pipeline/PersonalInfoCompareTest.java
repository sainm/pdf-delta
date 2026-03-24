package org.sainm.pipeline;

import org.junit.jupiter.api.Test;
import org.sainm.PersonalInfoPdfFactory;
import org.sainm.model.CompareRequest;
import org.sainm.model.PdfSource;
import org.sainm.report.DiffReportPdfGenerator;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonalInfoCompareTest {

    @Test
    void generatePersonalInfoDiffReport() throws Exception {
        byte[] pdfA = PersonalInfoPdfFactory.generateV1();
        byte[] pdfB = PersonalInfoPdfFactory.generateV2();

        Path outDir = Path.of("build/contracts");
        Files.createDirectories(outDir);
        Files.write(outDir.resolve("personal-v1.pdf"), pdfA);
        Files.write(outDir.resolve("personal-v2.pdf"), pdfB);

        var result = new PdfComparator().compare(
            new CompareRequest(new PdfSource.Bytes(pdfA), new PdfSource.Bytes(pdfB)));

        System.out.println("=== 個人情報比較結果 ===");
        System.out.printf("差分合計: %d件%n", result.getSummary().totalDiffs());
        for (var item : result.getItems()) {
            System.out.printf("[%s/%s] \"%s\" → \"%s\"%n",
                item.getType(), item.getSeverity(),
                item.getOriginal(), item.getRevised());
        }

        byte[] report = new DiffReportPdfGenerator().generate(result, pdfA, pdfB);
        Files.write(outDir.resolve("personal-diff-report.pdf"), report);
        System.out.println("レポート出力: " + outDir.resolve("personal-diff-report.pdf").toAbsolutePath());

        assertThat(result.getItems()).isNotEmpty();
    }
}
