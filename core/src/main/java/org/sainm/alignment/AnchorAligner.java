package org.sainm.alignment;

import org.sainm.model.*;
import org.sainm.spi.LogicalBlock;

import java.util.ArrayList;
import java.util.List;

final class AnchorAligner {
    private final LcsAligner lcsAligner;

    public AnchorAligner(LcsAligner lcsAligner) { this.lcsAligner = lcsAligner; }

    public List<AlignedPair> align(List<LogicalBlock> blocksA, List<LogicalBlock> blocksB,
                                    CompareOptions options) {
        
        
        return lcsAligner.align(blocksA, blocksB, options);
    }
}
