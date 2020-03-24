package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.languagehandlers.api.model.AnnotationTokenizerResult;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class PreanalyzedTrainingItemsTest {

    @Test
    public void testStream() {
        Set<PreanalyzedTrainingItem> set = Set.of(
                new PreanalyzedTrainingItem(
                        createAnnotationTokenizerResult("expression1", "grammatical1", "lemma1", AffixType.of("AFF1"), AffixType.of("AFF2")),
                        FrequencyAwareWordPair.of("a", "b")
                ),
                new PreanalyzedTrainingItem(
                        createAnnotationTokenizerResult("expression2", "grammatical2", "lemma2", AffixType.of("AFF3"), AffixType.of("AFF4")),
                        FrequencyAwareWordPair.of("a", "b")
                )
        );
        PreanalyzedTrainingItems preanalyzedTrainingItems = PreanalyzedTrainingItems.of(set);
        Set<PreanalyzedTrainingItem> result = preanalyzedTrainingItems
                .stream()
                .collect(toSet());
        assertThat(result).isEqualTo(set);
    }

    @Test
    public void testEquals() {
        PreanalyzedTrainingItems preanalyzedTrainingItems1 = PreanalyzedTrainingItems.of(Set.of(createPreanalyzedTrainingItem("expression1")));
        PreanalyzedTrainingItems preanalyzedTrainingItems2 = PreanalyzedTrainingItems.of(Set.of(createPreanalyzedTrainingItem("expression2")));
        PreanalyzedTrainingItems preanalyzedTrainingItems3 = PreanalyzedTrainingItems.of(Set.of(createPreanalyzedTrainingItem("expression1")));

        assertThat(preanalyzedTrainingItems1.equals(preanalyzedTrainingItems1)).isTrue();
        assertThat(preanalyzedTrainingItems1).isNotEqualTo(null);
        assertThat(preanalyzedTrainingItems1).isNotEqualTo("string");
        assertThat(preanalyzedTrainingItems1).isNotEqualTo(preanalyzedTrainingItems2);
        assertThat(preanalyzedTrainingItems1).isEqualTo(preanalyzedTrainingItems3);
    }

    @Test
    public void testHashCode() {
        PreanalyzedTrainingItems preanalyzedTrainingItems = PreanalyzedTrainingItems.of(Set.of(createPreanalyzedTrainingItem("expression")));
        int result = preanalyzedTrainingItems.hashCode();
        int expected = Objects.hash(preanalyzedTrainingItems.stream().collect(toSet()));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        PreanalyzedTrainingItems preanalyzedTrainingItems = PreanalyzedTrainingItems.of(Set.of(createPreanalyzedTrainingItem("expression")));
        assertThat(preanalyzedTrainingItems).hasToString("PreanalyzedTrainingItems[" + preanalyzedTrainingItems.stream().collect(toSet()) + "]");
    }

    private static PreanalyzedTrainingItem createPreanalyzedTrainingItem(String expression) {
        AnnotationTokenizerResult annotationTokenizerResult = createAnnotationTokenizerResult(expression, "grammaticalForm", "lemma");
        annotationTokenizerResult.addAffixType(AffixType.of("AFF"));
        FrequencyAwareWordPair wordPair = FrequencyAwareWordPair.of("word1", "word2");
        return new PreanalyzedTrainingItem(annotationTokenizerResult, wordPair);
    }

    private static AnnotationTokenizerResult createAnnotationTokenizerResult(String expression, String grammaticalForm, String lemma, AffixType... affixTypes) {
        AnnotationTokenizerResult annotationTokenizerResult = new AnnotationTokenizerResult(expression, grammaticalForm, lemma, 1);
        for (AffixType affixType : affixTypes) {
            annotationTokenizerResult.addAffixType(affixType);
        }
        return annotationTokenizerResult;
    }

}
