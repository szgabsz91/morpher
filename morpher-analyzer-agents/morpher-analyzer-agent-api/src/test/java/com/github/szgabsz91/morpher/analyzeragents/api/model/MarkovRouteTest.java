package com.github.szgabsz91.morpher.analyzeragents.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkovRouteTest {

    @Test
    public void testOfAndGetters() {
        List<ProbabilisticAffixType> affixTypes = List.of(
                ProbabilisticAffixType.of(AffixType.of("AFF1"), 0.5),
                ProbabilisticAffixType.of(AffixType.of("AFF2"), 0.1)
        );
        double probability = 0.5;
        MarkovRoute markovRoute = MarkovRoute.of(affixTypes, probability);
        assertThat(markovRoute.getAffixTypes()).isEqualTo(affixTypes);
        assertThat(markovRoute.getProbability()).isEqualTo(probability);
    }

    @Test
    public void testCompareToWithNegativeResult() {
        MarkovRoute markovRoute1 = MarkovRoute.of(Collections.emptyList(), 0.5);
        MarkovRoute markovRoute2 = MarkovRoute.of(Collections.emptyList(), 0.4);
        int result = markovRoute1.compareTo(markovRoute2);
        assertThat(result).isNegative();
    }

    @Test
    public void testCompareToWithZeroResult() {
        MarkovRoute markovRoute1 = MarkovRoute.of(Collections.emptyList(), 0.5);
        MarkovRoute markovRoute2 = MarkovRoute.of(Collections.emptyList(), 0.5);
        int result = markovRoute1.compareTo(markovRoute2);
        assertThat(result).isZero();
    }

    @Test
    public void testCompareToWithPositiveResult() {
        MarkovRoute markovRoute1 = MarkovRoute.of(Collections.emptyList(), 0.4);
        MarkovRoute markovRoute2 = MarkovRoute.of(Collections.emptyList(), 0.5);
        int result = markovRoute1.compareTo(markovRoute2);
        assertThat(result).isPositive();
    }

    @Test
    public void testEquals() {
        MarkovRoute markovRoute1 = MarkovRoute.of(List.of(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5)), 0.5);
        MarkovRoute markovRoute2 = MarkovRoute.of(List.of(ProbabilisticAffixType.of(AffixType.of("AFF2"), 0.5)), 0.5);
        MarkovRoute markovRoute3 = MarkovRoute.of(List.of(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5)), 0.4);
        MarkovRoute markovRoute4 = MarkovRoute.of(List.of(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5)), 0.5);

        assertThat(markovRoute1).isEqualTo(markovRoute1);
        assertThat(markovRoute1).isNotEqualTo(null);
        assertThat(markovRoute1).isNotEqualTo("string");
        assertThat(markovRoute1).isNotEqualTo(markovRoute2);
        assertThat(markovRoute1).isNotEqualTo(markovRoute3);
        assertThat(markovRoute1).isEqualTo(markovRoute4);
    }

    @Test
    public void testHashCode() {
        MarkovRoute markovRoute = MarkovRoute.of(List.of(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5)), 0.5);
        int result = markovRoute.hashCode();
        assertThat(result).isEqualTo(Objects.hash(markovRoute.getAffixTypes(), markovRoute.getProbability()));
    }

    @Test
    public void testToString() {
        MarkovRoute markovRoute = MarkovRoute.of(List.of(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5)), 0.1);
        assertThat(markovRoute).hasToString("MarkovRoute[affixTypes=" + markovRoute.getAffixTypes() + ", probability=" + markovRoute.getProbability() + ']');
    }

}
