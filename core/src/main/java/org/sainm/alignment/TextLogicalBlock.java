package org.sainm.alignment;

import org.sainm.model.*;
import org.sainm.spi.LogicalBlock;

public final class TextLogicalBlock implements LogicalBlock {
    private final String blockId;
    private final String text;
    private final BoundingBox bbox;

    public TextLogicalBlock(String text, BoundingBox bbox) {
        this.blockId = java.util.UUID.randomUUID().toString();
        this.text = text;
        this.bbox = bbox;
    }

    @Override public String getBlockId() { return blockId; }
    @Override public String getNormalizedText() { return text; }
    @Override public BoundingBox getBbox() { return bbox; }
    @Override public BlockType getBlockType() { return BlockType.TEXT; }
}
