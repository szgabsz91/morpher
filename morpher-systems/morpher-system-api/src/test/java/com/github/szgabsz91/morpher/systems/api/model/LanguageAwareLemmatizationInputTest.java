package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.engines.api.model.LemmatizationInput;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class LanguageAwareLemmatizationInputTest {

    @Test
    public void testConstructorAndGetters() {
        Language language = Language.of("code");
        LemmatizationInput lemmatizationInput = LemmatizationInput.of(Word.of("input"));
        LanguageAwareLemmatizationInput languageAwareLemmatizationInput = new LanguageAwareLemmatizationInput(language, lemmatizationInput);
        assertThat(languageAwareLemmatizationInput.getLanguage()).isEqualTo(language);
        assertThat(languageAwareLemmatizationInput.getContent()).isEqualTo(lemmatizationInput);
    }

    @Test
    public void testEquals() {
        LanguageAwareLemmatizationInput languageAwareLemmatizationInput1 = new LanguageAwareLemmatizationInput(Language.of("code1"), LemmatizationInput.of(Word.of("input1")));
        LanguageAwareLemmatizationInput languageAwareLemmatizationInput2 = new LanguageAwareLemmatizationInput(Language.of("code2"), LemmatizationInput.of(Word.of("input1")));
        LanguageAwareLemmatizationInput languageAwareLemmatizationInput3 = new LanguageAwareLemmatizationInput(Language.of("code1"), LemmatizationInput.of(Word.of("input2")));
        LanguageAwareLemmatizationInput languageAwareLemmatizationInput4 = new LanguageAwareLemmatizationInput(Language.of("code1"), LemmatizationInput.of(Word.of("input1")));

        assertThat(languageAwareLemmatizationInput1).isEqualTo(languageAwareLemmatizationInput1);
        assertThat(languageAwareLemmatizationInput1).isNotEqualTo(null);
        assertThat(languageAwareLemmatizationInput1).isNotEqualTo("string");
        assertThat(languageAwareLemmatizationInput1).isNotEqualTo(languageAwareLemmatizationInput2);
        assertThat(languageAwareLemmatizationInput1).isNotEqualTo(languageAwareLemmatizationInput3);
        assertThat(languageAwareLemmatizationInput1).isEqualTo(languageAwareLemmatizationInput4);
    }

    @Test
    public void testHashCode() {
        LanguageAwareLemmatizationInput languageAwareLemmatizationInput = new LanguageAwareLemmatizationInput(Language.of("code"), LemmatizationInput.of(Word.of("input")));
        int result = languageAwareLemmatizationInput.hashCode();
        int expected = Objects.hash(languageAwareLemmatizationInput.getLanguage(), languageAwareLemmatizationInput.getContent());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        LanguageAwareLemmatizationInput languageAwareLemmatizationInput = new LanguageAwareLemmatizationInput(Language.of("code"), LemmatizationInput.of(Word.of("input")));
        assertThat(languageAwareLemmatizationInput).hasToString("LanguageAware[language=" + languageAwareLemmatizationInput.getLanguage() + ", content=" + languageAwareLemmatizationInput.getContent() + "]");
    }

}
