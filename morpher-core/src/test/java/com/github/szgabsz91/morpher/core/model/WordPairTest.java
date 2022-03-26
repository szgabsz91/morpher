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

}
