package org.sainm.cli;

import org.sainm.PdfFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CompareCommandTest {
    @Test void exitCode0ForIdenticalFiles(@TempDir Path tmp) throws Exception {
        byte[] pdf = PdfFixtures.singlePageWithText("Hello");
        Path a = tmp.resolve("a.pdf"); Files.write(a, pdf);
        Path b = tmp.resolve("b.pdf"); Files.write(b, pdf);

        int exit = new CommandLine(new CompareCommand()).execute(
            a.toString(), b.toString(), "--format", "json");
        assertThat(exit).isEqualTo(0);
    }

    @Test void exitCode1ForDifferentFiles(@TempDir Path tmp) throws Exception {
        Path a = tmp.resolve("a.pdf"); Files.write(a, PdfFixtures.singlePageWithText("Original text content here."));
        Path b = tmp.resolve("b.pdf"); Files.write(b, PdfFixtures.singlePageWithText("Modified text content here."));

        int exit = new CommandLine(new CompareCommand()).execute(a.toString(), b.toString());
        assertThat(exit).isEqualTo(1);
    }
}
