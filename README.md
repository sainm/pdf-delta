# pdf-delta

`pdf-delta` 是一个面向 PDF 文档比对的多模块 Java 工程。它的核心能力是把两个 PDF 解析为文本块、表格块和图像页，再做对齐、差异识别和报告输出，适合做合同、报表、扫描件、图片型 PDF 的差异分析。

当前工程已经提供 4 类使用方式：

- `core`：程序内嵌调用的核心比较 API
- `cli`：命令行比较工具
- `web`：HTTP 文件上传比较服务
- `mcp`：MCP Tool Server，方便被 Agent / LLM 工具链调用

同时工程还拆分了可插拔扩展模块：

- `ocr-paddle`：本地 Paddle OCR
- `ocr-remote`：远程 OCR 适配层
- `ai`：AI 增强能力，如 OCR 修正、差异摘要

## 1. 工程定位

从代码实现看，这个工程的主链路是：

1. 读取 PDF
2. 提取文本页 / 图像页内容
3. 对提取结果做预处理和归一化
4. 重建表格结构
5. 组织成逻辑文档
6. 执行块级对齐
7. 生成文本差异、表格差异、视觉差异
8. 输出 JSON / HTML / PDF 报告

核心入口类是：

- `org.sainm.pipeline.PdfComparator`
- `org.sainm.model.CompareRequest`
- `org.sainm.model.CompareOptions`
- `org.sainm.model.CompareResult`

## 2. 工程结构

```text
pdf-delta
├─ core         核心比较流程、模型、报告渲染、SPI
├─ cli          Picocli 命令行入口
├─ web          Spring Boot HTTP 服务
├─ mcp          MCP Server
├─ ocr-paddle   本地 Paddle OCR Provider
├─ ocr-remote   远程 OCR Provider
├─ ai           AI 增强实现
└─ local-ocr    本地 Paddle 推理库与依赖文件
```

各模块职责如下。

### `core`

核心领域模块，包含：

- `model`：比较请求、结果、差异项、OCR 配置等数据结构
- `extractor`：文本提取、图像页提取、OCR 装配
- `preprocessor`：页面预处理
- `normalizer`：文本归一化
- `table`：表格聚类与重建
- `document`：逻辑文档组织
- `alignment`：块对齐
- `diff`：差异计算和严重级别分类
- `report`：JSON / HTML / PDF 报告生成
- `spi`：OCR、报告、归一化、AI 等扩展接口

### `cli`

命令行入口，主类：

- `org.sainm.cli.PdfCompareCli`
- `org.sainm.cli.CompareCommand`

### `web`

Spring Boot Web 服务，主类：

- `org.sainm.web.PdfCompareApplication`

控制器：

- `org.sainm.web.CompareController`

### `mcp`

MCP Server，主类：

- `org.sainm.mcp.PdfCompareMcpServer`

已注册工具：

- `compare_pdfs`
- `get_diff_summary`
- `get_page_diff`

### `ocr-paddle`

本地 OCR 实现：

- `org.sainm.ocr.paddle.PaddleOcrProvider`

### `ocr-remote`

远程 OCR 实现：

- `org.sainm.ocr.remote.RemoteOcrProvider`

### `ai`

AI 增强实现：

- `org.sainm.ai.ClaudeOcrEnhancer`
- `org.sainm.ai.ClaudeDiffInterpreter`
- `org.sainm.ai.ClaudeTableStructureRecognizer`

## 3. 构建与运行

### 环境要求

- JDK 17
- Gradle Wrapper
- Windows 环境下如果启用本地 Paddle OCR，需要 `local-ocr` 中的本地库可被加载

根工程 `build.gradle` 指定了：

```gradle
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
```

### 编译

```bash
./gradlew build
```

Windows PowerShell：

```powershell
.\gradlew.bat build
```

### 运行测试

```bash
./gradlew test
```

## 4. 使用方式

## 4.1 CLI

CLI 子命令为 `compare`。

示例：

