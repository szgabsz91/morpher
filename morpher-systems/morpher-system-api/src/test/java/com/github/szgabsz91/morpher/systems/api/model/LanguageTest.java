package com.github.szgabsz91.morpher.systems.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LanguageTest {

    @Test
    public void testOfAndGetters() {
        String languageCode = "code";
        Language language = Language.of(languageCode);
        assertThat(language.getLanguageCode()).isEqualTo(languageCode);
    }

}
