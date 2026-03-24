package org.sainm.model;

import java.util.HashMap;
import java.util.Map;

public final class PageMap {
    private final Map<LogicalLocation, PhysicalLocation> mapping = new HashMap<>();

    public void put(LogicalLocation logical, PhysicalLocation physical) {
        mapping.put(logical, physical);
    }

    public PhysicalLocation resolve(LogicalLocation loc) {
        return mapping.get(loc);
    }
}
