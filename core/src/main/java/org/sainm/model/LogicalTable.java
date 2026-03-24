package org.sainm.model;

import java.util.ArrayList;
import java.util.List;

public final class LogicalTable {
    private String tableId;
    private List<List<TableCell>> cells = new ArrayList<>();
    private List<String> headerRow = new ArrayList<>();
    private List<PhysicalLocation> pageLocations = new ArrayList<>();

    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }
    public List<List<TableCell>> getCells() { return cells; }
    public void setCells(List<List<TableCell>> cells) { this.cells = cells; }
    public List<String> getHeaderRow() { return headerRow; }
    public void setHeaderRow(List<String> headerRow) { this.headerRow = headerRow; }
    public List<PhysicalLocation> getPageLocations() { return pageLocations; }
    public void setPageLocations(List<PhysicalLocation> pageLocations) { this.pageLocations = pageLocations; }
}
