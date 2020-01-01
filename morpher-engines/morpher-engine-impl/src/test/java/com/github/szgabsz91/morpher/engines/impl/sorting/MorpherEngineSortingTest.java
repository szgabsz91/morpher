package com.github.szgabsz91.morpher.engines.impl.sorting;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.core.services.ClassBasedServiceProvider;
import com.github.szgabsz91.morpher.core.services.ServiceProvider;
import com.github.szgabsz91.morpher.core.utils.Timer;
import com.github.szgabsz91.morpher.engines.api.model.AnalysisInput;
import com.github.szgabsz91.morpher.engines.api.model.InflectionOrderedInput;
import com.github.szgabsz91.morpher.engines.api.model.MorpherEngineResponse;
import com.github.szgabsz91.morpher.engines.api.model.PreanalyzedTrainingItem;
import com.github.szgabsz91.morpher.engines.api.model.PreanalyzedTrainingItems;
import com.github.szgabsz91.morpher.engines.api.model.Step;
import com.github.szgabsz91.morpher.engines.impl.MorpherEngineBuilder;
import com.github.szgabsz91.morpher.engines.impl.impl.MorpherEngine;
import com.github.szgabsz91.morpher.engines.impl.impl.probability.MultiplyProbabilityCalculator;
import com.github.szgabsz91.morpher.engines.impl.impl.transformationengineholders.EagerTransformationEngineHolder;
import com.github.szgabsz91.morpher.engines.impl.sorting.components.CharacterAbstractTransformationEngineFactory;
import com.github.szgabsz91.morpher.engines.impl.sorting.components.CharacterTransformationEngine;
import com.github.szgabsz91.morpher.engines.impl.sorting.model.GeneratedItem;
import com.github.szgabsz91.morpher.engines.impl.transformationengineholderfactories.EagerTransformationEngineHolderFactory;
import com.github.szgabsz91.morpher.engines.impl.transformationengineholders.ITransformationEngineHolder;
import com.github.szgabsz91.morpher.engines.impl.utils.ExcludeDuringBuild;
import com.github.szgabsz91.morpher.languagehandlers.api.ILanguageHandler;
import com.github.szgabsz91.morpher.languagehandlers.api.model.AnnotationTokenizerResult;
import com.github.szgabsz91.morpher.languagehandlers.api.model.LemmaMap;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.HunmorphLanguageHandler;
import com.github.szgabsz91.morpher.transformationengines.api.factories.IAbstractTransformationEngineFactory;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