```bash
java -jar cli/build/libs/cli-*.jar compare a.pdf b.pdf --format json
```

输出文件命名规则来自 `CompareCommand`：

```text
{fileA名称}_vs_{fileB名称}.{format}
```

例如：

```text
a.pdf_vs_b.pdf.json
```

支持的 `--format`：

- `json`
- `html`
- `pdf-annotated`
- `pdf-image-marked`

多个格式可逗号分隔：

```bash
java -jar cli/build/libs/cli-*.jar compare a.pdf b.pdf --format json,html,pdf-image-marked
```

CLI 当前支持的参数：

- `--format`：输出格式
- `--fuzzy-threshold`：模糊匹配阈值，默认 `0.85`
- `--ignore-whitespace`：是否忽略空白，默认 `true`
- `--enable-visual-diff`：是否启用视觉差异，默认 `true`
- `--visual-threshold`：视觉差异阈值，默认 `0.01`
- `--render-dpi`：PDF 渲染 DPI，默认 `150`
- `--ocr-min-confidence`：OCR 最低置信度，默认 `0.6`
- `--ocr-provider`：`AUTO` / `LOCAL_PADDLE` / `REMOTE`
- `--ocr-language`：默认 `zh`

CLI 返回码：

- `0`：没有差异
- `1`：存在差异

如果有 OCR 失败页，CLI 会额外打印失败页号；如果检测到图像型页面，还会打印视觉比较摘要。

## 4.2 Java API

最核心的程序化调用方式如下：

```java
import org.sainm.model.CompareOptions;
import org.sainm.model.CompareRequest;
import org.sainm.model.CompareResult;
import org.sainm.model.PdfSource;
import org.sainm.pipeline.PdfComparator;

import java.nio.file.Path;

CompareOptions options = CompareOptions.defaults();
options.setFuzzyThreshold(0.85);
options.setIgnoreWhitespace(true);
options.setEnableVisualDiff(true);
options.setRenderDpi(150);
options.getOcrOptions().setLanguage("zh");

CompareRequest request = new CompareRequest(
    new PdfSource.FilePath(Path.of("a.pdf")),
    new PdfSource.FilePath(Path.of("b.pdf")),
    options
);

CompareResult result = new PdfComparator().compare(request);
```

也可以直接传字节数组：

```java
CompareRequest request = new CompareRequest(
    new PdfSource.Bytes(bytesA),
    new PdfSource.Bytes(bytesB),
    CompareOptions.defaults()
);
```

### `CompareRequest`

定义：

```java
public record CompareRequest(PdfSource sourceA, PdfSource sourceB, CompareOptions options)
```

作用：

- 指定对比源 A
- 指定对比源 B
- 指定本次比较参数

### `PdfSource`

支持三种输入：

- `PdfSource.FilePath`
- `PdfSource.Stream`
- `PdfSource.Bytes`

### `CompareOptions`

默认构造方式：

```java
CompareOptions.defaults()
```

当前代码中比较常用的配置项有：

- `fuzzyThreshold`
- `positionTolerance`
- `ignoreWhitespace`
- `ignoreNumberFormat`
- `ignoreDateFormat`
- `ignoreHeaderFooter`
- `ignoreWatermark`
- `enableVisualDiff`
- `visualDiffThreshold`
- `renderDpi`
- `sectionNumberMode`
- `ocrOptions`

### `OcrOptions`

OCR 配置项包括：

- `providerType`：`AUTO` / `LOCAL_PADDLE` / `REMOTE`
- `language`
- `remoteEndpoint`
- `remoteApiKey`
- `modelDir`
- `enableCache`
- `cacheDir`
- `cacheScope`
- `minConfidence`

### `CompareResult`

比较结果主要包含：

- `jobId`
- `items`：文本 / 表格差异项列表
- `summary`：严重级别汇总
- `ocrFailedPages`
- `imageComparisonSummary`
- `visualDiffItems`
- `pageSummaries`
- `warnings`
- `optionsUsed`

