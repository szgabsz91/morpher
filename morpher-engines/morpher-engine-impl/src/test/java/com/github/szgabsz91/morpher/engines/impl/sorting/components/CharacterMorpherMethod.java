package com.github.szgabsz91.morpher.engines.impl.sorting.components;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.engines.impl.sorting.model.RandomizableCharacter;
import com.github.szgabsz91.morpher.methods.api.IMorpherMethod;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessageV3;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class CharacterMorpherMethod implements IMorpherMethod<GeneratedMessageV3> {

    public static final List<String> ALPHABET;

    private final String character;
    private final Random random;

    static {
        ALPHABET = List.of(
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
                "s", "t", "u", "v", "w", "x", "y", "z"
        );
    }

    public CharacterMorpherMethod(String character, Random random) {
        this.character = character;
        this.random = random;
    }

    @Override
    public AffixType getAffixType() {
        return null;
    }

    @Override
    public void learn(TrainingSet trainingSet) {

    }

    @Override
    public Optional<MethodResponse> inflect(Word rootForm) {
        String rootFormString = rootForm.toString();
        List<RandomizableCharacter> randomizableCharacters = parse(rootFormString);
        boolean alreadyRandomized = randomizableCharacters
                .stream()
                .filter(c -> c.getCharacter().equals(this.character))
                .anyMatch(RandomizableCharacter::isRandomized);

        if (alreadyRandomized) {
            return Optional.empty();
        }

        int occurrences = StringUtils.countMatches(rootFormString, this.character);
        List<RandomizableCharacter> strippedRandomizableCharacters = randomizableCharacters
                .stream()
                .filter(c -> !c.getCharacter().equals(this.character))
                .collect(toList());
        for (int i = 0; i < occurrences; i++) {
            int randomIndex = random.nextInt(strippedRandomizableCharacters.size() + 1);
            strippedRandomizableCharacters.add(randomIndex, new RandomizableCharacter(this.character, true));
        }
        String randomizedString = strippedRandomizableCharacters
                .stream()
                .map(RandomizableCharacter::toString)
                .collect(joining());
        Word randomizedWord = Word.of(randomizedString);
        return Optional.of(MethodResponse.singleton(randomizedWord));
    }

    @Override
    public Optional<MethodResponse> lemmatize(Word inflectedForm) {
        String inflectedString = inflectedForm.toString();
        final List<RandomizableCharacter> randomizableCharacters = parse(inflectedString);

        boolean characterRandomized = randomizableCharacters
                .stream()
                .filter(c -> c.getCharacter().equals(this.character))
                .anyMatch(RandomizableCharacter::isRandomized);
        if (!characterRandomized) {
            return Optional.empty();
        }

        int characterIndex = ALPHABET.indexOf(this.character);
        boolean nextCharactersAreInPlace = IntStream.range(characterIndex + 1, ALPHABET.size())
                .mapToObj(ALPHABET::get)
                .flatMap(character -> {
                    return randomizableCharacters
                            .stream()
                            .filter(c -> c.getCharacter().equals(character));
                })
                .noneMatch(RandomizableCharacter::isRandomized);
        if (!nextCharactersAreInPlace) {
            return Optional.empty();
        }

        int count = (int) randomizableCharacters
                .stream()
                .filter(c -> c.getCharacter().equals(this.character))
                .count();
        List<RandomizableCharacter> transformableRandomizableCharacters = randomizableCharacters
                .stream()
                .filter(c -> !c.getCharacter().equals(this.character))
                .collect(toList());
        int startIndex = -1;
        for (int i = 0; i < transformableRandomizableCharacters.size(); i++) {
            RandomizableCharacter randomizableCharacter = transformableRandomizableCharacters.get(i);
            String character = randomizableCharacter.getCharacter();
            int index = ALPHABET.indexOf(character);
            boolean randomized = randomizableCharacter.isRandomized();

            if (!randomized && index >= characterIndex) {
                startIndex = i;
                break;
            }
        }
        for (int i = 0; i < count; i++) {
            RandomizableCharacter randomizableCharacter = new RandomizableCharacter(this.character, false);
            if (startIndex > -1) {
                transformableRandomizableCharacters.add(startIndex, randomizableCharacter);
            }
            else {
                transformableRandomizableCharacters.add(randomizableCharacter);
            }
        }

        String resultingString = transformableRandomizableCharacters
                .stream()
                .map(RandomizableCharacter::toString)
                .collect(joining());
        Word resultingWord = Word.of(resultingString);
        MethodResponse methodResponse = MethodResponse.singleton(resultingWord);
        return Optional.of(methodResponse);
    }

    @Override
    public GeneratedMessageV3 toMessage() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void fromMessage(GeneratedMessageV3 generatedMessageV3) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void fromMessage(Any message) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void saveTo(Path file) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void loadFrom(Path file) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported");
    }

    private static List<RandomizableCharacter> parse(String word) {
        return word
                .chars()
                .mapToObj(character -> Character.toString((char) character))
                .map(RandomizableCharacter::parse)
                .collect(toList());
    }

}
