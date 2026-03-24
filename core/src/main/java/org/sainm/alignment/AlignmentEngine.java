package org.sainm.alignment;

import org.sainm.model.*;
import org.sainm.spi.LogicalBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class AlignmentEngine {
    private final ToleranceAligner toleranceAligner = new ToleranceAligner();
    private final LcsAligner lcsAligner = new LcsAligner();
    private final FuzzyAligner fuzzyAligner = new FuzzyAligner();

    public List<AlignedPair> align(List<LogicalBlock> blocksA, List<LogicalBlock> blocksB,
                                    CompareOptions options) {

        
        var toleranceMatched = toleranceAligner.align(blocksA, blocksB, options);
        List<AlignedPair> result = new ArrayList<>(toleranceMatched);
        var matchedA = toleranceMatched.stream().map(AlignedPair::blockA).collect(Collectors.toSet());
        var matchedB = toleranceMatched.stream().map(AlignedPair::blockB).collect(Collectors.toSet());
        var remainA = blocksA.stream().filter(b -> !matchedA.contains(b)).toList();
        var remainB = blocksB.stream().filter(b -> !matchedB.contains(b)).toList();

        if (remainA.isEmpty() && remainB.isEmpty()) return result;

        
        List<AlignedPair> lcsResult;
        if (!options.getAnchors().isEmpty()) {
            lcsResult = new AnchorAligner(lcsAligner).align(remainA, remainB, options);
        } else {
            lcsResult = lcsAligner.align(remainA, remainB, options);
        }

        
        result.addAll(fuzzyAligner.merge(lcsResult, options));
        return result;
    }
}
