package org.sainm.diff;

import org.junit.jupiter.api.Test;
import org.sainm.model.AlignedPair;
import org.sainm.model.AlignmentStrategy;
import org.sainm.model.BlockType;
import org.sainm.model.BoundingBox;
import org.sainm.model.CompareOptions;
import org.sainm.spi.LogicalBlock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiffEngineTableDiffTest {
    @Test void buildsRowAndCellDiffsForTableModify() {
        LogicalBlock tableA = tableBlock("col1\tcol2\nA\t1\nB\t2");
        LogicalBlock tableB = tableBlock("col1\tcol2\nA\t9\nC\t2\nD\t3");

        var items = new DiffEngine().diff(List.of(
            new AlignedPair(tableA, tableB, 0.9, AlignmentStrategy.FUZZY, AlignedPair.AlignedPairType.MODIFY)
        ), CompareOptions.defaults());

        assertThat(items).hasSize(1);
        var item = items.get(0);
        assertThat(item.getTableDiff()).isNotNull();
        assertThat(item.getTableDiff().getRowDiffs()).isNotEmpty();
        assertThat(item.getTableDiff().getCellDiffs()).anySatisfy(cell -> {
            assertThat(cell.getOriginal()).isEqualTo("1");
            assertThat(cell.getRevised()).isEqualTo("9");
        });
        assertThat(item.getTableDiff().getRowDiffs()).anySatisfy(row -> {
            assertThat(row.getType()).isEqualTo(org.sainm.model.DiffType.ADD);
            assertThat(row.getRowIndex()).isEqualTo(3);
        });
    }

    @Test void alignsColumnsByHeaderBeforeComparingCells() {
        LogicalBlock tableA = tableBlock("id\tname\tdept\n1\tAlice\tSales");
        LogicalBlock tableB = tableBlock("dept\tid\tname\nSales\t1\tAlice");

        var items = new DiffEngine().diff(List.of(
            new AlignedPair(tableA, tableB, 0.9, AlignmentStrategy.FUZZY, AlignedPair.AlignedPairType.MODIFY)
        ), CompareOptions.defaults());

        assertThat(items).hasSize(1);
        var detail = items.get(0).getTableDiff();
        assertThat(detail).isNotNull();
        assertThat(detail.getColumnDiffs()).isNotEmpty();
        assertThat(detail.getColumnDiffs()).anySatisfy(column -> {
            assertThat(column.getType()).isEqualTo(org.sainm.model.DiffType.MODIFY);
            assertThat(column.getOriginalHeader()).isEqualTo("id");
            assertThat(column.getRevisedColumnIndex()).isEqualTo(1);
        });
        assertThat(detail.getCellDiffs()).isEmpty();
    }

    private LogicalBlock tableBlock(String text) {
        return new LogicalBlock() {
            @Override public String getBlockId() { return "table"; }
            @Override public String getNormalizedText() { return text; }
            @Override public BoundingBox getBbox() { return null; }
            @Override public BlockType getBlockType() { return BlockType.TABLE; }
        };
    }
}
