package com.github.szgabsz91.morpher.methods.api.characters.statistics;

import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.Consonant;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.EndSound;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.StartSound;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.Vowel;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.Discriminator;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.SoundProductionPlace;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.UvulaPosition;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.Voice;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AttributedAttributeStatisticsTest {

    private AttributedAttributeStatistics attributeStatistics;

    @BeforeEach
    public void setUp() {
        this.attributeStatistics = new AttributedAttributeStatistics();
    }

    @Test
    public void testGetRelativeFrequencyWithNonExistentAttributeSet() {
        Set<IAttribute> attributes = Set.of(Voice.VOICED, Length.SHORT);
        CustomCharacter character = new CustomCharacter(attributes);
        double relativeFrequency = this.attributeStatistics.getRelativeFrequency(character);
        assertThat(relativeFrequency).isEqualTo(0.0);
    }

    @Test
    public void testGetRelativeFrequencyWithExistingAttributeSet() {
        ICharacter character = Consonant.create(
                Voice.VOICED,
                SoundProductionPlace.DENTAL_ALVEOLAR,
                UvulaPosition.ORAL,
                Discriminator.NORMAL
        );
        double relativeFrequency = this.attributeStatistics.getRelativeFrequency(character);
        assertThat(relativeFrequency).isGreaterThan(0.0);
    }

    @Test
    public void testGetRelativeFrequencyWithAnEmptyVowel() {
        ICharacter vowel = Vowel.create();
        double relativeFrequency = this.attributeStatistics.getRelativeFrequency(vowel);
        assertThat(relativeFrequency).isGreaterThan(0.0);
    }

    @Test
    public void testGetRelativeFrequencyWithAnEmptyConsonant() {
        ICharacter consonant = Consonant.create();
        double relativeFrequency = this.attributeStatistics.getRelativeFrequency(consonant);
        assertThat(relativeFrequency).isGreaterThan(0.0);
    }

    @Test
    public void testGetRelativeFrequencyWithAStartSound() {
        ICharacter startSound = StartSound.get();
        double relativeFrequency = this.attributeStatistics.getRelativeFrequency(startSound);
        assertThat(relativeFrequency).isGreaterThan(0.0);
    }

    @Test
    public void testGetRelativeFrequencyWithAnEndSound() {
        ICharacter endSound = EndSound.get();
        double relativeFrequency = this.attributeStatistics.getRelativeFrequency(endSound);
        assertThat(relativeFrequency).isGreaterThan(0.0);
    }

    @Test
    public void testSumOfRelativeFrequencies() {
        double sum = this.attributeStatistics.relativeFrequencies.values()
                .stream()
                .mapToDouble(v -> v)
                .sum();
        assertThat(sum).isEqualTo(1.0);
    }

    @Test
    public void testInternalDiscriminatorValueOf() {
        for (AttributedAttributeStatistics.Discriminator discriminator : AttributedAttributeStatistics.Discriminator.values()) {
            String toString = discriminator.toString();
            AttributedAttributeStatistics.Discriminator result = AttributedAttributeStatistics.Discriminator.valueOf(toString);
            assertThat(result).isEqualTo(discriminator);
        }
    }

    @Test
    public void testInternalDiscriminatorFromClassWithUnknownClass() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> AttributedAttributeStatistics.Discriminator.fromClass(ICharacter.class));
        assertThat(exception.getMessage()).isEqualTo("The given class is not known: " + ICharacter.class);
    }

}
