package com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HunmorphResultTest {

    @Test
    public void testConstructorAndGetters() {
        String grammaticalForm = "grammatical";
        List<String> outpuLines = List.of("output", "lines");
        HunmorphResult hunmorphResult = new HunmorphResult(grammaticalForm, outpuLines);
        assertThat(hunmorphResult.getGrammaticalForm()).isEqualTo(grammaticalForm);
        assertThat(hunmorphResult.getOutputLines()).containsExactlyInAnyOrder("output", "lines");
    }

    @Test
    public void testEquals() {
        HunmorphResult hunmorphResult1 = new HunmorphResult("grammatical1", Collections.emptyList());
        HunmorphResult hunmorphResult2 = new HunmorphResult("grammatical2", Collections.emptyList());
        HunmorphResult hunmorphResult3 = new HunmorphResult("grammatical1", List.of("output"));
        HunmorphResult hunmorphResult4 = new HunmorphResult("grammatical1", Collections.emptyList());

        assertThat(hunmorphResult1.equals(hunmorphResult1)).isTrue();
        assertThat(hunmorphResult1.equals(null)).isFalse();
        assertThat(hunmorphResult1).isNotEqualTo("string");
        assertThat(hunmorphResult1).isNotEqualTo(hunmorphResult2);
        assertThat(hunmorphResult1).isNotEqualTo(hunmorphResult3);
        assertThat(hunmorphResult1).isEqualTo(hunmorphResult4);
    }

    @Test
    public void testHashCode() {
        HunmorphResult hunmorphResult = new HunmorphResult("grammatical", List.of("output"));
        int result = hunmorphResult.hashCode();
        int expected = 31 * hunmorphResult.getGrammaticalForm().hashCode() + hunmorphResult.getOutputLines().hashCode();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        String grammaticalForm = "grammatical";
        List<String> outputLines = List.of("output");
        HunmorphResult hunmorphResult = new HunmorphResult(grammaticalForm, outputLines);
        assertThat(hunmorphResult).hasToString("HunmorphResult[grammaticalForm=" + grammaticalForm + ", outputLines=" + outputLines + "]");
    }

}
