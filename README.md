# pdf-delta

`pdf-delta` is a multi-module Java project for comparing two PDF files and generating structured diff reports.

It is suitable for:

- text-first PDF comparison
- table diff detection
- image-like page comparison
- report export to JSON, HTML, and PDF

The project currently contains these modules:

- `core`: comparison pipeline, models, renderers, and SPI
- `cli`: command-line entrypoint
- `web`: Spring Boot HTTP service
- `mcp`: MCP tool server
- `ocr-paddle`: local Paddle OCR provider
- `ocr-remote`: remote OCR provider
- `ai`: AI-based enhancement hooks

## Project Layout

```text
pdf-delta
|- core
|- cli
|- web
|- mcp
|- ocr-paddle
|- ocr-remote
|- ai
`- local-ocr
```

## Core Flow

The main entrypoint is `org.sainm.pipeline.PdfComparator`.

At a high level, the pipeline does this:

1. Read PDF A and PDF B
2. Extract page content
3. Normalize text and structure
4. Align blocks
5. Generate text/table/visual diffs
6. Return `CompareResult`
7. Optionally render the result to JSON, HTML, or PDF

## Build Requirements

- Java 17
- Gradle

The root [`build.gradle`](/D:/pdf-delta/build.gradle) configures Java 17 toolchains.

### Build

```bash
gradle build
```

### Test

```bash
gradle test
```

Note:
This repository currently includes `gradle/wrapper` files, but the launcher scripts `gradlew` / `gradlew.bat` are not present yet. If you want wrapper-based commands in the README later, add those scripts first.

## Output Formats

There are two different "output" concepts in this repository:

1. External artifact formats exposed through `ReportRenderer`
2. Internal text-diff PDF layout modes exposed through `DiffReportPdfGenerator`

### 1. ReportRenderer Formats

These are the format ids currently exposed through CLI / Web / `ServiceLoader`-based report rendering:

- `json`
- `html`
- `pdf-annotated`
- `pdf-image-marked`

Practical guidance:

- `json`: best for downstream systems and automated checks
- `html`: best for quickly reviewing text diffs in a browser
- `pdf-image-marked`: best current PDF output for human review
- `pdf-annotated`: available as a format id, but currently less complete than `pdf-image-marked`

If your goal is "compare two text PDFs and export a PDF report" through CLI or Web, prefer `pdf-image-marked`.

### 2. Text Diff PDF Layout Modes

For text-focused comparison inside `core`, there is also a separate PDF generation path:

- `TEXT_DIFF`
- `SIDE_BY_SIDE`
- `SINGLE_PAGE`
- `DUAL_PAGE`
- `HTML`
- `TEXT_LAYOUT`

These modes are driven by:

- [`DiffReportPdfGenerator.java`](/D:/pdf-delta/core/src/main/java/org/sainm/report/DiffReportPdfGenerator.java)
- [`ReportMode.java`](/D:/pdf-delta/core/src/main/java/org/sainm/report/ReportMode.java)

Important:
`DUAL_PAGE` here refers to an internal PDF layout strategy implemented by [`DualPageRenderer.java`](/D:/pdf-delta/core/src/main/java/org/sainm/report/DualPageRenderer.java). It is not the same thing as the external `pdf-annotated` / `pdf-image-marked` format ids.

## CLI Usage

The CLI main class is `org.sainm.cli.PdfCompareCli`, and the subcommand is `compare`.

General form:

```bash
java -jar cli/build/libs/cli-*.jar compare <fileA.pdf> <fileB.pdf> [options]
```

Output file naming convention:

```text
{fileA-name}_vs_{fileB-name}.{format}
```

Example:

```text
contract-v1.pdf_vs_contract-v2.pdf.json
```

### Most Common Text PDF Example

If you are mainly comparing text PDFs, start with this:

```bash
java -jar cli/build/libs/cli-*.jar compare docs/contract-v1.pdf docs/contract-v2.pdf --format json
```

This produces:

```text
contract-v1.pdf_vs_contract-v2.pdf.json
```

### Export HTML For Review

```bash
java -jar cli/build/libs/cli-*.jar compare docs/contract-v1.pdf docs/contract-v2.pdf --format html
```

This produces:

```text
contract-v1.pdf_vs_contract-v2.pdf.html
```

### Export PDF Report Directly

For a directly reviewable PDF report:

```bash
java -jar cli/build/libs/cli-*.jar compare docs/contract-v1.pdf docs/contract-v2.pdf --format pdf-image-marked
```

This produces:

```text
contract-v1.pdf_vs_contract-v2.pdf.pdf-image-marked
```

If you want a PDF result as the primary artifact, this is the current recommended output format.

### Export Multiple Formats In One Run

This is usually the most practical workflow:

```bash
java -jar cli/build/libs/cli-*.jar compare docs/contract-v1.pdf docs/contract-v2.pdf --format json,html,pdf-image-marked
```

This gives you:

- machine-readable JSON
- browser-readable HTML
- human-review PDF output

### Text PDF Example With Tuned Options

For dense text documents, you may want explicit settings:

```bash
java -jar cli/build/libs/cli-*.jar compare docs/a.pdf docs/b.pdf \
  --format json,html,pdf-image-marked \
  --fuzzy-threshold 0.90 \
  --ignore-whitespace true \
  --enable-visual-diff true \
  --visual-threshold 0.01 \
  --render-dpi 150
