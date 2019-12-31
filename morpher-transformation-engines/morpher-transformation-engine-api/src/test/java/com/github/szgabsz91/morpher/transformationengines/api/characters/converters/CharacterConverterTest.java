package com.github.szgabsz91.morpher.transformationengines.api.characters.converters;

import com.github.szgabsz91.morpher.transformationengines.api.characters.ICharacter;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.Consonant;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.Vowel;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.transformationengines.api.protocolbuffers.CharacterMessage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CharacterConverterTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    CharacterConverter characterConverter = new CharacterConverter();
                    characterConverter.setCharacterRepository(characterRepository);
                    return Arguments.of(
                            characterRepository,
                            characterConverter
                    );
                });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithRealLetter(ICharacterRepository characterRepository, CharacterConverter characterConverter) {
        ICharacter character = characterRepository.getCharacter("a");
        CharacterMessage characterMessage = characterConverter.convert(character);

        assertThat(characterMessage.getType()).isEmpty();
        assertThat(characterMessage.getAttributeList()).isEmpty();
        assertThat(characterMessage.getLetter()).isEqualTo(character.toString());

        ICharacter result = characterConverter.convertBack(characterMessage);
        assertThat(result).isEqualTo(character);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithNonRealLetter(ICharacterRepository characterRepository, CharacterConverter characterConverter) {
        ICharacter character = Vowel.create(Length.LONG);
        CharacterMessage characterMessage = characterConverter.convert(character);

        assertThat(characterMessage.getAttributeList()).hasSize(1);
        assertThat(characterMessage.getAttribute(0)).isEqualTo(Length.class.getName() + "." + Length.LONG);
        assertThat(characterMessage.getLetter()).isEmpty();

        ICharacter result = characterConverter.convertBack(characterMessage);
        assertThat(result).isEqualTo(character);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithConsonant(ICharacterRepository characterRepository, CharacterConverter characterConverter) {
        ICharacter character = Consonant.create();
        CharacterMessage characterMessage = characterConverter.convert(character);
        ICharacter result = characterConverter.convertBack(characterMessage);
        assertThat(result).isEqualTo(character);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithUnknownCharacterCategory(ICharacterRepository characterRepository, CharacterConverter characterConverter) {
        CharacterMessage characterMessage = CharacterMessage.getDefaultInstance();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> characterConverter.convertBack(characterMessage));
        assertThat(exception.getMessage()).isEqualTo("Unknown character type found: ");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithClassNotFoundExceptionFromAttributeCreation(ICharacterRepository characterRepository, CharacterConverter characterConverter) {
        CharacterMessage characterMessage = CharacterMessage.newBuilder()
                .setType(Vowel.class.getCanonicalName())
                .addAttribute("nonExistentType.nonExistentValue")
                .build();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> characterConverter.convertBack(characterMessage));
        assertThat(exception.getMessage()).isEqualTo("Attribute enum could not be instantiated");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testParse(ICharacterRepository characterRepository, CharacterConverter characterConverter) throws IOException {
        Path file = Paths.get("build/character.pb");
        CharacterMessage characterMessage = CharacterMessage.newBuilder()
                .setLetter("x")
                .build();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(Files.newOutputStream(file))) {
            characterMessage.writeTo(gzipOutputStream);
        }
        CharacterMessage result = characterConverter.parse(file);
        assertThat(result).isEqualTo(characterMessage);
        Files.delete(file);
    }

}
