package com.github.szgabsz91.morpher.core.model;

import org.junit.jupiter.api.Test;

import java.util.Objects;

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

    @Test
    public void testEquals() {
        FrequencyAwareWordPair frequencyAwareWordPair1 = FrequencyAwareWordPair.of(WordPair.of("left1", "right1"), 2);
        FrequencyAwareWordPair frequencyAwareWordPair2 = FrequencyAwareWordPair.of(WordPair.of("left2", "right2"), 2);
        FrequencyAwareWordPair frequencyAwareWordPair3 = FrequencyAwareWordPair.of(WordPair.of("left1", "right1"), 3);
        FrequencyAwareWordPair frequencyAwareWordPair4 = FrequencyAwareWordPair.of(WordPair.of("left1", "right1"), 2);

        assertThat(frequencyAwareWordPair1.equals(frequencyAwareWordPair1)).isTrue();
        assertThat(frequencyAwareWordPair1.equals(null)).isFalse();
        assertThat(frequencyAwareWordPair1).isNotEqualTo("string");
        assertThat(frequencyAwareWordPair1).isNotEqualTo(frequencyAwareWordPair2);
        assertThat(frequencyAwareWordPair1).isNotEqualTo(frequencyAwareWordPair3);
        assertThat(frequencyAwareWordPair1).isEqualTo(frequencyAwareWordPair4);
    }

    @Test
    public void tetHashCode() {
        FrequencyAwareWordPair frequencyAwareWordPair = FrequencyAwareWordPair.of(WordPair.of("left", "right"), 2);
        int result = frequencyAwareWordPair.hashCode();
        assertThat(result).isEqualTo(Objects.hash(frequencyAwareWordPair.getWordPair(), frequencyAwareWordPair.getFrequency()));
    }

    @Test
    public void testToString() {
        FrequencyAwareWordPair frequencyAwareWordPair = FrequencyAwareWordPair.of(WordPair.of("left", "right"), 2);
        assertThat(frequencyAwareWordPair).hasToString("FrequencyAwareWordPair[" + frequencyAwareWordPair.getWordPair() + ", " + frequencyAwareWordPair.getFrequency() + ']');
    }

}
