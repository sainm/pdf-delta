package org.sainm.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import org.sainm.model.TextBlock;
import org.sainm.spi.OcrEnhancer;

import java.awt.image.BufferedImage;
import java.util.List;

public final class ClaudeOcrEnhancer implements OcrEnhancer {
    private final AnthropicClient client;

    public ClaudeOcrEnhancer(String apiKey) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    ClaudeOcrEnhancer(AnthropicClient client) { this.client = client; }

    @Override
    public List<TextBlock> enhance(List<TextBlock> raw, BufferedImage original, double threshold) {
        return raw.stream().map(block -> {
            if (block.getConfidence() >= threshold) return block;
            return enhanceBlock(block);
        }).toList();
    }

    private TextBlock enhanceBlock(TextBlock block) {
        try {
            var response = client.messages().create(
                MessageCreateParams.builder()
                    .model("claude-sonnet-4-6")
                    .maxTokens(64)
                    .addUserMessage("OCR correction: the following text may have errors, return only the corrected text: " + block.getText())
                    .build());
            String corrected = response.content().stream()
                .filter(b -> b.isText())
                .map(b -> b.asText().text())
                .findFirst().orElse(null);
            if (corrected != null && !corrected.isBlank()) {
                var enhanced = new TextBlock(corrected.strip(), block.getBbox());
                enhanced.setConfidence(0.9);
                enhanced.setSource(TextBlock.Source.OCR);
                return enhanced;
            }
        } catch (Exception e) {
            System.err.println("[ClaudeOcrEnhancer] enhanceBlock failed: " + e.getMessage());
        }
        return block;
    }
}