```

Windows PowerShell version:

```powershell
java -jar cli/build/libs/cli-*.jar compare docs/a.pdf docs/b.pdf `
  --format json,html,pdf-image-marked `
  --fuzzy-threshold 0.90 `
  --ignore-whitespace true `
  --enable-visual-diff true `
  --visual-threshold 0.01 `
  --render-dpi 150
```

### OCR-Related CLI Options

These are mostly useful when the PDF is scanned or image-heavy:

- `--ocr-provider AUTO|LOCAL_PADDLE|REMOTE`
- `--ocr-language zh`
- `--ocr-min-confidence 0.6`

For ordinary text PDFs, you usually do not need to tune OCR settings first.

### CLI Options

- `--format`: `json`, `html`, `pdf-annotated`, `pdf-image-marked`
- `--fuzzy-threshold`: default `0.85`
- `--ignore-whitespace`: default `true`
- `--enable-visual-diff`: default `true`
- `--visual-threshold`: default `0.01`
- `--render-dpi`: default `150`
- `--ocr-min-confidence`: default `0.6`
- `--ocr-provider`: `AUTO`, `LOCAL_PADDLE`, `REMOTE`
- `--ocr-language`: default `zh`

### CLI Exit Code

- `0`: no diffs
- `1`: diffs found

## Recommended CLI Workflow For Text PDFs

If your primary use case is text PDF comparison, this is the simplest path:

1. Run with `--format json` to verify the diff structure.
2. Run with `--format html` for readable inspection.
3. Run with `--format pdf-image-marked` when you need a deliverable PDF report.
4. Use `--format json,html,pdf-image-marked` once the workflow is stable.

## Java API Usage

### Minimal Example

```java
import org.sainm.model.CompareOptions;
import org.sainm.model.CompareRequest;
import org.sainm.model.CompareResult;
import org.sainm.model.PdfSource;
import org.sainm.pipeline.PdfComparator;

import java.nio.file.Path;

CompareOptions options = CompareOptions.defaults();

CompareRequest request = new CompareRequest(
    new PdfSource.FilePath(Path.of("docs/contract-v1.pdf")),
    new PdfSource.FilePath(Path.of("docs/contract-v2.pdf")),
    options
);

CompareResult result = new PdfComparator().compare(request);
System.out.println(result.getSummary());
```

### Text PDF Example With Explicit Options

```java
import org.sainm.model.CompareOptions;
import org.sainm.model.CompareRequest;
import org.sainm.model.CompareResult;
import org.sainm.model.PdfSource;
import org.sainm.pipeline.PdfComparator;

import java.nio.file.Path;

CompareOptions options = CompareOptions.defaults();
options.setFuzzyThreshold(0.90);
options.setIgnoreWhitespace(true);
options.setIgnoreNumberFormat(true);
options.setIgnoreDateFormat(true);
options.setEnableVisualDiff(true);
options.setVisualDiffThreshold(0.01);
options.setRenderDpi(150);

CompareRequest request = new CompareRequest(
    new PdfSource.FilePath(Path.of("docs/a.pdf")),
    new PdfSource.FilePath(Path.of("docs/b.pdf")),
    options
);

