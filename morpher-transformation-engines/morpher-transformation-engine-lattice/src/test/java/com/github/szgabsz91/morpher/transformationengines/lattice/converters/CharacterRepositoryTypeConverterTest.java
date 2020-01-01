package com.github.szgabsz91.morpher.transformationengines.lattice.converters;

import com.github.szgabsz91.morpher.transformationengines.api.characters.ICharacter;
import com.github.szgabsz91.morpher.transformationengines.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.statistics.IAttributeStatistics;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.CharacterRepositoryType;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.CharacterRepositoryTypeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CharacterRepositoryTypeConverterTest {

    private CharacterRepositoryTypeConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new CharacterRepositoryTypeConverter();
    }

    @Test
    public void testConvert() {
        assertThat(this.converter.convert(CharacterRepositoryType.ATTRIBUTED)).isEqualTo(CharacterRepositoryTypeMessage.ATTRIBUTED);
        assertThat(this.converter.convert(CharacterRepositoryType.SIMPLE)).isEqualTo(CharacterRepositoryTypeMessage.SIMPLE);
    }

    @Test
    public void testFromImplementation() {
        assertThat(this.converter.fromImplementation(HungarianAttributedCharacterRepository.get())).isEqualTo(CharacterRepositoryTypeMessage.ATTRIBUTED);
        assertThat(this.converter.fromImplementation(HungarianSimpleCharacterRepository.get())).isEqualTo(CharacterRepositoryTypeMessage.SIMPLE);
        assertThat(this.converter.fromImplementation(new CustomCharacterRepository())).isEqualTo(CharacterRepositoryTypeMessage.SIMPLE);
    }

    @Test
    public void testConvertBack() {
        assertThat(this.converter.convertBack(CharacterRepositoryTypeMessage.ATTRIBUTED)).isEqualTo(CharacterRepositoryType.ATTRIBUTED);
        assertThat(this.converter.convertBack(CharacterRepositoryTypeMessage.SIMPLE)).isEqualTo(CharacterRepositoryType.SIMPLE);
    }

    @Test
    public void testToImplementation() {
        assertThat(this.converter.toImplementation(CharacterRepositoryTypeMessage.ATTRIBUTED)).isInstanceOf(HungarianAttributedCharacterRepository.class);
        assertThat(this.converter.toImplementation(CharacterRepositoryTypeMessage.SIMPLE)).isInstanceOf(HungarianSimpleCharacterRepository.class);
        assertThat(this.converter.toImplementation(null)).isInstanceOf(HungarianSimpleCharacterRepository.class);
    }

    private static class CustomCharacterRepository implements ICharacterRepository {

        @Override
        public ICharacter getCharacter(String letter) {
            return null;
        }

        @Override
        public String getLetter(ICharacter character) {
            return null;
        }

        @Override
        public ICharacter getCharacter(Set<? extends IAttribute> attributes) {
            return null;
        }

        @Override
        public String getLetter(Set<? extends IAttribute> attributes) {
            return null;
        }

        @Override
        public ICharacter getStartCharacter() {
            return null;
        }

        @Override
        public ICharacter getEndCharacter() {
            return null;
        }

        @Override
        public IAttributeStatistics getAttributeStatistics() {
            return null;
        }

        @Override
        public double calculateSimilarity(ICharacter character1, ICharacter character2) {
            return 0;
        }

    }

}
