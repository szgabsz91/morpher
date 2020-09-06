package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.core.model.Corpus;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class LanguageAwareCorpusTest {

    @Test
    public void testConstructorAndGetters() {
        Language language = Language.of("code");
        Corpus corpus = Corpus.of(Word.of("word"));
        LanguageAwareCorpus languageAwareCorpus = new LanguageAwareCorpus(language, corpus);
        assertThat(languageAwareCorpus.getLanguage()).isEqualTo(language);
        assertThat(languageAwareCorpus.getContent()).isEqualTo(corpus);
    }

    @Test
    public void testEquals() {
        LanguageAwareCorpus languageAwareCorpus1 = new LanguageAwareCorpus(Language.of("code1"), Corpus.of(Word.of("word1")));
        LanguageAwareCorpus languageAwareCorpus2 = new LanguageAwareCorpus(Language.of("code2"), Corpus.of(Word.of("word1")));
        LanguageAwareCorpus languageAwareCorpus3 = new LanguageAwareCorpus(Language.of("code1"), Corpus.of(Word.of("word2")));
        LanguageAwareCorpus languageAwareCorpus4 = new LanguageAwareCorpus(Language.of("code1"), Corpus.of(Word.of("word1")));

        assertThat(languageAwareCorpus1.equals(languageAwareCorpus1)).isTrue();
        assertThat(languageAwareCorpus1.equals(null)).isFalse();
        assertThat(languageAwareCorpus1).isNotEqualTo("string");
        assertThat(languageAwareCorpus1).isNotEqualTo(languageAwareCorpus2);
        assertThat(languageAwareCorpus1).isNotEqualTo(languageAwareCorpus3);
        assertThat(languageAwareCorpus1).isEqualTo(languageAwareCorpus4);
    }

    @Test
    public void testHashCode() {
        LanguageAwareCorpus languageAwareCorpus = new LanguageAwareCorpus(Language.of("code"), Corpus.of(Word.of("word")));
        int result = languageAwareCorpus.hashCode();
        int expected = Objects.hash(languageAwareCorpus.getLanguage(), languageAwareCorpus.getContent());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        LanguageAwareCorpus languageAwareCorpus = new LanguageAwareCorpus(Language.of("code"), Corpus.of(Word.of("word")));
        assertThat(languageAwareCorpus).hasToString("LanguageAware[language=" + languageAwareCorpus.getLanguage() + ", content=" + languageAwareCorpus.getContent() + "]");
    }

}
