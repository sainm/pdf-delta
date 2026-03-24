package org.sainm.ocr.remote.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sainm.exception.OcrException;
import org.sainm.model.BoundingBox;
import org.sainm.model.OcrOptions;
import org.sainm.model.TextBlock;
import org.sainm.ocr.remote.OcrServiceAdapter;
import org.sainm.ocr.remote.exception.OcrNetworkException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PaddleOcrAdapter implements OcrServiceAdapter {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PaddleOcrAdapter() {
        this(HttpClient.newHttpClient());
    }

    public PaddleOcrAdapter(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<TextBlock> recognize(BufferedImage image, OcrOptions options) throws OcrException {
        String endpoint = options.getRemoteEndpoint();
        if (endpoint == null) {
            throw new OcrException("remoteEndpoint not configured");
        }

        try {
            String base64Image = encodeImageToBase64(image);
            String requestBody = String.format(
                "{\"image\":\"%s\",\"language\":\"%s\"}",
                base64Image, options.getLanguage()
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + "/ocr"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new OcrNetworkException(response.statusCode(),
                    "OCR request failed: " + response.statusCode());
            }

            return parsePaddleResponse(response.body());
        } catch (OcrNetworkException e) {
            throw e;
        } catch (IOException | InterruptedException e) {
            throw new OcrException("OCR request failed", e);
        }
    }

    @Override
    public String adapterId() {
        return "paddle";
    }

    @Override
    public boolean isAvailable(OcrOptions options) {
        return options.getRemoteEndpoint() != null;
    }

    private String encodeImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private List<TextBlock> parsePaddleResponse(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode results = root.get("results");
        if (results == null || !results.isArray()) {
            return List.of();
        }

        List<TextBlock> blocks = new ArrayList<>();
        for (JsonNode result : results) {
            String text = result.get("text").asText();
            double score = result.get("score").asDouble();
            JsonNode box = result.get("box");

            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            for (JsonNode point : box) {
                double x = point.get(0).asDouble();
                double y = point.get(1).asDouble();
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }

            TextBlock block = new TextBlock();
            block.setText(text);
            block.setConfidence(score);
            block.setBbox(new BoundingBox(minX, minY, maxX - minX, maxY - minY, 1));
            block.setSource(TextBlock.Source.OCR);
            blocks.add(block);
        }
        return blocks;
    }
}
