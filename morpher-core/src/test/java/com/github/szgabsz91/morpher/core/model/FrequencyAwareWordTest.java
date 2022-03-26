package com.github.szgabsz91.morpher.core.model;

import org.junit.jupiter.api.Test;

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

}
