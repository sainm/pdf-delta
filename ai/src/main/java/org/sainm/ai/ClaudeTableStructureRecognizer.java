package org.sainm.ai;

import org.sainm.model.*;
import org.sainm.spi.TableStructureRecognizer;

import java.util.List;

public final class ClaudeTableStructureRecognizer implements TableStructureRecognizer {

    @Override
    public TableBlock recognize(List<TextBlock> blocks, BoundingBox region, CompareOptions options) {
        throw new UnsupportedOperationException(
            "ClaudeTableStructureRecognizer is not yet implemented — response parsing pending future plan");
    }
}
