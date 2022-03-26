package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.engines.api.model.InflectionInput;
import org.junit.jupiter.api.Test;

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

}
