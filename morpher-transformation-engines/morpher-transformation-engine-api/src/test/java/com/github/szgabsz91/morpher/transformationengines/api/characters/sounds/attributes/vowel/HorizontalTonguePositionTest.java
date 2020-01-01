package com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HorizontalTonguePositionTest {

    @Test
    public void testValueOf() {
        assertThat(HorizontalTonguePosition.valueOf("BACK")).isEqualTo(HorizontalTonguePosition.BACK);
        assertThat(HorizontalTonguePosition.valueOf("FRONT")).isEqualTo(HorizontalTonguePosition.FRONT);
    }

}
