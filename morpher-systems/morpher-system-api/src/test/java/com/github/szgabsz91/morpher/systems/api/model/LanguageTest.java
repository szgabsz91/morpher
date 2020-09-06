package com.github.szgabsz91.morpher.systems.api.model;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class LanguageTest {

    @Test
    public void testOfAndGetters() {
        String languageCode = "code";
        Language language = Language.of(languageCode);
        assertThat(language.getLanguageCode()).isEqualTo(languageCode);
    }

    @Test
    public void testEquals() {
        Language language1 = Language.of("code1");
        Language language2 = Language.of("code2");
        Language language3 = Language.of("code1");

        assertThat(language1.equals(language1)).isTrue();
        assertThat(language1.equals(null)).isFalse();
        assertThat(language1).isNotEqualTo("string");
        assertThat(language1).isNotEqualTo(language2);
        assertThat(language1).isEqualTo(language3);
    }

    @Test
    public void testHashCode() {
        Language language = Language.of("code");
        int result = language.hashCode();
        int expected = Objects.hash(language.getLanguageCode());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        String languageCode = "code";
        Language language = Language.of(languageCode);
        assertThat(language).hasToString(languageCode);
    }

}
