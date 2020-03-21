package com.github.szgabsz91.morpher.transformationengines.api.characters;

import com.github.szgabsz91.morpher.transformationengines.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.transformationengines.api.characters.attributes.Letter;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.consonant.Voice;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CharacterTest {

    @Test
    public void testCreate() {
        Letter letter = Letter.A;
        Character character = Character.create(letter);
        assertThat(character.get(Letter.class)).isEqualTo(letter);
    }

    @Test
    public void testGetWithNonLetterAttribute() {
        Character character = Character.create(Letter.A);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> character.get(Voice.class));
        assertThat(exception.getMessage()).isEqualTo("Only the Letter attribute is supported by the Character class");
    }

    @Test
    public void testGetWithLetterAttribute() {
        Letter letter = Letter.A;
        Character character = Character.create(letter);
        assertThat(character.get(Letter.class)).isEqualTo(letter);
    }

    @Test
    public void testGetAttributes() {
        Letter letter = Letter.A;
        Character character = Character.create(letter);
        assertThat(character.getAttributes()).hasSize(1);
        @SuppressWarnings("unchecked")
        Collection<IAttribute> attributeCollection = (Collection<IAttribute>) character.getAttributes();
        assertThat(attributeCollection).contains(letter);
    }

    @Test
    public void testIsEmpty() {
        Character character = Character.create(Letter.A);
        assertThat(character.isEmpty()).isFalse();
    }

    @Test
    public void testIsStartWithStartSymbol() {
        Character character = Character.create(Letter.START_SYMBOL);
        assertThat(character.isStart()).isTrue();
    }

    @Test
    public void testIsStartWithNonStartSymbol() {
        Character character = Character.create(Letter.A);
        assertThat(character.isStart()).isFalse();
    }

    @Test
    public void testIsEndWithEndSymbol() {
        Character character = Character.create(Letter.END_SYMBOL);
        assertThat(character.isEnd()).isTrue();
    }

    @Test
    public void testIsEndWithNonEndSymbol() {
        Character character = Character.create(Letter.A);
        assertThat(character.isEnd()).isFalse();
    }

    @Test
    public void testEquals() {
        Character character1 = Character.create(Letter.A);
        Character character2 = Character.create(Letter.A);
        Character character3 = Character.create(Letter.B);

        assertThat(character1.equals(character1)).isTrue();
        assertThat(character1).isEqualTo(character2);
        assertThat(character1).isNotEqualTo(null);
        assertThat(character1).isNotEqualTo("string");
        assertThat(character1).isNotEqualTo(character3);
    }

    @Test
    public void testHashCode() {
        Letter letter = Letter.A;
        Character character = Character.create(letter);
        List<Letter> attributes = List.of(letter);
        int expected = attributes.hashCode();
        assertThat(character.hashCode()).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        Letter letter = Letter.A;
        Character character = Character.create(letter);
        String expected = letter.toString();
        assertThat(character).hasToString(expected);
    }

}
