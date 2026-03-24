package org.sainm.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DiffItemTest {
    @Test void severityClassification() {
        var item = DiffItem.builder()
            .type(DiffType.MODIFY)
            .severity(DiffSeverity.CRITICAL)
            .original("100")
            .revised("200")
            .build();
        assertThat(item.getSeverity()).isEqualTo(DiffSeverity.CRITICAL);
        assertThat(item.getType()).isEqualTo(DiffType.MODIFY);
    }
}
