package com.github.szgabsz91.morpher.engines.impl.impl.probability;

import com.github.szgabsz91.morpher.analyzeragents.api.model.ProbabilisticAffixType;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.engines.api.model.MorpherEngineResponse;
import com.github.szgabsz91.morpher.engines.api.model.ProbabilisticStep;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiplyProbabilityCalculatorTest {

    @Test
    public void testSetProbability() {
        IProbabilityCalculator probabilityCalculator = new MultiplyProbabilityCalculator();
        MorpherEngineResponse response = MorpherEngineResponse.inflectionResponse(
                null,
                null,
                null,
                0.0,
                List.of(
                        new ProbabilisticStep(null, null, null, 0.0, 0.2, 0.0),
                        new ProbabilisticStep(null, null, null, 0.0, 0.3, 0.0)
                )
        );
        response.setNormalizedAffixTypeChainProbability(0.4);
        probabilityCalculator.setProbability(response);
        assertThat(response.getAggregatedWeight()).isEqualTo(0.2 * 0.3 * 0.4);
    }

}