@ExcludeDuringBuild
public class MorpherEngineSortingTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MorpherEngineSortingTest.class);
    private static final int TOTAL_COUNT = 100;
    private static final double RANDOMNESS_RATIO = 0.8;
    private static final AffixType AFFIX_TYPE_POS = AffixType.of("/POS");

    private MorpherEngine engine;
    private Random random;

    private static IntStream parameters() {
        return IntStream.iterate(100, value -> value + 100)
                .limit(50L);
    }

    @BeforeEach
    public void setUp() {
        random = new Random();

        final Function<Class<?>, Stream<? extends ServiceLoader.Provider<?>>> serviceLoader = clazz -> {
            if (clazz.equals(IAbstractTransformationEngineFactory.class)) {
                ServiceLoader.Provider<?> abstractTransformationEngineFactoryProvider = new ClassBasedServiceProvider<>(CharacterAbstractTransformationEngineFactory.class);
                return Stream.of(abstractTransformationEngineFactoryProvider);
            }
            else if (clazz.equals(ILanguageHandler.class)) {
                ServiceLoader.Provider<?> languageHandlerProvider = new ClassBasedServiceProvider<>(HunmorphLanguageHandler.class);
                return Stream.of(languageHandlerProvider);
            }
            throw new IllegalArgumentException("Cannot load service for class " + clazz.getName());
        };
        final ServiceProvider serviceProvider = new ServiceProvider(serviceLoader);
        this.engine = (MorpherEngine) new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .transformationEngineHolderFactory(new EagerTransformationEngineHolderFactory())
                .probabilityCalculator(new MultiplyProbabilityCalculator())
                .build();

        // Training
        for (String letter : CharacterTransformationEngine.ALPHABET) {
            AffixType affixType = AffixType.of(letter);
            ITransformationEngineHolder transformationEngineHolder = new EagerTransformationEngineHolder(new CharacterTransformationEngine(letter, random));
            this.engine.getTransformationEngineHolderMap().put(affixType, transformationEngineHolder);
        }
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(int inputLength) {
        LOGGER.debug("Input length: {}", inputLength);

        // Training
        Set<GeneratedItem> generatedItems = generateRandomItems(TOTAL_COUNT, inputLength, RANDOMNESS_RATIO);
        Set<PreanalyzedTrainingItem> preanalyzedTrainingItems = generatedItems
                .stream()
                .map(GeneratedItem::getPreanalyzedTrainingItems)
                .flatMap(Collection::stream)
                .collect(toSet());
        Map<Word, Set<AffixType>> lemmaMap = generatedItems
                .stream()
                .map(generatedItem -> generatedItem.getWordPair().getLeftWord())
                .map(lemma -> Map.entry(lemma, Set.of(AFFIX_TYPE_POS)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.engine.learn(PreanalyzedTrainingItems.of(preanalyzedTrainingItems));
        this.engine.learn(LemmaMap.of(lemmaMap));

        // Evaluation
        Timer timer = new Timer();
        for (GeneratedItem generatedItem : generatedItems) {
            WordPair wordPair = generatedItem.getWordPair();
            Word expectedOutput = wordPair.getLeftWord();
            Word input = wordPair.getRightWord();
            PreanalyzedTrainingItem preanalyzedTrainingItem = generatedItem.getPreanalyzedTrainingItems().get(generatedItem.getPreanalyzedTrainingItems().size() - 1);
            AnnotationTokenizerResult annotationTokenizerResult = preanalyzedTrainingItem.getAnnotationTokenizerResult();
            List<AffixType> reversedAffixTypes = annotationTokenizerResult.getAffixTypes();
            AffixType expectedPOS = reversedAffixTypes.remove(0);
            List<AffixType> expectedAffixTypes = IntStream.range(0, reversedAffixTypes.size())
                    .map(index -> reversedAffixTypes.size() - index - 1)
                    .mapToObj(reversedAffixTypes::get)
                    .collect(toList());
            AnalysisInput analysisInput = AnalysisInput.of(input);
            List<MorpherEngineResponse> responses = timer.measure(() -> this.engine.analyze(analysisInput));
            assertThat(responses).hasSize(1);
            MorpherEngineResponse response = responses.get(0);
            List<AffixType> affixTypes = response.getSteps()
                    .stream()
                    .map(Step::getAffixType)
                    .collect(toList());
            assertThat(response.getOutput()).isEqualTo(expectedOutput);
            assertThat(response.getPos().getAffixType()).isEqualTo(expectedPOS);
            assertThat(affixTypes).isEqualTo(expectedAffixTypes);
        }

        LOGGER.debug("Average sorting time: {} seconds", timer.getSeconds() / generatedItems.size());
    }

    private Set<GeneratedItem> generateRandomItems(int n, int inputLength, double randomnessRatio) {
        Set<GeneratedItem> generatedItems = new LinkedHashSet<>();

        for (int i = 0; i < n; i++) {
            RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder()
                    .withinRange('a', 'z')
                    .build();
            String randomString = randomStringGenerator.generate(inputLength);
            List<String> characters = randomString
                    .chars()
                    .mapToObj(character -> Character.toString((char) character))
                    .sorted()
                    .collect(toList());
            String sortedString = String.join("", characters);
            Word sortedWord = Word.of(sortedString);
            List<String> uniqueCharacters = characters
                    .stream()
                    .distinct()
                    .sorted()
                    .collect(toList());
            int randomCharacterCount = (int) Math.floor(uniqueCharacters.size() * randomnessRatio);
            Set<Integer> characterIndicesToRandomize = generateRandomIndices(uniqueCharacters, randomCharacterCount);
            Set<String> charactersToRandomize = characterIndicesToRandomize
                    .stream()
                    .map(uniqueCharacters::get)
                    .collect(toSet());
            List<AffixType> affixTypes = charactersToRandomize
                    .stream()
                    .map(AffixType::of)
                    .collect(toList());

            List<PreanalyzedTrainingItem> preanalyzedTrainingItems = new ArrayList<>();
            Word currentWord = sortedWord;
            List<AffixType> currentAffixTypes = new ArrayList<>();
            currentAffixTypes.add(AFFIX_TYPE_POS);

            for (AffixType affixType : affixTypes) {
                currentAffixTypes.add(affixType);
                InflectionOrderedInput inflectionOrderedInput = new InflectionOrderedInput(currentWord, List.of(affixType));
                List<MorpherEngineResponse> responses = this.engine.inflect(inflectionOrderedInput);
                assertThat(responses).hasSize(1);
                MorpherEngineResponse response = responses.get(0);
                Word newWord = response.getOutput();
                AnnotationTokenizerResult annotationTokenizerResult = createAnnotationTokenizerResult(newWord, sortedWord, currentAffixTypes);
                FrequencyAwareWordPair wordPair = FrequencyAwareWordPair.of(currentWord, newWord, annotationTokenizerResult.getFrequency());
                PreanalyzedTrainingItem preanalyzedTrainingItem = new PreanalyzedTrainingItem(annotationTokenizerResult, wordPair);
                preanalyzedTrainingItems.add(preanalyzedTrainingItem);
                currentWord = newWord;
            }

            WordPair wordPair = WordPair.of(sortedWord, preanalyzedTrainingItems.get(preanalyzedTrainingItems.size() - 1).getWordPair().getRightWord());
            GeneratedItem generatedItem = new GeneratedItem(preanalyzedTrainingItems, wordPair);
            generatedItems.add(generatedItem);
        }

        return generatedItems;
    }

    private Set<Integer> generateRandomIndices(List<String> uniqueCharacters, int randomCharacterCount) {
        Set<Integer> result = new HashSet<>();

        while (result.size() < randomCharacterCount) {
            int randomIndex = random.nextInt(uniqueCharacters.size());
            result.add(randomIndex);
        }

        return result;
    }

    private static AnnotationTokenizerResult createAnnotationTokenizerResult(Word grammaticalForm, Word lemma, List<AffixType> affixTypes) {
        AnnotationTokenizerResult annotationTokenizerResult = new AnnotationTokenizerResult("", grammaticalForm.toString(), lemma.toString(), 1);
        for (AffixType affixType : affixTypes) {
            annotationTokenizerResult.addAffixType(AffixType.of(affixType.toString()));
        }
        return annotationTokenizerResult;
    }

}
