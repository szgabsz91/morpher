package com.github.szgabsz91.morpher.methods.lattice.converters;

import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IdentityWordConverter;
import com.github.szgabsz91.morpher.methods.lattice.config.WordConverterType;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.WordConverterTypeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WordConverterTypeConverterTest {

    private WordConverterTypeConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new WordConverterTypeConverter();
    }

    @Test
    public void testConvert() {
        assertThat(this.converter.convert(WordConverterType.DOUBLE_CONSONANT)).isEqualTo(WordConverterTypeMessage.DOUBLE_CONSONANT);
        assertThat(this.converter.convert(WordConverterType.IDENTITY)).isEqualTo(WordConverterTypeMessage.IDENTITY);
    }

    @Test
    public void testFromImplementation() {
        assertThat(this.converter.fromImplementation(new DoubleConsonantWordConverter())).isEqualTo(WordConverterTypeMessage.DOUBLE_CONSONANT);
        assertThat(this.converter.fromImplementation(new IdentityWordConverter())).isEqualTo(WordConverterTypeMessage.IDENTITY);
        assertThat(this.converter.fromImplementation(new CustomWordConverter())).isEqualTo(WordConverterTypeMessage.IDENTITY);
    }

    @Test
    public void testConvertBack() {
        assertThat(this.converter.convertBack(WordConverterTypeMessage.DOUBLE_CONSONANT)).isEqualTo(WordConverterType.DOUBLE_CONSONANT);
        assertThat(this.converter.convertBack(WordConverterTypeMessage.IDENTITY)).isEqualTo(WordConverterType.IDENTITY);
    }

    @Test
    public void testToImplementation() {
        assertThat(this.converter.toImplementation(WordConverterTypeMessage.DOUBLE_CONSONANT)).isInstanceOf(DoubleConsonantWordConverter.class);
        assertThat(this.converter.toImplementation(WordConverterTypeMessage.IDENTITY)).isInstanceOf(IdentityWordConverter.class);
    }

    private static class CustomWordConverter implements IWordConverter {

        @Override
        public String convert(String string) {
            return null;
        }

        @Override
        public Word convert(Word word) {
            return null;
        }

        @Override
        public List<ICharacter> convert(Word word, ICharacterRepository characterRepository) {
            return null;
        }

        @Override
        public WordPair convert(WordPair wordPair) {
            return null;
        }

        @Override
        public FrequencyAwareWordPair convert(FrequencyAwareWordPair wordPair) {
            return null;
        }

        @Override
        public String convertBack(String string) {
            return null;
        }

        @Override
        public Word convertBack(Word word) {
            return null;
        }

        @Override
        public Word convertBack(List<ICharacter> characters, ICharacterRepository characterRepository) {
            return null;
        }

        @Override
        public WordPair convertBack(WordPair wordPair) {
            return null;
        }
        
    }
    
}