CompareResult result = new PdfComparator().compare(request);
```

### Input Types

`PdfSource` currently supports:

- `PdfSource.FilePath`
- `PdfSource.Stream`
- `PdfSource.Bytes`

Example using byte arrays:

```java
CompareRequest request = new CompareRequest(
    new PdfSource.Bytes(bytesA),
    new PdfSource.Bytes(bytesB),
    CompareOptions.defaults()
);
```

## Render Result To Different Output Types In Java

The renderers are loaded by `ServiceLoader` through `ReportRenderer`.

Interface:

```java
public interface ReportRenderer {
    byte[] render(CompareResult result, CompareOptions options);
    String formatId();
}
```

### Write JSON Output

```java
import org.sainm.spi.ReportRenderer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;

ReportRenderer renderer = ServiceLoader.load(ReportRenderer.class).stream()
    .map(ServiceLoader.Provider::get)
    .filter(r -> r.formatId().equals("json"))
    .findFirst()
    .orElseThrow();

byte[] output = renderer.render(result, options);
Files.write(Path.of("compare-result.json"), output);
```

### Write HTML Output

```java
ReportRenderer renderer = ServiceLoader.load(ReportRenderer.class).stream()
    .map(ServiceLoader.Provider::get)
    .filter(r -> r.formatId().equals("html"))
    .findFirst()
    .orElseThrow();

byte[] output = renderer.render(result, options);
Files.write(Path.of("compare-result.html"), output);
```

### Write PDF Output

Recommended:

```java
ReportRenderer renderer = ServiceLoader.load(ReportRenderer.class).stream()
    .map(ServiceLoader.Provider::get)
    .filter(r -> r.formatId().equals("pdf-image-marked"))
    .findFirst()
    .orElseThrow();

byte[] output = renderer.render(result, options);
Files.write(Path.of("compare-result.pdf"), output);
```

This is the most direct Java-side example for "compare two PDFs and export a PDF report".

## Text PDF Layout Examples In Core

If what you want is specifically the text-comparison PDF layouts in `core`, use `DiffReportPdfGenerator` instead of `ReportRenderer`.

### Shared Setup

```java
import org.sainm.model.CompareOptions;
import org.sainm.model.CompareRequest;
import org.sainm.model.CompareResult;
import org.sainm.model.PdfSource;
import org.sainm.pipeline.PdfComparator;
import org.sainm.report.DiffReportPdfGenerator;
import org.sainm.report.ReportMode;

import java.nio.file.Files;
import java.nio.file.Path;

CompareOptions options = CompareOptions.defaults();

byte[] bytesA = Files.readAllBytes(Path.of("docs/a.pdf"));
byte[] bytesB = Files.readAllBytes(Path.of("docs/b.pdf"));

CompareResult result = new PdfComparator().compare(
    new CompareRequest(
        new PdfSource.Bytes(bytesA),
        new PdfSource.Bytes(bytesB),
        options
    )
);
```

### Default Text Diff PDF

```java
byte[] pdf = new DiffReportPdfGenerator()
    .mode(ReportMode.TEXT_DIFF)
    .generate(result, bytesA, bytesB);

Files.write(Path.of("report-text-diff.pdf"), pdf);
```

### Available Text PDF Modes

- `TEXT_DIFF`: text-oriented diff PDF
- `SIDE_BY_SIDE`: original and revised content shown side by side
- `SINGLE_PAGE`: merged single-page comparison layout
- `DUAL_PAGE`: original page and revised page rendered as separate pages
- `HTML`: HTML diff output from the same generator family
- `TEXT_LAYOUT`: layout-aware text rendering mode

### Example: TEXT_DIFF

```java
byte[] pdf = new DiffReportPdfGenerator()
    .mode(ReportMode.TEXT_DIFF)
    .generate(result, bytesA, bytesB);
Files.write(Path.of("report-text-diff.pdf"), pdf);
```

### Example: SIDE_BY_SIDE

```java
byte[] pdf = new DiffReportPdfGenerator()
    .mode(ReportMode.SIDE_BY_SIDE)
    .generate(result, bytesA, bytesB);
Files.write(Path.of("report-side-by-side.pdf"), pdf);
```

### Example: SINGLE_PAGE

```java
byte[] pdf = new DiffReportPdfGenerator()
    .mode(ReportMode.SINGLE_PAGE)
    .generate(result, bytesA, bytesB);
