package org.sainm.model;

import java.util.*;

public final class CompareOptions {
    
    public enum ExtractionMode { AUTO, TEXT, IMAGE }

    private ExtractionMode extractionMode = ExtractionMode.AUTO;
    private Set<PageRange> includePages = new HashSet<>();
    private Set<Region> excludeRegions = new HashSet<>();
    private List<Anchor> anchors = new ArrayList<>();
    private double fuzzyThreshold = 0.85;
    private double positionTolerance = 5.0;
    private Set<DiffSeverity> reportLevels = new HashSet<>();
    private boolean ignoreWhitespace = true;
    private boolean ignoreNumberFormat = true;
    private boolean ignoreDateFormat = true;
    private boolean ignoreHeaderFooter = true;
    private boolean ignoreWatermark = true;
    private boolean enableVisualDiff = true;
    private double visualDiffThreshold = 0.01;
    private int renderDpi = 150;
    private NumberPrefixIgnoreMode sectionNumberMode = NumberPrefixIgnoreMode.IGNORE;
    private OcrOptions ocrOptions = OcrOptions.defaults();

    public static CompareOptions defaults() { return new CompareOptions(); }

    public Set<PageRange> getIncludePages() { return includePages; }
    public Set<Region> getExcludeRegions() { return excludeRegions; }
    public List<Anchor> getAnchors() { return anchors; }
    public double getFuzzyThreshold() { return fuzzyThreshold; }
    public void setFuzzyThreshold(double fuzzyThreshold) { this.fuzzyThreshold = fuzzyThreshold; }
    public double getPositionTolerance() { return positionTolerance; }
    public void setPositionTolerance(double positionTolerance) { this.positionTolerance = positionTolerance; }
    public Set<DiffSeverity> getReportLevels() { return reportLevels; }
    public boolean isIgnoreWhitespace() { return ignoreWhitespace; }
    public void setIgnoreWhitespace(boolean ignoreWhitespace) { this.ignoreWhitespace = ignoreWhitespace; }
    public boolean isIgnoreNumberFormat() { return ignoreNumberFormat; }
    public void setIgnoreNumberFormat(boolean ignoreNumberFormat) { this.ignoreNumberFormat = ignoreNumberFormat; }
    public boolean isIgnoreDateFormat() { return ignoreDateFormat; }
    public void setIgnoreDateFormat(boolean ignoreDateFormat) { this.ignoreDateFormat = ignoreDateFormat; }
    public boolean isIgnoreHeaderFooter() { return ignoreHeaderFooter; }
    public void setIgnoreHeaderFooter(boolean ignoreHeaderFooter) { this.ignoreHeaderFooter = ignoreHeaderFooter; }
    public boolean isIgnoreWatermark() { return ignoreWatermark; }
    public void setIgnoreWatermark(boolean ignoreWatermark) { this.ignoreWatermark = ignoreWatermark; }
    public boolean isEnableVisualDiff() { return enableVisualDiff; }
    public void setEnableVisualDiff(boolean enableVisualDiff) { this.enableVisualDiff = enableVisualDiff; }
    public double getVisualDiffThreshold() { return visualDiffThreshold; }
    public void setVisualDiffThreshold(double visualDiffThreshold) { this.visualDiffThreshold = visualDiffThreshold; }
    public int getRenderDpi() { return renderDpi; }
    public void setRenderDpi(int renderDpi) { this.renderDpi = renderDpi; }
    public NumberPrefixIgnoreMode getSectionNumberMode() { return sectionNumberMode; }
    public void setSectionNumberMode(NumberPrefixIgnoreMode sectionNumberMode) { this.sectionNumberMode = sectionNumberMode; }
    public OcrOptions getOcrOptions() { return ocrOptions; }
    public void setOcrOptions(OcrOptions ocrOptions) { this.ocrOptions = ocrOptions; }

    public CompareOptions withExtractionMode(ExtractionMode value) { this.extractionMode = value; return this; }
    public CompareOptions withIncludePages(Set<PageRange> value) { this.includePages = new HashSet<>(value); return this; }
    public CompareOptions withExcludeRegions(Set<Region> value) { this.excludeRegions = new HashSet<>(value); return this; }
    public CompareOptions withAnchors(List<Anchor> value) { this.anchors = new ArrayList<>(value); return this; }
    public CompareOptions withFuzzyThreshold(double value) { this.fuzzyThreshold = value; return this; }
    public CompareOptions withPositionTolerance(double value) { this.positionTolerance = value; return this; }
    public CompareOptions withReportLevels(Set<DiffSeverity> value) { this.reportLevels = new HashSet<>(value); return this; }
    public CompareOptions withIgnoreWhitespace(boolean value) { this.ignoreWhitespace = value; return this; }
    public CompareOptions withIgnoreNumberFormat(boolean value) { this.ignoreNumberFormat = value; return this; }
    public CompareOptions withIgnoreDateFormat(boolean value) { this.ignoreDateFormat = value; return this; }
    public CompareOptions withIgnoreHeaderFooter(boolean value) { this.ignoreHeaderFooter = value; return this; }
    public CompareOptions withIgnoreWatermark(boolean value) { this.ignoreWatermark = value; return this; }
    public CompareOptions withEnableVisualDiff(boolean value) { this.enableVisualDiff = value; return this; }
    public CompareOptions withVisualDiffThreshold(double value) { this.visualDiffThreshold = value; return this; }
    public CompareOptions withRenderDpi(int value) { this.renderDpi = value; return this; }
    public CompareOptions withSectionNumberMode(NumberPrefixIgnoreMode value) { this.sectionNumberMode = value; return this; }
    public CompareOptions withOcrOptions(OcrOptions value) { this.ocrOptions = value; return this; }
}
