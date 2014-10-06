package com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VoiceTest {

    @Test
    public void testValueOf() {
        assertThat(Voice.valueOf("VOICED")).isEqualTo(Voice.VOICED);
        assertThat(Voice.valueOf("UNVOICED")).isEqualTo(Voice.UNVOICED);
    }

}
