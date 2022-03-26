package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.core.model.Corpus;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

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

}
