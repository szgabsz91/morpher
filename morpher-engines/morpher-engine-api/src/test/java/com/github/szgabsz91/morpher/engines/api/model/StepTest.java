package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class StepTest {

    @Test
    public void testConstructorAndGetters() {
        Word input = Word.of("input");
        Word output = Word.of("output");
        AffixType affixType = AffixType.of("AFF");
        Step step = new Step(input, output, affixType);
        assertThat(step.getInput()).isEqualTo(input);
        assertThat(step.getOutput()).isEqualTo(output);
        assertThat(step.getAffixType()).isEqualTo(affixType);
    }

    @Test
    public void testEquals() {
        Step step1 = new Step(Word.of("input"), Word.of("output"), AffixType.of("AFF"));
        Step step2 = new Step(Word.of("input2"), Word.of("output"), AffixType.of("AFF"));
        Step step3 = new Step(Word.of("input"), Word.of("output2"), AffixType.of("AFF"));
        Step step4 = new Step(Word.of("input"), Word.of("output"), AffixType.of("AFF2"));
        Step step5 = new Step(Word.of("input"), Word.of("output"), AffixType.of("AFF"));

        assertThat(step1.equals(step1)).isTrue();
        assertThat(step1).isNotEqualTo(null);
        assertThat(step1).isNotEqualTo("string");
        assertThat(step1).isNotEqualTo(step2);
        assertThat(step1).isNotEqualTo(step3);
        assertThat(step1).isNotEqualTo(step4);
        assertThat(step1).isEqualTo(step5);
    }

    @Test
    public void testHashCode() {
        Step step = new Step(Word.of("input"), Word.of("output"), AffixType.of("AFF"));
        int result = step.hashCode();
        int expected = Objects.hash(step.getInput(), step.getOutput(), step.getAffixType());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        Step step = new Step(Word.of("input"), Word.of("output"), AffixType.of("AFF"));
        assertThat(step).hasToString("Step[input=" + step.getInput() + ", output=" + step.getOutput() + ", affixType=" + step.getAffixType() + ']');
    }

}
