package com.github.szgabsz91.morpher.core.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WordPairTest {

    @Test
    public void testOfWithWords() {
        Word leftWord = Word.of("left");
        Word rightWord = Word.of("right");
        WordPair wordPair = WordPair.of(leftWord, rightWord);
        assertThat(wordPair.getLeftWord()).isEqualTo(leftWord);
        assertThat(wordPair.getRightWord()).isEqualTo(rightWord);
    }

    @Test
    public void testOfWithStrings() {
        String leftWord = "left";
        String rightWord = "right";
        WordPair wordPair = WordPair.of(leftWord, rightWord);
        assertThat(wordPair.getLeftWord()).hasToString(leftWord);
        assertThat(wordPair.getRightWord()).hasToString(rightWord);
    }

    @Test
    public void testInverse() {
        Word originalLeftWord = Word.of("a");
        Word originalRightWord = Word.of("b");
        WordPair originalWordPair = WordPair.of(originalLeftWord, originalRightWord);
        WordPair result = originalWordPair.inverse();
        assertThat(result.getLeftWord()).isEqualTo(originalRightWord);
        assertThat(result.getRightWord()).isEqualTo(originalLeftWord);
    }

    @Test
    public void testEquals() {
        WordPair wordPair1 = WordPair.of("left", "right");
        WordPair wordPair2 = WordPair.of("left2", "right");
        WordPair wordPair3 = WordPair.of("left", "right2");
        WordPair wordPair4 = WordPair.of("left", "right");

        assertThat(wordPair1.equals(wordPair1)).isTrue();
        assertThat(wordPair1.equals(null)).isFalse();
        assertThat(wordPair1).isNotEqualTo("string");
        assertThat(wordPair1).isNotEqualTo(wordPair2);
        assertThat(wordPair1).isNotEqualTo(wordPair3);
        assertThat(wordPair1).isEqualTo(wordPair4);
    }

    @Test
    public void testHashCode() {
        Word leftWord = Word.of("left");
        Word rightWord = Word.of("right");
        WordPair wordPair = WordPair.of(leftWord, rightWord);

        int expected = 31 * leftWord.hashCode() + rightWord.hashCode();

        assertThat(wordPair.hashCode()).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        WordPair wordPair = WordPair.of("left", "right");
        assertThat(wordPair).hasToString("[left, right]");
    }

}