### `DiffSummary`

```java
public record DiffSummary(int totalDiffs, int critical, int major, int minor, int info)
```

### `DiffItem`

单条差异项包含的主要字段：

- `itemId`
- `type`
- `severity`
- `original`
- `revised`
- `confidence`
- `context`
- `charDiff`
- `blockType`
- `tableDiff`

### 结果渲染

报告渲染通过 `ServiceLoader` 加载 `ReportRenderer` 实现。

接口：

```java
public interface ReportRenderer {
    byte[] render(CompareResult result, CompareOptions options);
    String formatId();
}
```

当前已注册 renderer：

- `json`
- `html`
- `pdf-annotated`
- `pdf-image-marked`

## 4.3 Web API

`web` 模块提供 Spring Boot 服务，默认端口 `8080`。

配置文件：

```yaml
server:
  port: 8080
spring:
  servlet:
    multipart:
      max-file-size: 200MB
      max-request-size: 400MB
```

启动：

```bash
./gradlew :web:bootRun
```

### 1. 同步比较

`POST /api/compare`

`multipart/form-data` 参数：

- `fileA`
- `fileB`
- `options`：可选，JSON 字符串，对应 `CompareOptions`

返回：

- `CompareResult` JSON

`curl` 示例：

```bash
curl -X POST "http://localhost:8080/api/compare" \
  -F "fileA=@a.pdf" \
  -F "fileB=@b.pdf"
```

### 2. 同步生成报告文件

`POST /api/compare/artifact/{format}`

例如：

```bash
curl -X POST "http://localhost:8080/api/compare/artifact/pdf-image-marked" \
  -F "fileA=@a.pdf" \
  -F "fileB=@b.pdf" \
  --output result.pdf
```

返回 `Content-Type`：

- `json` -> `application/json`
- `html` -> `text/html`
- `pdf-annotated` / `pdf-image-marked` -> `application/pdf`

### 3. 异步比较

`POST /api/compare/async`

返回示例：

```json
{
  "jobId": "xxxx",
  "statusUrl": "/api/compare/xxxx/status"
}
```

### 4. 查询任务状态

`GET /api/compare/{jobId}/status`

返回内容包含：

- `jobId`
- `status`
- `progress`
- `warnings`
- `imageComparisonSummary`

当前任务状态类型：

- `PENDING`
- `RUNNING`
- `DONE`
- `FAILED`

### 5. 下载异步任务报告

`GET /api/compare/{jobId}/report/{format}`

说明：

- 只有任务状态为 `DONE` 才能下载
- 当前实现下载时使用的是 `CompareOptions.defaults()` 来渲染报告，而不是任务原始 options

### Web 服务运行约束

从 `JobStore` 实现看：

- 最大并发任务数为 `10`
- 任务结果保留时间为 `30` 分钟
- 超出并发时返回 `429`

## 4.4 MCP 使用方式

MCP Server 主类：

```text
org.sainm.mcp.PdfCompareMcpServer
```

它通过标准输入输出启动 MCP 传输层。

已暴露工具如下。

### `compare_pdfs`

输入字段：

- `fileA`
- `fileB`
- `inputType`：`path` 或 `base64`
- `options`：可选

返回字段：

- `jobId`
- `items`
- `summary`
- `ocrFailedPages`
- `imageComparisonSummary`
- `visualDiffItems`
- `pageSummaries`
- `warnings`

### `get_diff_summary`

输入字段：

- `jobId`
- `language`：默认 `zh`

返回字段：

- `jobId`
- `summary`
- `countBySeverity`
- `totalDiffs`

### `get_page_diff`

输入字段：

- `jobId`
- `page`

当前实现说明：

- `page` 参数已预留
- 目前尚未真正做按页过滤，返回的是全部 diff items

## 5. SPI 与扩展机制

这个工程大量使用 `ServiceLoader` 做扩展装配。

## 5.1 OCR Provider

接口：

