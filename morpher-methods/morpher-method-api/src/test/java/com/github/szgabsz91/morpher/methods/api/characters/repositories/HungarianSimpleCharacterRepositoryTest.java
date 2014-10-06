package com.github.szgabsz91.morpher.methods.api.characters.repositories;

import com.github.szgabsz91.morpher.methods.api.characters.Character;
import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.attributes.Letter;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.Voice;
import com.github.szgabsz91.morpher.methods.api.characters.statistics.IAttributeStatistics;
import com.github.szgabsz91.morpher.methods.api.characters.statistics.SimpleAttributeStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HungarianSimpleCharacterRepositoryTest {

    private ICharacterRepository characterRepository;

    @BeforeEach
    public void setUp() {
        this.characterRepository = HungarianSimpleCharacterRepository.get();
    }

    @Test
    public void testGetCharacterByLetterWithExistingLetters() {
        for (String letter : HungarianSimpleCharacterRepository.LETTERS) {
            ICharacter character = characterRepository.getCharacter(letter);
            assertThat(character).isNotNull();
            assertThat(character).hasToString(letter);
        }

        ICharacter startCharacter = characterRepository.getCharacter(HungarianSimpleCharacterRepository.LETTER_START);
        assertThat(startCharacter).isNotNull();
        assertThat(startCharacter).hasToString(HungarianAttributedCharacterRepository.LETTER_START);

        ICharacter endCharacter = characterRepository.getCharacter(HungarianAttributedCharacterRepository.LETTER_END);
        assertThat(endCharacter).isNotNull();
        assertThat(endCharacter).hasToString(HungarianAttributedCharacterRepository.LETTER_END);
    }

    @Test
    public void testGetCharacterByLetterWithInvalidLetter() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> characterRepository.getCharacter("ł"));
        assertThat(exception.getMessage()).isEqualTo("Letter ł is not recognized!");
    }

    @Test
    public void testGetCharacterWithMultipleAttributes() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> characterRepository.getCharacter(Set.of(Letter.A, Voice.VOICED)));
        assertThat(exception.getMessage()).isEqualTo("Every Character contains only 1 attribute");
    }

    @Test
    public void testGetCharacterWithNonLetterAttributes() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> characterRepository.getCharacter(Set.of(Voice.VOICED)));
        assertThat(exception.getMessage()).isEqualTo("A Character's single attribute must be its Letter, but it was Voice");
    }

    @Test
    public void testGetLetterBySoundWithExistingLetters() {
        for (String letter : HungarianAttributedCharacterRepository.LETTERS) {
            ICharacter character = characterRepository.getCharacter(letter);
            String result = characterRepository.getLetter(character);
            assertThat(result).isEqualTo(letter);
        }
    }

    @Test
    public void testGetLetterByCharacterWithInvalidCharacter() {
        ICharacter sound = Character.create(null);
        String letter = characterRepository.getLetter(sound);
        assertThat(letter).isNull();
    }

    @Test
    public void testGetLetterWithMultipleAttributes() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> characterRepository.getLetter(Set.of(Letter.A, Voice.VOICED)));
        assertThat(exception.getMessage()).isEqualTo("Every Character contains only 1 attribute");
    }

    @Test
    public void testGetLetterWithNonLetterAttributes() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> characterRepository.getLetter(Set.of(Voice.VOICED)));
        assertThat(exception.getMessage()).isEqualTo("A Character's single attribute must be its Letter, but it was Voice");
    }

    @Test
    public void testGetCharacterByAttributesWithExistingLetters() {
        for (String letter : HungarianSimpleCharacterRepository.LETTERS) {
            ICharacter character = characterRepository.getCharacter(letter);
            Set<? extends IAttribute> attributes = new HashSet<>(character.getAttributes());
            ICharacter result = characterRepository.getCharacter(attributes);
            assertThat(result).isEqualTo(character);
        }
    }

    @Test
    public void testGetLetterByAttributesWithExistingLetters() {
        for (String letter : HungarianAttributedCharacterRepository.LETTERS) {
            ICharacter character = characterRepository.getCharacter(letter);
            Set<? extends IAttribute> attributes = new HashSet<>(character.getAttributes());
            String result = characterRepository.getLetter(attributes);
            assertThat(result).isEqualTo(letter);
        }
    }

    @Test
    public void testGetStartCharacter() {
        ICharacter startCharacter = characterRepository.getStartCharacter();
        assertThat(startCharacter).isEqualTo(Character.create(Letter.START_SYMBOL));
    }

    @Test
    public void testGetEndCharacter() {
        ICharacter endCharacter = characterRepository.getEndCharacter();
        assertThat(endCharacter).isEqualTo(Character.create(Letter.END_SYMBOL));
    }

    @Test
    public void testGetAttributeStatistics() {
        IAttributeStatistics result = characterRepository.getAttributeStatistics();
        assertThat(result).isInstanceOf(SimpleAttributeStatistics.class);
    }

    @Test
    public void testCalculateSimilarityWithSameCharacters() {
        ICharacter character1 = characterRepository.getCharacter("a");
        ICharacter character2 = characterRepository.getCharacter("a");
        double result = characterRepository.calculateSimilarity(character1, character2);
        assertThat(result).isEqualTo(1.0);
    }

    @Test
    public void testCalculateSimilarityWithDifferentCharacters() {
        ICharacter character1 = characterRepository.getCharacter("a");
        ICharacter character2 = characterRepository.getCharacter("á");
        double result = characterRepository.calculateSimilarity(character1, character2);
        assertThat(result).isEqualTo(0.0);
    }

}
