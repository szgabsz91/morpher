package com.github.szgabsz91.morpher.transformationengines.api.model;

import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

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

    @Test
    public void testEquals() {
        TransformationEngineResponse transformationEngineResponse1 = TransformationEngineResponse.of(List.of(ProbabilisticWord.of(Word.of("word"), 0.5)));
        TransformationEngineResponse transformationEngineResponse2 = TransformationEngineResponse.of(List.of(ProbabilisticWord.of(Word.of("word2"), 0.5)));
        TransformationEngineResponse transformationEngineResponse3 = TransformationEngineResponse.of(List.of(ProbabilisticWord.of(Word.of("word"), 0.5)));

        assertThat(transformationEngineResponse1).isEqualTo(transformationEngineResponse1);
        assertThat(transformationEngineResponse1).isNotEqualTo(null);
        assertThat(transformationEngineResponse1).isNotEqualTo("string");
        assertThat(transformationEngineResponse1).isNotEqualTo(transformationEngineResponse2);
        assertThat(transformationEngineResponse1).isEqualTo(transformationEngineResponse3);
    }

    @Test
    public void testHashCode() {
        TransformationEngineResponse transformationEngineResponse = TransformationEngineResponse.of(List.of(ProbabilisticWord.of(Word.of("word"), 0.5)));
        int result = transformationEngineResponse.hashCode();
        assertThat(result).isEqualTo(Objects.hash(transformationEngineResponse.getResults()));
    }

    @Test
    public void testToString() {
        TransformationEngineResponse transformationEngineResponse = TransformationEngineResponse.of(List.of(ProbabilisticWord.of(Word.of("word"), 0.5)));
        assertThat(transformationEngineResponse).hasToString("TransformationEngineResponse[results=" + transformationEngineResponse.getResults() + "]");
    }

}
