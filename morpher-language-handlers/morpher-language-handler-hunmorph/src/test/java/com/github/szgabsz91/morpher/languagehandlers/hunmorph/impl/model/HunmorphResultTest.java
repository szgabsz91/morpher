package com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HunmorphResultTest {

    @Test
    public void testConstructorAndGetters() {
        String grammaticalForm = "grammatical";
        List<String> outputLines = List.of("output", "lines");
        HunmorphResult hunmorphResult = new HunmorphResult(grammaticalForm, outputLines);
        assertThat(hunmorphResult.getGrammaticalForm()).isEqualTo(grammaticalForm);
        assertThat(hunmorphResult.getOutputLines()).containsExactlyInAnyOrder("output", "lines");
    }

}
