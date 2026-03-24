package org.sainm.mcp;

import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GetPageDiffToolTest {
    @Test void returnsItemsForPage() {
        var store = new McpJobStore();
        var r = new CompareResult();
        r.setJobId(UUID.randomUUID().toString());
        var item = DiffItem.builder()
            .itemId(UUID.randomUUID().toString())
            .type(DiffType.MODIFY)
            .original("old").revised("new")
            .severity(DiffSeverity.MAJOR).build();
        r.setItems(List.of(item));
        r.setSummary(new DiffSummary(1, 0, 1, 0, 0));
        store.put("job-2", r);
        var output = new GetPageDiffTool(store).execute(Map.of("jobId", "job-2", "page", 2));
        assertThat((List<?>) output.get("items")).isNotEmpty();
    }
}
