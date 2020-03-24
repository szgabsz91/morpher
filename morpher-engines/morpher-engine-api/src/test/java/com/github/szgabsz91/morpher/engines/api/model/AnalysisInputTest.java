package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class AnalysisInputTest {

    @Test
    public void testOfAndGetters() {
        Word input = Word.of("input");
        AnalysisInput analysisInput = AnalysisInput.of(input);
        assertThat(analysisInput.getInput()).isEqualTo(input);
    }

    @Test
    public void testEquals() {
        AnalysisInput analysisInput1 = AnalysisInput.of(Word.of("input"));
        AnalysisInput analysisInput2 = AnalysisInput.of(Word.of("input2"));

        assertThat(analysisInput1.equals(analysisInput1)).isTrue();
        assertThat(analysisInput1).isNotEqualTo(null);
        assertThat(analysisInput1).isNotEqualTo("string");
        assertThat(analysisInput1).isNotEqualTo(analysisInput2);
    }

    @Test
    public void testHashCode() {
        Word input = Word.of("input");
        AnalysisInput analysisInput = AnalysisInput.of(input);
        int result = analysisInput.hashCode();
        assertThat(result).isEqualTo(Objects.hash(input));
    }

    @Test
    public void testToString() {
        Word input = Word.of("input");
        AnalysisInput analysisInput = AnalysisInput.of(input);
        assertThat(analysisInput).hasToString("AnalysisInput[input=" + input + "]");
    }

}
