package org.sainm.mcp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ComparePdfsToolTest {
    @Test void comparesIdenticalPdfsReturnsEmptyItems() throws Exception {
        byte[] pdf = PdfFixtures.singlePageWithText("Hello");
        String base64 = Base64.getEncoder().encodeToString(pdf);
        var tool = new ComparePdfsTool(new McpJobStore());
        var result = tool.execute(Map.of("fileA", base64, "fileB", base64, "inputType", "base64"));
        assertThat(result).containsKey("jobId");
        assertThat((List<?>) result.get("items")).isEmpty();
    }

    @Test void pathInputReadsFromDisk(@TempDir Path tmp) throws Exception {
        Path p = tmp.resolve("test.pdf");
        Files.write(p, PdfFixtures.singlePageWithText("Hello"));
        var tool = new ComparePdfsTool(new McpJobStore());
        var result = tool.execute(Map.of("fileA", p.toString(), "fileB", p.toString(), "inputType", "path"));
        assertThat(result).containsKey("jobId");
    }
}
