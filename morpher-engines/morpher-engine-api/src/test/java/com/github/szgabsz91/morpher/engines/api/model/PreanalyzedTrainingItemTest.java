package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.languagehandlers.api.model.AnnotationTokenizerResult;
import org.junit.jupiter.api.Test;

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

}
