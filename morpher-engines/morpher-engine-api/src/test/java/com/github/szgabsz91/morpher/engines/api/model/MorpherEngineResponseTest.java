package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.languagehandlers.api.model.ProbabilisticAffixType;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MorpherEngineResponseTest {

    @Test
    public void testInflectionResponseAndGetters() {
        Word input = Word.of("input");
        Word output = Word.of("output");
        double probability = 0.5;
        ProbabilisticAffixType pos = ProbabilisticAffixType.of(AffixType.of("POS"), 0.6);
        List<ProbabilisticStep> steps = List.of(new ProbabilisticStep(input, output, AffixType.of("AFF"), 0.7, 0.8, 0.9));
        MorpherEngineResponse response = MorpherEngineResponse.inflectionResponse(input, output, pos, probability, steps);
        assertThat(response.getMode()).isEqualTo(Mode.INFLECTION);
        assertThat(response.getInput()).isEqualTo(input);
        assertThat(response.getOutput()).isEqualTo(output);
        assertThat(response.getPos()).isEqualTo(pos);
        assertThat(response.getAffixTypeChainProbability()).isEqualTo(probability);
        assertThat(response.getSteps()).isEqualTo(steps);
        assertThat(response.getNormalizedAffixTypeChainProbability()).isZero();
        assertThat(response.getAggregatedWeight()).isZero();
        double normalizedAffixTypeChainProbability = 0.8;
        response.setNormalizedAffixTypeChainProbability(normalizedAffixTypeChainProbability);
        assertThat(response.getNormalizedAffixTypeChainProbability()).isEqualTo(normalizedAffixTypeChainProbability);
        double aggregatedWeight = 1.0;
        response.setAggregatedWeight(aggregatedWeight);
        assertThat(response.getAggregatedWeight()).isEqualTo(1.0);
    }

    @Test
    public void testAnalysisResponseAndGetters() {
        Word input = Word.of("input");
        Word output = Word.of("output");
        double probability = 0.6;
        ProbabilisticAffixType pos = ProbabilisticAffixType.of(AffixType.of("POS"), 0.5);
        List<ProbabilisticStep> steps = List.of(new ProbabilisticStep(input, output, AffixType.of("AFF"), 0.7, 0.8, 0.9));
        MorpherEngineResponse response = MorpherEngineResponse.analysisResponse(input, output, pos, probability, steps);
        assertThat(response.getMode()).isEqualTo(Mode.ANALYSIS);
        assertThat(response.getInput()).isEqualTo(input);
        assertThat(response.getOutput()).isEqualTo(output);
        assertThat(response.getPos()).isEqualTo(pos);
        assertThat(response.getAffixTypeChainProbability()).isEqualTo(probability);
        assertThat(response.getSteps()).isEqualTo(steps);
        assertThat(response.getNormalizedAffixTypeChainProbability()).isZero();
        assertThat(response.getAggregatedWeight()).isZero();
        double normalizedAffixTypeChainProbability = 0.8;
        response.setNormalizedAffixTypeChainProbability(normalizedAffixTypeChainProbability);
        assertThat(response.getNormalizedAffixTypeChainProbability()).isEqualTo(normalizedAffixTypeChainProbability);
        double aggregatedWeight = 1.0;
        response.setAggregatedWeight(aggregatedWeight);
        assertThat(response.getAggregatedWeight()).isEqualTo(1.0);
    }

    @Test
    public void testCompareToWithNegativeResult() {
        MorpherEngineResponse morpherEngineResponse1 = MorpherEngineResponse.inflectionResponse(null, null, null, 0.0, null);
        morpherEngineResponse1.setAggregatedWeight(1.0);
        MorpherEngineResponse morpherEngineResponse2 = MorpherEngineResponse.inflectionResponse(null, null, null, 0.0, null);
        morpherEngineResponse2.setAggregatedWeight(0.5);
        int result = morpherEngineResponse1.compareTo(morpherEngineResponse2);
        assertThat(result).isNegative();
    }

    @Test
    public void testCompareToWithZeroResult() {
        MorpherEngineResponse morpherEngineResponse1 = MorpherEngineResponse.inflectionResponse(null, null, null, 0.0, null);
        morpherEngineResponse1.setAggregatedWeight(0.5);
        MorpherEngineResponse morpherEngineResponse2 = MorpherEngineResponse.inflectionResponse(null, null, null, 0.0, null);
        morpherEngineResponse2.setAggregatedWeight(0.5);
        int result = morpherEngineResponse1.compareTo(morpherEngineResponse2);
        assertThat(result).isZero();
    }

    @Test
    public void testCompareToWithPositiveResult() {
        MorpherEngineResponse morpherEngineResponse1 = MorpherEngineResponse.inflectionResponse(null, null, null, 0.0, null);
        morpherEngineResponse1.setAggregatedWeight(0.5);
        MorpherEngineResponse morpherEngineResponse2 = MorpherEngineResponse.inflectionResponse(null, null, null, 0.0, null);
        morpherEngineResponse2.setAggregatedWeight(1.0);
        int result = morpherEngineResponse1.compareTo(morpherEngineResponse2);
        assertThat(result).isPositive();
    }

    @Test
    public void testEquals() {
        MorpherEngineResponse response1 = MorpherEngineResponse.inflectionResponse(Word.of("input"), Word.of("output"), ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), 0.5, Collections.emptyList());
        MorpherEngineResponse response2 = MorpherEngineResponse.analysisResponse(Word.of("input"), Word.of("output"), ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), 0.5, Collections.emptyList());
        MorpherEngineResponse response3 = MorpherEngineResponse.inflectionResponse(Word.of("input2"), Word.of("output"), ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), 0.5, Collections.emptyList());
        MorpherEngineResponse response4 = MorpherEngineResponse.inflectionResponse(Word.of("input"), Word.of("output2"), ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), 0.5, Collections.emptyList());
        MorpherEngineResponse response5 = MorpherEngineResponse.inflectionResponse(Word.of("input"), Word.of("output"), ProbabilisticAffixType.of(AffixType.of("AFF2"), 0.5), 0.5, Collections.emptyList());
        MorpherEngineResponse response6 = MorpherEngineResponse.inflectionResponse(Word.of("input"), Word.of("output"), ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), 0.6, Collections.emptyList());
        MorpherEngineResponse response7 = MorpherEngineResponse.inflectionResponse(Word.of("input"), Word.of("output"), ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), 0.5, Collections.singletonList(null));
        MorpherEngineResponse response8 = MorpherEngineResponse.inflectionResponse(Word.of("input"), Word.of("output"), ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), 0.5, Collections.emptyList());

        assertThat(response1.equals(response1)).isTrue();
        assertThat(response1.equals(null)).isFalse();
        assertThat(response1).isNotEqualTo("string");
        assertThat(response1).isNotEqualTo(response2);
        assertThat(response1).isNotEqualTo(response3);
        assertThat(response1).isNotEqualTo(response4);
        assertThat(response1).isNotEqualTo(response5);
        assertThat(response1).isNotEqualTo(response6);
        assertThat(response1).isNotEqualTo(response7);
        assertThat(response1).isEqualTo(response8);
    }

    @Test
    public void testHashCode() {
        MorpherEngineResponse response = MorpherEngineResponse.inflectionResponse(Word.of("input"), Word.of("output"), ProbabilisticAffixType.of(AffixType.of("POS"), 0.6), 0.5, Collections.emptyList());
        int result = response.hashCode();

        int expected;
        long temp;
        expected = response.getMode().hashCode();
        expected = 31 * expected + response.getInput().hashCode();
        expected = 31 * expected + response.getOutput().hashCode();
        expected = 31 * expected + response.getPos().hashCode();
        temp = Double.doubleToLongBits(response.getAffixTypeChainProbability());
        expected = 31 * expected + (int) (temp ^ (temp >>> 32));
        expected = 31 * expected + response.getSteps().hashCode();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        MorpherEngineResponse response = MorpherEngineResponse.inflectionResponse(Word.of("input"), Word.of("output"), ProbabilisticAffixType.of(AffixType.of("POS"), 0.6), 0.5, Collections.singletonList(null));
        response.setAggregatedWeight(10.0);
        assertThat(response).hasToString("MorpherEngineResponse[" +
                "mode=" + response.getMode() +
                ", input=" + response.getInput() +
                ", output=" + response.getOutput() +
                ", pos=" + response.getPos() +
                ", affixTypeChainProbability=" + response.getAffixTypeChainProbability() +
                ", steps=" + response.getSteps() +
                ", normalizedAffixTypeChainProbability=" + response.getNormalizedAffixTypeChainProbability() +
                ", aggregatedWeight=" + response.getAggregatedWeight() +
                ']'
        );
    }

}
