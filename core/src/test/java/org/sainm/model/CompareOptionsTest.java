package org.sainm.model;

import org.junit.jupiter.api.Test;
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
}
