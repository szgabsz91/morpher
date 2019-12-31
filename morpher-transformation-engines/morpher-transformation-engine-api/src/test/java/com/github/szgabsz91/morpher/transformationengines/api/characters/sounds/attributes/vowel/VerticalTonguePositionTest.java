package com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VerticalTonguePositionTest {

    @Test
    public void testValueOf() {
        assertThat(VerticalTonguePosition.valueOf("CLOSE")).isEqualTo(VerticalTonguePosition.CLOSE);
        assertThat(VerticalTonguePosition.valueOf("MIDDLE")).isEqualTo(VerticalTonguePosition.MIDDLE);
        assertThat(VerticalTonguePosition.valueOf("SEMI_OPEN")).isEqualTo(VerticalTonguePosition.SEMI_OPEN);
        assertThat(VerticalTonguePosition.valueOf("OPEN")).isEqualTo(VerticalTonguePosition.OPEN);
    }

}
