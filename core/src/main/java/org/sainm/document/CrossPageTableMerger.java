package org.sainm.document;

import org.sainm.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CrossPageTableMerger {
    public record PageTableEntry(int page, TableBlock table) {}

    public LogicalTable merge(List<PageTableEntry> entries) {
        if (entries.isEmpty()) throw new IllegalArgumentException("entries must not be empty");
        var first = entries.get(0).table();
        
        List<List<TableCell>> allRows = new ArrayList<>();
        if (first.getHeaderRow() != null && !first.getHeaderRow().isEmpty()) {
            allRows.add(headerAsCells(first.getHeaderRow()));
        }
        allRows.addAll(first.getCells());
        List<PhysicalLocation> locations = new ArrayList<>();
        locations.add(new PhysicalLocation(entries.get(0).page(), first.getBbox()));

        for (int i = 1; i < entries.size(); i++) {
            var entry = entries.get(i);
            var table = entry.table();
            var rows = table.getCells();
            if (rows.isEmpty()) continue;
            int startRow = isRepeatedHeader(rows.get(0), first.getHeaderRow()) ? 1 : 0;
            allRows.addAll(rows.subList(startRow, rows.size()));
            locations.add(new PhysicalLocation(entry.page(), table.getBbox()));
        }

        var logical = new LogicalTable();
        logical.setTableId(UUID.randomUUID().toString());
        logical.setCells(allRows);
        logical.setHeaderRow(first.getHeaderRow());
        logical.setPageLocations(locations);
        return logical;
    }

    private boolean isRepeatedHeader(List<TableCell> row, List<String> header) {
        if (header == null || row.size() != header.size()) return false;
        for (int i = 0; i < row.size(); i++) {
            if (!row.get(i).getText().equals(header.get(i))) return false;
        }
        return true;
    }

    private List<TableCell> headerAsCells(List<String> header) {
        List<TableCell> row = new ArrayList<>();
        for (int i = 0; i < header.size(); i++) {
            var c = new TableCell();
            c.setText(header.get(i));
            c.setRow(0); c.setCol(i);
            row.add(c);
        }
        return row;
    }
}
