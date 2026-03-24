package org.sainm.table;

import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TableReconstructorTest {
    @Test void reconstructsSimpleTable() {
        var page = new PageContent();
        page.setPageNumber(1);
        page.setType(PageType.TEXT);
        page.setWidth(595);
        page.setHeight(842);
        page.setTextBlocks(new ArrayList<>(List.of(
            block("Name",  10,  10, 80, 15),
            block("Value", 100, 10, 80, 15),
            block("Alice", 10,  30, 80, 15),
            block("100",   100, 30, 80, 15)
        )));
        var reconstructor = new TableReconstructor();
        var tables = reconstructor.reconstruct(page, CompareOptions.defaults());
        assertThat(tables).hasSize(1);
        assertThat(tables.get(0).getHeaderRow()).containsExactly("Name", "Value");
        assertThat(tables.get(0).getCells().get(1).get(0).getText()).isEqualTo("Alice");
    }

    @Test void expandsMergedCells() {
        var expander = new MergedCellExpander();
        var merged = new TableCell();
        merged.setText("merged");
        merged.setRow(0); merged.setCol(0);
        merged.setRowSpan(2); merged.setColSpan(1);
        var grid = expander.expand(List.of(List.of(merged)), 2, 1);
        assertThat(grid.get(0).get(0).getText()).isEqualTo("merged");
        assertThat(grid.get(1).get(0).getText()).isEqualTo("merged");
    }

    private TextBlock block(String text, double x, double y, double w, double h) {
        var b = new TextBlock();
        b.setText(text);
        b.setBbox(new BoundingBox(x, y, w, h, 1));
        return b;
    }
}
