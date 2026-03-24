package org.sainm.model;

import java.util.ArrayList;
import java.util.List;

public final class LogicalParagraph {
    private final String paragraphId;
    private final StringBuilder text;
    private final List<PhysicalLocation> locations = new ArrayList<>();

    public LogicalParagraph(String paragraphId, TextBlock block) {
        this.paragraphId = paragraphId;
        this.text = new StringBuilder(block.getText() != null ? block.getText() : "");
    }

    public String getParagraphId() { return paragraphId; }
    public String getText() { return text.toString(); }
    public void appendText(String more) { text.append(" ").append(more); }
    public List<PhysicalLocation> getLocations() { return locations; }
    public void addLocation(PhysicalLocation loc) { locations.add(loc); }
}
