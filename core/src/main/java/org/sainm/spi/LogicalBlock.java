package org.sainm.spi;

import org.sainm.model.BlockType;
import org.sainm.model.BoundingBox;

public interface LogicalBlock {
    String getBlockId();
    String getNormalizedText();
    BoundingBox getBbox();
    BlockType getBlockType();
}
