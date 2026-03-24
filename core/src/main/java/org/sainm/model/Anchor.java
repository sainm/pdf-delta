package org.sainm.model;

public record Anchor(String text, int searchRadius) {
    public Anchor(String text) { this(text, 50); }
}
