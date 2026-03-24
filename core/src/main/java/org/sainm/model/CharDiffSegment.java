package org.sainm.model;

public record CharDiffSegment(String text, SegmentType type) {
    public enum SegmentType { EQUAL, INSERT, DELETE }
}
