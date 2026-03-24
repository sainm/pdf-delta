package org.sainm.report;

import org.sainm.exception.RenderException;
import org.sainm.model.*;
import org.sainm.spi.ReportRenderer;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationHighlight;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class PdfAnnotationRenderer implements ReportRenderer {
    @Override public String formatId() { return "pdf-annotated"; }

    @Override
    public byte[] render(CompareResult result, CompareOptions options) {
        byte[] sourceBytes = result.getSourceBytesA();
        if (sourceBytes == null || sourceBytes.length == 0) {
            
            try (var doc = new PDDocument()) {
                doc.addPage(new org.apache.pdfbox.pdmodel.PDPage());
                var out = new ByteArrayOutputStream();
                doc.save(out);
                return out.toByteArray();
            } catch (IOException e) {
                throw new RenderException("Failed to create empty PDF", e);
            }
        }
        try (var doc = Loader.loadPDF(sourceBytes)) {
            for (var item : result.getItems()) {
                annotateItem(doc, item);
            }
            var out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RenderException("Failed to render annotated PDF", e);
        }
    }

    private void annotateItem(PDDocument doc, DiffItem item) {
        
    }

    private PDColor severityColor(DiffSeverity s) {
        return switch (s) {
            case CRITICAL -> new PDColor(new float[]{1f, 0f, 0f}, PDDeviceRGB.INSTANCE);
            case MAJOR    -> new PDColor(new float[]{1f, 0.5f, 0f}, PDDeviceRGB.INSTANCE);
            case MINOR    -> new PDColor(new float[]{1f, 1f, 0f}, PDDeviceRGB.INSTANCE);
            case INFO     -> new PDColor(new float[]{0.7f, 0.7f, 0.7f}, PDDeviceRGB.INSTANCE);
        };
    }
}
