package com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor;

import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.Discriminator;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.SoundProductionPlace;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.SoundProductionWay;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.UvulaPosition;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.Voice;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.HorizontalTonguePosition;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.LipShape;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.VerticalTonguePosition;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.ITransformation;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Replacement;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.model.WordPairProcessorResponse;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.tree.WordPairProcessorTreeNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WordPairProcessorTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    IWordConverter wordConverter = new DoubleConsonantWordConverter();
                    ICostCalculator costCalculator = new AttributeBasedCostCalculator();
                    IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                            .characterRepository(characterRepository)
                            .wordConverter(wordConverter)
                            .costCalculator(costCalculator)
                            .build();
                    IWordPairProcessor cutWordPairProcessor = new WordPairProcessor.Builder()
                            .characterRepository(characterRepository)
                            .wordConverter(wordConverter)
                            .costCalculator(costCalculator)
                            .maximalContextSize(3)
                            .build();
                    return Arguments.of(
                            characterRepository,
                            wordPairProcessor,
                            cutWordPairProcessor
                    );
                });
    }

    @Test
    public void testConstructorAndGetters() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        int pathLevelsToProcess = 2;
        int maximalContextSize = 5;
        WordPairProcessor wordPairProcessor = (WordPairProcessor) new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .pathLevelsToProcess(pathLevelsToProcess)
                .maximalContextSize(maximalContextSize)
                .build();
        assertThat(wordPairProcessor.getCharacterRepository()).hasSameClassAs(characterRepository);
        assertThat(wordPairProcessor.getWordConverter()).hasSameClassAs(wordConverter);
        assertThat(wordPairProcessor.getCostCalculator()).hasSameClassAs(costCalculator);
        assertThat(wordPairProcessor.getPathLevelsToProcess()).isEqualTo(pathLevelsToProcess);
        assertThat(wordPairProcessor.getMaximalContextSize()).isEqualTo(maximalContextSize);
    }

    @Test
    public void testWithAlma() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .build();
        WordPair wordPair = WordPair.of("alma", "almát");
        WordPairProcessorResponse response = wordPairProcessor.induceRules(wordPair);
        Rule rule = response.getRules().get(0);

        @SuppressWarnings("unchecked")
        List<ITransformation> transformations = List.of(
                new Replacement(
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("á"),
                        characterRepository
                ),
                new Addition(
                        Set.of(
                                SoundProductionWay.PLOSIVE,
                                Discriminator.NORMAL,
                                SoundProductionPlace.DENTAL_ALVEOLAR,
                                UvulaPosition.ORAL,
                                Voice.UNVOICED
                        ),
                        characterRepository
                )
        );

        assertRule(
                rule,
                "$alm",
                "a",
                "#",
                Position.identity(),
                Position.identity(),
                transformations,
                characterRepository
        );

        assertThat(rule.transform(wordPair.getLeftWord()))
                .withFailMessage("Failed for transform()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromFront(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromFront()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromBack(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromBack()")
                .isEqualTo(wordPair.getRightWord());
    }

    @Test
    public void testWithAlma2() {
        WordPair wordPair = WordPair.of("alma", "almát");
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        IWordPairProcessor cutWordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .maximalContextSize(3)
                .build();
        WordPairProcessorResponse response = cutWordPairProcessor.induceRules(wordPair);
        Rule rule = response.getRules().get(0);

        @SuppressWarnings("unchecked")
        List<ITransformation> transformations = List.of(
                new Replacement(
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("á"),
                        characterRepository
                ),
                new Addition(
                        Set.of(
                                SoundProductionWay.PLOSIVE,
                                Discriminator.NORMAL,
                                SoundProductionPlace.DENTAL_ALVEOLAR,
                                UvulaPosition.ORAL,
                                Voice.UNVOICED
                        ),
                        characterRepository
                )
        );

        assertRule(
                rule,
                "alm",
                "a",
                "#",
                Position.identity(),
                Position.identity(),
                transformations,
                characterRepository
        );

        assertThat(rule.transform(wordPair.getLeftWord()))
                .withFailMessage("Failed for transform()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromFront(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromFront()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromBack(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromBack()")
                .isEqualTo(wordPair.getRightWord());
    }

    @Test
    public void testWithMalma() {
        WordPair wordPair = WordPair.of("malma", "malmát");
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        IWordPairProcessor cutWordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .maximalContextSize(3)
                .build();
        WordPairProcessorResponse response = cutWordPairProcessor.induceRules(wordPair);
        Rule rule = response.getRules().get(0);

        @SuppressWarnings("unchecked")
        List<ITransformation> transformations = List.of(
                new Replacement(
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("á"),
                        characterRepository
                ),
                new Addition(
                        Set.of(
                                SoundProductionWay.PLOSIVE,
                                Discriminator.NORMAL,
                                SoundProductionPlace.DENTAL_ALVEOLAR,
                                UvulaPosition.ORAL,
                                Voice.UNVOICED
                        ),
                        characterRepository
                )
        );

        assertRule(
                rule,
                "alm",
                "a",
                "#",
                Position.identity(),
                Position.identity(),
                transformations,
                characterRepository
        );

        assertThat(rule.transform(wordPair.getLeftWord()))
                .withFailMessage("Failed for transform()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromFront(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromFront()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromBack(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromBack()")
                .isEqualTo(wordPair.getRightWord());
    }

    @Test
    public void testWithKenyer() {
        WordPair wordPair = WordPair.of("kenyér", "kenyeret");
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .build();
        WordPairProcessorResponse response = wordPairProcessor.induceRules(wordPair);
        Rule rule = response.getRules().get(0);

        @SuppressWarnings("unchecked")
        List<ITransformation> transformations = List.of(
                new Replacement(
                        characterRepository.getCharacter("é"),
                        characterRepository.getCharacter("e"),
                        characterRepository
                ),
                new Replacement(Set.of(), characterRepository),
                new Addition(
                        Set.of(
                                HorizontalTonguePosition.FRONT,
                                VerticalTonguePosition.SEMI_OPEN,
                                Length.SHORT,
                                LipShape.UNROUNDED
                        ),
                        characterRepository
                ),
                new Addition(
                        Set.of(
                                SoundProductionWay.PLOSIVE,
                                Voice.UNVOICED,
                                SoundProductionPlace.DENTAL_ALVEOLAR,
                                UvulaPosition.ORAL,
                                Discriminator.NORMAL
                        ),
                        characterRepository
                )
        );

        assertRule(
                rule,
                "$keny",
                "ér",
                "#",
                Position.identity(),
                Position.identity(),
                transformations,
                characterRepository
        );

        assertThat(rule.transform(wordPair.getLeftWord()))
                .withFailMessage("Failed for transform()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromFront(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromFront()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromBack(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromBack()")
                .isEqualTo(wordPair.getRightWord());
    }

    @Test
    public void testWithArbitraryString() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .build();
        WordPair wordPair = WordPair.of("abcdefg", "abxxdefg");
        WordPairProcessorResponse response = wordPairProcessor.induceRules(wordPair);
        Rule rule = response.getRules().get(0);

        @SuppressWarnings("unchecked")
        List<ITransformation> transformations = List.of(
                new Replacement(
                        characterRepository.getCharacter("c"),
                        characterRepository.getCharacter("x"),
                        characterRepository
                ),
                new Addition(
                        Set.of(Discriminator.X),
                        characterRepository
                )
        );

        assertRule(
                rule,
                "$ab",
                "c",
                "defg#",
                Position.identity(),
                Position.identity(),
                transformations,
                characterRepository
        );

        assertThat(rule.transform(wordPair.getLeftWord()))
                .withFailMessage("Failed for transform()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromFront(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromFront()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromBack(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromBack()")
                .isEqualTo(wordPair.getRightWord());
    }

    @Test
    public void testWithStartTransformation() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .build();
        WordPair wordPair = WordPair.of("abc", "xabc");
        WordPairProcessorResponse response = wordPairProcessor.induceRules(wordPair);
        Rule rule = response.getRules().get(0);

        @SuppressWarnings("unchecked")
        List<ITransformation> transformations = List.of(
                new Addition(
                        Set.of(Discriminator.X),
                        characterRepository
                )
        );

        assertRule(
                rule,
                "$",
                "",
                "abc#",
                Position.identity(),
                Position.identity(),
                transformations,
                characterRepository
        );

        assertThat(rule.transform(wordPair.getLeftWord()))
                .withFailMessage("Failed for transform()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromFront(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromFront()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromBack(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromBack()")
                .isEqualTo(wordPair.getRightWord());
    }

    @Test
    public void testWithEndTransformation() {
        WordPair wordPair = WordPair.of("abc", "abcx");
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .build();
        WordPairProcessorResponse response = wordPairProcessor.induceRules(wordPair);
        Rule rule = response.getRules().get(0);

        @SuppressWarnings("unchecked")
        List<ITransformation> transformations = List.of(
                new Addition(
                        Set.of(Discriminator.X),
                        characterRepository
                )
        );

        assertRule(
                rule,
                "$abc",
                "",
                "#",
                Position.identity(),
                Position.identity(),
                transformations,
                characterRepository
        );

        assertThat(rule.transform(wordPair.getLeftWord()))
                .withFailMessage("Failed for transform()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromFront(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromFront()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromBack(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromBack()")
                .isEqualTo(wordPair.getRightWord());
    }

    @Test
    public void testWithAlmaalmaAtBeginning() {
        WordPair wordPair = WordPair.of("almaalmaxx", "almátalmaxx");
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        IWordPairProcessor cutWordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .maximalContextSize(3)
                .build();
        WordPairProcessorResponse response = cutWordPairProcessor.induceRules(wordPair);
        Rule rule = response.getRules().get(0);

        @SuppressWarnings("unchecked")
        List<ITransformation> transformations = List.of(
                new Replacement(
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("á"),
                        characterRepository
                ),
                new Addition(
                        Set.of(
                                SoundProductionWay.PLOSIVE,
                                Discriminator.NORMAL,
                                SoundProductionPlace.DENTAL_ALVEOLAR,
                                UvulaPosition.ORAL,
                                Voice.UNVOICED
                        ),
                        characterRepository
                )
        );

        assertRule(
                rule,
                "alm",
                "a",
                "alm",
                Position.identity(),
                Position.identity(),
                transformations,
                characterRepository
        );

        assertThat(rule.transform(wordPair.getLeftWord()))
                .withFailMessage("Failed for transform()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromFront(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromFront()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromBack(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromBack()")
                .isEqualTo(wordPair.getRightWord());
    }

    @Test
    public void testWithAlmaalmaAtEnd() {
        WordPair wordPair = WordPair.of("xxalmaalma", "xxalmaalmát");
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        IWordPairProcessor cutWordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .maximalContextSize(3)
                .build();
        WordPairProcessorResponse response = cutWordPairProcessor.induceRules(wordPair);
        Rule rule = response.getRules().get(0);

        @SuppressWarnings("unchecked")
        List<ITransformation> transformations = List.of(
                new Replacement(
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("á"),
                        characterRepository
                ),
                new Addition(
                        Set.of(
                                SoundProductionWay.PLOSIVE,
                                Discriminator.NORMAL,
                                SoundProductionPlace.DENTAL_ALVEOLAR,
                                UvulaPosition.ORAL,
                                Voice.UNVOICED
                        ),
                        characterRepository
                )
        );

        assertRule(
                rule,
                "alm",
                "a",
                "#",
                Position.identity(),
                Position.identity(),
                transformations,
                characterRepository
        );

        assertThat(rule.transform(wordPair.getLeftWord()))
                .withFailMessage("Failed for transform()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromFront(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromFront()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromBack(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromBack()")
                .isEqualTo(wordPair.getRightWord());
    }

    @Test
    public void testWithAlmaalmaalmaAtMiddle() {
        WordPair wordPair = WordPair.of("xxalmaalmaalmaxx", "xxalmaalmátalmaxx");
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        IWordPairProcessor cutWordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .maximalContextSize(3)
                .build();
        WordPairProcessorResponse response = cutWordPairProcessor.induceRules(wordPair);
        Rule rule = response.getRules().get(0);

        @SuppressWarnings("unchecked")
        List<ITransformation> transformations = List.of(
                new Replacement(
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("á"),
                        characterRepository
                ),
                new Addition(
                        Set.of(
                                SoundProductionWay.PLOSIVE,
                                Discriminator.NORMAL,
                                SoundProductionPlace.DENTAL_ALVEOLAR,
                                UvulaPosition.ORAL,
                                Voice.UNVOICED
                        ),
                        characterRepository
                )
        );

        assertRule(
                rule,
                "alm",
                "a",
                "alm",
                Position.of(1),
                Position.identity(),
                transformations,
                characterRepository
        );

        assertThat(rule.transform(wordPair.getLeftWord()))
                .withFailMessage("Failed for transform()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromFront(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromFront()")
                .isEqualTo(wordPair.getRightWord());
        assertThat(rule.transformFromBack(wordPair.getLeftWord()))
                .withFailMessage("Failed for transformFromBack()")
                .isEqualTo(wordPair.getRightWord());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testWithHighProcessablePath(
            ICharacterRepository characterRepository,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) {
        WordPair wordPair = WordPair.of("gyapot", "gyapotot");
        WordPairProcessorResponse response = cutWordPairProcessor.induceRules(wordPair);
        assertThat(response.getRules()).hasSize(3);
        int score = response.getLeaves().get(0).getScoreSoFar();

        for (WordPairProcessorTreeNode leaf : response.getLeaves()) {
            assertThat(leaf.getScoreSoFar()).isEqualTo(score);
        }
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testBuilderWithNoCharacterRepository(
            ICharacterRepository characterRepository,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) {
        WordPairProcessor.Builder builder = new WordPairProcessor.Builder();
        builder.costCalculator(new AttributeBasedCostCalculator());
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception.getMessage()).isEqualTo("There is no character repository but it is mandatory");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testBuilderWithNoWordConverter(
            ICharacterRepository characterRepository,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) {
        WordPairProcessor.Builder builder = new WordPairProcessor.Builder();
        builder.characterRepository(HungarianAttributedCharacterRepository.get());
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception.getMessage()).isEqualTo("There is no word converter but it is mandatory");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testBuilderWithNoCostCalculator(
            ICharacterRepository characterRepository,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) {
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        WordPairProcessor.Builder builder = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter);
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception.getMessage()).isEqualTo("There is no cost calculator but it is mandatory");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testBuilderWithDefaultValues(
            ICharacterRepository characterRepository,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) {
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        WordPairProcessor concreteWordPairProcessor = (WordPairProcessor) new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .build();
        assertThat(concreteWordPairProcessor.getCharacterRepository()).isSameAs(characterRepository);
        assertThat(concreteWordPairProcessor.getWordConverter()).isSameAs(wordConverter);
        assertThat(concreteWordPairProcessor.getCostCalculator()).isSameAs(costCalculator);
        assertThat(concreteWordPairProcessor.getPathLevelsToProcess()).isEqualTo(1);
        assertThat(concreteWordPairProcessor.getMaximalContextSize()).isEqualTo(Integer.MAX_VALUE);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testBuilderWithCustomValues(
            ICharacterRepository characterRepository,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) {
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        int pathLevelsToProcess = 3;
        int maximalContextSize = 3;
        WordPairProcessor concreteWordPairProcessor = (WordPairProcessor) new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .pathLevelsToProcess(pathLevelsToProcess)
                .maximalContextSize(maximalContextSize)
                .build();
        assertThat(concreteWordPairProcessor.getCharacterRepository()).isSameAs(characterRepository);
        assertThat(concreteWordPairProcessor.getWordConverter()).isSameAs(wordConverter);
        assertThat(concreteWordPairProcessor.getCostCalculator()).isSameAs(costCalculator);
        assertThat(concreteWordPairProcessor.getPathLevelsToProcess()).isEqualTo(pathLevelsToProcess);
        assertThat(concreteWordPairProcessor.getMaximalContextSize()).isEqualTo(maximalContextSize);
    }

    private static void assertRule(
            Rule rule,
            String prefix,
            String core,
            String postfix,
            Position frontPosition,
            Position backPosition,
            List<ITransformation> transformations,
            ICharacterRepository characterRepository) {
        String actualPrefix = rule.getContext().getPrefix()
                .stream()
                .map(characterRepository::getLetter)
                .collect(joining());
        assertThat(actualPrefix).isEqualTo(prefix);

        String actualCore = rule.getContext().getCore()
                .stream()
                .map(characterRepository::getLetter)
                .collect(joining());
        assertThat(actualCore).isEqualTo(core);

        String actualPostfix = rule.getContext().getPostfix()
                .stream()
                .map(characterRepository::getLetter)
                .collect(joining());
        assertThat(actualPostfix).isEqualTo(postfix);

        assertThat(rule.getContext().getFrontPosition()).isEqualTo(frontPosition);
        assertThat(rule.getContext().getBackPosition()).isEqualTo(backPosition);

        assertThat(rule.getTransformations()).isEqualTo(transformations);
    }

}
