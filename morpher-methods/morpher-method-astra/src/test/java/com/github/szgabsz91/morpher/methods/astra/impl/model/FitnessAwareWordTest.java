package com.github.szgabsz91.morpher.methods.astra.impl.model;

import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class FitnessAwareWordTest {

    @Test
    public void testOfAndGetters() {
        Word word = Word.of("word");
        double fitness = 0.5;
        FitnessAwareWord fitnessAwareWord = FitnessAwareWord.of(word, fitness);
        assertThat(fitnessAwareWord.getWord()).isEqualTo(word);
        assertThat(fitnessAwareWord.getFitness()).isEqualTo(fitness);
    }

    @Test
    public void testEquals() {
        FitnessAwareWord fitnessAwareWord1 = FitnessAwareWord.of(Word.of("word"), 0.5);
        FitnessAwareWord fitnessAwareWord2 = FitnessAwareWord.of(Word.of("word2"), 0.5);
        FitnessAwareWord fitnessAwareWord3 = FitnessAwareWord.of(Word.of("word"), 0.6);
        FitnessAwareWord fitnessAwareWord4 = FitnessAwareWord.of(Word.of("word"), 0.5);

        assertThat(fitnessAwareWord1).isEqualTo(fitnessAwareWord1);
        assertThat(fitnessAwareWord1).isNotEqualTo(null);
        assertThat(fitnessAwareWord1).isNotEqualTo("string");
        assertThat(fitnessAwareWord1).isNotEqualTo(fitnessAwareWord2);
        assertThat(fitnessAwareWord1).isNotEqualTo(fitnessAwareWord3);
        assertThat(fitnessAwareWord1).isEqualTo(fitnessAwareWord4);
    }

    @Test
    public void testHashCode() {
        FitnessAwareWord fitnessAwareWord = FitnessAwareWord.of(Word.of("word"), 0.5);
        int result = fitnessAwareWord.hashCode();
        assertThat(result).isEqualTo(Objects.hash(fitnessAwareWord.getWord(), fitnessAwareWord.getFitness()));
    }

    @Test
    public void testToString() {
        FitnessAwareWord fitnessAwareWord = FitnessAwareWord.of(Word.of("word"), 0.5);
        assertThat(fitnessAwareWord).hasToString("FitnessAwareWord[word=" + fitnessAwareWord.getWord() + ", fitness=" + fitnessAwareWord.getFitness() + "]");
    }

}
