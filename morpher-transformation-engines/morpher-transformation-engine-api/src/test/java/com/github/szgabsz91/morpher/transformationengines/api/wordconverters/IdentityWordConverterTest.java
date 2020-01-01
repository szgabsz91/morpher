package com.github.szgabsz91.morpher.transformationengines.api.wordconverters;

import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.characters.ICharacter;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IdentityWordConverterTest {

    private IWordConverter wordConverter;

    @BeforeEach
    public void setUp() {
        this.wordConverter = new IdentityWordConverter();
    }

    @Test
    public void testConvertWithString() {
        String expected = "abc";
        String result = this.wordConverter.convert(expected);
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void testConvertWithWord() {
        Word expected = Word.of("abc");
        Word result = this.wordConverter.convert(expected);
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void testConvertWithWordAndCharacterRepository() {
        Word input = Word.of("abc");
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        List<ICharacter> result = this.wordConverter.convert(input, characterRepository);
        List<ICharacter> expected = List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testConvertWithWordPair() {
        WordPair expected = WordPair.of("abc", "abc");
        WordPair result = this.wordConverter.convert(expected);
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void testConvertWithFrequencyAwareWordPair() {
        FrequencyAwareWordPair expected = FrequencyAwareWordPair.of("abc", "abc", 2);
        FrequencyAwareWordPair result = this.wordConverter.convert(expected);
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void testConvertBackWithString() {
        String expected = "abc";
        String result = this.wordConverter.convertBack(expected);
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void testConvertBackWithWord() {
        Word expected = Word.of("abc");
        Word result = this.wordConverter.convertBack(expected);
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void testConvertBackWithCharacterListAndCharacterRepository() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        List<ICharacter> input = List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        Word result = this.wordConverter.convertBack(input, characterRepository);
        Word expected = Word.of("abc");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testConvertBackWithWordPair() {
        WordPair expected = WordPair.of("abc", "abc");
        WordPair result = this.wordConverter.convertBack(expected);
        assertThat(result).isSameAs(expected);
    }

}
