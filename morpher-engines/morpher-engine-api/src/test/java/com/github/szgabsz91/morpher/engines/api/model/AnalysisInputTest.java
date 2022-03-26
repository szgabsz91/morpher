package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnalysisInputTest {

    @Test
    public void testOfAndGetters() {
        Word input = Word.of("input");
        AnalysisInput analysisInput = AnalysisInput.of(input);
        assertThat(analysisInput.getInput()).isEqualTo(input);
    }

}
