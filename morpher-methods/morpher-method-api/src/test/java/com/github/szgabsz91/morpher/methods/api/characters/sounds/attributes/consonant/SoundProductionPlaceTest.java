package com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SoundProductionPlaceTest {

    @Test
    public void testValueOf() {
        assertThat(SoundProductionPlace.valueOf("BILABIAL")).isEqualTo(SoundProductionPlace.BILABIAL);
        assertThat(SoundProductionPlace.valueOf("LABIO_DENTAL")).isEqualTo(SoundProductionPlace.LABIO_DENTAL);
        assertThat(SoundProductionPlace.valueOf("DENTAL_ALVEOLAR")).isEqualTo(SoundProductionPlace.DENTAL_ALVEOLAR);
        assertThat(SoundProductionPlace.valueOf("DENTAL_POSTALVEOLAR")).isEqualTo(SoundProductionPlace.DENTAL_POSTALVEOLAR);
        assertThat(SoundProductionPlace.valueOf("PALATAL")).isEqualTo(SoundProductionPlace.PALATAL);
        assertThat(SoundProductionPlace.valueOf("VELAR")).isEqualTo(SoundProductionPlace.VELAR);
        assertThat(SoundProductionPlace.valueOf("GLOTTAL")).isEqualTo(SoundProductionPlace.GLOTTAL);
    }

}
