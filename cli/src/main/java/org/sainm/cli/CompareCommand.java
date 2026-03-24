package org.sainm.cli;

import org.sainm.model.*;
import org.sainm.pipeline.PdfComparator;
import org.sainm.spi.ReportRenderer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(name = "compare", mixinStandardHelpOptions = true,
         description = "Compare two PDF files")
public class CompareCommand implements Callable<Integer> {
    @Parameters(index = "0", description = "First PDF file") Path fileA;
    @Parameters(index = "1", description = "Second PDF file") Path fileB;

    @Option(names = "--format", defaultValue = "json",
            description = "Output format: json, html, pdf-annotated, pdf-image-marked (comma-separated)")
    String format;

    @Option(names = "--fuzzy-threshold", defaultValue = "0.85") double fuzzyThreshold;
    @Option(names = "--ignore-whitespace", defaultValue = "true") boolean ignoreWhitespace;
    @Option(names = "--enable-visual-diff", defaultValue = "true") boolean enableVisualDiff;
    @Option(names = "--visual-threshold", defaultValue = "0.01") double visualThreshold;
    @Option(names = "--render-dpi", defaultValue = "150") int renderDpi;
    @Option(names = "--ocr-min-confidence", defaultValue = "0.6") double ocrMinConfidence;
    @Option(names = "--ocr-provider", defaultValue = "AUTO") OcrOptions.ProviderType ocrProvider;
    @Option(names = "--ocr-language", defaultValue = "zh") String ocrLanguage;

    @Override
    public Integer call() throws Exception {
        var options = CompareOptions.defaults();
        options.setFuzzyThreshold(fuzzyThreshold);
        options.setIgnoreWhitespace(ignoreWhitespace);
        options.setEnableVisualDiff(enableVisualDiff);
        options.setVisualDiffThreshold(visualThreshold);
        options.setRenderDpi(renderDpi);
        options.getOcrOptions().setMinConfidence(ocrMinConfidence);
        options.getOcrOptions().setProviderType(ocrProvider);
        options.getOcrOptions().setLanguage(ocrLanguage);

        var request = new CompareRequest(
            new PdfSource.FilePath(fileA), new PdfSource.FilePath(fileB), options);
        var result = new PdfComparator().compare(request);

        var renderers = ServiceLoader.load(ReportRenderer.class).stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toMap(ReportRenderer::formatId, r -> r));

        for (String fmt : format.split(",")) {
            fmt = fmt.trim();
            var renderer = renderers.get(fmt);
            if (renderer == null) {
                System.err.println("Unknown format: " + fmt);
                continue;
            }
            byte[] output = renderer.render(result, options);
            String outFile = fileA.getFileName() + "_vs_" + fileB.getFileName() + "." + fmt;
            Files.write(Path.of(outFile), output);
            System.out.println("Report written: " + outFile);
        }

        if (!result.getOcrFailedPages().isEmpty()) {
            System.err.println("OCR failed on pages: " + result.getOcrFailedPages());
        }
        if (result.getImageComparisonSummary() != null && result.getImageComparisonSummary().getImageLikePages() > 0) {
            var summary = result.getImageComparisonSummary();
            System.out.printf("Image-like pages: %d | Visual diff pages: %d | Visual diff items: %d%n",
                summary.getImageLikePages(), summary.getVisualDiffPages(), summary.getVisualDiffItems());
        }
        return result.getItems().isEmpty() ? 0 : 1;
    }
}
