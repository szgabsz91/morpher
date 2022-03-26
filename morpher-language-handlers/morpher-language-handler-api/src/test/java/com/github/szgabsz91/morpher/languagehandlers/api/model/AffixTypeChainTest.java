package com.github.szgabsz91.morpher.languagehandlers.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AffixTypeChainTest {

    @Test
    public void testOfAndGetters() {
        List<ProbabilisticAffixType> affixTypes = List.of(
                ProbabilisticAffixType.of(AffixType.of("AFF1"), 0.5),
                ProbabilisticAffixType.of(AffixType.of("AFF2"), 0.1)
        );
        double probability = 0.5;
        AffixTypeChain affixTypeChain = AffixTypeChain.of(affixTypes, probability);
        assertThat(affixTypeChain.getAffixTypes()).isEqualTo(affixTypes);
        assertThat(affixTypeChain.getProbability()).isEqualTo(probability);
    }

    @Test
    public void testCompareToWithNegativeResult() {
        AffixTypeChain affixTypeChain1 = AffixTypeChain.of(Collections.emptyList(), 0.5);
        AffixTypeChain affixTypeChain2 = AffixTypeChain.of(Collections.emptyList(), 0.4);
        int result = affixTypeChain1.compareTo(affixTypeChain2);
        assertThat(result).isNegative();
    }

    @Test
    public void testCompareToWithZeroResult() {
        AffixTypeChain affixTypeChain1 = AffixTypeChain.of(Collections.emptyList(), 0.5);
        AffixTypeChain affixTypeChain2 = AffixTypeChain.of(Collections.emptyList(), 0.5);
        int result = affixTypeChain1.compareTo(affixTypeChain2);
        assertThat(result).isZero();
    }

    @Test
    public void testCompareToWithPositiveResult() {
        AffixTypeChain affixTypeChain1 = AffixTypeChain.of(Collections.emptyList(), 0.4);
        AffixTypeChain affixTypeChain2 = AffixTypeChain.of(Collections.emptyList(), 0.5);
        int result = affixTypeChain1.compareTo(affixTypeChain2);
        assertThat(result).isPositive();
    }

}
