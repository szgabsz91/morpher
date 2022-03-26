package com.github.szgabsz91.morpher.core.model;

import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

public class WordTest {

    @Test
    public void testEmpty() {
        Word word = Word.empty();
        assertThat(word.toString()).isEmpty();
        assertThat(word.charAt(0)).isEqualTo('\0');
        assertThat(word.isEmpty()).isTrue();
        assertThat(word.length()).isEqualTo(0);
        assertThat(word.characters().map(String::valueOf).collect(joining())).isEqualTo("");
        assertThat(word.dropFirstLetter().toString()).isEmpty();
        assertThat(word.appendFirstLetterOf(Word.of("s")).toString()).isEqualTo("s");
        assertThat(word.appendFirstLetterOf(Word.empty()).toString()).isEmpty();
    }

    @Test
    public void testOf() {
        String string = "string";
        Word word = Word.of(string);
        assertThat(word).hasToString(string);
        assertThat(word.charAt(0)).isEqualTo('s');
        assertThat(word.isEmpty()).isFalse();
        assertThat(word.length()).isEqualTo(string.length());
        assertThat(word.characters().map(String::valueOf).collect(joining())).isEqualTo(string);
        assertThat(word.dropFirstLetter()).hasToString(string.substring(1));
        assertThat(word.appendFirstLetterOf(Word.of("s"))).hasToString("strings");
    }

    @Test
    public void testGetAffixRelativeToWithMatchingWords() {
        Word word1 = Word.of("abcde");
        Word word2 = Word.of("abc");
        String result = word1.getAffixRelativeTo(word2);
        assertThat(result).isEqualTo("de");
    }

    @Test
    public void testGetAffixRelativeToWithNonMatchingWords() {
        Word word1 = Word.of("abc");
        Word word2 = Word.of("def");
        String result = word1.getAffixRelativeTo(word2);
        assertThat(result).isEqualTo(word2.toString());
    }

    @Test
    public void testEndsWithWithTrueResult() {
        Word word1 = Word.of("abcxyz");
        Word word2 = Word.of("xyz");
        boolean result = word1.endsWith(word2);
        assertThat(result).isTrue();
    }

    @Test
    public void testEndsWithWithFalseResult() {
        Word word1 = Word.of("abcxyz");
        Word word2 = Word.of("abc");
        boolean result = word1.endsWith(word2);
        assertThat(result).isFalse();
    }

    @Test
    public void testReverse() {
        Word word = Word.of("word");
        Word reversed = word.reverse();
        assertThat(reversed).hasToString("drow");
    }

    @Test
    public void testReplace() {
        Word word = Word.of("abcbd");
        Word replaced = word.replace("b", "x");
        assertThat(replaced).hasToString("axcxd");
    }

    @Test
    public void testCompareToWithNegativeResult() {
        Word word1 = Word.of("a");
        Word word2 = Word.of("b");
        assertThat(word1.compareTo(word2)).isNegative();
    }

    @Test
    public void testCompareToWithZeroResult() {
        Word word1 = Word.of("a");
        Word word2 = Word.of("a");
        assertThat(word1.compareTo(word2)).isZero();
    }

    @Test
    public void testCompareToWithPositiveResult() {
        Word word1 = Word.of("b");
        Word word2 = Word.of("a");
        assertThat(word1.compareTo(word2)).isPositive();
    }

    @Test
    public void testToString() {
        String string = "string";
        Word word = Word.of(string);
        assertThat(word).hasToString(string);
    }

}
