package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.engines.api.model.AnalysisInput;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class LanguageAwareAnalysisInputTest {

    @Test
    public void testConstructorAndGetters() {
        Language language = Language.of("code");
        AnalysisInput analysisInput = AnalysisInput.of(Word.of("input"));
        LanguageAwareAnalysisInput languageAwareAnalysisInput = new LanguageAwareAnalysisInput(language, analysisInput);
        assertThat(languageAwareAnalysisInput.getLanguage()).isEqualTo(language);
        assertThat(languageAwareAnalysisInput.getContent()).isEqualTo(analysisInput);
    }

    @Test
    public void testEquals() {
        LanguageAwareAnalysisInput languageAwareAnalysisInput1 = new LanguageAwareAnalysisInput(Language.of("code1"), AnalysisInput.of(Word.of("input1")));
        LanguageAwareAnalysisInput languageAwareAnalysisInput2 = new LanguageAwareAnalysisInput(Language.of("code2"), AnalysisInput.of(Word.of("input1")));
        LanguageAwareAnalysisInput languageAwareAnalysisInput3 = new LanguageAwareAnalysisInput(Language.of("code1"), AnalysisInput.of(Word.of("input2")));
        LanguageAwareAnalysisInput languageAwareAnalysisInput4 = new LanguageAwareAnalysisInput(Language.of("code1"), AnalysisInput.of(Word.of("input1")));

        assertThat(languageAwareAnalysisInput1.equals(languageAwareAnalysisInput1)).isTrue();
        assertThat(languageAwareAnalysisInput1).isNotEqualTo(null);
        assertThat(languageAwareAnalysisInput1).isNotEqualTo("string");
        assertThat(languageAwareAnalysisInput1).isNotEqualTo(languageAwareAnalysisInput2);
        assertThat(languageAwareAnalysisInput1).isNotEqualTo(languageAwareAnalysisInput3);
        assertThat(languageAwareAnalysisInput1).isEqualTo(languageAwareAnalysisInput4);
    }

    @Test
    public void testHashCode() {
        LanguageAwareAnalysisInput languageAwareAnalysisInput = new LanguageAwareAnalysisInput(Language.of("code"), AnalysisInput.of(Word.of("input")));
        int result = languageAwareAnalysisInput.hashCode();
        int expected = Objects.hash(languageAwareAnalysisInput.getLanguage(), languageAwareAnalysisInput.getContent());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        LanguageAwareAnalysisInput languageAwareAnalysisInput = new LanguageAwareAnalysisInput(Language.of("code"), AnalysisInput.of(Word.of("input")));
        assertThat(languageAwareAnalysisInput).hasToString("LanguageAware[language=" + languageAwareAnalysisInput.getLanguage() + ", content=" + languageAwareAnalysisInput.getContent() + "]");
    }

}
