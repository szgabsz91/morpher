package com.github.szgabsz91.morpher.methods.api.model;

import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodResponseTest {

    @Test
    public void testOfAndGetters() {
        List<ProbabilisticWord> results = List.of(ProbabilisticWord.of(Word.of("word"), 0.5));
        MethodResponse methodResponse = MethodResponse.of(results);
        assertThat(methodResponse.getResults()).isEqualTo(results);
    }

    @Test
    public void testSingletonAndGetters() {
        Word word = Word.of("word");
        MethodResponse methodResponse = MethodResponse.singleton(word);
        assertThat(methodResponse.getResults()).containsExactly(ProbabilisticWord.of(word, 1.0));
    }

    @Test
    public void testEquals() {
        MethodResponse methodResponse1 = MethodResponse.of(List.of(ProbabilisticWord.of(Word.of("word"), 0.5)));
        MethodResponse methodResponse2 = MethodResponse.of(List.of(ProbabilisticWord.of(Word.of("word2"), 0.5)));
        MethodResponse methodResponse3 = MethodResponse.of(List.of(ProbabilisticWord.of(Word.of("word"), 0.5)));

        assertThat(methodResponse1).isEqualTo(methodResponse1);
        assertThat(methodResponse1).isNotEqualTo(null);
        assertThat(methodResponse1).isNotEqualTo("string");
        assertThat(methodResponse1).isNotEqualTo(methodResponse2);
        assertThat(methodResponse1).isEqualTo(methodResponse3);
    }

    @Test
    public void testHashCode() {
        MethodResponse methodResponse = MethodResponse.of(List.of(ProbabilisticWord.of(Word.of("word"), 0.5)));
        int result = methodResponse.hashCode();
        assertThat(result).isEqualTo(Objects.hash(methodResponse.getResults()));
    }

    @Test
    public void testToString() {
        MethodResponse methodResponse = MethodResponse.of(List.of(ProbabilisticWord.of(Word.of("word"), 0.5)));
        assertThat(methodResponse).hasToString("MethodResponse[results=" + methodResponse.getResults() + "]");
    }

}