```java
public interface OcrProvider {
    List<TextBlock> recognize(BufferedImage image, OcrOptions options);
    String providerId();
    boolean isAvailable();
    int priority();
}
```

当前实现：

- `org.sainm.ocr.paddle.PaddleOcrProvider`
- `org.sainm.ocr.remote.RemoteOcrProvider`

说明：

- OCR Provider 是按 classpath 中注册的 SPI 自动发现的
- 优先级高的 provider 会优先被选中
- 如果没有可用 OCR provider，图像页会标记为 `OCR_FAILED`

## 5.2 ReportRenderer

接口：

```java
public interface ReportRenderer {
    byte[] render(CompareResult result, CompareOptions options);
    String formatId();
}
```

当前实现：

- `JsonRenderer`
- `HtmlRenderer`
- `PdfAnnotationRenderer`
- `ImageMarkedPdfRenderer`

## 5.3 Normalizer

接口：

```java
public interface Normalizer {
    String normalize(String text, CompareOptions options);
    int order();
}
```

当前已注册：

- `WhitespaceNormalizer`
- `GlyphNormalizer`
- `NumberNormalizer`
- `DateNormalizer`
- `UnitNormalizer`

## 5.4 AI 扩展接口

接口包括：

- `OcrEnhancer`
- `DiffInterpreter`
- `TableStructureRecognizer`

当前 `ai` 模块中已有：

- `ClaudeOcrEnhancer`
- `ClaudeDiffInterpreter`
- `ClaudeTableStructureRecognizer`

## 6. 核心处理流程

`PdfComparator.compare()` 的实际执行流程如下：

```text
CompareRequest
  -> PdfSource 转字节
  -> PdfExtractorFactory 提取页面内容
  -> PreprocessorChain 预处理
  -> NormalizerChain 归一化
  -> TableReconstructor 表格重建
  -> DocumentStructurer 逻辑文档结构化
  -> AlignmentEngine 对齐
  -> DiffEngine 生成差异
  -> ImagePageComparator 生成视觉差异
  -> CompareResult
```

处理结果中会同时给出：

- 文本 / 表格差异
- 图像页面视觉差异
- OCR 失败页
- 页级摘要
- 严重级别统计

## 7. 报告格式说明

### `json`

最完整、最适合程序处理的输出格式，直接序列化 `CompareResult`。

### `html`

适合人工查看，显示：

- 差异项
- 严重级别
- 字符级 diff
- 表格 diff
- 图像页差异摘要

### `pdf-annotated`

接口已经接好，但当前 `PdfAnnotationRenderer.annotateItem()` 还是空实现，因此目前更接近“占位版本”。

### `pdf-image-marked`

当前是更实用的 PDF 输出方式，会：

- 将 A / B 两份 PDF 页面并排渲染
- 用色框标记表格 / 文本差异
- 用蓝框标记视觉差异区域

## 8. 当前实现中的注意点

这些内容是根据代码当前状态整理的，写在这里便于后续使用时少踩坑。

- `cli`、`web`、`mcp` 模块本身只直接依赖 `core`，OCR / AI 能力是否生效取决于运行时 classpath 是否把 `ocr-paddle`、`ocr-remote`、`ai` 等模块一起带上。
- `ImagePdfExtractor` 当前是按 provider 优先级自动选择 OCR 实现；虽然 `OcrOptions.ProviderType` 已定义，但核心提取流程里还没有基于这个枚举做强制路由。
- `PdfExtractorFactory.extractStreaming()` 目前只是预留，实际仍回落到 `extractAll()`。
- `CompareProgressListener` 字段已经出现在 `PdfComparator` 中，但当前核心流程并没有真正上报进度，因此 Web 的 `progress` 目前不会反映细粒度进度。
- `get_page_diff` 的按页过滤还没真正实现。
- `pdf-annotated` 报告还没有完成真实标注绘制。

## 9. 推荐使用姿势

如果你只是想把工程先跑起来，建议优先这样用：

