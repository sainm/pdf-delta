package org.sainm.diff;

import org.sainm.alignment.*;
import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiffEngineTest {
    private AlignedPair equalPair(TextLogicalBlock block) {
        return new AlignedPair(block, block, 1.0, AlignmentStrategy.LCS, AlignedPair.AlignedPairType.EQUAL);
    }

    private AlignedPair addPair(TextLogicalBlock block) {
        return new AlignedPair(null, block, 0.0, AlignmentStrategy.LCS, AlignedPair.AlignedPairType.ADD);
    }

    private AlignedPair modifyPair(TextLogicalBlock a, TextLogicalBlock b, double score) {
        return new AlignedPair(a, b, score, AlignmentStrategy.FUZZY, AlignedPair.AlignedPairType.MODIFY);
    }

    @Test void equalPairProducesNoDiffItem() {
        var pair = equalPair(new TextLogicalBlock("same", null));
        var items = new DiffEngine().diff(List.of(pair), CompareOptions.defaults());
        assertThat(items).isEmpty();
    }

    @Test void addPairProducesAddDiffItem() {
        var pair = addPair(new TextLogicalBlock("new content", null));
        var items = new DiffEngine().diff(List.of(pair), CompareOptions.defaults());
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getType()).isEqualTo(DiffType.ADD);
        assertThat(items.get(0).getRevised()).isEqualTo("new content");
        assertThat(items.get(0).getOriginal()).isNull();
    }

    @Test void modifyPairIncludesCharDiff() {
        var a = new TextLogicalBlock("hello world", null);
        var b = new TextLogicalBlock("hello earth", null);
        var pair = modifyPair(a, b, 0.9);
        var items = new DiffEngine().diff(List.of(pair), CompareOptions.defaults());
        assertThat(items.get(0).getCharDiff()).isNotEmpty();
    }
}
