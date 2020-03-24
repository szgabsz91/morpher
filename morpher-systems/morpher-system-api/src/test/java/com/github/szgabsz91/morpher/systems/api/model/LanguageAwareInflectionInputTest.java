package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.engines.api.model.InflectionInput;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class LanguageAwareInflectionInputTest {

    @Test
    public void testConstructorAndGetters() {
        Language language = Language.of("code");
        InflectionInput inflectionInput = new InflectionInput(Word.of("input"), Set.of(AffixType.of("AFF")));
        LanguageAwareInflectionInput languageAwareInflectionInput = new LanguageAwareInflectionInput(language, inflectionInput);
        assertThat(languageAwareInflectionInput.getLanguage()).isEqualTo(language);
        assertThat(languageAwareInflectionInput.getContent()).isEqualTo(inflectionInput);
    }

    @Test
    public void testEquals() {
        LanguageAwareInflectionInput languageAwareInflectionInput1 = new LanguageAwareInflectionInput(Language.of("code1"), new InflectionInput(Word.of("input1"), Set.of()));
        LanguageAwareInflectionInput languageAwareInflectionInput2 = new LanguageAwareInflectionInput(Language.of("code2"), new InflectionInput(Word.of("input1"), Set.of()));
        LanguageAwareInflectionInput languageAwareInflectionInput3 = new LanguageAwareInflectionInput(Language.of("code1"), new InflectionInput(Word.of("input2"), Set.of()));
        LanguageAwareInflectionInput languageAwareInflectionInput4 = new LanguageAwareInflectionInput(Language.of("code1"), new InflectionInput(Word.of("input1"), Set.of()));

        assertThat(languageAwareInflectionInput1.equals(languageAwareInflectionInput1)).isTrue();
        assertThat(languageAwareInflectionInput1).isNotEqualTo(null);
        assertThat(languageAwareInflectionInput1).isNotEqualTo("string");
        assertThat(languageAwareInflectionInput1).isNotEqualTo(languageAwareInflectionInput2);
        assertThat(languageAwareInflectionInput1).isNotEqualTo(languageAwareInflectionInput3);
        assertThat(languageAwareInflectionInput1).isEqualTo(languageAwareInflectionInput4);
    }

    @Test
    public void testHashCode() {
        LanguageAwareInflectionInput languageAwareInflectionInput = new LanguageAwareInflectionInput(Language.of("code"), new InflectionInput(Word.of("input"), Set.of()));
        int result = languageAwareInflectionInput.hashCode();
        int expected = Objects.hash(languageAwareInflectionInput.getLanguage(), languageAwareInflectionInput.getContent());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        LanguageAwareInflectionInput languageAwareInflectionInput = new LanguageAwareInflectionInput(Language.of("code"), new InflectionInput(Word.of("word"), Set.of()));
        assertThat(languageAwareInflectionInput).hasToString("LanguageAware[language=" + languageAwareInflectionInput.getLanguage() + ", content=" + languageAwareInflectionInput.getContent() + "]");
    }

}
