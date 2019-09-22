package com.github.szgabsz91.morpher.methods.astra.impl;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.utils.Timer;
import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.ParallelSearcher;
import com.github.szgabsz91.morpher.methods.astra.impl.testutils.ASTRABuilder;
import com.github.szgabsz91.morpher.methods.astra.utils.ExcludeDuringBuild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@ExcludeDuringBuild
public class ASTRASortingTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ASTRASortingTest.class);
    private static final int RANDOM_PART_LENGTH = 5;
    private static final int INVERSION_PERCENT = 30;

    private IASTRA astra;
    private Random random;
    private List<? extends ICharacter> alphabet;

    private static IntStream parameters() {
        return IntStream.iterate(10, value -> value + 10)
                .limit(5L);
    }

    @BeforeEach
    public void setUp() {
        int minimalMatchingSegmentLength = 1;
        double fitnessThreshold = Double.MIN_VALUE;

        astra = new ASTRABuilder()
                .affixType(AffixType.of("AFF"))
                .minimalMatchingSegmentLength(minimalMatchingSegmentLength)
                .fitnessThreshold(fitnessThreshold)
                .searcher(new ParallelSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, true))
                .build();
        random = new Random();
        alphabet = createAlphabet(ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(int inputLength) {
        LOGGER.debug("Input length: {}", inputLength);
        Set<FrequencyAwareWordPair> wordPairs = generateTrainingWordPairs(alphabet, RANDOM_PART_LENGTH, random);
        this.astra.learnWordPairs(wordPairs);
        SortInput sortInput = generateRandomInput(alphabet, inputLength, INVERSION_PERCENT);
        Word input = sortInput.getWord();
        LOGGER.info("Input:    {}", input);
        Timer timer = new Timer();
        SortResult sortResult = timer.measure(() -> inflect(input, astra));
        Word output = sortResult.getOutput();
        int cycles = sortResult.getCycles();
        LOGGER.debug("Output:   {}", output);
        Word expected = calculateExpectedOutput(sortInput, alphabet);
        LOGGER.debug("Expected: {}", expected);
        LOGGER.debug("Inflection cycles: {}", cycles);
        LOGGER.debug("Sorting time: {} seconds", timer.getSeconds());
        assertThat(output).isEqualTo(expected);
    }

    private static List<? extends ICharacter> createAlphabet(ICharacterRepository characterRepository) {
        return Stream.of(
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
                "s", "t", "u", "v", "w", "x", "y", "z")
                .map(characterRepository::getCharacter)
                .collect(toList());
    }

    private static Set<FrequencyAwareWordPair> generateTrainingWordPairs(List<? extends ICharacter> alphabet, int randomPartLength, Random random) {
        Set<FrequencyAwareWordPair> wordPairs = new HashSet<>();

        for (int i = 0; i < alphabet.size() - 1; i++) {
            ICharacter leftCharacter = alphabet.get(i);

            for (int j = i + 1; j < alphabet.size(); j++) {
                ICharacter rightCharacter = alphabet.get(j);
                String randomPart = generateRandomPart(alphabet, randomPartLength, random, leftCharacter, rightCharacter);
                String leftWord = randomPart + rightCharacter.toString() + leftCharacter.toString() + randomPart;
                String rightWord = randomPart + leftCharacter.toString() + rightCharacter.toString() + randomPart;
                FrequencyAwareWordPair wordPair = FrequencyAwareWordPair.of(leftWord, rightWord, 1);
                wordPairs.add(wordPair);
            }
        }

        return wordPairs;
    }

    private static String generateRandomPart(List<? extends ICharacter> alphabet, int randomPartLength, Random random, ICharacter leftCharacter, ICharacter rightCharacter) {
        List<ICharacter> characters = new ArrayList<>();

        for (int i = 0; i < randomPartLength; i++) {
            ICharacter character;

            do {
                int characterIndex = random.nextInt(alphabet.size());
                character = alphabet.get(characterIndex);
            } while (character.equals(leftCharacter) || character.equals(rightCharacter));

            characters.add(character);
        }

        return characters
                .stream()
                .map(ICharacter::toString)
                .collect(joining());
    }

    private static SortInput generateRandomInput(List<? extends ICharacter> alphabet, int length, int inversionPercent) {
        int inversions = (int) Math.floor(length * (inversionPercent / 100.0));
        LOGGER.info("Inversions: {}", inversions);
        List<ICharacter> characters = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int characterIndex = random.nextInt(alphabet.size());
            ICharacter character = alphabet.get(characterIndex);
            characters.add(character);
        }

        Collections.sort(characters, comparing(Object::toString));
        int inversionsPerformed = 0;
        while (inversionsPerformed < inversions) {
            int index1 = random.nextInt(characters.size());
            int index2 = random.nextInt(characters.size());

            if (index1 == index2) {
                continue;
            }

            ICharacter character1 = characters.get(index1);
            ICharacter character2 = characters.get(index2);

            if (character1.toString().compareTo(character2.toString()) >= 0) {
                continue;
            }

            characters.set(index1, character2);
            characters.set(index2, character1);
            inversionsPerformed++;
        }

        return new SortInput(characters);
    }

    private static SortResult inflect(Word input, IASTRA astra) {
        Word previousResult = input;
        Word result = previousResult;
        int cycles = 0;

        while (true) {
            previousResult = result;

            cycles++;
            Optional<MethodResponse> response = astra.inflect(previousResult);

            if (!response.isPresent()) {
                break;
            }

            result = response.get().getResults().get(0).getWord();
        }

        return new SortResult(result, cycles);
    }

    private static Word calculateExpectedOutput(SortInput sortInput, List<? extends ICharacter> alphabet) {
        Map<ICharacter, Integer> counter = new HashMap<>();
        List<? extends ICharacter> characters = sortInput.getCharacters();
        for (int i = 0; i < characters.size(); i++) {
            ICharacter character = characters.get(i);
            Integer count = counter.get(character);
            if (count == null) {
                count = 0;
            }
            counter.put(character, count + 1);
        }

        StringBuilder expectedBuilder = new StringBuilder();
        for (ICharacter character : alphabet) {
            if (!counter.containsKey(character)) {
                continue;
            }

            int count = counter.get(character);
            String substring = generateString(character.toString(), count);
            expectedBuilder.append(substring);
        }

        return Word.of(expectedBuilder.toString());
    }

    private static String generateString(String string, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(string);
        }
        return sb.toString();
    }

    private static final class SortInput {

        private final List<? extends ICharacter> characters;

        private SortInput(List<? extends ICharacter> characters) {
            this.characters = characters;
        }

        public List<? extends ICharacter> getCharacters() {
            return characters;
        }

        public Word getWord() {
            String wordString = characters
                    .stream()
                    .map(ICharacter::toString)
                    .collect(joining());
            return Word.of(wordString);
        }

    }

    private static final class SortResult {

        private final Word output;
        private final int cycles;

        public SortResult(Word output, int cycles) {
            this.output = output;
            this.cycles = cycles;
        }

        public Word getOutput() {
            return output;
        }

        public int getCycles() {
            return cycles;
        }

    }

}
