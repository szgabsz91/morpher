package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class LemmatizationInputTest {

    @Test
    public void testOfAndGetters() {
        Word input = Word.of("input");
        LemmatizationInput lemmatizationInput = LemmatizationInput.of(input);
        assertThat(lemmatizationInput.getInput()).isEqualTo(input);
    }

    @Test
    public void testEquals() {
        LemmatizationInput lemmatizationInput1 = LemmatizationInput.of(Word.of("input"));
        LemmatizationInput lemmatizationInput2 = LemmatizationInput.of(Word.of("input2"));

        assertThat(lemmatizationInput1).isEqualTo(lemmatizationInput1);
        assertThat(lemmatizationInput1).isNotEqualTo(null);
        assertThat(lemmatizationInput1).isNotEqualTo("string");
        assertThat(lemmatizationInput1).isNotEqualTo(lemmatizationInput2);
    }

    @Test
    public void testHashCode() {
        Word input = Word.of("input");
        LemmatizationInput lemmatizationInput = LemmatizationInput.of(input);
        int result = lemmatizationInput.hashCode();
        assertThat(result).isEqualTo(Objects.hash(input));
    }

    @Test
    public void testToString() {
        Word input = Word.of("input");
        LemmatizationInput lemmatizationInput = LemmatizationInput.of(input);
        assertThat(lemmatizationInput).hasToString("LemmatizationInput[input=" + input + "]");
    }

}
