package org.sainm.mcp;

import org.sainm.model.*;

import java.util.List;
import java.util.Map;

public final class GetPageDiffTool {
    private final McpJobStore store;

    public GetPageDiffTool(McpJobStore store) { this.store = store; }

    public Map<String, Object> execute(Map<String, Object> input) {
        String jobId = (String) input.get("jobId");
        
        int page = input.containsKey("page") ? ((Number) input.get("page")).intValue() : 0;
        return store.get(jobId).map(result -> {
            
            
            return (Map<String, Object>) Map.of(
                "jobId", jobId,
                "page", page,
                "items", result.getItems(),
                "warning", "Page filtering not yet supported — all diff items returned"
            );
        }).orElse(Map.of("error", "Job not found: " + jobId));
    }
}
