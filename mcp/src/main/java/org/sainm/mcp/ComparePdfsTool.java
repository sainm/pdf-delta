package org.sainm.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sainm.model.*;
import org.sainm.pipeline.PdfComparator;

import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

public final class ComparePdfsTool {
    private final McpJobStore store;
    private final PdfComparator comparator = new PdfComparator();
    private final ObjectMapper mapper = new ObjectMapper();

    public ComparePdfsTool(McpJobStore store) { this.store = store; }

    public Map<String, Object> execute(Map<String, Object> input) throws Exception {
        String inputType = (String) input.getOrDefault("inputType", "path");
        PdfSource sourceA = resolveSource((String) input.get("fileA"), inputType);
        PdfSource sourceB = resolveSource((String) input.get("fileB"), inputType);
        var opts = buildOptions(input);
        var result = comparator.compare(new CompareRequest(sourceA, sourceB, opts));
        store.put(result.getJobId(), result);
        return Map.of(
            "jobId", result.getJobId(),
            "items", result.getItems(),
            "summary", result.getSummary() != null ? result.getSummary() : new DiffSummary(0, 0, 0, 0, 0),
            "ocrFailedPages", result.getOcrFailedPages(),
            "imageComparisonSummary", result.getImageComparisonSummary(),
            "visualDiffItems", result.getVisualDiffItems(),
            "pageSummaries", result.getPageSummaries(),
            "warnings", result.getWarnings()
        );
    }

    private PdfSource resolveSource(String value, String inputType) {
        return switch (inputType) {
            case "base64" -> new PdfSource.Bytes(Base64.getDecoder().decode(value));
            case "path"   -> new PdfSource.FilePath(Path.of(value));
            default -> throw new IllegalArgumentException("Unknown inputType: " + inputType);
        };
    }

    private CompareOptions buildOptions(Map<String, Object> input) {
        var opts = CompareOptions.defaults();
        if (input.containsKey("options")) {
            try {
                var mcpOpts = mapper.convertValue(input.get("options"), McpCompareOptions.class);
                opts.setFuzzyThreshold(mcpOpts.fuzzyThreshold());
                opts.setIgnoreWhitespace(mcpOpts.ignoreWhitespace());
                opts.setIgnoreNumberFormat(mcpOpts.ignoreNumberFormat());
                opts.setIgnoreDateFormat(mcpOpts.ignoreDateFormat());
                opts.setIgnoreHeaderFooter(mcpOpts.ignoreHeaderFooter());
                opts.setEnableVisualDiff(mcpOpts.enableVisualDiff());
                opts.setVisualDiffThreshold(mcpOpts.visualDiffThreshold());
                opts.setRenderDpi(mcpOpts.renderDpi());
                opts.getOcrOptions().setMinConfidence(mcpOpts.ocrMinConfidence());
            } catch (Exception e) {
                System.err.println("[ComparePdfsTool] Failed to parse options, using defaults: " + e.getMessage());
            }
        }
        return opts;
    }
}
