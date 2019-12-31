package com.github.szgabsz91.morpher.transformationengines.lattice.impl.transformationengine;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.IBidirectionalTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.api.model.TransformationEngineResponse;
import com.github.szgabsz91.morpher.transformationengines.api.utils.TrainingDataRetriever;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.CharacterRepositoryType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.CostCalculatorType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.LatticeBuilderType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.LatticeTransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.WordConverterType;
import com.github.szgabsz91.morpher.transformationengines.lattice.converters.LatticeTransformationEngineConverter;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.LatticeTransformationEngineMessage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public class LatticeTransformationEngineFunctionalTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LatticeTransformationEngineFunctionalTest.class);

    public static Stream<Arguments> parameters() {
        return Stream.of(
                createConfiguration(LatticeBuilderType.COMPLETE, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, Integer.MAX_VALUE),
                createConfiguration(LatticeBuilderType.CONSISTENT, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, Integer.MAX_VALUE)
        ).map(Arguments::of);
    }

    private static LatticeTransformationEngineConfiguration createConfiguration(
            LatticeBuilderType latticeBuilderType,
            WordConverterType wordConverterType,
            CostCalculatorType costCalculatorType,
            CharacterRepositoryType characterRepositoryType,
            int maximalContextSize) {
        return new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(latticeBuilderType)
                .wordConverterType(wordConverterType)
                .costCalculatorType(costCalculatorType)
                .characterRepositoryType(characterRepositoryType)
                .maximalContextSize(maximalContextSize)
                .build();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(LatticeTransformationEngineConfiguration configuration) throws IOException {
        LOGGER.debug("Using configuration: {}", configuration);

        AffixType affixType = AffixType.of("AFF");
        LatticeAbstractTransformationEngineFactory latticeAbstractTransformationEngineFactory = new LatticeAbstractTransformationEngineFactory(configuration);
        Supplier<IBidirectionalTransformationEngine<?>> supplier = latticeAbstractTransformationEngineFactory.getBidirectionalFactory(affixType);
        LatticeTransformationEngine latticeTransformationEngine = (LatticeTransformationEngine) supplier.get();

        try (BufferedReader reader = Files.newBufferedReader(TrainingDataRetriever.getTrainingDataFile("CAS(ACC).csv"), StandardCharsets.UTF_8)) {
            List<WordPair> wordPairs = reader
                    .lines()
                    .limit(500L)
                    .map(line -> line.split(","))
                    .map(lineParts -> WordPair.of(lineParts[0], lineParts[1]))
                    .collect(toList());
            wordPairs = removeDuplicates(wordPairs);
            List<WordPair> wordPairs1 = wordPairs.subList(0, wordPairs.size() / 3);
            List<WordPair> wordPairs2 = wordPairs.subList(wordPairs.size() / 3, 2 * wordPairs.size() / 3);
            List<WordPair> wordPairs1_2 = new ArrayList<>(wordPairs1);
            wordPairs1_2.addAll(wordPairs2);
            List<WordPair> wordPairs3 = wordPairs.subList(2 * wordPairs.size() / 3, wordPairs.size());
            List<WordPair> wordPairs1_3 = new ArrayList<>(wordPairs1_2);
            wordPairs1_3.addAll(wordPairs3);

            assertLatticeTransformationEngine(latticeTransformationEngine, wordPairs1, wordPairs1);
            assertLatticeTransformationEngine(latticeTransformationEngine, wordPairs2, wordPairs1_2);
            assertLatticeTransformationEngine(latticeTransformationEngine, wordPairs3, wordPairs1_3);
        }
    }

    private static void assertLatticeTransformationEngine(LatticeTransformationEngine latticeTransformationEngine, List<WordPair> additionalWordPairs, List<WordPair> allWordPairs) throws IOException {
        // Teach
        List<FrequencyAwareWordPair> additionalFrequencyAwareWordPairs = additionalWordPairs
                .stream()
                .map(FrequencyAwareWordPair::of)
                .collect(toList());
        latticeTransformationEngine.learn(TrainingSet.of(new HashSet<>(additionalFrequencyAwareWordPairs)));

        // Save and reload
        Serializer<LatticeTransformationEngine, LatticeTransformationEngineMessage> serializer = new Serializer<>(new LatticeTransformationEngineConverter(), latticeTransformationEngine);
        Path file = Files.createTempFile("transformation-engine", "lattice");
        LatticeTransformationEngine rebuiltLatticeTransformationEngine;
        try {
            serializer.serialize(latticeTransformationEngine, file);
            rebuiltLatticeTransformationEngine = serializer.deserialize(file);
        }
        finally {
            Files.delete(file);
        }

        // Check transform
        allWordPairs.forEach(wordPair -> {
            Word input = wordPair.getLeftWord();
            TransformationEngineResponse expected = TransformationEngineResponse.singleton(wordPair.getRightWord());
            Optional<TransformationEngineResponse> response = rebuiltLatticeTransformationEngine.transform(input);
            assertThat(response)
                    .withFailMessage("Inflection error: %s --> %s instead of %s", input, response, expected)
                    .hasValue(expected);
        });

        // Check transform back
        allWordPairs.forEach(wordPair -> {
            Word input = wordPair.getRightWord();
            TransformationEngineResponse expected = TransformationEngineResponse.singleton(wordPair.getLeftWord());
            Optional<TransformationEngineResponse> response = rebuiltLatticeTransformationEngine.transformBack(input);
            assertThat(response)
                    .withFailMessage("Backwards transformation error: %s --> %s instead of %s", input, response, expected)
                    .hasValue(expected);
        });
    }

    private static List<WordPair> removeDuplicates(List<WordPair> wordPairs) {
        Collection<WordPair> nonRedundantWordPairs = wordPairs
                .stream()
                .collect(toMap(WordPair::getLeftWord, Function.identity(), (x, y) -> x))
                .values();
        return new ArrayList<>(nonRedundantWordPairs);
    }

}
