package org.sainm.spi;

import org.sainm.model.BoundingBox;
import org.sainm.model.CompareOptions;
import org.sainm.model.TableBlock;
import org.sainm.model.TextBlock;

import java.util.List;

public interface TableStructureRecognizer {
    TableBlock recognize(List<TextBlock> blocks, BoundingBox region, CompareOptions options);
}
