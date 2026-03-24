package org.sainm.alignment;

import com.github.difflib.DiffUtils;
import org.sainm.model.*;
import org.sainm.spi.LogicalBlock;

import java.util.ArrayList;
import java.util.List;

final class LcsAligner {
    public List<AlignedPair> align(List<LogicalBlock> blocksA, List<LogicalBlock> blocksB,
                                    CompareOptions options) {
        var textsA = blocksA.stream().map(b -> b.getNormalizedText() != null ? b.getNormalizedText() : "").toList();
        var textsB = blocksB.stream().map(b -> b.getNormalizedText() != null ? b.getNormalizedText() : "").toList();
        var patch = DiffUtils.diff(textsA, textsB);

        List<AlignedPair> result = new ArrayList<>();
        int ai = 0, bi = 0;
        for (var delta : patch.getDeltas()) {
            
            while (ai < delta.getSource().getPosition()) {
                result.add(new AlignedPair(blocksA.get(ai), blocksB.get(bi), 1.0,
                    AlignmentStrategy.LCS, AlignedPair.AlignedPairType.EQUAL));
                ai++; bi++;
            }
            
            for (int i = 0; i < delta.getTarget().size(); i++) {
                result.add(new AlignedPair(null, blocksB.get(bi + i), 0.0,
                    AlignmentStrategy.LCS, AlignedPair.AlignedPairType.ADD));
            }
            
            for (int i = 0; i < delta.getSource().size(); i++) {
                result.add(new AlignedPair(blocksA.get(ai + i), null, 0.0,
                    AlignmentStrategy.LCS, AlignedPair.AlignedPairType.DELETE));
            }
            ai += delta.getSource().size();
            bi += delta.getTarget().size();
        }
        
        while (ai < blocksA.size() && bi < blocksB.size()) {
            result.add(new AlignedPair(blocksA.get(ai), blocksB.get(bi), 1.0,
                AlignmentStrategy.LCS, AlignedPair.AlignedPairType.EQUAL));
            ai++; bi++;
        }
        return result;
    }
}
