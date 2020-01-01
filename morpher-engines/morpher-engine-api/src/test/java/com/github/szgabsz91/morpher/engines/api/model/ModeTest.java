package com.github.szgabsz91.morpher.engines.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ModeTest {

    @Test
    public void testValues() {
        assertThat(Mode.values()).contains(Mode.INFLECTION, Mode.ANALYSIS);
    }

    @Test
    public void testValueOf() {
        assertThat(Mode.valueOf("INFLECTION")).isEqualTo(Mode.INFLECTION);
        assertThat(Mode.valueOf("ANALYSIS")).isEqualTo(Mode.ANALYSIS);
    }

}
