package org.sainm.model;

public record DiffSummary(int totalDiffs, int critical, int major, int minor, int info) {}
