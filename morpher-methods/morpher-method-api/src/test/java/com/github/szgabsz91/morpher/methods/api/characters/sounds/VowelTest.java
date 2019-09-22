package com.github.szgabsz91.morpher.methods.api.characters.sounds;

import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.SoundProductionPlace;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.IVowelAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.LipShape;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

public class VowelTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testFactoryMethod() {
        Vowel vowel = Vowel.create(Length.LONG, LipShape.ROUNDED);
        assertThat(vowel.getAttributes()).hasSize(2);
        assertThat((Collection<IAttribute>) vowel.getAttributes()).contains(Length.LONG, LipShape.ROUNDED);
        assertThat(vowel.get(Length.class)).isEqualTo(Length.LONG);
        assertThat(vowel.get(LipShape.class)).isEqualTo(LipShape.ROUNDED);
        assertThat(vowel.get(SoundProductionPlace.class)).isNull();
    }

    @Test
    public void testIsEmpty() {
        assertThat(Vowel.create().isEmpty()).isTrue();
        assertThat(Vowel.create(Length.LONG).isEmpty()).isFalse();
    }

    @Test
    public void testIsStart() {
        assertThat(Vowel.create().isStart()).isFalse();
    }

    @Test
    public void testIsEnd() {
        assertThat(Vowel.create().isEnd()).isFalse();
    }

    @Test
    public void testEquals() {
        Vowel vowel1 = Vowel.create();
        Vowel vowel2 = Vowel.create(Length.LONG);
        Vowel vowel3 = Vowel.create(Length.SHORT);

        assertThat(vowel1).isEqualTo(vowel1);
        assertThat(vowel1).isNotEqualTo(null);
        assertThat(vowel1).isNotEqualTo("string");
        assertThat(vowel2).isNotEqualTo(vowel3);
    }

    @Test
    public void testHashCode() {
        Vowel vowel = Vowel.create(Length.LONG);
        int expected = new HashSet<>(vowel.getAttributes()).hashCode();
        assertThat(vowel.hashCode()).isEqualTo(expected);
    }

    @Test
    public void testToStringWithRealLetter() {
        String letter = "a";
        Vowel vowel = (Vowel) HungarianAttributedCharacterRepository.get().getCharacter(letter);
        assertThat(vowel).hasToString(letter);
    }

    @Test
    public void testToStringWithNonRealLetter() {
        Vowel vowel = Vowel.create(Length.LONG);
        Map<Class<? extends IVowelAttribute>, IVowelAttribute> attributeMap = vowel.getAttributeMap();
        Collection<IVowelAttribute> attributes = attributeMap.values();
        String expectedInnerPart = attributes
                .stream()
                .map(IVowelAttribute::toString)
                .collect(joining(", "));
        String expected = "[" + expectedInnerPart + "]";
        assertThat(vowel).hasToString(expected);
    }

}
