package org.sainm.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class CompareOptionsTest {
    @Test void defaultValues() {
        var opts = CompareOptions.defaults();
        assertThat(opts.getFuzzyThreshold()).isEqualTo(0.85);
        assertThat(opts.isIgnoreWhitespace()).isTrue();
        assertThat(opts.isIgnoreNumberFormat()).isTrue();
        assertThat(opts.isIgnoreHeaderFooter()).isTrue();
        assertThat(opts.getPositionTolerance()).isEqualTo(5.0);
    }

    @Test void supportsStandardSettersForCollectionAndEnumFields() {
        var opts = CompareOptions.defaults();
        var includePages = Set.of(new PageRange(1, 3));
        var excludeRegions = Set.of(new Region(1, 0, 0, 100, 100, Region.RegionType.CUSTOM));
        var anchors = List.of(new Anchor("invoice", 1));
        var reportLevels = Set.of(DiffSeverity.CRITICAL, DiffSeverity.MAJOR);

        opts.setExtractionMode(CompareOptions.ExtractionMode.IMAGE);
        opts.setIncludePages(includePages);
        opts.setExcludeRegions(excludeRegions);
        opts.setAnchors(anchors);
        opts.setReportLevels(reportLevels);

        assertThat(opts.getExtractionMode()).isEqualTo(CompareOptions.ExtractionMode.IMAGE);
        assertThat(opts.getIncludePages()).isEqualTo(includePages);
        assertThat(opts.getExcludeRegions()).isEqualTo(excludeRegions);
        assertThat(opts.getAnchors()).isEqualTo(anchors);
        assertThat(opts.getReportLevels()).isEqualTo(reportLevels);
    }
}