1. 先用 `core` 或 `cli` 验证基础文本比对能力。
2. 如果要处理扫描件或图片型 PDF，再把 `ocr-paddle` 或 `ocr-remote` 放到运行时 classpath。
3. 如果要对外提供服务，用 `web` 模块。
4. 如果要接入 Agent / LLM 调用链，用 `mcp` 模块。

如果你要二次开发，推荐从下面几个类开始读：

- `org.sainm.pipeline.PdfComparator`
- `org.sainm.extractor.PdfExtractorFactory`
- `org.sainm.document.DocumentStructurer`
- `org.sainm.alignment.AlignmentEngine`
- `org.sainm.diff.DiffEngine`
- `org.sainm.report.ImageMarkedPdfRenderer`

## 10. 一段最小可运行示例

```java
import org.sainm.model.CompareRequest;
import org.sainm.model.CompareResult;
import org.sainm.model.PdfSource;
import org.sainm.pipeline.PdfComparator;

import java.nio.file.Path;

public class Demo {
    public static void main(String[] args) {
        CompareResult result = new PdfComparator().compare(
            new CompareRequest(
                new PdfSource.FilePath(Path.of("a.pdf")),
                new PdfSource.FilePath(Path.of("b.pdf"))
            )
        );

        System.out.println("jobId = " + result.getJobId());
        System.out.println("diffs = " + result.getItems().size());
        System.out.println("summary = " + result.getSummary());
    }
}
```
## 11. 最终结果输出示例

基于上面的 `Demo`，控制台中拿到的比较结果通常会类似这样：

```text
jobId = 9f4d9b62-7d25-4c9a-bd1d-1f8b4f9e2a31
diffs = 3
summary = DiffSummary[totalDiffs=3, critical=0, major=1, minor=2, info=0]
```

如果你希望把比较结果输出成 PDF 文件，推荐使用 `pdf-image-marked` 格式：

```java
import org.sainm.model.CompareOptions;
import org.sainm.model.CompareRequest;
import org.sainm.model.CompareResult;
import org.sainm.model.PdfSource;
import org.sainm.pipeline.PdfComparator;
import org.sainm.spi.ReportRenderer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;

public class DemoWithOutput {
    public static void main(String[] args) throws Exception {
        CompareOptions options = CompareOptions.defaults();

        CompareResult result = new PdfComparator().compare(
            new CompareRequest(
                new PdfSource.FilePath(Path.of("a.pdf")),
                new PdfSource.FilePath(Path.of("b.pdf")),
                options
            )
        );

        ReportRenderer renderer = ServiceLoader.load(ReportRenderer.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .filter(it -> it.formatId().equals("pdf-image-marked"))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("pdf-image-marked renderer not found"));

        byte[] bytes = renderer.render(result, options);
        Files.write(Path.of("a_vs_b_diff.pdf"), bytes);

        System.out.println("report written to a_vs_b_diff.pdf");
    }
}
```

运行后会生成结果文件：

```text
a_vs_b_diff.pdf
```

这个 PDF 会把 A / B 两份文档并排展示，并在页面上标出检测到的差异区域。

如果你希望把每一种输出方式都写成明确示例，可以按下面理解：

### 11.1 文本比较结果的 5 种 PDF 出力方式

这些模式对应 `DiffReportPdfGenerator` 的 `ReportMode`，适合“文本型 PDF”的差异展示。

#### 1. `TEXT_DIFF`

适合看逐段、逐行的文本差异。

```java
import org.sainm.report.DiffReportPdfGenerator;
import org.sainm.report.ReportMode;

byte[] bytes = new DiffReportPdfGenerator()
    .mode(ReportMode.TEXT_DIFF)
    .generate(result, result.getSourceBytesA(), result.getSourceBytesB());

Files.write(Path.of("text-diff.pdf"), bytes);
```

输出文件示例：

```text
text-diff.pdf
```

效果示例：

```text
Page 1
[-合同金额：100000-]
[+合同金额：120000+]
```

#### 2. `SIDE_BY_SIDE`

