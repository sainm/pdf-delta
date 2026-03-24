package org.sainm.mcp;

import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GetDiffSummaryToolTest {
    private CompareResult compareResultWithSummary() {
        var r = new CompareResult();
        r.setJobId(UUID.randomUUID().toString());
        r.setItems(List.of());
        var s = new DiffSummary(3, 1, 1, 1, 0);
        r.setSummary(s);
        return r;
    }

    @Test void returnsSummaryForKnownJob() {
        var store = new McpJobStore();
        var result = compareResultWithSummary();
        store.put("job-1", result);
        var output = new GetDiffSummaryTool(store).execute(Map.of("jobId", "job-1", "language", "zh"));
        assertThat(output).containsKey("summary");
        assertThat(output).containsKey("countBySeverity");
    }

    @Test void returnsErrorForUnknownJob() {
        var output = new GetDiffSummaryTool(new McpJobStore()).execute(Map.of("jobId", "nope"));
        assertThat(output).containsKey("error");
    }
}
