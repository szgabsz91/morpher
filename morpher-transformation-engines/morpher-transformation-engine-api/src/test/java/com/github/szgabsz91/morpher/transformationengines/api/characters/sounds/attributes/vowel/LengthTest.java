package com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LengthTest {

    @Test
    public void testValueOf() {
        assertThat(Length.valueOf("SHORT")).isEqualTo(Length.SHORT);
        assertThat(Length.valueOf("LONG")).isEqualTo(Length.LONG);
    }

}
