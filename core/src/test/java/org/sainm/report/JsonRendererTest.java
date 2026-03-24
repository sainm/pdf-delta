package org.sainm.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JsonRendererTest {
    private CompareResult minimalCompareResult() {
        var r = new CompareResult();
        r.setJobId(UUID.randomUUID().toString());
        r.setItems(List.of());
        var s = new DiffSummary(0, 0, 0, 0, 0);
        r.setSummary(s);
        return r;
    }

    @Test void rendersValidJson() throws Exception {
        var result = minimalCompareResult();
        byte[] output = new JsonRenderer().render(result, CompareOptions.defaults());
        var json = new ObjectMapper().readTree(output);
        assertThat(json.has("jobId")).isTrue();
        assertThat(json.has("items")).isTrue();
        assertThat(json.has("summary")).isTrue();
    }

    @Test void formatIdIsJson() {
        assertThat(new JsonRenderer().formatId()).isEqualTo("json");
    }
}
