package com.github.szgabsz91.morpher.core.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CorpusTest {

    @Test
    public void testOfAndGettersWithFrequencyAwareWords() {
        Set<FrequencyAwareWord> words = Collections.singleton(FrequencyAwareWord.of("word"));
        Corpus corpus = Corpus.of(words);
        assertThat(corpus.getWords()).isEqualTo(words);
    }

    @Test
    public void testOfAndGettersWithWord() {
        Word word = Word.of("word");
        Corpus corpus = Corpus.of(word);
        assertThat(corpus.getWords()).containsExactly(FrequencyAwareWord.of(word));
    }

}
