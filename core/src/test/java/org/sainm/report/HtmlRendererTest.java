package org.sainm.report;

import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlRendererTest {
    private CompareResult compareResultWithModify(String orig, String revised) {
        var item = DiffItem.builder()
            .itemId(UUID.randomUUID().toString())
            .type(DiffType.MODIFY)
            .original(orig)
            .revised(revised)
            .severity(DiffSeverity.MAJOR)
            .build();
        var r = new CompareResult();
        r.setJobId(UUID.randomUUID().toString());
        r.setItems(List.of(item));
        r.setSummary(new DiffSummary(0, 0, 0, 0, 0));
        return r;
    }

    @Test void rendersHtmlWithDiffHighlights() {
        var result = compareResultWithModify("original text", "modified text");
        byte[] output = new HtmlRenderer().render(result, CompareOptions.defaults());
        String html = new String(output, StandardCharsets.UTF_8);
        assertThat(html).contains("<html");
        assertThat(html).contains("original text");
        assertThat(html).contains("modified text");
        assertThat(html).contains("diff-delete");
        assertThat(html).contains("diff-insert");
    }

    @Test void formatIdIsHtml() {
        assertThat(new HtmlRenderer().formatId()).isEqualTo("html");
    }
}
