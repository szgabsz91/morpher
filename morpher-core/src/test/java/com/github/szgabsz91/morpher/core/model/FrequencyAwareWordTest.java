package com.github.szgabsz91.morpher.core.model;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class FrequencyAwareWordTest {

    @Test
    public void testOfWithWordAndFrequency() {
        Word word = Word.of("word");
        int frequency = 2;
        FrequencyAwareWord frequencyAwareWord = FrequencyAwareWord.of(word, frequency);
        assertThat(frequencyAwareWord.getWord()).isEqualTo(word);
        assertThat(frequencyAwareWord.getFrequency()).isEqualTo(frequency);
    }

    @Test
    public void testOfWithWord() {
        Word word = Word.of("word");
        FrequencyAwareWord frequencyAwareWord = FrequencyAwareWord.of(word);
        assertThat(frequencyAwareWord.getWord()).isEqualTo(word);
        assertThat(frequencyAwareWord.getFrequency()).isOne();
    }

    @Test
    public void testOfWithStringAndFrequency() {
        Word word = Word.of("word");
        int frequency = 2;
        FrequencyAwareWord frequencyAwareWord = FrequencyAwareWord.of(word.toString(), frequency);
        assertThat(frequencyAwareWord.getWord()).isEqualTo(word);
        assertThat(frequencyAwareWord.getFrequency()).isEqualTo(frequency);
    }

    @Test
    public void testOfWithString() {
        Word word = Word.of("word");
        FrequencyAwareWord frequencyAwareWord = FrequencyAwareWord.of(word.toString());
        assertThat(frequencyAwareWord.getWord()).isEqualTo(word);
        assertThat(frequencyAwareWord.getFrequency()).isOne();
    }

    @Test
    public void testEquals() {
        FrequencyAwareWord frequencyAwareWord1 = FrequencyAwareWord.of(Word.of("word1"), 2);
        FrequencyAwareWord frequencyAwareWord2 = FrequencyAwareWord.of(Word.of("word2"), 2);
        FrequencyAwareWord frequencyAwareWord3 = FrequencyAwareWord.of(Word.of("word1"), 3);
        FrequencyAwareWord frequencyAwareWord4 = FrequencyAwareWord.of(Word.of("word1"), 2);

        assertThat(frequencyAwareWord1).isEqualTo(frequencyAwareWord1);
        assertThat(frequencyAwareWord1).isNotEqualTo(null);
        assertThat(frequencyAwareWord1).isNotEqualTo("string");
        assertThat(frequencyAwareWord1).isNotEqualTo(frequencyAwareWord2);
        assertThat(frequencyAwareWord1).isNotEqualTo(frequencyAwareWord3);
        assertThat(frequencyAwareWord1).isEqualTo(frequencyAwareWord4);
    }

    @Test
    public void tetHashCode() {
        FrequencyAwareWord frequencyAwareWord = FrequencyAwareWord.of(Word.of("word"), 2);
        int result = frequencyAwareWord.hashCode();
        assertThat(result).isEqualTo(Objects.hash(frequencyAwareWord.getWord(), frequencyAwareWord.getFrequency()));
    }

    @Test
    public void testToString() {
        FrequencyAwareWord frequencyAwareWord = FrequencyAwareWord.of(Word.of("word"), 2);
        assertThat(frequencyAwareWord).hasToString("FrequencyAwareWord[" + frequencyAwareWord.getWord() + ", " + frequencyAwareWord.getFrequency() + ']');
    }

}
