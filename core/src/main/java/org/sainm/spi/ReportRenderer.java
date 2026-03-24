package org.sainm.spi;

import org.sainm.model.CompareOptions;
import org.sainm.model.CompareResult;

public interface ReportRenderer {
    byte[] render(CompareResult result, CompareOptions options);
    String formatId();
}
