package com.github.szgabsz91.morpher.transformationengines.astra.impl.transformationengine;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.transformationengines.api.IBidirectionalTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.api.model.TransformationEngineResponse;
import com.github.szgabsz91.morpher.transformationengines.api.utils.TrainingDataRetriever;
import com.github.szgabsz91.morpher.transformationengines.astra.config.ASTRATransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.astra.config.SearcherType;
import com.github.szgabsz91.morpher.transformationengines.astra.converters.ASTRATransformationEngineConverter;
import com.github.szgabsz91.morpher.transformationengines.astra.protocolbuffers.ASTRATransformationEngineMessage;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.assertj.core.api.Assertions.assertThat;

public class ASTRATransformationEngineFunctionalTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ASTRATransformationEngineFunctionalTest.class);

    public static Stream<Arguments> parameters() {
        return Stream.of(
                new ASTRATransformationEngineConfiguration.Builder()
                        .searcherType(SearcherType.SEQUENTIAL)
                        .build(),
                new ASTRATransformationEngineConfiguration.Builder()
                        .searcherType(SearcherType.PARALLEL)
                        .build(),
                new ASTRATransformationEngineConfiguration.Builder()
                        .searcherType(SearcherType.PREFIX_TREE)
                        .build()
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(ASTRATransformationEngineConfiguration configuration) throws IOException {
        LOGGER.debug("Using configuration: {}", configuration);

        ASTRAAbstractTransformationEngineFactory astraAbstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory(configuration);
        Supplier<IBidirectionalTransformationEngine<?>> supplier = astraAbstractTransformationEngineFactory.getBidirectionalFactory(AffixType.of("<CAS<ACC>>"));
        ASTRATransformationEngine astraTransformationEngine = (ASTRATransformationEngine) supplier.get();

        try (BufferedReader reader = Files.newBufferedReader(TrainingDataRetriever.getTrainingDataFile("CAS(ACC).csv"), StandardCharsets.UTF_8)) {
            List<FrequencyAwareWordPair> wordPairs = reader
                    .lines()
                    .limit(1000L)
                    .map(line -> line.split(","))
                    .map(lineParts -> FrequencyAwareWordPair.of(lineParts[0], lineParts[1], Integer.parseInt(lineParts[2])))
                    .toList();
            wordPairs = removeDuplicates(wordPairs);
            List<FrequencyAwareWordPair> wordPairs1 = wordPairs.subList(0, wordPairs.size() / 3);
            List<FrequencyAwareWordPair> wordPairs2 = wordPairs.subList(wordPairs.size() / 3, 2 * wordPairs.size() / 3);
            List<FrequencyAwareWordPair> wordPairs1_2 = new ArrayList<>(wordPairs1);
            wordPairs1_2.addAll(wordPairs2);
            List<FrequencyAwareWordPair> wordPairs3 = wordPairs.subList(2 * wordPairs.size() / 3, wordPairs.size());
            List<FrequencyAwareWordPair> wordPairs1_3 = new ArrayList<>(wordPairs1_2);
            wordPairs1_3.addAll(wordPairs3);

            astraTransformationEngine = assertASTRATransformationEngine(astraTransformationEngine, wordPairs1, wordPairs1);
            astraTransformationEngine = assertASTRATransformationEngine(astraTransformationEngine, wordPairs2, wordPairs1_2);
            astraTransformationEngine = assertASTRATransformationEngine(astraTransformationEngine, wordPairs3, wordPairs1_3);
        }
    }

    private static ASTRATransformationEngine assertASTRATransformationEngine(ASTRATransformationEngine astraTransformationEngine, List<FrequencyAwareWordPair> additionalWordPairs, List<FrequencyAwareWordPair> allWordPairs) throws IOException {
        // Teach
        astraTransformationEngine.learn(TrainingSet.of(new HashSet<>(additionalWordPairs)));

        // Save and reload
        Serializer<ASTRATransformationEngine, ASTRATransformationEngineMessage> serializer = new Serializer<>(new ASTRATransformationEngineConverter(), astraTransformationEngine);
        Path file = Files.createTempFile("transformation-engine", "astra");
        ASTRATransformationEngine rebuiltASTRATransformationEngine;
        try {
            serializer.serialize(astraTransformationEngine, file);
            rebuiltASTRATransformationEngine = serializer.deserialize(file);
        }
        finally {
            Files.delete(file);
        }

        // Check forwards transformation
        allWordPairs.forEach(wordPair -> {
            Word input = wordPair.getLeftWord();
            TransformationEngineResponse expected = TransformationEngineResponse.singleton(wordPair.getRightWord());
            Optional<TransformationEngineResponse> response = rebuiltASTRATransformationEngine.transform(input);
            assertThat(response)
                    .withFailMessage("Forwards transformation error: %s --> %s instead of %s", input, response, expected)
                    .hasValue(expected);
        });

        // Check backwards transformation
        allWordPairs.forEach(wordPair -> {
            Word input = wordPair.getRightWord();
            TransformationEngineResponse expected = TransformationEngineResponse.singleton(wordPair.getLeftWord());
            Optional<TransformationEngineResponse> response = rebuiltASTRATransformationEngine.transformBack(input);
            assertThat(response)
                    .withFailMessage("Backwards transformation error: %s --> %s instead of %s", input, response, expected)
                    .hasValue(expected);
        });

        return astraTransformationEngine;
    }

    private static List<FrequencyAwareWordPair> removeDuplicates(List<FrequencyAwareWordPair> wordPairs) {
        return new ArrayList<>(wordPairs
                .stream()
                .collect(toUnmodifiableMap(FrequencyAwareWordPair::getLeftWord, Function.identity(), (x, y) -> x))
                .values());
    }

}
