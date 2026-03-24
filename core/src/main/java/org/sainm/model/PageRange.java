package org.sainm.model;

public record PageRange(int from, int to) {
    
    public static PageRange of(int from, int to) { return new PageRange(from, to); }
}
