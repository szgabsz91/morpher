package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.Objects;

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

    @Test
    public void testEquals() {
        ProbabilisticStep step1 = new ProbabilisticStep(Word.of("input"), Word.of("output"), AffixType.of("AFF"), 0.5, 0.6, 0.7);
        ProbabilisticStep step2 = new ProbabilisticStep(Word.of("input2"), Word.of("output"), AffixType.of("AFF"), 0.5, 0.6, 0.7);
        ProbabilisticStep step3 = new ProbabilisticStep(Word.of("input"), Word.of("output2"), AffixType.of("AFF"), 0.5, 0.6, 0.7);
        ProbabilisticStep step4 = new ProbabilisticStep(Word.of("input"), Word.of("output"), AffixType.of("AFF2"), 0.5, 0.6, 0.7);
        ProbabilisticStep step5 = new ProbabilisticStep(Word.of("input"), Word.of("output"), AffixType.of("AFF"), 0.6, 0.6, 0.7);
        ProbabilisticStep step6 = new ProbabilisticStep(Word.of("input"), Word.of("output"), AffixType.of("AFF"), 0.5, 0.7, 0.7);
        ProbabilisticStep step7 = new ProbabilisticStep(Word.of("input"), Word.of("output"), AffixType.of("AFF"), 0.5, 0.6, 0.8);
        ProbabilisticStep step8 = new ProbabilisticStep(Word.of("input"), Word.of("output"), AffixType.of("AFF"), 0.5, 0.6, 0.7);

        assertThat(step1).isEqualTo(step1);
        assertThat(step1).isNotEqualTo(null);
        assertThat(step1).isNotEqualTo("string");
        assertThat(step1).isNotEqualTo(step2);
        assertThat(step1).isNotEqualTo(step3);
        assertThat(step1).isNotEqualTo(step4);
        assertThat(step1).isNotEqualTo(step5);
        assertThat(step1).isNotEqualTo(step6);
        assertThat(step1).isNotEqualTo(step7);
        assertThat(step1).isEqualTo(step8);
    }

    @Test
    public void testHashCode() {
        ProbabilisticStep step = new ProbabilisticStep(Word.of("input"), Word.of("output"), AffixType.of("AFF"), 0.5, 0.6, 0.7);
        int result = step.hashCode();
        int expected = Objects.hash(Objects.hash(step.getInput(), step.getOutput(), step.getAffixType()), step.getAffixTypeProbability(), step.getOutputWordProbability(), step.getAggregatedProbability());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        ProbabilisticStep step = new ProbabilisticStep(Word.of("input"), Word.of("output"), AffixType.of("AFF"), 0.5, 0.0, 0.0);
        assertThat(step).hasToString("ProbabilisticStep[input=" + step.getInput() + ", output=" + step.getOutput() + ", affixType=" + step.getAffixType() + ", affixTypeProbability=" + step.getAffixTypeProbability() + ", outputWordProbability=" + step.getOutputWordProbability() + ", aggregatedProbability=" + step.getAggregatedProbability() + ']');
    }

}
