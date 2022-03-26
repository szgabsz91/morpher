package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.engines.api.model.AnalysisInput;
import org.junit.jupiter.api.Test;

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

}
