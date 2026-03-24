package org.sainm.web;

import org.sainm.PdfFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompareController.class)
@Import(JobStore.class)
class CompareControllerTest {
    @Autowired MockMvc mvc;

    @Test void syncCompareReturnsResult() throws Exception {
        byte[] pdf = PdfFixtures.singlePageWithText("Hello");
        mvc.perform(multipart("/api/compare")
                .file(new MockMultipartFile("fileA", "a.pdf", "application/pdf", pdf))
                .file(new MockMultipartFile("fileB", "b.pdf", "application/pdf", pdf)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jobId").isNotEmpty())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test void statusEndpointReturnsNotFoundForUnknownJob() throws Exception {
        mvc.perform(get("/api/compare/nonexistent/status"))
            .andExpect(status().isNotFound());
    }

    @Test void syncArtifactReturnsPdf() throws Exception {
        byte[] pdf = PdfFixtures.singlePageWithText("Hello");
        mvc.perform(multipart("/api/compare/artifact/pdf-image-marked")
                .file(new MockMultipartFile("fileA", "a.pdf", "application/pdf", pdf))
                .file(new MockMultipartFile("fileB", "b.pdf", "application/pdf", pdf)))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/pdf"));
    }
}
