package org.sainm.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.Map;

@FunctionalInterface
interface ThrowingFunction {
    Map<String, Object> apply(Map<String, Object> args) throws Exception;
}

public final class PdfCompareMcpServer {
    public static void main(String[] args) {
        var store = new McpJobStore();
        var compareTool = new ComparePdfsTool(store);
        var summaryTool = new GetDiffSummaryTool(store);
        var pageTool = new GetPageDiffTool(store);
        var mapper = new ObjectMapper();

        var transport = new StdioServerTransportProvider();

        var server = McpServer.sync(transport)
            .serverInfo("pdf-compare", "1.0.0")
            .tool(
                new McpSchema.Tool("compare_pdfs", "比较两个 PDF 文件的差异，返回结构化差异结果",
                    new McpSchema.JsonSchema("object",
                        Map.of(
                            "fileA", Map.of("type", "string"),
                            "fileB", Map.of("type", "string"),
                            "inputType", Map.of("type", "string", "enum", new String[]{"path", "base64"})
                        ),
                        java.util.List.of("fileA", "fileB"), null)),
                (exchange, arguments) -> McpSchema.CallToolResult.builder()
                    .addTextContent(toJson(mapper, safeExecute(compareTool::execute, arguments)))
                    .build())
            .tool(
                new McpSchema.Tool("get_diff_summary", "获取比较结果的自然语言摘要",
                    new McpSchema.JsonSchema("object",
                        Map.of("jobId", Map.of("type", "string")),
                        java.util.List.of("jobId"), null)),
                (exchange, arguments) -> McpSchema.CallToolResult.builder()
                    .addTextContent(toJson(mapper, safeExecute(summaryTool::execute, arguments)))
                    .build())
            .tool(
                new McpSchema.Tool("get_page_diff", "获取指定页的详细差异（page 参数保留，过���功能待 core 模型支持）",
                    new McpSchema.JsonSchema("object",
                        Map.of(
                            "jobId", Map.of("type", "string"),
                            "page", Map.of("type", "integer", "description", "reserved — page filtering not yet implemented")
                        ),
                        java.util.List.of("jobId"), null)),
                (exchange, arguments) -> McpSchema.CallToolResult.builder()
                    .addTextContent(toJson(mapper, safeExecute(pageTool::execute, arguments)))
                    .build())
            .build();

        try { Thread.currentThread().join(); } catch (InterruptedException ignored) {}
    }

    private static Map<String, Object> safeExecute(ThrowingFunction fn, Map<String, Object> args) {
        try { return fn.apply(args); }
        catch (Exception e) { return Map.of("error", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()); }
    }

    private static String toJson(ObjectMapper mapper, Object obj) {
        try { return mapper.writeValueAsString(obj); }
        catch (Exception e) { return "{\"error\":\"serialization failed\"}"; }
    }
}
