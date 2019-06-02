package com.github.szgabsz91.morpher.methods.api.characters.statistics;

import com.github.szgabsz91.morpher.methods.api.characters.Character;
import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.attributes.Letter;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.Consonant;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.Voice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleAttributeStatisticsTest {

    private IAttributeStatistics attributeStatistics;

    @BeforeEach
    public void setUp() {
        this.attributeStatistics = new SimpleAttributeStatistics();
    }

    @Test
    public void testGetRelativeFrequencyWithMultipleAttributes() {
        Set<IAttribute> attributes = Set.of(
                Letter.A,
                Voice.VOICED
        );
        CustomCharacter customCharacter = new CustomCharacter(attributes);
        double result = this.attributeStatistics.getRelativeFrequency(customCharacter);
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    public void testGetRelativeFrequencyWithASingleNonLetterAttribute() {
        ICharacter consonant = Consonant.create(Voice.VOICED);
        double result = this.attributeStatistics.getRelativeFrequency(consonant);
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    public void testGetRelativeFrequencyWithASingleLetter() {
        Character character = Character.create(Letter.A);
        double result = this.attributeStatistics.getRelativeFrequency(character);
        double expected = 1.0 / HungarianSimpleCharacterRepository.LETTERS.size();
        assertThat(result).isEqualTo(expected);
    }

}
