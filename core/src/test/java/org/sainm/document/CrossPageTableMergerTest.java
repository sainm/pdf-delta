package org.sainm.document;

import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrossPageTableMergerTest {
    @Test void mergesTableAcrossPages() {
        var page1Table = tableBlock(List.of("Name", "Value"),
            List.of(row("Alice", "100"), row("Bob", "200")));
        var page2Table = tableBlock(List.of("Name", "Value"),
            List.of(row("Name", "Value"), row("Carol", "300")));

        var merger = new CrossPageTableMerger();
        var merged = merger.merge(List.of(
            new CrossPageTableMerger.PageTableEntry(1, page1Table),
            new CrossPageTableMerger.PageTableEntry(2, page2Table)
        ));

        
        assertThat(merged.getCells()).hasSize(4);
        assertThat(merged.getCells().get(3).get(0).getText()).isEqualTo("Carol");
    }

    private TableBlock tableBlock(List<String> header, List<List<TableCell>> rows) {
        var tb = new TableBlock();
        tb.setHeaderRow(header);
        tb.setCells(new ArrayList<>(rows));
        return tb;
    }

    private List<TableCell> row(String... texts) {
        List<TableCell> row = new ArrayList<>();
        for (int i = 0; i < texts.length; i++) {
            var c = new TableCell(); c.setText(texts[i]); c.setCol(i); row.add(c);
        }
        return row;
    }
}
