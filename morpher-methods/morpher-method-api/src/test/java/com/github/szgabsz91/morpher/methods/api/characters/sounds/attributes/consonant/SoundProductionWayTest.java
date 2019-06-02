package com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SoundProductionWayTest {

    @Test
    public void testValueOf() {
        assertThat(SoundProductionWay.valueOf("PLOSIVE")).isEqualTo(SoundProductionWay.PLOSIVE);
        assertThat(SoundProductionWay.valueOf("FRICATIVE")).isEqualTo(SoundProductionWay.FRICATIVE);
        assertThat(SoundProductionWay.valueOf("LATERAL_FRICATIVE")).isEqualTo(SoundProductionWay.LATERAL_FRICATIVE);
        assertThat(SoundProductionWay.valueOf("LATERAL_APPROXIMATIVE")).isEqualTo(SoundProductionWay.LATERAL_APPROXIMATIVE);
        assertThat(SoundProductionWay.valueOf("TRILL")).isEqualTo(SoundProductionWay.TRILL);
    }

}
