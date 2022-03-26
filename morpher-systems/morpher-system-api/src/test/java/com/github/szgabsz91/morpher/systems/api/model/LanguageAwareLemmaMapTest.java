package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.languagehandlers.api.model.LemmaMap;
import org.junit.jupiter.api.Test;

import java.util.Map;
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

}
