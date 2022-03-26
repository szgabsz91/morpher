package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.engines.api.model.PreanalyzedTrainingItem;
import com.github.szgabsz91.morpher.engines.api.model.PreanalyzedTrainingItems;
import com.github.szgabsz91.morpher.languagehandlers.api.model.AnnotationTokenizerResult;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class LanguageAwarePreanalyzedTrainingItemsTest {

    @Test
    public void testConstructorAndGetters() {
        Language language = Language.of("code");
        PreanalyzedTrainingItems preanalyzedTrainingItems = PreanalyzedTrainingItems.of(Set.of(createPreanalyzedTrainingItem("expression")));
        LanguageAwarePreanalyzedTrainingItems languageAwarePreanalyzedTrainingItems = new LanguageAwarePreanalyzedTrainingItems(language, preanalyzedTrainingItems);
        assertThat(languageAwarePreanalyzedTrainingItems.getLanguage()).isEqualTo(language);
        assertThat(languageAwarePreanalyzedTrainingItems.getContent()).isEqualTo(preanalyzedTrainingItems);
    }

    private static PreanalyzedTrainingItem createPreanalyzedTrainingItem(String expression) {
        AnnotationTokenizerResult annotationTokenizerResult = new AnnotationTokenizerResult(expression, "grammaticalForm", "lemma", 1);
        annotationTokenizerResult.addAffixType(AffixType.of("AFF"));
        FrequencyAwareWordPair wordPair = FrequencyAwareWordPair.of("word1", "word2");
        return new PreanalyzedTrainingItem(annotationTokenizerResult, wordPair);
    }

}
