package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.analyzeragents.api.model.LemmaMap;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class LanguageAwareLemmaMapTest {

    @Test
    public void testConstructorAndGetters() {
        Language language = Language.of("code");
        LemmaMap lemmaMap = LemmaMap.of(Map.of(Word.of("lemma"), Set.of()));
        LanguageAwareLemmaMap languageAwareLemmaMap = new LanguageAwareLemmaMap(language, lemmaMap);
        assertThat(languageAwareLemmaMap.getLanguage()).isEqualTo(language);
        assertThat(languageAwareLemmaMap.getContent()).isEqualTo(lemmaMap);
    }

    @Test
    public void testEquals() {
        LanguageAwareLemmaMap languageAwareLemmaMap1 = new LanguageAwareLemmaMap(Language.of("code1"), LemmaMap.of(Map.of(Word.of("lemma"), Set.of())));
        LanguageAwareLemmaMap languageAwareLemmaMap2 = new LanguageAwareLemmaMap(Language.of("code2"), LemmaMap.of(Map.of(Word.of("lemma"), Set.of())));
        LanguageAwareLemmaMap languageAwareLemmaMap3 = new LanguageAwareLemmaMap(Language.of("code1"), LemmaMap.of(Map.of(Word.of("lemma2"), Set.of())));
        LanguageAwareLemmaMap languageAwareLemmaMap4 = new LanguageAwareLemmaMap(Language.of("code1"), LemmaMap.of(Map.of(Word.of("lemma"), Set.of())));

        assertThat(languageAwareLemmaMap1).isEqualTo(languageAwareLemmaMap1);
        assertThat(languageAwareLemmaMap1).isNotEqualTo(null);
        assertThat(languageAwareLemmaMap1).isNotEqualTo("string");
        assertThat(languageAwareLemmaMap1).isNotEqualTo(languageAwareLemmaMap2);
        assertThat(languageAwareLemmaMap1).isNotEqualTo(languageAwareLemmaMap3);
        assertThat(languageAwareLemmaMap1).isEqualTo(languageAwareLemmaMap4);
    }

    @Test
    public void testHashCode() {
        LanguageAwareLemmaMap languageAwareLemmaMap = new LanguageAwareLemmaMap(Language.of("code"), LemmaMap.of(Map.of(Word.of("lemma"), Set.of())));
        int result = languageAwareLemmaMap.hashCode();
        int expected = Objects.hash(languageAwareLemmaMap.getLanguage(), languageAwareLemmaMap.getContent());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        LanguageAwareLemmaMap languageAwareLemmaMap = new LanguageAwareLemmaMap(Language.of("code"), LemmaMap.of(Map.of(Word.of("lemma"), Set.of())));
        assertThat(languageAwareLemmaMap).hasToString("LanguageAware[language=" + languageAwareLemmaMap.getLanguage() + ", content=" + languageAwareLemmaMap.getContent() + "]");
    }

}
