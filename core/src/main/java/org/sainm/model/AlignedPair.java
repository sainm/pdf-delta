package org.sainm.model;

import org.sainm.spi.LogicalBlock;

public record AlignedPair(
    LogicalBlock blockA,
    LogicalBlock blockB,
    double alignmentScore,
    AlignmentStrategy usedStrategy,
    AlignedPairType type
) {
    public enum AlignedPairType { EQUAL, ADD, DELETE, MODIFY }
}
