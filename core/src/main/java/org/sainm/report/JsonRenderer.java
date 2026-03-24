package org.sainm.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.sainm.exception.RenderException;
import org.sainm.model.CompareOptions;
import org.sainm.model.CompareResult;
import org.sainm.spi.ReportRenderer;

public final class JsonRenderer implements ReportRenderer {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(SerializationFeature.INDENT_OUTPUT, true);

    @Override
    public String formatId() { return "json"; }

    @Override
    public byte[] render(CompareResult result, CompareOptions options) {
        try {
            return MAPPER.writeValueAsBytes(result);
        } catch (Exception e) {
            throw new RenderException("Failed to render JSON report", e);
        }
    }
}
