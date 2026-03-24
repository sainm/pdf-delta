package org.sainm.spi;

import org.sainm.model.CompareResult;

public interface DiffInterpreter {
    String interpret(CompareResult result, String language);
}
