package org.sainm.report;

import org.sainm.model.*;
import org.sainm.spi.ReportRenderer;

import java.nio.charset.StandardCharsets;

public final class HtmlRenderer implements ReportRenderer {
    @Override public String formatId() { return "html"; }

    @Override
    public byte[] render(CompareResult result, CompareOptions options) {
        var sb = new StringBuilder();
        sb.append("""
            <!DOCTYPE html><html lang="zh"><head>
            <meta charset="UTF-8">
            <title>PDF Compare Report</title>
            <style>
              .diff-delete { background:#fdd; text-decoration:line-through; }
              .diff-insert { background:#dfd; }
              .diff-equal  { }
              .item { margin:1em 0; padding:0.5em; border:1px solid #ccc; }
              .severity-CRITICAL { border-left:4px solid red; }
              .severity-MAJOR    { border-left:4px solid orange; }
              .severity-MINOR    { border-left:4px solid yellow; }
              .severity-INFO     { border-left:4px solid gray; }
            </style></head><body>
            """);
        sb.append("<h1>PDF Compare Report</h1>");
        sb.append("<p>Job ID: ").append(escape(result.getJobId())).append("</p>");
        if (result.getSummary() != null) {
            var s = result.getSummary();
            sb.append("<p>Total: ").append(s.totalDiffs())
              .append(" | Critical: ").append(s.critical())
              .append(" | Major: ").append(s.major())
              .append(" | Minor: ").append(s.minor()).append("</p>");
        }
        if (result.getWarnings() != null && !result.getWarnings().isEmpty()) {
            sb.append("<h2>Warnings</h2><ul>");
            for (var warning : result.getWarnings()) {
                sb.append("<li>").append(escape(warning)).append("</li>");
            }
            sb.append("</ul>");
        }
        for (var item : result.getItems()) appendItem(sb, item);
        if (result.getVisualDiffItems() != null && !result.getVisualDiffItems().isEmpty()) {
            sb.append("<h2>Image Page Differences</h2>");
            for (var item : result.getVisualDiffItems()) {
                sb.append("<div class=\"item severity-INFO\">")
                    .append("<strong>Page ").append(item.getPageNumber()).append("</strong>")
                    .append(" ratio=").append(String.format("%.4f", item.getDiffRatio()))
                    .append(" reason=").append(escape(item.getReason()));
                if (item.getBbox() != null) {
                    sb.append(" bbox=(")
                        .append((int) item.getBbox().x()).append(",")
                        .append((int) item.getBbox().y()).append(",")
                        .append((int) item.getBbox().width()).append(",")
                        .append((int) item.getBbox().height()).append(")");
                }
                sb.append("</div>");
            }
        }
        sb.append("</body></html>");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendItem(StringBuilder sb, DiffItem item) {
        sb.append("<div class=\"item severity-").append(item.getSeverity()).append("\">");
        sb.append("<span class=\"type\">").append(item.getType()).append("</span> ");
        if (item.getCharDiff() != null && !item.getCharDiff().isEmpty()) {
            for (var seg : item.getCharDiff()) {
                sb.append("<span class=\"diff-").append(seg.type().name().toLowerCase())
                  .append("\">").append(escape(seg.text())).append("</span>");
            }
        } else {
            if (item.getOriginal() != null)
                sb.append("<span class=\"diff-delete\">").append(escape(item.getOriginal())).append("</span>");
            if (item.getRevised() != null)
                sb.append("<span class=\"diff-insert\">").append(escape(item.getRevised())).append("</span>");
        }
        sb.append("</div>");
        if (item.getTableDiff() != null) {
            sb.append("<div class=\"table-diff\">");
            sb.append("<div>Rows: ").append(item.getTableDiff().getOriginalRowCount())
                .append(" -> ").append(item.getTableDiff().getRevisedRowCount())
                .append(", Cols: ").append(item.getTableDiff().getOriginalColumnCount())
                .append(" -> ").append(item.getTableDiff().getRevisedColumnCount())
                .append("</div>");
            if (!item.getTableDiff().getColumnDiffs().isEmpty()) {
                sb.append("<ul>");
                for (var columnDiff : item.getTableDiff().getColumnDiffs()) {
                    sb.append("<li>Column ")
                        .append(columnDiff.getType())
                        .append(": ")
                        .append(escape(columnDiff.getOriginalHeader()))
                        .append(" [").append(columnDiff.getOriginalColumnIndex()).append("]")
                        .append(" -> ")
                        .append(escape(columnDiff.getRevisedHeader()))
                        .append(" [").append(columnDiff.getRevisedColumnIndex()).append("]")
                        .append("</li>");
                }
                sb.append("</ul>");
            }
            if (!item.getTableDiff().getRowDiffs().isEmpty()) {
                sb.append("<ul>");
                for (var rowDiff : item.getTableDiff().getRowDiffs()) {
                    sb.append("<li>Row ").append(rowDiff.getRowIndex())
                        .append(" ").append(rowDiff.getType())
                        .append(": ").append(escape(rowDiff.getOriginal()))
                        .append(" -> ").append(escape(rowDiff.getRevised()))
                        .append("</li>");
                }
                sb.append("</ul>");
            }
            if (!item.getTableDiff().getCellDiffs().isEmpty()) {
                sb.append("<ul>");
                for (var cellDiff : item.getTableDiff().getCellDiffs()) {
                    sb.append("<li>Cell [").append(cellDiff.getRowIndex()).append(",")
                        .append(cellDiff.getColumnIndex()).append("] ")
                        .append(cellDiff.getType())
                        .append(": ").append(escape(cellDiff.getOriginal()))
                        .append(" -> ").append(escape(cellDiff.getRevised()))
                        .append("</li>");
                }
                sb.append("</ul>");
            }
            sb.append("</div>");
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
