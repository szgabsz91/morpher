package com.github.szgabsz91.morpher.core.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;
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

    @Test
    public void testEquals() {
        Corpus corpus1 = Corpus.of(Collections.singleton(FrequencyAwareWord.of("word")));
        Corpus corpus2 = Corpus.of(Collections.singleton(FrequencyAwareWord.of("word2")));
        Corpus corpus3 = Corpus.of(Collections.singleton(FrequencyAwareWord.of("word")));

        assertThat(corpus1.equals(corpus1)).isTrue();
        assertThat(corpus1.equals(null)).isFalse();
        assertThat(corpus1).isNotEqualTo("string");
        assertThat(corpus1).isNotEqualTo(corpus2);
        assertThat(corpus1).isEqualTo(corpus3);
    }

    @Test
    public void testHashCode() {
        Set<FrequencyAwareWord> words = Collections.singleton(FrequencyAwareWord.of("word"));
        Corpus corpus = Corpus.of(words);
        int result = corpus.hashCode();
        assertThat(result).isEqualTo(Objects.hash(words));
    }

    @Test
    public void testToString() {
        Set<FrequencyAwareWord> words = Collections.singleton(FrequencyAwareWord.of("word"));
        Corpus corpus = Corpus.of(words);
        assertThat(corpus).hasToString("Corpus[words=" + words + "]");
    }

}
