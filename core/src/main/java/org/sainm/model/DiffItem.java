package org.sainm.model;

import java.util.ArrayList;
import java.util.List;

public final class DiffItem {
    private String itemId;
    private DiffType type;
    private DiffSeverity severity;
    private String original;
    private String revised;
    private double confidence;
    private String context;
    private List<CharDiffSegment> charDiff = new ArrayList<>();
    private BlockType blockType;
    private TableDiffDetail tableDiff;

    private DiffItem() {}

    public String getItemId() { return itemId; }
    public DiffType getType() { return type; }
    public DiffSeverity getSeverity() { return severity; }
    public String getOriginal() { return original; }
    public String getRevised() { return revised; }
    public double getConfidence() { return confidence; }
    public String getContext() { return context; }
    public List<CharDiffSegment> getCharDiff() { return charDiff; }
    public BlockType getBlockType() { return blockType; }
    public TableDiffDetail getTableDiff() { return tableDiff; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final DiffItem item = new DiffItem();

        public Builder itemId(String itemId) { item.itemId = itemId; return this; }
        public Builder type(DiffType type) { item.type = type; return this; }
        public Builder severity(DiffSeverity severity) { item.severity = severity; return this; }
        public Builder original(String original) { item.original = original; return this; }
        public Builder revised(String revised) { item.revised = revised; return this; }
        public Builder confidence(double confidence) { item.confidence = confidence; return this; }
        public Builder context(String context) { item.context = context; return this; }
        public Builder charDiff(List<CharDiffSegment> charDiff) { item.charDiff = charDiff; return this; }
        public Builder blockType(BlockType blockType) { item.blockType = blockType; return this; }
        public Builder tableDiff(TableDiffDetail tableDiff) { item.tableDiff = tableDiff; return this; }
        public DiffItem build() {
            if (item.type == null) throw new IllegalStateException("DiffItem.type must not be null");
            return item;
        }
    }
}
