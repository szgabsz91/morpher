package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

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

}
