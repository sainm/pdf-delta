package org.sainm.model;

public record LogicalLocation(String blockId, Integer row, Integer col) {
    public static LogicalLocation ofParagraph(String id) { return new LogicalLocation(id, null, null); }
    public static LogicalLocation ofTableCell(String tableId, int row, int col) {
        return new LogicalLocation(tableId, row, col);
    }
}
