package org.sainm.mcp;

import org.sainm.model.DiffSummary;

import java.util.Map;

public final class GetDiffSummaryTool {
    private final McpJobStore store;

    public GetDiffSummaryTool(McpJobStore store) { this.store = store; }

    public Map<String, Object> execute(Map<String, Object> input) {
        String jobId = (String) input.get("jobId");
        String language = (String) input.getOrDefault("language", "zh");
        return store.get(jobId).map(result -> {
            var summary = result.getSummary();
            if (summary == null) summary = new DiffSummary(0, 0, 0, 0, 0);
            String natural = buildStatsSummary(summary, language);
            return (Map<String, Object>) Map.of(
                "jobId", jobId,
                "summary", natural,
                "countBySeverity", Map.of(
                    "CRITICAL", summary.critical(),
                    "MAJOR", summary.major(),
                    "MINOR", summary.minor(),
                    "INFO", summary.info()
                ),
                "totalDiffs", summary.totalDiffs()
            );
        }).orElse(Map.of("error", "Job not found: " + jobId));
    }

    private String buildStatsSummary(DiffSummary summary, String language) {
        if ("zh".equals(language)) {
            return String.format("共发现 %d 处差异（严重: %d, 重要: %d, 次要: %d）",
                summary.totalDiffs(), summary.critical(), summary.major(), summary.minor());
        }
        return String.format("Found %d differences (critical: %d, major: %d, minor: %d)",
            summary.totalDiffs(), summary.critical(), summary.major(), summary.minor());
    }
}
