package org.sainm.alignment;

import org.sainm.model.*;
import org.sainm.spi.LogicalBlock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class FuzzyAligner {
    public List<AlignedPair> merge(List<AlignedPair> pairs, CompareOptions options) {
        double threshold = options.getFuzzyThreshold();

        
        List<Integer> deleteIdx = new ArrayList<>();
        List<Integer> addIdx = new ArrayList<>();
        for (int i = 0; i < pairs.size(); i++) {
            var type = pairs.get(i).type();
            if (type == AlignedPair.AlignedPairType.DELETE) deleteIdx.add(i);
            else if (type == AlignedPair.AlignedPairType.ADD) addIdx.add(i);
        }

        
        Set<Integer> mergedDeletes = new HashSet<>();
        Set<Integer> mergedAdds = new HashSet<>();
        List<AlignedPair> merged = new ArrayList<>();

        for (int di : deleteIdx) {
            var delPair = pairs.get(di);
            String textA = delPair.blockA().getNormalizedText();
            textA = textA != null ? textA : "";
            double bestSim = -1;
            int bestAi = -1;
            for (int ai : addIdx) {
                if (mergedAdds.contains(ai)) continue;
                var addPair = pairs.get(ai);
                String textB = addPair.blockB().getNormalizedText();
                textB = textB != null ? textB : "";
                double sim = similarity(textA, textB);
                if (sim >= threshold && sim > bestSim) { bestSim = sim; bestAi = ai; }
            }
            if (bestAi >= 0) {
                var addPair = pairs.get(bestAi);
                merged.add(new AlignedPair(delPair.blockA(), addPair.blockB(), bestSim,
                    AlignmentStrategy.FUZZY, AlignedPair.AlignedPairType.MODIFY));
                mergedDeletes.add(di);
                mergedAdds.add(bestAi);
            }
        }

        
        List<AlignedPair> result = new ArrayList<>();
        Set<Integer> emittedMerge = new HashSet<>();
        for (int i = 0; i < pairs.size(); i++) {
            if (mergedDeletes.contains(i)) {
                
                if (!emittedMerge.contains(i)) {
                    
                    for (var mp : merged) {
                        if (mp.blockA() == pairs.get(i).blockA()) {
                            result.add(mp);
                            emittedMerge.add(i);
                            break;
                        }
                    }
                }
            } else if (!mergedAdds.contains(i)) {
                result.add(pairs.get(i));
            }
        }
        return result;
    }

    double similarity(String a, String b) {
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;
        return 1.0 - (double) editDistance(a, b) / maxLen;
    }

    private int editDistance(String a, String b) {
        int[] dp = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) dp[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            int prev = dp[0]; dp[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int temp = dp[j];
                dp[j] = a.charAt(i-1) == b.charAt(j-1) ? prev
                    : 1 + Math.min(prev, Math.min(dp[j], dp[j-1]));
                prev = temp;
            }
        }
        return dp[b.length()];
    }
}
