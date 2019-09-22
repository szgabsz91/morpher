package com.github.szgabsz91.morpher.methods.api.characters.sounds;

import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.IConsonantAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.SoundProductionPlace;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.SoundProductionWay;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsonantTest {

    @Test
    public void testFactoryMethod() {
        Consonant consonant = Consonant.create(SoundProductionPlace.BILABIAL, SoundProductionWay.FRICATIVE);
        assertThat(consonant.getAttributes()).hasSize(2);
        @SuppressWarnings("unchecked")
        Collection<IAttribute> attributeCollection = (Collection<IAttribute>) consonant.getAttributes();
        assertThat(attributeCollection).contains(SoundProductionPlace.BILABIAL, SoundProductionWay.FRICATIVE);
        assertThat(consonant.get(SoundProductionPlace.class)).isEqualTo(SoundProductionPlace.BILABIAL);
        assertThat(consonant.get(SoundProductionWay.class)).isEqualTo(SoundProductionWay.FRICATIVE);
        assertThat(consonant.get(Length.class)).isNull();
    }

    @Test
    public void testIsEmpty() {
        assertThat(Consonant.create().isEmpty()).isTrue();
        assertThat(Consonant.create(SoundProductionPlace.BILABIAL).isEmpty()).isFalse();
    }

    @Test
    public void testIsStart() {
        assertThat(Consonant.create().isStart()).isFalse();
    }

    @Test
    public void testIsEnd() {
        assertThat(Consonant.create().isEnd()).isFalse();
    }

    @Test
    public void testEquals() {
        Consonant consonant1 = Consonant.create();
        Consonant consonant2 = Consonant.create(SoundProductionPlace.BILABIAL);
        Consonant consonant3 = Consonant.create(SoundProductionPlace.DENTAL_ALVEOLAR);

        assertThat(consonant1).isEqualTo(consonant1);
        assertThat(consonant1).isNotEqualTo(null);
        assertThat(consonant1).isNotEqualTo("string");
        assertThat(consonant2).isNotEqualTo(consonant3);
    }

    @Test
    public void testHashCode() {
        Consonant consonant = Consonant.create(SoundProductionPlace.BILABIAL);
        int expected = new HashSet<>(consonant.getAttributes()).hashCode();
        assertThat(consonant.hashCode()).isEqualTo(expected);
    }

    @Test
    public void testToStringWithRealLetter() {
        String letter = "b";
        Consonant consonant = (Consonant) HungarianAttributedCharacterRepository.get().getCharacter(letter);
        assertThat(consonant).hasToString(letter);
    }

    @Test
    public void testToStringWithNonRealLetter() {
        Consonant consonant = Consonant.create(SoundProductionPlace.BILABIAL);
        Map<Class<? extends IConsonantAttribute>, IConsonantAttribute> attributeMap = consonant.getAttributeMap();
        Collection<IConsonantAttribute> attributes = attributeMap.values();
        String expectedInnerPart = attributes
                .stream()
                .map(IConsonantAttribute::toString)
                .collect(joining(", "));
        String expected = "[" + expectedInnerPart + "]";
        assertThat(consonant).hasToString(expected);
    }

}
