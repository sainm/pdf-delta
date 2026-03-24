package org.sainm.alignment;

import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ToleranceAlignerTest {
    @Test void marksIdenticalBlocksWithSmallPositionShiftAsEqual() {
        var blockA = new TextLogicalBlock("hello", new BoundingBox(10, 10, 100, 15, 1));
        var blockB = new TextLogicalBlock("hello", new BoundingBox(12, 11, 100, 15, 1));
        var aligner = new ToleranceAligner();
        var pairs = aligner.align(List.of(blockA), List.of(blockB), CompareOptions.defaults());
        assertThat(pairs).hasSize(1);
        assertThat(pairs.get(0).alignmentScore()).isEqualTo(1.0);
        assertThat(pairs.get(0).usedStrategy()).isEqualTo(AlignmentStrategy.TOLERANCE);
    }

    @Test void doesNotMatchBlocksBeyondTolerance() {
        var blockA = new TextLogicalBlock("hello", new BoundingBox(10, 10, 100, 15, 1));
        var blockB = new TextLogicalBlock("hello", new BoundingBox(50, 50, 100, 15, 1));
        var aligner = new ToleranceAligner();
        var pairs = aligner.align(List.of(blockA), List.of(blockB), CompareOptions.defaults());
        assertThat(pairs).isEmpty();
    }
}
