package org.sainm.ocr.remote.adapters;

import org.sainm.exception.OcrException;
import org.sainm.model.OcrOptions;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaddleOcrAdapterTest {

    @Test void adapterIdIsPaddle() {
        assertThat(new PaddleOcrAdapter().adapterId()).isEqualTo("paddle");
    }

    @Test void isAvailableWhenEndpointConfigured() {
        var adapter = new PaddleOcrAdapter();
        var options = OcrOptions.defaults();

        options.setRemoteEndpoint(null);
        assertThat(adapter.isAvailable(options)).isFalse();

        options.setRemoteEndpoint("http://localhost:8000");
        assertThat(adapter.isAvailable(options)).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test void recognizeSuccess() throws Exception {
        var mockClient = mock(HttpClient.class);
        var mockResponse = mock(HttpResponse.class);

        String jsonResponse = """
            {
              "results": [
                {
                  "text": "测试文字",
                  "box": [[10,20],[110,20],[110,50],[10,50]],
                  "score": 0.95
                }
              ]
            }
            """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        var adapter = new PaddleOcrAdapter(mockClient);
        var options = OcrOptions.defaults();
        options.setRemoteEndpoint("http://localhost:8000");

        var image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        var blocks = adapter.recognize(image, options);

        assertThat(blocks).hasSize(1);
        assertThat(blocks.get(0).getText()).isEqualTo("测试文字");
        assertThat(blocks.get(0).getConfidence()).isEqualTo(0.95);
        assertThat(blocks.get(0).getBbox()).isNotNull();
    }

    @SuppressWarnings("unchecked")
    @Test void recognizeNetworkError() throws Exception {
        var mockClient = mock(HttpClient.class);
        var mockResponse = mock(HttpResponse.class);

        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("Internal Server Error");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        var adapter = new PaddleOcrAdapter(mockClient);
        var options = OcrOptions.defaults();
        options.setRemoteEndpoint("http://localhost:8000");

        var image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        assertThatThrownBy(() -> adapter.recognize(image, options))
            .isInstanceOf(OcrException.class)
            .hasMessageContaining("500");
    }
}
