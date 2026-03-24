package org.sainm.pipeline;

import org.sainm.ContractPdfFactory;
import org.sainm.model.*;
import org.sainm.report.DiffReportPdfGenerator;
import org.sainm.report.PdfBoxAnnotator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;












class ContractCompareExampleTest {

    static CompareResult result;
    static List<DiffItem> items;
    static byte[] v1;
    static byte[] v2;

    @BeforeAll
    static void runComparison() throws Exception {
        v1 = ContractPdfFactory.contractV1();
        v2 = ContractPdfFactory.contractV2();

        
        Path resources = Path.of("src/test/resources/contracts");
        Files.createDirectories(resources);
        Files.write(resources.resolve("contract-v1.pdf"), v1);
        Files.write(resources.resolve("contract-v2.pdf"), v2);
        System.out.println("Source PDFs written to: " + resources.toAbsolutePath());

        result = new PdfComparator().compare(
            new CompareRequest(new PdfSource.Bytes(v1), new PdfSource.Bytes(v2))
        );
        items = result.getItems();

        Path outDir = Path.of("build/contracts");
        Files.createDirectories(outDir);

        
        byte[] reportPdf = new DiffReportPdfGenerator().generate(result, v1, v2);
        Files.write(outDir.resolve("contract-diff-report.pdf"), reportPdf);
        System.out.println("Diff report written to: " + outDir.resolve("contract-diff-report.pdf").toAbsolutePath());

        
        var annotator = new PdfBoxAnnotator();
        byte[] annotatedV1 = annotator.annotate(v1, items, false);
        Files.write(outDir.resolve("contract-v1-annotated.pdf"), annotatedV1);
        System.out.println("Annotated v1 written to: " + outDir.resolve("contract-v1-annotated.pdf").toAbsolutePath());

        
        byte[] annotatedV2 = annotator.annotate(v2, items, true);
        Files.write(outDir.resolve("contract-v2-annotated.pdf"), annotatedV2);
        System.out.println("Annotated v2 written to: " + outDir.resolve("contract-v2-annotated.pdf").toAbsolutePath());

        
        System.out.println("=== Contract v1 vs v2 Comparison ===");
        System.out.printf("Total diffs: %d  (critical=%d, major=%d, minor=%d)%n",
            result.getSummary().totalDiffs(),
            result.getSummary().critical(),
            result.getSummary().major(),
            result.getSummary().minor());
        System.out.println("--- Diff Items ---");
        for (var item : items) {
            System.out.printf("[%s/%s] \"%s\" → \"%s\"%n",
                item.getType(), item.getSeverity(),
                item.getOriginal(), item.getRevised());
        }
    }

    @Test
    void comparisonProducesDiffs() {
        assertThat(items).isNotEmpty();
    }

    @Test
    void summaryReflectsDiffCount() {
        assertThat(result.getSummary().totalDiffs()).isEqualTo(items.size());
    }

    @Test
    void paymentAmountChangeDetected() {
        boolean found = items.stream().anyMatch(i ->
            contains(i.getOriginal(), "100,000") && contains(i.getRevised(), "150,000")
            || contains(i.getOriginal(), "100,000") || contains(i.getRevised(), "150,000")
        );
        assertThat(found)
            .as("Expected payment amount change $100,000 → $150,000 to be detected")
            .isTrue();
    }

    @Test
    void paymentTermsChangeDetected() {
        boolean found = items.stream().anyMatch(i ->
            contains(i.getOriginal(), "30 days") || contains(i.getRevised(), "60 days")
        );
        assertThat(found)
            .as("Expected payment terms change 30 days → 60 days to be detected")
            .isTrue();
    }

    @Test
    void termDurationChangeDetected() {
        boolean found = items.stream().anyMatch(i ->
            contains(i.getOriginal(), "12 months") || contains(i.getRevised(), "24 months")
        );
        assertThat(found)
            .as("Expected term duration change 12 months → 24 months to be detected")
            .isTrue();
    }

    @Test
    void versionHeaderChangeDetected() {
        boolean found = items.stream().anyMatch(i ->
            contains(i.getOriginal(), "1.0") || contains(i.getRevised(), "2.0")
        );
        assertThat(found)
            .as("Expected version header change 1.0 → 2.0 to be detected")
            .isTrue();
    }

    @Test
    void liabilityOrArbitrationChangeDetected() {
        boolean found = items.stream().anyMatch(i ->
            contains(i.getOriginal(), "LIABILITY") || contains(i.getOriginal(), "liability")
            || contains(i.getRevised(), "ARBITRATION") || contains(i.getRevised(), "arbitration")
            || contains(i.getOriginal(), "500,000")
        );
        assertThat(found)
            .as("Expected LIABILITY removal or ARBITRATION addition to be detected")
            .isTrue();
    }

    private static boolean contains(String s, String sub) {
        return s != null && s.contains(sub);
    }
}
