package com.github.szgabsz91.morpher.languagehandlers.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProbabilisticAffixTypeTest {

    @Test
    public void testOfAndGetters() {
        AffixType affixType = AffixType.of("AFF");
        double probability = 0.5;
        ProbabilisticAffixType probabilisticAffixType = ProbabilisticAffixType.of(affixType, probability);
        assertThat(probabilisticAffixType.getAffixType()).isEqualTo(affixType);
        assertThat(probabilisticAffixType.getProbability()).isEqualTo(probability);
    }

    @Test
    public void testCompareToWithNegativeResult() {
        ProbabilisticAffixType probabilisticAffixType1 = ProbabilisticAffixType.of(null, 0.75);
        ProbabilisticAffixType probabilisticAffixType2 = ProbabilisticAffixType.of(null, 0.50);
        int result = probabilisticAffixType1.compareTo(probabilisticAffixType2);
        assertThat(result).isNegative();
    }

    @Test
    public void testCompareToWithZeroResult() {
        ProbabilisticAffixType probabilisticAffixType1 = ProbabilisticAffixType.of(null, 0.5);
        ProbabilisticAffixType probabilisticAffixType2 = ProbabilisticAffixType.of(null, 0.5);
        int result = probabilisticAffixType1.compareTo(probabilisticAffixType2);
        assertThat(result).isZero();
    }

    @Test
    public void testCompareToWithPositiveResult() {
        ProbabilisticAffixType probabilisticAffixType1 = ProbabilisticAffixType.of(null, 0.5);
        ProbabilisticAffixType probabilisticAffixType2 = ProbabilisticAffixType.of(null, 0.75);
        int result = probabilisticAffixType1.compareTo(probabilisticAffixType2);
        assertThat(result).isPositive();
    }

}