Files.write(Path.of("report-single-page.pdf"), pdf);
```

### Example: DUAL_PAGE

```java
byte[] pdf = new DiffReportPdfGenerator()
    .mode(ReportMode.DUAL_PAGE)
    .generate(result, bytesA, bytesB);
Files.write(Path.of("report-dual-page.pdf"), pdf);
```

### Example: HTML

```java
byte[] html = new DiffReportPdfGenerator()
    .mode(ReportMode.HTML)
    .generate(result, bytesA, bytesB);
Files.write(Path.of("report.html"), html);
```

### Example: TEXT_LAYOUT

```java
byte[] pdf = new DiffReportPdfGenerator()
    .mode(ReportMode.TEXT_LAYOUT)
    .generate(result, bytesA, bytesB);
Files.write(Path.of("report-text-layout.pdf"), pdf);
```

So yes: if your render path is based on `DiffReportPdfGenerator`, then different PDF outputs such as `DualPageRenderer` being different is expected and correct.

### One Comparison, Multiple Artifacts

```java
import org.sainm.spi.ReportRenderer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

Map<String, ReportRenderer> renderers = ServiceLoader.load(ReportRenderer.class).stream()
    .map(ServiceLoader.Provider::get)
    .collect(Collectors.toMap(ReportRenderer::formatId, r -> r));

Files.write(Path.of("compare-result.json"), renderers.get("json").render(result, options));
Files.write(Path.of("compare-result.html"), renderers.get("html").render(result, options));
Files.write(Path.of("compare-result.pdf"), renderers.get("pdf-image-marked").render(result, options));
```

## Web API Usage

The `web` module exposes HTTP endpoints for upload-and-compare workflows.

Start the service:

```bash
gradle :web:bootRun
```

Default server port:

```text
8080
```

### Synchronous Compare Returning JSON

```bash
curl -X POST "http://localhost:8080/api/compare" \
  -F "fileA=@docs/contract-v1.pdf" \
  -F "fileB=@docs/contract-v2.pdf"
```

### Synchronous Compare Returning PDF

This is the most direct HTTP example for producing a PDF output:

```bash
curl -X POST "http://localhost:8080/api/compare/artifact/pdf-image-marked" \
  -F "fileA=@docs/contract-v1.pdf" \
  -F "fileB=@docs/contract-v2.pdf" \
  --output compare-result.pdf
```

### Return HTML Instead

```bash
curl -X POST "http://localhost:8080/api/compare/artifact/html" \
  -F "fileA=@docs/contract-v1.pdf" \
  -F "fileB=@docs/contract-v2.pdf" \
  --output compare-result.html
```

### Return JSON Artifact Instead

```bash
curl -X POST "http://localhost:8080/api/compare/artifact/json" \
  -F "fileA=@docs/contract-v1.pdf" \
  -F "fileB=@docs/contract-v2.pdf" \
  --output compare-result.json
```

### Send Compare Options

Example with multipart `options` JSON:

```bash
curl -X POST "http://localhost:8080/api/compare" \
  -F "fileA=@docs/a.pdf" \
  -F "fileB=@docs/b.pdf" \
  -F 'options={"fuzzyThreshold":0.9,"ignoreWhitespace":true,"enableVisualDiff":true,"renderDpi":150};type=application/json'
```

## Current Notes

- For text PDFs, start with default options before tuning OCR.
- For final PDF export, prefer `pdf-image-marked`.
- `pdf-annotated` exists as an output id, but `pdf-image-marked` is currently the safer recommendation.
- OCR and AI modules only take effect when their modules are present on the runtime classpath.
- `core` now respects `OcrOptions.providerType` when selecting OCR providers.

## Best Starting Points In Code

- [`PdfComparator.java`](/D:/pdf-delta/core/src/main/java/org/sainm/pipeline/PdfComparator.java)
- [`CompareOptions.java`](/D:/pdf-delta/core/src/main/java/org/sainm/model/CompareOptions.java)
- [`CompareCommand.java`](/D:/pdf-delta/cli/src/main/java/org/sainm/cli/CompareCommand.java)
- [`ImageMarkedPdfRenderer.java`](/D:/pdf-delta/core/src/main/java/org/sainm/report/ImageMarkedPdfRenderer.java)
