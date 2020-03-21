package com.github.szgabsz91.morpher.transformationengines.api.characters.sounds;

import com.github.szgabsz91.morpher.transformationengines.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.Length;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class StartSoundTest {

    private ISound startSound;

    @BeforeEach
    public void setUp() {
        this.startSound = StartSound.get();
    }

    @Test
    public void testGet() {
        assertThat(startSound.getAttributes()).isEmpty();
        assertThat(startSound.get(Length.class)).isNull();
    }

    @Test
    public void testGetAttributes() {
        Set<? extends IAttribute> result = new HashSet<>(startSound.getAttributes());
        assertThat(result).isEmpty();
    }

    @Test
    public void testIsEmpty() {
        assertThat(startSound.isEmpty()).isTrue();
    }

    @Test
    public void testIsStart() {
        assertThat(startSound.isStart()).isTrue();
    }

    @Test
    public void testIsEnd() {
        assertThat(startSound.isEnd()).isFalse();
    }

    @Test
    public void testEquals() {
        assertThat(startSound.equals(StartSound.get())).isTrue();
        assertThat(startSound).isNotEqualTo(null);
        assertThat(startSound).isNotEqualTo("string");
        assertThat(startSound).isNotEqualTo(Vowel.create());
    }

    @Test
    public void testHashCode() {
        int result = startSound.hashCode();
        assertThat(result).isEqualTo(StartSound.class.hashCode());
    }

    @Test
    public void testToString() {
        assertThat(startSound).hasToString(HungarianAttributedCharacterRepository.LETTER_START);
    }

}
