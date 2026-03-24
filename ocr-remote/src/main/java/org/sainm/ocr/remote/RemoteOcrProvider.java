package org.sainm.ocr.remote;

import org.sainm.exception.OcrProviderUnavailableException;
import org.sainm.model.OcrOptions;
import org.sainm.model.TextBlock;
import org.sainm.ocr.remote.adapters.PaddleOcrAdapter;
import org.sainm.ocr.remote.retry.RetryPolicy;
import org.sainm.spi.OcrProvider;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class RemoteOcrProvider implements OcrProvider {
    private final Map<String, OcrServiceAdapter> adapters;
    private final RetryPolicy retryPolicy;

    public RemoteOcrProvider() {
        adapters = Map.of("paddle", new PaddleOcrAdapter());
        retryPolicy = RetryPolicy.defaultPolicy();
    }

    @Override
    public List<TextBlock> recognize(BufferedImage image, OcrOptions options) {
        OcrServiceAdapter adapter = selectAdapter(options);
        return retryPolicy.execute(() -> adapter.recognize(image, options));
    }

    @Override
    public String providerId() {
        return "remote";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int priority() {
        return 50;
    }

    OcrServiceAdapter selectAdapter(OcrOptions options) {
        if (options.getRemoteEndpoint() != null) {
            return adapters.get("paddle");
        }
        throw new OcrProviderUnavailableException("No remote OCR adapter available");
    }
}
