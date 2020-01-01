package com.github.szgabsz91.morpher.transformationengines.api.model;

import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class ProbabilisticWordTest {

    @Test
    public void testOfAndGetters() {
        Word word = Word.of("word");
        double probability = 0.5;
        ProbabilisticWord probabilisticWord = ProbabilisticWord.of(word, probability);
        assertThat(probabilisticWord.getWord()).isEqualTo(word);
        assertThat(probabilisticWord.getProbability()).isEqualTo(probability);
    }

    @Test
    public void testCompare() {
        List<ProbabilisticWord> probabilisticWords = List.of(
                ProbabilisticWord.of(Word.of("1.0"), 1.0),
                ProbabilisticWord.of(Word.of("0.5"), 0.5),
                ProbabilisticWord.of(Word.of("0.8"), 0.8)
        );
        List<ProbabilisticWord> sortedProbabilisticWords = probabilisticWords
                .stream()
                .sorted()
                .collect(toList());
        List<Double> probabilities = sortedProbabilisticWords
                .stream()
                .map(ProbabilisticWord::getProbability)
                .collect(toList());
        assertThat(probabilities).containsExactly(1.0, 0.8, 0.5);
    }

    @Test
    public void testEquals() {
        ProbabilisticWord probabilisticWord1 = ProbabilisticWord.of(Word.of("word"), 0.5);
        ProbabilisticWord probabilisticWord2 = ProbabilisticWord.of(Word.of("word2"), 0.5);
        ProbabilisticWord probabilisticWord3 = ProbabilisticWord.of(Word.of("word"), 0.6);
        ProbabilisticWord probabilisticWord4 = ProbabilisticWord.of(Word.of("word"), 0.5);

        assertThat(probabilisticWord1).isEqualTo(probabilisticWord1);
        assertThat(probabilisticWord1).isNotEqualTo(null);
        assertThat(probabilisticWord1).isNotEqualTo("string");
        assertThat(probabilisticWord1).isNotEqualTo(probabilisticWord2);
        assertThat(probabilisticWord1).isNotEqualTo(probabilisticWord3);
        assertThat(probabilisticWord1).isEqualTo(probabilisticWord4);
    }

    @Test
    public void testHashCode() {
        ProbabilisticWord probabilisticWord = ProbabilisticWord.of(Word.of("word"), 0.5);
        int result = probabilisticWord.hashCode();
        assertThat(result).isEqualTo(Objects.hash(probabilisticWord.getWord(), probabilisticWord.getProbability()));
    }

    @Test
    public void testToString() {
        ProbabilisticWord probabilisticWord = ProbabilisticWord.of(Word.of("word"), 0.5);
        assertThat(probabilisticWord).hasToString("ProbabilisticWord[word=" + probabilisticWord.getWord() + ", probability=" + probabilisticWord.getProbability() + "]");
    }

}
