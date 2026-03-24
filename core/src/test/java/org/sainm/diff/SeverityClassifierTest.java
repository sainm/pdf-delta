package org.sainm.diff;

import org.sainm.alignment.*;
import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeverityClassifierTest {
    private DiffItem diffItem(DiffType type, String orig, String revised) {
        return DiffItem.builder().type(type).original(orig).revised(revised).build();
    }

    @Test void classifiesNumericChangeAsCritical() {
        var item = diffItem(DiffType.MODIFY, "合同金额：100万", "合同金额：200万");
        assertThat(new SeverityClassifier().classify(item)).isEqualTo(DiffSeverity.CRITICAL);
    }

    @Test void classifiesWhitespaceChangeAsMinor() {
        var item = diffItem(DiffType.MODIFY, "hello world", "hello  world");
        assertThat(new SeverityClassifier().classify(item)).isEqualTo(DiffSeverity.MINOR);
    }

    @Test void classifiesTableDataChangeAsMajor() {
        var item = DiffItem.builder()
            .type(DiffType.MODIFY).original("产品A").revised("产品B")
            .blockType(BlockType.TABLE).build();
        assertThat(new SeverityClassifier().classify(item)).isEqualTo(DiffSeverity.MAJOR);
    }
}
