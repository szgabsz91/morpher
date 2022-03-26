package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProbabilisticStepTest {

    @Test
    public void testConstructorAndGetters() {
        Word input = Word.of("input");
        Word output = Word.of("output");
        AffixType affixType = AffixType.of("AFF");
        double affixTypeProbability = 0.5;
        double outputWordProbability = 0.6;
        double aggregatedProbability = 0.6;
        ProbabilisticStep step = new ProbabilisticStep(input, output, affixType, affixTypeProbability, outputWordProbability, aggregatedProbability);
        assertThat(step.getInput()).isEqualTo(input);
        assertThat(step.getOutput()).isEqualTo(output);
        assertThat(step.getAffixType()).isEqualTo(affixType);
        assertThat(step.getAffixTypeProbability()).isEqualTo(affixTypeProbability);
        assertThat(step.getOutputWordProbability()).isEqualTo(outputWordProbability);
        assertThat(step.getAggregatedProbability()).isEqualTo(aggregatedProbability);
    }

}
