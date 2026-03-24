package org.sainm.mcp;

public record McpCompareOptions(
    double fuzzyThreshold,
    boolean ignoreWhitespace,
    boolean ignoreNumberFormat,
    boolean ignoreDateFormat,
    boolean ignoreHeaderFooter,
    boolean enableVisualDiff,
    double visualDiffThreshold,
    int renderDpi,
    double ocrMinConfidence
) {
    public McpCompareOptions() { this(0.85, true, true, true, true, true, 0.01, 150, 0.6); }
}
