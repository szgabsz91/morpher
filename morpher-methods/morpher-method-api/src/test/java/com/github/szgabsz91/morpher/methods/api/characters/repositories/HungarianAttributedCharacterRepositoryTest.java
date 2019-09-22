package com.github.szgabsz91.morpher.methods.api.characters.repositories;

import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.Consonant;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.EndSound;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.ISound;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.StartSound;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.Vowel;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.IConsonantAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.SoundProductionPlace;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.IVowelAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.methods.api.characters.statistics.AttributedAttributeStatistics;
import com.github.szgabsz91.morpher.methods.api.characters.statistics.IAttributeStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HungarianAttributedCharacterRepositoryTest {

    private ICharacterRepository characterRepository;

    @BeforeEach
    public void setUp() {
        this.characterRepository = HungarianAttributedCharacterRepository.get();
    }

    @Test
    public void testGetCharacterByLetterWithExistingLetters() {
        for (String letter : HungarianAttributedCharacterRepository.LETTERS) {
            ICharacter character = characterRepository.getCharacter(letter);
            assertThat(character).isNotNull();
            assertThat(character).hasToString(letter);
        }

        ICharacter startCharacter = characterRepository.getCharacter(HungarianAttributedCharacterRepository.LETTER_START);
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
    public void testGetLetterByCharacterWithExistingLetters() {
        for (String letter : HungarianAttributedCharacterRepository.LETTERS) {
            ICharacter character = characterRepository.getCharacter(letter);
            String result = characterRepository.getLetter(character);
            assertThat(result).isEqualTo(letter);
        }
    }

    @Test
    public void testGetLetterByCharacterWithInvalidCharacter() {
        ISound sound = Vowel.create();
        String letter = characterRepository.getLetter(sound);
        assertThat(letter).isNull();
    }

    @Test
    public void testGetLetterForStartSound() {
        ISound sound = StartSound.get();
        String letter = characterRepository.getLetter(sound);
        assertThat(letter).isEqualTo(HungarianSimpleCharacterRepository.LETTER_START);
    }

    @Test
    public void testGetLetterForEndSound() {
        ISound sound = EndSound.get();
        String letter = characterRepository.getLetter(sound);
        assertThat(letter).isEqualTo(HungarianSimpleCharacterRepository.LETTER_END);
    }

    @Test
    public void testGetCharacterByAttributesWithExistingLetters() {
        for (String letter : HungarianAttributedCharacterRepository.LETTERS) {
            ICharacter character = characterRepository.getCharacter(letter);
            Set<? extends IAttribute> attributes = new HashSet<>(character.getAttributes());
            ICharacter result = characterRepository.getCharacter(attributes);
            assertThat(result).isEqualTo(character);
        }
    }

    @Test
    public void testGetCharacterByAttributesWithVowelAttributes() {
        ICharacter character = characterRepository.getCharacter(Set.of(Length.LONG));
        assertThat(character).isInstanceOf(Vowel.class);
        assertThat(character.getAttributes()).hasSize(1);
        assertThat(character.get(Length.class)).isEqualTo(Length.LONG);
    }

    @Test
    public void testGetCharacterByAttributesWithConsonantAttributes() {
        ICharacter character = characterRepository.getCharacter(Set.of(SoundProductionPlace.BILABIAL));
        assertThat(character).isInstanceOf(Consonant.class);
        assertThat(character.getAttributes()).hasSize(1);
        assertThat(character.get(SoundProductionPlace.class)).isEqualTo(SoundProductionPlace.BILABIAL);
    }

    @Test
    public void testGetCharacterByAttributesWithInvalidAttributeSet() {
        ICharacter character = characterRepository.getCharacter(Set.of(SoundProductionPlace.BILABIAL));
        assertThat(character).isNotNull();
        assertThat(character.getAttributes()).hasSize(1);
        @SuppressWarnings("unchecked")
        Collection<IAttribute> attributeCollection = (Collection<IAttribute>) character.getAttributes();
        assertThat(attributeCollection).containsSequence(SoundProductionPlace.BILABIAL);
    }

    @Test
    public void testGetCharacterWithAnExceptionalState() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> characterRepository.getCharacter(Set.of(VowelConsonantAttribute.VALUE)));
        assertThat(exception.getMessage()).isEqualTo("The requested attributes contain both vowel and consonant attributes");
    }

    @Test
    public void testGetCharacterWithAnUnknownAttribute() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> characterRepository.getCharacter(Set.of(CustomAttribute.VALUE)));
        assertThat(exception.getMessage()).isEqualTo("The given attribute set contains neither vowel nor consonant attributes");
    }

    @Test
    public void testGetCharacterWithAnEmptyAttributeSet() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> characterRepository.getCharacter(Set.of()));
        assertThat(exception.getMessage()).isEqualTo("The given attribute set is empty");
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
    public void testGetLetterByAttributesWithInvalidAttributeSet() {
        String letter = characterRepository.getLetter(Set.of(SoundProductionPlace.BILABIAL));
        assertThat(letter).isNull();
    }

    @Test
    public void testGetStartCharacter() {
        ICharacter startCharacter = characterRepository.getStartCharacter();
        assertThat(startCharacter).isEqualTo(StartSound.get());
    }

    @Test
    public void testGetEndCharacter() {
        ICharacter endCharacter = characterRepository.getEndCharacter();
        assertThat(endCharacter).isEqualTo(EndSound.get());
    }

    @Test
    public void testGetAttributeStatistics() {
        IAttributeStatistics result = characterRepository.getAttributeStatistics();
        assertThat(result).isInstanceOf(AttributedAttributeStatistics.class);
    }

    @Test
    public void testCalculateSimilarityWithSameCharacters() {
        ICharacter character1 = characterRepository.getCharacter("a");
        ICharacter character2 = characterRepository.getCharacter("a");
        double result = characterRepository.calculateSimilarity(character1, character2);
        assertThat(result).isEqualTo(1.0);
    }

    @Test
    public void testCalculateSimilarityWithDisjointCharacters() {
        ICharacter character1 = characterRepository.getCharacter("a");
        ICharacter character2 = characterRepository.getCharacter("b");
        double result = characterRepository.calculateSimilarity(character1, character2);
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    public void testCalculateSimilarityWithSimilarCharacters() {
        ICharacter character1 = characterRepository.getCharacter("a");
        ICharacter character2 = characterRepository.getCharacter("á");
        double result = characterRepository.calculateSimilarity(character1, character2);
        assertThat(result).isEqualTo(0.75);
    }

    @Test
    public void testCalculateSimilarityWithEmptyCharacters() {
        ICharacter character1 = Vowel.create();
        ICharacter character2 = Consonant.create();
        double result = characterRepository.calculateSimilarity(character1, character2);
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    public void testCalculateSimilarityWithEmptyAndNonEmptyCharacters() {
        ICharacter character1 = Vowel.create();
        ICharacter character2 = Vowel.create(Length.SHORT);
        double result = characterRepository.calculateSimilarity(character1, character2);
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    public void testCalculateSimilarityWithNonEmptyAndEmptyCharacters() {
        ICharacter character1 = Vowel.create(Length.SHORT);
        ICharacter character2 = Vowel.create();
        double result = characterRepository.calculateSimilarity(character1, character2);
        assertThat(result).isEqualTo(0.0);
    }

    private static enum VowelConsonantAttribute implements IVowelAttribute, IConsonantAttribute {

        VALUE

    }

    private static enum CustomAttribute implements IAttribute {

        VALUE

    }

}
