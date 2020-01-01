package com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LipShapeTest {

    @Test
    public void testValueOf() {
        assertThat(LipShape.valueOf("ROUNDED")).isEqualTo(LipShape.ROUNDED);
        assertThat(LipShape.valueOf("UNROUNDED")).isEqualTo(LipShape.UNROUNDED);
    }

}
