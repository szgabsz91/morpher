package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.engines.api.model.PreanalyzedTrainingItem;
import com.github.szgabsz91.morpher.engines.api.model.PreanalyzedTrainingItems;
import com.github.szgabsz91.morpher.languagehandlers.api.model.AnnotationTokenizerResult;
import org.junit.jupiter.api.Test;

import java.util.Objects;
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

    @Test
    public void testEquals() {
        LanguageAwarePreanalyzedTrainingItems languageAwarePreanalyzedTrainingItems1 = new LanguageAwarePreanalyzedTrainingItems(Language.of("code1"), PreanalyzedTrainingItems.of(Set.of(createPreanalyzedTrainingItem("expression1"))));
        LanguageAwarePreanalyzedTrainingItems languageAwarePreanalyzedTrainingItems2 = new LanguageAwarePreanalyzedTrainingItems(Language.of("code2"), PreanalyzedTrainingItems.of(Set.of(createPreanalyzedTrainingItem("expression1"))));
        LanguageAwarePreanalyzedTrainingItems languageAwarePreanalyzedTrainingItems3 = new LanguageAwarePreanalyzedTrainingItems(Language.of("code1"), PreanalyzedTrainingItems.of(Set.of(createPreanalyzedTrainingItem("expression1"), createPreanalyzedTrainingItem("expression2"))));
        LanguageAwarePreanalyzedTrainingItems languageAwarePreanalyzedTrainingItems4 = new LanguageAwarePreanalyzedTrainingItems(Language.of("code1"), PreanalyzedTrainingItems.of(Set.of(createPreanalyzedTrainingItem("expression1"))));

        assertThat(languageAwarePreanalyzedTrainingItems1).isEqualTo(languageAwarePreanalyzedTrainingItems1);
        assertThat(languageAwarePreanalyzedTrainingItems1).isNotEqualTo(null);
        assertThat(languageAwarePreanalyzedTrainingItems1).isNotEqualTo("string");
        assertThat(languageAwarePreanalyzedTrainingItems1).isNotEqualTo(languageAwarePreanalyzedTrainingItems2);
        assertThat(languageAwarePreanalyzedTrainingItems1).isNotEqualTo(languageAwarePreanalyzedTrainingItems3);
        assertThat(languageAwarePreanalyzedTrainingItems1).isEqualTo(languageAwarePreanalyzedTrainingItems4);
    }

    @Test
    public void testHashCode() {
        LanguageAwarePreanalyzedTrainingItems languageAwarePreanalyzedTrainingItems = new LanguageAwarePreanalyzedTrainingItems(Language.of("code"), PreanalyzedTrainingItems.of(Set.of(createPreanalyzedTrainingItem("expression"))));
        int result = languageAwarePreanalyzedTrainingItems.hashCode();
        int expected = Objects.hash(languageAwarePreanalyzedTrainingItems.getLanguage(), languageAwarePreanalyzedTrainingItems.getContent());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        LanguageAwarePreanalyzedTrainingItems languageAwarePreanalyzedTrainingItems = new LanguageAwarePreanalyzedTrainingItems(Language.of("code"), PreanalyzedTrainingItems.of(Set.of(createPreanalyzedTrainingItem("expression"))));
        assertThat(languageAwarePreanalyzedTrainingItems).hasToString("LanguageAware[language=" + languageAwarePreanalyzedTrainingItems.getLanguage() + ", content=" + languageAwarePreanalyzedTrainingItems.getContent() + "]");
    }

    private static PreanalyzedTrainingItem createPreanalyzedTrainingItem(String expression) {
        AnnotationTokenizerResult annotationTokenizerResult = new AnnotationTokenizerResult(expression, "grammaticalForm", "lemma", 1);
        annotationTokenizerResult.addAffixType(AffixType.of("AFF"));
        FrequencyAwareWordPair wordPair = FrequencyAwareWordPair.of("word1", "word2");
        return new PreanalyzedTrainingItem(annotationTokenizerResult, wordPair);
    }

}
