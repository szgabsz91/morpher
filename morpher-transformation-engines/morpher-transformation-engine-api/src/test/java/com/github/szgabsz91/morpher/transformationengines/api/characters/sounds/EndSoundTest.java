package com.github.szgabsz91.morpher.transformationengines.api.characters.sounds;

import com.github.szgabsz91.morpher.transformationengines.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.Length;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class EndSoundTest {

    private ISound endSound;

    @BeforeEach
    public void setUp() {
        this.endSound = EndSound.get();
    }

    @Test
    public void testGet() {
        assertThat(endSound.getAttributes()).isEmpty();
        assertThat(endSound.get(Length.class)).isNull();
    }

    @Test
    public void testGetAttributes() {
        Set<? extends IAttribute> result = new HashSet<>(endSound.getAttributes());
        assertThat(result).isEmpty();
    }

    @Test
    public void testIsEmpty() {
        assertThat(endSound.isEmpty()).isTrue();
    }

    @Test
    public void testIsStart() {
        assertThat(endSound.isStart()).isFalse();
    }

    @Test
    public void testIsEnd() {
        assertThat(endSound.isEnd()).isTrue();
    }

    @Test
    public void testEquals() {
        assertThat(endSound.equals(EndSound.get())).isTrue();
        assertThat(endSound.equals(null)).isFalse();
        assertThat(endSound).isNotEqualTo("string");
        assertThat(endSound).isNotEqualTo(Vowel.create());
    }

    @Test
    public void testHashCode() {
        int result = endSound.hashCode();
        assertThat(result).isEqualTo(EndSound.class.hashCode());
    }

    @Test
    public void testToString() {
        assertThat(endSound).hasToString(HungarianAttributedCharacterRepository.LETTER_END);
    }

}
