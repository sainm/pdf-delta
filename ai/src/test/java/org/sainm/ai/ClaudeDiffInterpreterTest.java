package org.sainm.ai;

import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ClaudeDiffInterpreterTest {
    private CompareResult emptyCompareResult() {
        var r = new CompareResult();
        r.setJobId(UUID.randomUUID().toString());
        r.setItems(List.of());
        r.setSummary(new DiffSummary(0, 0, 0, 0, 0));
        return r;
    }

    @Test void returnsNullWhenNoDiffs() {
        
        var interpreter = new ClaudeDiffInterpreter("fake-key");
        assertThat(interpreter.interpret(emptyCompareResult(), "zh")).isNull();
    }

    @Test void formatIdIsNotNull() {
        
        var interpreter = new ClaudeDiffInterpreter("fake-key");
        assertThat(interpreter).isNotNull();
    }
}
