package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.engines.api.model.InflectionOrderedInput;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class LanguageAwareInflectionOrderedInputTest {

    @Test
    public void testConstructorAndGetters() {
        Language language = Language.of("code");
        InflectionOrderedInput inflectionOrderedInput = new InflectionOrderedInput(Word.of("input"), List.of(AffixType.of("AFF")));
        LanguageAwareInflectionOrderedInput languageAwareInflectionOrderedInput = new LanguageAwareInflectionOrderedInput(language, inflectionOrderedInput);
        assertThat(languageAwareInflectionOrderedInput.getLanguage()).isEqualTo(language);
        assertThat(languageAwareInflectionOrderedInput.getContent()).isEqualTo(inflectionOrderedInput);
    }

    @Test
    public void testEquals() {
        LanguageAwareInflectionOrderedInput languageAwareInflectionOrderedInput1 = new LanguageAwareInflectionOrderedInput(Language.of("code1"), new InflectionOrderedInput(Word.of("input1"), List.of()));
        LanguageAwareInflectionOrderedInput languageAwareInflectionOrderedInput2 = new LanguageAwareInflectionOrderedInput(Language.of("code2"), new InflectionOrderedInput(Word.of("input1"), List.of()));
        LanguageAwareInflectionOrderedInput languageAwareInflectionOrderedInput3 = new LanguageAwareInflectionOrderedInput(Language.of("code1"), new InflectionOrderedInput(Word.of("input2"), List.of()));
        LanguageAwareInflectionOrderedInput languageAwareInflectionOrderedInput4 = new LanguageAwareInflectionOrderedInput(Language.of("code1"), new InflectionOrderedInput(Word.of("input1"), List.of()));

        assertThat(languageAwareInflectionOrderedInput1).isEqualTo(languageAwareInflectionOrderedInput1);
        assertThat(languageAwareInflectionOrderedInput1).isNotEqualTo(null);
        assertThat(languageAwareInflectionOrderedInput1).isNotEqualTo("string");
        assertThat(languageAwareInflectionOrderedInput1).isNotEqualTo(languageAwareInflectionOrderedInput2);
        assertThat(languageAwareInflectionOrderedInput1).isNotEqualTo(languageAwareInflectionOrderedInput3);
        assertThat(languageAwareInflectionOrderedInput1).isEqualTo(languageAwareInflectionOrderedInput4);
    }

    @Test
    public void testHashCode() {
        LanguageAwareInflectionOrderedInput languageAwareInflectionOrderedInput = new LanguageAwareInflectionOrderedInput(Language.of("code"), new InflectionOrderedInput(Word.of("input"), List.of()));
        int result = languageAwareInflectionOrderedInput.hashCode();
        int expected = Objects.hash(languageAwareInflectionOrderedInput.getLanguage(), languageAwareInflectionOrderedInput.getContent());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        LanguageAwareInflectionOrderedInput languageAwareInflectionOrderedInput = new LanguageAwareInflectionOrderedInput(Language.of("code"), new InflectionOrderedInput(Word.of("input"), List.of()));
        assertThat(languageAwareInflectionOrderedInput).hasToString("LanguageAware[language=" + languageAwareInflectionOrderedInput.getLanguage() + ", content=" + languageAwareInflectionOrderedInput.getContent() + "]");
    }

}
