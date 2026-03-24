package org.sainm.diff;

import org.sainm.model.*;

import java.util.regex.Pattern;

final class SeverityClassifier {
    private static final Pattern CRITICAL_PATTERN = Pattern.compile(
        "\\d+[万亿千百]?元?|\\d{4}[-/年]\\d{1,2}[-/月]\\d{1,2}|合同|金额|总价|单价");

    public DiffSeverity classify(DiffItem item) {
        String orig = item.getOriginal() != null ? item.getOriginal() : "";
        String rev = item.getRevised() != null ? item.getRevised() : "";

        
        if (!orig.equals(rev) &&
                (CRITICAL_PATTERN.matcher(orig).find() || CRITICAL_PATTERN.matcher(rev).find())) {
            return DiffSeverity.CRITICAL;
        }
        
        if (orig.replaceAll("\\s+", "").equals(rev.replaceAll("\\s+", ""))) {
            return DiffSeverity.MINOR;
        }
        
        if (item.getBlockType() == BlockType.TABLE) return DiffSeverity.MAJOR;
        return DiffSeverity.MAJOR;
    }
}
