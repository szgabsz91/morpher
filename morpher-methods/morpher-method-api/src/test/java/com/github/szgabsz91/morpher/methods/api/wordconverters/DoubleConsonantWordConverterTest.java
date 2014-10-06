package com.github.szgabsz91.morpher.methods.api.wordconverters;

import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DoubleConsonantWordConverterTest {

    private static final String ORIGINAL = "acsacsadzadzadzsadzsagyagyalyalyanyanyaszaszatyatyazsazsa";
    private static final String CONVERTED = "aCaCaDaDaBaBaGaGaLaLaNaNaSaSaTaTaZaZa";

    private IWordConverter wordConverter;

    @BeforeEach
    public void setUp() {
        this.wordConverter = new DoubleConsonantWordConverter();
    }

    @Test
    public void testConvertWithString() {
        String input = ORIGINAL;
        String expected = CONVERTED;
        String result = this.wordConverter.convert(input);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testConvertWithWord() {
        Word input = Word.of(ORIGINAL);
        Word expected = Word.of(CONVERTED);
        Word result = this.wordConverter.convert(input);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testConvertWithWordAndCharacterRepository() {
        Word input = Word.of("csdzdzsgylynysztyzs");
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        List<ICharacter> result = this.wordConverter.convert(input, characterRepository);
        List<ICharacter> expected = List.of(
                characterRepository.getCharacter("cs"),
                characterRepository.getCharacter("dz"),
                characterRepository.getCharacter("dzs"),
                characterRepository.getCharacter("gy"),
                characterRepository.getCharacter("ly"),
                characterRepository.getCharacter("ny"),
                characterRepository.getCharacter("sz"),
                characterRepository.getCharacter("ty"),
                characterRepository.getCharacter("zs")
        );
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testConvertWithWordPair() {
        WordPair input = WordPair.of(ORIGINAL, ORIGINAL);
        WordPair expected = WordPair.of(CONVERTED, CONVERTED);
        WordPair result = this.wordConverter.convert(input);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testConvertWithFrequencyAwareWordPair() {
        FrequencyAwareWordPair input = FrequencyAwareWordPair.of(ORIGINAL, ORIGINAL, 2);
        FrequencyAwareWordPair expected = FrequencyAwareWordPair.of(CONVERTED, CONVERTED, 2);
        FrequencyAwareWordPair result = this.wordConverter.convert(input);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testConvertBackWithString() {
        String input = CONVERTED;
        String expected = ORIGINAL;
        String result = this.wordConverter.convertBack(input);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testConvertBackWithWord() {
        Word input = Word.of(CONVERTED);
        Word expected = Word.of(ORIGINAL);
        Word result = this.wordConverter.convertBack(input);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testConvertBackWithCharacterListAndCharacterRepository() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        List<ICharacter> input = List.of(
                characterRepository.getCharacter("cs"),
                characterRepository.getCharacter("dz"),
                characterRepository.getCharacter("dzs"),
                characterRepository.getCharacter("gy"),
                characterRepository.getCharacter("ly"),
                characterRepository.getCharacter("ny"),
                characterRepository.getCharacter("sz"),
                characterRepository.getCharacter("ty"),
                characterRepository.getCharacter("zs")
        );
        Word result = this.wordConverter.convertBack(input, characterRepository);
        Word expected = Word.of("csdzdzsgylynysztyzs");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testConvertBackWithWordPair() {
        WordPair input = WordPair.of(CONVERTED, CONVERTED);
        WordPair expected = WordPair.of(ORIGINAL, ORIGINAL);
        WordPair result = this.wordConverter.convertBack(input);
        assertThat(result).isEqualTo(expected);
    }

}