适合左右对照看 A / B 两份文本。

```java
byte[] bytes = new DiffReportPdfGenerator()
    .mode(ReportMode.SIDE_BY_SIDE)
    .generate(result, result.getSourceBytesA(), result.getSourceBytesB());

Files.write(Path.of("side-by-side.pdf"), bytes);
```

输出文件示例：

```text
side-by-side.pdf
```

效果示例：

```text
Left : 合同金额：100000
Right: 合同金额：120000
```

#### 3. `SINGLE_PAGE`

适合把差异叠加到单页视图中查看。

```java
byte[] bytes = new DiffReportPdfGenerator()
    .mode(ReportMode.SINGLE_PAGE)
    .generate(result, result.getSourceBytesA(), result.getSourceBytesB());

Files.write(Path.of("single-page.pdf"), bytes);
```

输出文件示例：

```text
single-page.pdf
```

效果示例：

```text
Page 1 overlay
[changed] 合同金额：100000 -> 120000
```

#### 4. `DUAL_PAGE`

适合把两页并排放在一个 PDF 页面里比较。

```java
byte[] bytes = new DiffReportPdfGenerator()
    .mode(ReportMode.DUAL_PAGE)
    .generate(result, result.getSourceBytesA(), result.getSourceBytesB());

Files.write(Path.of("dual-page.pdf"), bytes);
```

输出文件示例：

```text
dual-page.pdf
```

效果示例：

```text
| Page A | Page B |
| 100000 | 120000 |
```

#### 5. `TEXT_LAYOUT`

适合尽量保留原始文本布局后再标出差异。

```java
byte[] bytes = new DiffReportPdfGenerator()
    .mode(ReportMode.TEXT_LAYOUT)
    .generate(result, result.getSourceBytesA(), result.getSourceBytesB());

Files.write(Path.of("text-layout.pdf"), bytes);
```

输出文件示例：

```text
text-layout.pdf
```

效果示例：

```text
第一条  金额：100000
第一条  金额：120000   <- changed
```

### 11.2 文本比较结果的 HTML 出力方式

这个模式也是文本比较的一种展示方式，但输出的是 HTML，不是 PDF。

#### 6. `HTML`

```java
byte[] bytes = new DiffReportPdfGenerator()
    .mode(ReportMode.HTML)
    .generate(result, result.getSourceBytesA(), result.getSourceBytesB());

Files.write(Path.of("text-diff.html"), bytes);
```

输出文件示例：

```text
text-diff.html
```

效果示例：

```html
<span class="delete">100000</span>
<span class="insert">120000</span>
```

### 11.3 图像比较结果的 1 种 PDF 出力方式

图像型 PDF 当前推荐使用 `ReportRenderer` 的 `pdf-image-marked`。

#### 7. `pdf-image-marked`

```java
import org.sainm.spi.ReportRenderer;
import java.util.ServiceLoader;

ReportRenderer renderer = ServiceLoader.load(ReportRenderer.class)
    .stream()
    .map(ServiceLoader.Provider::get)
    .filter(it -> it.formatId().equals("pdf-image-marked"))
    .findFirst()
    .orElseThrow(() -> new IllegalStateException("pdf-image-marked renderer not found"));

byte[] bytes = renderer.render(result, options);
Files.write(Path.of("image-marked-diff.pdf"), bytes);
```

输出文件示例：

```text
image-marked-diff.pdf
```

效果示例：

```text
Page A and Page B rendered side by side
[red box] text/table diff area
[blue box] visual diff area
```

### 11.4 其他对外格式

`pdf-annotated` 也已经注册为对外格式，但当前实现还更接近占位版本，不建议作为主输出格式。

如果后续要继续完善这个工程，最值得优先补强的点通常会是：

- 运行时装配方式说明与打包方式统一
- `pdf-annotated` 真正标注实现
- `ProviderType` 强制选择 OCR provider
- Web / MCP 的进度和按页查询能力
- 流式提取与大文件处理
