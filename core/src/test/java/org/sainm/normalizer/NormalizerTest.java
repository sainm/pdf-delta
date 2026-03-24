package org.sainm.normalizer;

import org.sainm.model.CompareOptions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NormalizerTest {
    @Test void numberNormalizerRemovesThousandSeparator() {
        var n = new NumberNormalizer();
        assertThat(n.normalize("1,000", opts())).isEqualTo("1000");
        assertThat(n.normalize("1.50", opts())).isEqualTo("1.5");
        assertThat(n.normalize("hello", opts())).isEqualTo("hello");
    }

    @Test void dateNormalizerUnifiesFormat() {
        var n = new DateNormalizer();
        assertThat(n.normalize("2024/1/1", opts())).isEqualTo("2024-01-01");
        assertThat(n.normalize("2024年1月1日", opts())).isEqualTo("2024-01-01");
    }

    @Test void unitNormalizerUnifiesCurrency() {
        var n = new UnitNormalizer();
        assertThat(n.normalize("RMB100", opts())).isEqualTo("CNY100");
        assertThat(n.normalize("￥100", opts())).isEqualTo("CNY100");
    }

    @Test void whitespaceNormalizerCollapses() {
        var n = new WhitespaceNormalizer();
        assertThat(n.normalize("hello　world  test", opts())).isEqualTo("hello world test");
    }

    @Test void glyphNormalizerFixesConfusables() {
        var n = new GlyphNormalizer();
        assertThat(n.normalize("己", opts())).isEqualTo("已");
        assertThat(n.normalize("０", opts())).isEqualTo("0");
    }

    @Test void chainAppliesAllNormalizers() {
        var chain = NormalizerChain.defaults();
        assertThat(chain.normalize("1,000.00", opts())).isEqualTo("1000");
    }

    private CompareOptions opts() { return CompareOptions.defaults(); }
}
