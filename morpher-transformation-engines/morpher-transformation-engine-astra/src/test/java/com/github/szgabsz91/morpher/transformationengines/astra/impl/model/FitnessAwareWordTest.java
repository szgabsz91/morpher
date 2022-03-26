package com.github.szgabsz91.morpher.transformationengines.astra.impl.model;

import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

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

}
