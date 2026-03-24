package org.sainm.table;

import org.junit.jupiter.api.Test;
import org.sainm.model.BoundingBox;
import org.sainm.model.TextBlock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CellClustererTest {

    @Test
    void clusterForOcrTableUsesDenseRowsAsColumnAnchors() {
        var blocks = List.of(
                block("TITLE", 200, 100, 200, 20),
                block("ID", 100, 200, 30, 20),
                block("ITEM", 190, 200, 70, 20),
                block("QTY", 460, 200, 45, 20),
                block("PRICE", 620, 200, 80, 20),
                block("CODE", 790, 200, 65, 20),
                block("1", 105, 300, 12, 18),
                block("ALPHA", 170, 300, 150, 18),
                block("120", 460, 300, 45, 18),
                block("15.50", 620, 300, 70, 18),
                block("A-100", 790, 300, 70, 18),
                block("2", 105, 400, 12, 18),
                block("BETA", 175, 400, 120, 18),
                block("298", 460, 400, 45, 18),
                block("8.20", 620, 400, 60, 18),
                block("B-216", 790, 400, 70, 18),
                block("Reviewer", 120, 500, 120, 18)
        );

        var clustered = CellClusterer.clusterForOcrTable(blocks, 5.0);

        assertThat(clustered.rowCount()).isEqualTo(3);
        assertThat(clustered.colCount()).isEqualTo(5);
        assertThat(clustered.cellAt(0, 0).getText()).isEqualTo("ID");
        assertThat(clustered.cellAt(0, 1).getText()).isEqualTo("ITEM");
        assertThat(clustered.cellAt(0, 2).getText()).isEqualTo("QTY");
        assertThat(clustered.cellAt(1, 1).getText()).isEqualTo("ALPHA");
        assertThat(clustered.cellAt(2, 4).getText()).isEqualTo("B-216");
    }

    private TextBlock block(String text, double x, double y, double width, double height) {
        return new TextBlock(text, new BoundingBox(x, y, width, height, 1));
    }
}
