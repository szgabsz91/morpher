package com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UvulaPositionTest {

    @Test
    public void testValueOf() {
        assertThat(UvulaPosition.valueOf("ORAL")).isEqualTo(UvulaPosition.ORAL);
        assertThat(UvulaPosition.valueOf("NASAL")).isEqualTo(UvulaPosition.NASAL);
    }

}
