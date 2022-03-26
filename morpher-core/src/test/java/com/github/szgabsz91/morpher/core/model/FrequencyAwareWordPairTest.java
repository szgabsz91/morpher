package com.github.szgabsz91.morpher.core.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FrequencyAwareWordPairTest {

    @Test
    public void testOfWithWordPairAndFrequency() {
        WordPair wordPair = WordPair.of("left", "right");
        int frequency = 2;
        FrequencyAwareWordPair frequencyAwareWordPair = FrequencyAwareWordPair.of(wordPair, frequency);
        assertThat(frequencyAwareWordPair.getWordPair()).isEqualTo(wordPair);
        assertThat(frequencyAwareWordPair.getLeftWord()).isEqualTo(wordPair.getLeftWord());
        assertThat(frequencyAwareWordPair.getRightWord()).isEqualTo(wordPair.getRightWord());
        assertThat(frequencyAwareWordPair.getFrequency()).isEqualTo(frequency);
    }

    @Test
    public void testOfWithWordPair() {
        WordPair wordPair = WordPair.of("left", "right");
        FrequencyAwareWordPair frequencyAwareWordPair = FrequencyAwareWordPair.of(wordPair);
        assertThat(frequencyAwareWordPair.getWordPair()).isEqualTo(wordPair);
        assertThat(frequencyAwareWordPair.getLeftWord()).isEqualTo(wordPair.getLeftWord());
        assertThat(frequencyAwareWordPair.getRightWord()).isEqualTo(wordPair.getRightWord());
        assertThat(frequencyAwareWordPair.getFrequency()).isOne();
    }

    @Test
    public void testOfWithWordsAndFrequency() {
        WordPair wordPair = WordPair.of("left", "right");
        int frequency = 2;
        FrequencyAwareWordPair frequencyAwareWordPair = FrequencyAwareWordPair.of(wordPair.getLeftWord(), wordPair.getRightWord(), frequency);
        assertThat(frequencyAwareWordPair.getWordPair()).isEqualTo(wordPair);
        assertThat(frequencyAwareWordPair.getLeftWord()).isEqualTo(wordPair.getLeftWord());
        assertThat(frequencyAwareWordPair.getRightWord()).isEqualTo(wordPair.getRightWord());
        assertThat(frequencyAwareWordPair.getFrequency()).isEqualTo(frequency);
    }

    @Test
    public void testOfWithWords() {
        WordPair wordPair = WordPair.of("left", "right");
        FrequencyAwareWordPair frequencyAwareWordPair = FrequencyAwareWordPair.of(wordPair.getLeftWord(), wordPair.getRightWord());
        assertThat(frequencyAwareWordPair.getWordPair()).isEqualTo(wordPair);
        assertThat(frequencyAwareWordPair.getLeftWord()).isEqualTo(wordPair.getLeftWord());
        assertThat(frequencyAwareWordPair.getRightWord()).isEqualTo(wordPair.getRightWord());
        assertThat(frequencyAwareWordPair.getFrequency()).isOne();
    }

    @Test
    public void testOfWithStringsAndFrequency() {
        WordPair wordPair = WordPair.of("left", "right");
        int frequency = 2;
        FrequencyAwareWordPair frequencyAwareWordPair = FrequencyAwareWordPair.of(wordPair.getLeftWord().toString(), wordPair.getRightWord().toString(), frequency);
        assertThat(frequencyAwareWordPair.getWordPair()).isEqualTo(wordPair);
        assertThat(frequencyAwareWordPair.getLeftWord()).isEqualTo(wordPair.getLeftWord());
        assertThat(frequencyAwareWordPair.getRightWord()).isEqualTo(wordPair.getRightWord());
        assertThat(frequencyAwareWordPair.getFrequency()).isEqualTo(frequency);
    }

    @Test
    public void testOfWithStrings() {
        WordPair wordPair = WordPair.of("left", "right");
        FrequencyAwareWordPair frequencyAwareWordPair = FrequencyAwareWordPair.of(wordPair.getLeftWord().toString(), wordPair.getRightWord().toString());
        assertThat(frequencyAwareWordPair.getWordPair()).isEqualTo(wordPair);
        assertThat(frequencyAwareWordPair.getLeftWord()).isEqualTo(wordPair.getLeftWord());
        assertThat(frequencyAwareWordPair.getRightWord()).isEqualTo(wordPair.getRightWord());
        assertThat(frequencyAwareWordPair.getFrequency()).isOne();
    }

    @Test
    public void testInverse() {
        WordPair wordPair = WordPair.of("left", "right");
        int frequency = 2;
        FrequencyAwareWordPair frequencyAwareWordPair = FrequencyAwareWordPair.of(wordPair, frequency);
        FrequencyAwareWordPair result = frequencyAwareWordPair.inverse();
        assertThat(result.getWordPair()).isEqualTo(wordPair.inverse());
        assertThat(result.getLeftWord()).isEqualTo(wordPair.getRightWord());
        assertThat(result.getRightWord()).isEqualTo(wordPair.getLeftWord());
        assertThat(result.getFrequency()).isEqualTo(frequency);
    }

}
