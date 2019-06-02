package com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DiscriminatorTest {

    @Test
    public void testValueOf() {
        assertThat(Discriminator.valueOf("NORMAL")).isEqualTo(Discriminator.NORMAL);
        assertThat(Discriminator.valueOf("LY")).isEqualTo(Discriminator.LY);
        assertThat(Discriminator.valueOf("Q")).isEqualTo(Discriminator.Q);
        assertThat(Discriminator.valueOf("W")).isEqualTo(Discriminator.W);
        assertThat(Discriminator.valueOf("X")).isEqualTo(Discriminator.X);
        assertThat(Discriminator.valueOf("Y")).isEqualTo(Discriminator.Y);
    }
    
}
