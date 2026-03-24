package org.sainm.alignment;

import org.sainm.model.*;
import org.sainm.spi.LogicalBlock;

import java.util.*;

final class ToleranceAligner {
    public List<AlignedPair> align(List<LogicalBlock> blocksA, List<LogicalBlock> blocksB,
                                    CompareOptions options) {
        double tol = options.getPositionTolerance();
        List<AlignedPair> result = new ArrayList<>();
        Set<Integer> matchedB = new HashSet<>();

        for (var a : blocksA) {
            for (int j = 0; j < blocksB.size(); j++) {
                if (matchedB.contains(j)) continue;
                var b = blocksB.get(j);
                String textA = a.getNormalizedText() != null ? a.getNormalizedText() : "";
                String textB = b.getNormalizedText() != null ? b.getNormalizedText() : "";
                if (textA.equals(textB) && bboxDistance(a.getBbox(), b.getBbox()) <= tol) {
                    result.add(new AlignedPair(a, b, 1.0, AlignmentStrategy.TOLERANCE,
                        AlignedPair.AlignedPairType.EQUAL));
                    matchedB.add(j);
                    break;
                }
            }
        }
        return result;
    }

    private double bboxDistance(BoundingBox a, BoundingBox b) {
        if (a == null || b == null) return Double.MAX_VALUE;
        return Math.max(Math.abs(a.x() - b.x()), Math.abs(a.y() - b.y()));
    }
}
