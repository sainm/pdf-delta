package org.sainm.alignment;

import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LcsAlignerTest {
    @Test void alignsSequenceWithInsertedBlock() {
        var a1 = new TextLogicalBlock("para1", new BoundingBox(0, 0, 100, 15, 1));
        var a2 = new TextLogicalBlock("para2", new BoundingBox(0, 20, 100, 15, 1));
        var b1 = new TextLogicalBlock("para1", new BoundingBox(0, 0, 100, 15, 1));
        var bNew = new TextLogicalBlock("new para", new BoundingBox(0, 20, 100, 15, 1));
        var b2 = new TextLogicalBlock("para2", new BoundingBox(0, 40, 100, 15, 1));

        var aligner = new LcsAligner();
        var pairs = aligner.align(List.of(a1, a2), List.of(b1, bNew, b2), CompareOptions.defaults());

        assertThat(pairs).hasSize(3);
        assertThat(pairs.get(0).type()).isEqualTo(AlignedPair.AlignedPairType.EQUAL);
        assertThat(pairs.get(1).type()).isEqualTo(AlignedPair.AlignedPairType.ADD);
        assertThat(pairs.get(1).blockA()).isNull();
        assertThat(pairs.get(2).type()).isEqualTo(AlignedPair.AlignedPairType.EQUAL);
    }
}
