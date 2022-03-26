package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.engines.api.model.InflectionOrderedInput;
import org.junit.jupiter.api.Test;

import java.util.List;

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

}
