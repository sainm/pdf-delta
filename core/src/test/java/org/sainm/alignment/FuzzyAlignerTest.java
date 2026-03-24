package org.sainm.alignment;

import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FuzzyAlignerTest {
    @Test void mergesAdjacentDeleteAddIntoModify() {
        var del = new AlignedPair(new TextLogicalBlock("hello world test", null), null, 0.0,
            AlignmentStrategy.LCS, AlignedPair.AlignedPairType.DELETE);
        var add = new AlignedPair(null, new TextLogicalBlock("hello world text", null), 0.0,
            AlignmentStrategy.LCS, AlignedPair.AlignedPairType.ADD);

        var fuzzy = new FuzzyAligner();
        var result = fuzzy.merge(List.of(del, add), CompareOptions.defaults());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(AlignedPair.AlignedPairType.MODIFY);
        assertThat(result.get(0).alignmentScore()).isGreaterThan(0.85);
    }

    @Test void keepsDeleteAddWhenSimilarityBelowThreshold() {
        var del = new AlignedPair(new TextLogicalBlock("completely different", null), null, 0.0,
            AlignmentStrategy.LCS, AlignedPair.AlignedPairType.DELETE);
        var add = new AlignedPair(null, new TextLogicalBlock("xyz abc 123", null), 0.0,
            AlignmentStrategy.LCS, AlignedPair.AlignedPairType.ADD);

        var result = new FuzzyAligner().merge(List.of(del, add), CompareOptions.defaults());
        assertThat(result).hasSize(2);
    }
}
