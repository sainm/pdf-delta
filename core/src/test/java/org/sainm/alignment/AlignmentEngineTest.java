package org.sainm.alignment;

import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import org.sainm.spi.LogicalBlock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlignmentEngineTest {
    @Test void fullPipelineHandlesInsertDeleteModify() {
        var engine = new AlignmentEngine();
        List<LogicalBlock> a = List.of(
            new TextLogicalBlock("unchanged", new BoundingBox(0, 0, 100, 15, 1)),
            new TextLogicalBlock("the contract value is one hundred", new BoundingBox(0, 20, 100, 15, 1))
        );
        List<LogicalBlock> b = List.of(
            new TextLogicalBlock("unchanged", new BoundingBox(2, 1, 100, 15, 1)),
            new TextLogicalBlock("the contract value is two hundred", new BoundingBox(0, 20, 100, 15, 1))
        );
        var pairs = engine.align(a, b, CompareOptions.defaults());
        var types = pairs.stream().map(AlignedPair::type).toList();
        assertThat(types).contains(AlignedPair.AlignedPairType.EQUAL);
        assertThat(types).contains(AlignedPair.AlignedPairType.MODIFY);
    }
}
