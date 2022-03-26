package com.github.szgabsz91.morpher.transformationengines.api.model;

import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TransformationEngineResponseTest {

    @Test
    public void testOfAndGetters() {
        List<ProbabilisticWord> results = List.of(ProbabilisticWord.of(Word.of("word"), 0.5));
        TransformationEngineResponse transformationEngineResponse = TransformationEngineResponse.of(results);
        assertThat(transformationEngineResponse.getResults()).isEqualTo(results);
    }

    @Test
    public void testSingletonAndGetters() {
        Word word = Word.of("word");
        TransformationEngineResponse transformationEngineResponse = TransformationEngineResponse.singleton(word);
        assertThat(transformationEngineResponse.getResults()).containsExactly(ProbabilisticWord.of(word, 1.0));
    }

    @Test
    public void testSingletonAndGettersWithExternalProbability() {
        Word word = Word.of("word");
        double probability = 0.5;
        TransformationEngineResponse transformationEngineResponse = TransformationEngineResponse.singleton(word, probability);
        assertThat(transformationEngineResponse.getResults()).containsExactly(ProbabilisticWord.of(word, probability));
    }

}
