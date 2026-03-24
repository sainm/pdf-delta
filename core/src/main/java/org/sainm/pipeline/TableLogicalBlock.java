package org.sainm.pipeline;

import org.sainm.model.*;
import org.sainm.spi.LogicalBlock;


final class TableLogicalBlock implements LogicalBlock {
    private final LogicalTable table;

    public TableLogicalBlock(LogicalTable table) { this.table = table; }

    @Override public String getBlockId() { return table.getTableId(); }
    @Override public String getNormalizedText() {
        
        var sb = new StringBuilder();
        if (table.getHeaderRow() != null) sb.append(String.join("\t", table.getHeaderRow()));
        for (var row : table.getCells()) {
            sb.append("\n");
            for (var cell : row) sb.append(cell.getNormalizedText() != null ? cell.getNormalizedText() : "").append("\t");
        }
        return sb.toString().trim();
    }
    @Override public BoundingBox getBbox() {
        var locs = table.getPageLocations();
        return locs.isEmpty() ? null : locs.get(0).bbox();
    }
    @Override public BlockType getBlockType() { return BlockType.TABLE; }
}
