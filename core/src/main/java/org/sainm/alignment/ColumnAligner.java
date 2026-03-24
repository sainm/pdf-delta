package org.sainm.alignment;

import org.sainm.model.*;
import org.sainm.spi.LogicalBlock;

import java.util.List;

final class ColumnAligner {
    public LogicalBlock reorderColumns(LogicalBlock block, List<String> targetHeader) {
        if (block.getBlockType() != BlockType.TABLE) return block;
        
        return block;
    }
}
