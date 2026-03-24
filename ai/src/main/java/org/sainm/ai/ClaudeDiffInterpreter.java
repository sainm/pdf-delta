package org.sainm.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import org.sainm.model.*;
import org.sainm.spi.DiffInterpreter;

import java.util.stream.Collectors;

public final class ClaudeDiffInterpreter implements DiffInterpreter {
    private static final int MAX_ITEMS_IN_PROMPT = 50;
    private final AnthropicClient client;

    public ClaudeDiffInterpreter(String apiKey) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    ClaudeDiffInterpreter(AnthropicClient client) { this.client = client; }

    @Override
    public String interpret(CompareResult result, String language) {
        if (result.getItems().isEmpty()) return null;
        String prompt = buildPrompt(result, language);
        try {
            var response = client.messages().create(
                MessageCreateParams.builder()
                    .model("claude-sonnet-4-6")
                    .maxTokens(512)
                    .addUserMessage(prompt)
                    .build());
            return response.content().stream()
                .filter(b -> b.isText())
                .map(b -> b.asText().text())
                .findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildPrompt(CompareResult result, String language) {
        var items = result.getItems().stream()
            .limit(MAX_ITEMS_IN_PROMPT)
            .map(i -> String.format("[%s/%s] %s → %s",
                i.getType(), i.getSeverity(), i.getOriginal(), i.getRevised()))
            .collect(Collectors.joining("\n"));
        String lang = "zh".equals(language) ? "中文" : "English";
        return String.format("""
            以下是两份 PDF 文档的比较差异列表，请用%s生成简洁摘要（不超过200字），
            重点说明关键数据变化（金额、日期、数量等）：

            %s

            共 %d 处差异（CRITICAL: %d, MAJOR: %d, MINOR: %d）
            """,
            lang, items, result.getItems().size(),
            countBySeverity(result, DiffSeverity.CRITICAL),
            countBySeverity(result, DiffSeverity.MAJOR),
            countBySeverity(result, DiffSeverity.MINOR));
    }

    private long countBySeverity(CompareResult result, DiffSeverity severity) {
        return result.getItems().stream().filter(i -> i.getSeverity() == severity).count();
    }
}
