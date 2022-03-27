package com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.characters.ICharacter;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.Consonant;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.EndSound;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.StartSound;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.Vowel;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.consonant.Discriminator;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.consonant.SoundProductionPlace;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.consonant.SoundProductionWay;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.consonant.UvulaPosition;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.consonant.Voice;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.HorizontalTonguePosition;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.LipShape;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.VerticalTonguePosition;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IWordConverter;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.AttributeDelta;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.ITransformation;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Removal;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Replacement;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.IWordPairProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.WordPairProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class RuleTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    IWordConverter wordConverter = new DoubleConsonantWordConverter();
                    ICostCalculator costCalculator = new AttributeBasedCostCalculator();
                    IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                            .characterRepository(characterRepository)
                            .wordConverter(wordConverter)
                            .costCalculator(costCalculator)
                            .maximalContextSize(3)
                            .build();
                    return Arguments.of(
                            characterRepository,
                            wordConverter,
                            wordPairProcessor
                    );
                });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConstructor(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Context context = new Context(List.of(), List.of(), List.of(), Position.identity(), Position.identity());
        List<ITransformation> transformations = List.of(new Addition(Set.of(), characterRepository));
        Rule rule = new Rule(context, transformations, characterRepository, null);
        assertThat(rule.getContext()).isEqualTo(context);
        assertThat(rule.getTransformations()).isEqualTo(transformations);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testSetTransformations(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        List<ITransformation> transformations = List.of(new Addition(Set.of(), characterRepository));
        Rule rule = new Rule(null, null, characterRepository, null);
        rule.setTransformations(transformations);
        assertThat(rule.getTransformations()).isEqualTo(transformations);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesWithWithIdentityRule(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule identity = Rule.identity();
        assertThat(identity.matches((List<ICharacter>) null)).isFalse();
        assertThat(identity.matches((Word) null)).isFalse();
        assertThat(identity.matchesFromFront(null)).isFalse();
        assertThat(identity.matchesFromBack(null)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesFromFrontWithNoStartSoundInPrefix(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        Context context = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                Position.identity()
        );
        Rule rule = new Rule(context, null, characterRepository, null);
        boolean result = rule.matchesFromFront(characters);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesFromFrontWithStartSoundInPrefix(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        Context context = new Context(
                List.of(StartSound.get(), characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                Position.identity()
        );
        Rule rule = new Rule(context, null, characterRepository, null);
        boolean result = rule.matchesFromFront(characters);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesFromFrontWithStartSoundInPrefixAndSounds(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        List<ICharacter> characters = List.of(
                StartSound.get(),
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        Context context = new Context(
                List.of(StartSound.get(), characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                Position.identity()
        );
        Rule rule = new Rule(context, null, characterRepository, null);
        boolean result = rule.matchesFromFront(characters);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesFromFrontWithStartIndexBelowZero(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        List<ICharacter> characters = List.of(
                StartSound.get(),
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        Context context = new Context(
                List.of(characterRepository.getCharacter("d")),
                List.of(characterRepository.getCharacter("e")),
                List.of(characterRepository.getCharacter("f")),
                Position.identity(),
                Position.identity()
        );
        Rule rule = new Rule(context, null, characterRepository, null);
        boolean result = rule.matchesFromFront(characters);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesFromFrontWithLongerContextThanWord(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("b")
        );
        Context context = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                Position.identity()
        );
        Rule rule = new Rule(context, null, characterRepository, null);
        boolean result = rule.matchesFromFront(characters);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesFromBackWithStartIndexBelowZero(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        List<ICharacter> characters = List.of(
                StartSound.get(),
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        Context context = new Context(
                List.of(characterRepository.getCharacter("d")),
                List.of(characterRepository.getCharacter("e")),
                List.of(characterRepository.getCharacter("f")),
                Position.identity(),
                Position.identity()
        );
        Rule rule = new Rule(context, null, characterRepository, null);
        boolean result = rule.matchesFromBack(characters);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesFromBackWithLongerContextThanWordAndNoEndSound(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        Context context = new Context(
                List.of(
                        StartSound.get(),
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("b"),
                        characterRepository.getCharacter("c")
                ),
                List.of(),
                List.of(characterRepository.getCharacter("d")),
                Position.identity(),
                Position.identity()
        );
        Rule rule = new Rule(context, null, characterRepository, null);
        boolean result = rule.matchesFromBack(characters);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesFromBackWithLongerContextButEmptyPostfix(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        Context context = new Context(
                List.of(
                        StartSound.get(),
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("b"),
                        characterRepository.getCharacter("c")
                ),
                List.of(characterRepository.getCharacter("d")),
                List.of(),
                Position.identity(),
                Position.identity()
        );
        Rule rule = new Rule(context, null, characterRepository, null);
        boolean result = rule.matchesFromBack(characters);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesFromBackWithFailingSubsetCheck(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        Context context = new Context(
                List.of(
                        StartSound.get(),
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("b")
                ),
                List.of(characterRepository.getCharacter("d")),
                List.of(),
                Position.identity(),
                Position.identity()
        );
        Rule rule = new Rule(context, null, characterRepository, null);
        boolean result = rule.matchesFromBack(characters);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesFromBackWithLongerContextThanWord(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("b")
        );
        Context context = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                Position.identity()
        );
        Rule rule = new Rule(context, null, characterRepository, null);
        boolean result = rule.matchesFromBack(characters);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesWithNullFrontPosition(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        Context context = new Context(
                List.of(
                        StartSound.get(),
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("b"),
                        characterRepository.getCharacter("c")
                ),
                List.of(),
                List.of(EndSound.get()),
                null,
                Position.identity()
        );
        Rule rule = new Rule(context, null, characterRepository, null);
        boolean result = rule.matches(characters);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesWithNullBackPosition(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        Context context = new Context(
                List.of(
                        StartSound.get(),
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("b"),
                        characterRepository.getCharacter("c")
                ),
                List.of(),
                List.of(EndSound.get()),
                Position.identity(),
                null
        );
        Rule rule = new Rule(context, null, characterRepository, null);
        boolean result = rule.matches(characters);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesWithAWordAndNullFrontPosition(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Word word = Word.of("abc");
        Context context = new Context(
                List.of(
                        StartSound.get(),
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("b"),
                        characterRepository.getCharacter("c")
                ),
                List.of(),
                List.of(EndSound.get()),
                null,
                Position.identity()
        );
        Rule rule = new Rule(context, null, characterRepository, wordConverter);
        boolean result = rule.matches(word);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesWithAWordAndNullBackPosition(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Word word = Word.of("abc");
        Context context = new Context(
                List.of(
                        StartSound.get(),
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("b"),
                        characterRepository.getCharacter("c")
                ),
                List.of(),
                List.of(EndSound.get()),
                Position.identity(),
                null
        );
        Rule rule = new Rule(context, null, characterRepository, wordConverter);
        boolean result = rule.matches(word);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testTransformWithIdentityRule(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule identity = Rule.identity();
        Word input = Word.of("abc");
        assertThat(identity.transform(input)).isEqualTo(input);
        assertThat(identity.transformFromFront(input)).isEqualTo(input);
        assertThat(identity.transformFromBack(input)).isEqualTo(input);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testTransformWithInconsistentRule(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Context context = new Context(List.of(), List.of(), List.of(), null, null);
        Rule rule = new Rule(context, null, characterRepository, null);
        Word input = Word.of("abc");
        Word result = rule.transform(input);
        assertThat(result).isEqualTo(input);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testTransformFromFrontWithInconsistentRule(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Context context = new Context(List.of(), List.of(), List.of(), null, null);
        Rule rule = new Rule(context, null, characterRepository, null);
        Word input = Word.of("abc");
        Word result = rule.transformFromFront(input);
        assertThat(result).isEqualTo(input);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testTransformFromBackWithInconsistentRule(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Context context = new Context(List.of(), List.of(), List.of(), null, null);
        Rule rule = new Rule(context, null, characterRepository, null);
        Word input = Word.of("abc");
        Word result = rule.transformFromBack(input);
        assertThat(result).isEqualTo(input);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testTransformWithNullFrontPosition(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule rule = new Rule(
                new Context(
                        List.of(characterRepository.getCharacter("a")),
                        List.of(characterRepository.getCharacter("b")),
                        List.of(characterRepository.getCharacter("c")),
                        null,
                        Position.identity()
                ),
                List.of(
                        new Replacement(
                                characterRepository.getCharacter("b"),
                                characterRepository.getCharacter("x"),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        );
        Word input = Word.of("abc");
        Word expected = Word.of("axc");
        Word result = rule.transform(input);
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testTransformWithNullBackPosition(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule rule = new Rule(
                new Context(
                        List.of(characterRepository.getCharacter("a")),
                        List.of(characterRepository.getCharacter("b")),
                        List.of(characterRepository.getCharacter("c")),
                        Position.identity(),
                        null
                ),
                List.of(
                        new Replacement(
                                characterRepository.getCharacter("b"),
                                characterRepository.getCharacter("x"),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        );
        Word input = Word.of("abc");
        Word expected = Word.of("axc");
        Word result = rule.transform(input);
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testTransformWithNoEndCharacterInPostfix(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule rule = new Rule(
                new Context(
                        List.of(characterRepository.getCharacter("a")),
                        List.of(characterRepository.getCharacter("b")),
                        List.of(characterRepository.getCharacter("c")),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Replacement(
                                characterRepository.getCharacter("b"),
                                characterRepository.getCharacter("x"),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        );
        Word input = Word.of("abc");
        Word expected = Word.of("axc");
        Word result = rule.transform(input);
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testTransformWithEndCharacterInPostfix(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule rule = new Rule(
                new Context(
                        List.of(characterRepository.getCharacter("a")),
                        List.of(characterRepository.getCharacter("b")),
                        List.of(
                                characterRepository.getCharacter("c"),
                                EndSound.get()
                        ),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Replacement(
                                characterRepository.getCharacter("b"),
                                characterRepository.getCharacter("x"),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        );
        Word input = Word.of("abc");
        Word expected = Word.of("axc");
        Word result = rule.transform(input);
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testTransformWithEmptyPostfix(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule rule = new Rule(
                new Context(
                        List.of(characterRepository.getCharacter("a")),
                        List.of(characterRepository.getCharacter("b")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Replacement(
                                characterRepository.getCharacter("b"),
                                characterRepository.getCharacter("x"),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        );
        Word input = Word.of("abc");
        Word expected = Word.of("axc");
        Word result = rule.transform(input);
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testEquals(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule rule1 = new Rule(
                new Context(
                        List.of(),
                        List.of(),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(),
                characterRepository,
                null
        );
        Rule rule2 = new Rule(
                new Context(
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(),
                characterRepository,
                null
        );
        Rule rule3 = new Rule(
                new Context(
                        List.of(),
                        List.of(),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(Set.of(), characterRepository)
                ),
                characterRepository,
                null
        );
        Rule rule4 = new Rule(
                new Context(
                        List.of(),
                        List.of(),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        );

        assertThat(rule1.equals(rule1)).isTrue();
        assertThat(rule1.equals(null)).isFalse();
        assertThat(rule1).isNotEqualTo("string");
        assertThat(rule1).isNotEqualTo(rule2);
        assertThat(rule1).isNotEqualTo(rule3);
        assertThat(rule1).isNotEqualTo(rule4);
        assertThat(rule4).isNotEqualTo(rule1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHashCode(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule rule = new Rule(
                new Context(
                        List.of(),
                        List.of(),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(),
                characterRepository,
                null
        );

        int expected = 31 * rule.getContext().hashCode() + rule.getTransformations().hashCode();

        assertThat(rule.hashCode()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testToString(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule rule = new Rule(
                new Context(
                        List.of(),
                        List.of(),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(),
                characterRepository,
                null
        );
        String expected = "Rule[" + rule.getContext() + ", " + rule.getTransformations() + "]";
        assertThat(rule).hasToString(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesRegression1(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        WordPair wordPair = WordPair.of("séf", "séfet");
        Rule rule = wordPairProcessor.induceRules(wordPair).getRules().get(0);
        List<ICharacter> word = "megrohanás"
                .chars()
                .mapToObj(character -> String.valueOf((char) character))
                .map(characterRepository::getCharacter)
                .toList();
        boolean matches = rule.matches(word);
        boolean matchesFromFront = rule.matchesFromFront(word);
        boolean matchesFromBack = rule.matchesFromBack(word);
        assertThat(matches).isFalse();
        assertThat(matchesFromFront).isFalse();
        assertThat(matchesFromBack).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfRegression1(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Rule rule1 = new Rule(
                new Context(
                        new LinkedList<>(),
                        List.of(
                                Consonant.create(UvulaPosition.ORAL, Discriminator.NORMAL),
                                Vowel.create(VerticalTonguePosition.SEMI_OPEN, Length.SHORT)
                        ),
                        List.of(
                                EndSound.get()
                        ),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Replacement(
                                new HashSet(List.of(
                                        new AttributeDelta<>(VerticalTonguePosition.class, VerticalTonguePosition.SEMI_OPEN, VerticalTonguePosition.MIDDLE),
                                        new AttributeDelta<>(Length.class, Length.SHORT, Length.LONG)
                                )),
                                characterRepository
                        ),
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Rule rule2 = new Rule(
                new Context(
                        new LinkedList<>(),
                        List.of(
                                Consonant.create(UvulaPosition.ORAL, Discriminator.NORMAL),
                                Vowel.create(VerticalTonguePosition.SEMI_OPEN, Length.SHORT)
                        ),
                        List.of(
                                EndSound.get()
                        ),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Replacement(
                                new HashSet(List.of(
                                        new AttributeDelta<>(VerticalTonguePosition.class, VerticalTonguePosition.SEMI_OPEN, VerticalTonguePosition.OPEN),
                                        new AttributeDelta<>(Length.class, Length.SHORT, Length.LONG)
                                )),
                                characterRepository
                        ),
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );

        assertThat(rule1).isNotEqualTo(rule2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIdentity(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule rule = Rule.identity();
        Word word = Word.of("abc123");
        Word transformedWord = rule.transform(word);
        assertThat(transformedWord).isEqualTo(word);
        assertThat(rule.getContext()).isEqualTo(Context.identity());
        assertThat(rule.getTransformations()).isEmpty();
        assertThat(rule.isIdentity()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsIdentityWithNonIdentityContext(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule rule = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(),
                characterRepository,
                null
        );
        assertThat(rule.isIdentity()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsIdentityWithNonEmptyTransformationList(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule rule = new Rule(
                Context.identity(),
                List.of(
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("a").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        assertThat(rule.isIdentity()).isFalse();
    }

    @Test
    public void testMatchesWithMiddleChangeRegression1() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        Rule rule = new Rule(
                new Context(
                        List.of(
                                StartSound.get(),
                                Consonant.create(UvulaPosition.ORAL, Discriminator.NORMAL, SoundProductionPlace.DENTAL_ALVEOLAR, Voice.UNVOICED),
                                characterRepository.getCharacter("i"),
                                Consonant.create(UvulaPosition.ORAL, SoundProductionWay.PLOSIVE, Discriminator.NORMAL, Voice.UNVOICED),
                                Consonant.create(UvulaPosition.ORAL, Discriminator.NORMAL),
                                Vowel.create(LipShape.ROUNDED, HorizontalTonguePosition.BACK),
                                Consonant.create(UvulaPosition.ORAL, Discriminator.NORMAL, SoundProductionPlace.DENTAL_ALVEOLAR, Voice.VOICED)
                        ),
                        List.of(),
                        List.of(
                                Consonant.create(UvulaPosition.ORAL, SoundProductionWay.PLOSIVE, Voice.UNVOICED)
                        ),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("a").getAttributes()),
                                characterRepository
                        ),
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        );
        Word input = Word.of("cipzárkzq");
        boolean result = rule.matches(input);
        assertThat(result).isTrue();
    }

    @Test
    public void testTransformRegression1() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        Rule rule = new Rule(
                new Context(
                        List.of(
                                characterRepository.getCharacter("l"),
                                characterRepository.getCharacter("a"),
                                characterRepository.getCharacter("p")
                        ),
                        List.of(
                                characterRepository.getCharacter("o"),
                                characterRepository.getCharacter("c"),
                                characterRepository.getCharacter("s"),
                                characterRepository.getCharacter("k"),
                                characterRepository.getCharacter("á"),
                                characterRepository.getCharacter("k")
                        ),
                        List.of(
                                characterRepository.getEndCharacter()
                        ),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Replacement(
                                characterRepository.getCharacter("o"),
                                characterRepository.getCharacter("k"),
                                characterRepository
                        ),
                        new Replacement(
                                characterRepository.getCharacter("c"),
                                characterRepository.getCharacter("á"),
                                characterRepository
                        ),
                        new Removal(
                                new HashSet<>(characterRepository.getCharacter("s").getAttributes()),
                                characterRepository
                        ),
                        new Replacement(
                                Set.of(),
                                characterRepository
                        ),
                        new Replacement(
                                characterRepository.getCharacter("á"),
                                characterRepository.getCharacter("a"),
                                characterRepository
                        ),
                        new Replacement(
                                characterRepository.getCharacter("k"),
                                characterRepository.getCharacter("t"),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        );
        Word word = Word.of("álcák");
        Word expected = Word.of("álcák");
        Word result = rule.transform(word);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testTransformRegression2() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        Rule rule = new Rule(
                new Context(
                        List.of(),
                        List.of(
                                Vowel.create(VerticalTonguePosition.SEMI_OPEN),
                                characterRepository.getCharacter("t")
                        ),
                        List.of(
                                characterRepository.getCharacter("x"),
                                characterRepository.getCharacter("x"),
                                characterRepository.getCharacter("x")
                        ),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Removal(
                                new HashSet<>(characterRepository.getCharacter("a").getAttributes()),
                                characterRepository
                        ),
                        new Removal(
                                new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        );
        Word word = Word.of("hűtőszekrényétxxx");
        Word expected = Word.of("hűtőszekrényétxxx");
        Word result = rule.transform(word);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testTransformRegression3() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Rule rule = new Rule(
                new Context(
                        List.of(),
                        List.of(
                                characterRepository.getCharacter("á"),
                                characterRepository.getCharacter("t")
                        ),
                        List.of(
                                characterRepository.getCharacter("x"),
                                characterRepository.getCharacter("x"),
                                characterRepository.getCharacter("x")
                        ),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Replacement(
                                Set.of(new AttributeDelta(Length.class, Length.LONG, Length.SHORT)),
                                characterRepository
                        ),
                        new Removal(
                                new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        );
        Word word = Word.of("hűtőszekrényétxxx");
        Word expected = Word.of("hűtőszekrényexxx");
        Word result = rule.transform(word);
        assertThat(result).isEqualTo(expected);
    }

}
