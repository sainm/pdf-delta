package org.sainm.pipeline;

import org.sainm.model.*;
import org.sainm.spi.LogicalBlock;


final class ParagraphBlock implements LogicalBlock {
    private final LogicalParagraph paragraph;

    public ParagraphBlock(LogicalParagraph paragraph) { this.paragraph = paragraph; }

    @Override public String getBlockId() { return paragraph.getParagraphId(); }
    @Override public String getNormalizedText() { return paragraph.getText(); }
    @Override public BoundingBox getBbox() {
        var locs = paragraph.getLocations();
        return locs.isEmpty() ? null : locs.get(0).bbox();
    }
    @Override public BlockType getBlockType() { return BlockType.TEXT; }
}
