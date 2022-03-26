package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.languagehandlers.api.model.AnnotationTokenizerResult;
import org.junit.jupiter.api.Test;

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

    private static AnnotationTokenizerResult createAnnotationTokenizerResult(String expression, String grammaticalForm, String lemma, AffixType... affixTypes) {
        AnnotationTokenizerResult annotationTokenizerResult = new AnnotationTokenizerResult(expression, grammaticalForm, lemma, 1);
        for (AffixType affixType : affixTypes) {
            annotationTokenizerResult.addAffixType(affixType);
        }
        return annotationTokenizerResult;
    }

}
