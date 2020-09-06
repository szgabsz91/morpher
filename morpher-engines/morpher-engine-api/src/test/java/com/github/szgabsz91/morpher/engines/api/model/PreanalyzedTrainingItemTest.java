package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.languagehandlers.api.model.AnnotationTokenizerResult;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class PreanalyzedTrainingItemTest {

    @Test
    public void testConstructorAndGetters() {
        AnnotationTokenizerResult annotationTokenizerResult = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1);
        FrequencyAwareWordPair wordPair = FrequencyAwareWordPair.of("word1", "word2");
        PreanalyzedTrainingItem preanalyzedTrainingItem = new PreanalyzedTrainingItem(annotationTokenizerResult, wordPair);
        assertThat(preanalyzedTrainingItem.getAnnotationTokenizerResult()).isEqualTo(annotationTokenizerResult);
        assertThat(preanalyzedTrainingItem.getWordPair()).isEqualTo(wordPair);
    }

    @Test
    public void testEquals() {
        PreanalyzedTrainingItem preanalyzedTrainingItem1 = new PreanalyzedTrainingItem(new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1), FrequencyAwareWordPair.of("word", "word"));
        PreanalyzedTrainingItem preanalyzedTrainingItem2 = new PreanalyzedTrainingItem(new AnnotationTokenizerResult("expression2", "grammatical", "lemma", 1), FrequencyAwareWordPair.of("word", "word"));
        PreanalyzedTrainingItem preanalyzedTrainingItem3 = new PreanalyzedTrainingItem(new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1), FrequencyAwareWordPair.of("word2", "word"));
        PreanalyzedTrainingItem preanalyzedTrainingItem4 = new PreanalyzedTrainingItem(new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1), FrequencyAwareWordPair.of("word", "word"));

        assertThat(preanalyzedTrainingItem1.equals(preanalyzedTrainingItem1)).isTrue();
        assertThat(preanalyzedTrainingItem1.equals(null)).isFalse();
        assertThat(preanalyzedTrainingItem1).isNotEqualTo("string");
        assertThat(preanalyzedTrainingItem1).isNotEqualTo(preanalyzedTrainingItem2);
        assertThat(preanalyzedTrainingItem1).isNotEqualTo(preanalyzedTrainingItem3);
        assertThat(preanalyzedTrainingItem1).isEqualTo(preanalyzedTrainingItem4);
    }

    @Test
    public void testHashCode() {
        PreanalyzedTrainingItem preanalyzedTrainingItem = new PreanalyzedTrainingItem(new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1), FrequencyAwareWordPair.of("word", "word"));
        int result = preanalyzedTrainingItem.hashCode();
        assertThat(result).isEqualTo(Objects.hash(preanalyzedTrainingItem.getAnnotationTokenizerResult(), preanalyzedTrainingItem.getWordPair()));
    }

    @Test
    public void testToString() {
        PreanalyzedTrainingItem preanalyzedTrainingItem = new PreanalyzedTrainingItem(new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1), FrequencyAwareWordPair.of("word", "word"));
        assertThat(preanalyzedTrainingItem).hasToString("PreanalyzedTrainingItem[annotationTokenizerResult=" + preanalyzedTrainingItem.getAnnotationTokenizerResult() + ", wordPair=" + preanalyzedTrainingItem.getWordPair() + ']');
    }

}
