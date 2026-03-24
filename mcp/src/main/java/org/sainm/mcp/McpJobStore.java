package org.sainm.mcp;

import org.sainm.model.CompareResult;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class McpJobStore {
    private final ConcurrentHashMap<String, CompareResult> results = new ConcurrentHashMap<>();

    public void put(String jobId, CompareResult result) { results.put(jobId, result); }
    public Optional<CompareResult> get(String jobId) { return Optional.ofNullable(results.get(jobId)); }
}
