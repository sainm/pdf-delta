package org.sainm;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.DeltaType;
import org.junit.jupiter.api.Test;
import org.sainm.report.DiffReportPdfGenerator;
import org.sainm.report.TextDiffRenderer;

public class DebugLinesTest {

    @Test
    void printLines() throws Exception {
        var gen = new TextDiffRenderer();
        byte[] v1 = ContractPdfFactory.contractV1();
        byte[] v2 = ContractPdfFactory.contractV2();

        var linesA = gen.extractLines(v1);
        var linesB = gen.extractLines(v2);

        var textsA = linesA.stream().map(TextDiffRenderer.TextLine::text).toList();
        var textsB = linesB.stream().map(TextDiffRenderer.TextLine::text).toList();
        var patch  = DiffUtils.diff(textsA, textsB);

        System.out.println("=== DIFF DELTAS ===");
        for (var delta : patch.getDeltas()) {
            System.out.printf("[%s] src@%d: %s  =>  tgt: %s%n",
                delta.getType(),
                delta.getSource().getPosition(),
                delta.getSource().getLines(),
                delta.getTarget().getLines());
        }
    }
}
