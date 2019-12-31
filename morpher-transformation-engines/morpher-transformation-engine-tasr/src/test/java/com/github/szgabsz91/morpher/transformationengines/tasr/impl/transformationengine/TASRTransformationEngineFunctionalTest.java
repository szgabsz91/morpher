package com.github.szgabsz91.morpher.transformationengines.tasr.impl.transformationengine;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.api.model.TransformationEngineResponse;
import com.github.szgabsz91.morpher.transformationengines.tasr.converters.TASRTransformationEngineConverter;
import com.github.szgabsz91.morpher.transformationengines.tasr.protocolbuffers.TASRTransformationEngineMessage;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TASRTransformationEngineFunctionalTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TASRTransformationEngineFunctionalTest.class);

    private static Stream<Arguments> parameters() {
        AffixType affixType = AffixType.of("AFF");
        return Stream.of(new TASRTransformationEngine(false, affixType), new TASRTransformationEngine(true, affixType))
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testWithUnidirectionalTransformationEngine(TASRTransformationEngine tasrTransformationEngine) throws IOException {
        LOGGER.debug("Testing with {} TASR transformation engine", tasrTransformationEngine.isUnidirectional() ? "unidirectional" : "bidirectional");

        try (BufferedReader reader = Files.newBufferedReader(Paths.get("src/test/resources/data/english-past-tense-500.csv"), StandardCharsets.UTF_8)) {
            List<WordPair> wordPairs = reader
                    .lines()
                    .map(line -> line.split(","))
                    .map(lineParts -> WordPair.of(lineParts[0], lineParts[1]))
                    .collect(toList());
            wordPairs = removeDuplicates(wordPairs);
            List<WordPair> wordPairs1 = wordPairs.subList(0, 160);
            List<WordPair> wordPairs2 = wordPairs.subList(160, 320);
            List<WordPair> wordPairs1_2 = new ArrayList<>(wordPairs1);
            wordPairs1_2.addAll(wordPairs2);
            List<WordPair> wordPairs3 = wordPairs.subList(320, wordPairs.size());
            List<WordPair> wordPairs1_3 = new ArrayList<>(wordPairs1_2);
            wordPairs1_3.addAll(wordPairs3);

            Set<Word> skippedBackwardsInputs = Set.of(
                    Word.of("wed")
            );
            assertTASRTransformationEngine(tasrTransformationEngine, wordPairs1, wordPairs1, skippedBackwardsInputs);
            assertTASRTransformationEngine(tasrTransformationEngine, wordPairs2, wordPairs1_2, skippedBackwardsInputs);
            assertTASRTransformationEngine(tasrTransformationEngine, wordPairs3, wordPairs1_3, skippedBackwardsInputs);
        }
    }

    private static void assertTASRTransformationEngine(TASRTransformationEngine tasrTransformationEngine, List<WordPair> additionalWordPairs, List<WordPair> allWordPairs, Set<Word> skippedBackwardsInputs) throws IOException {
        // Teach
        List<FrequencyAwareWordPair> additionalFrequencyAwareWordPairs = additionalWordPairs
                .stream()
                .map(FrequencyAwareWordPair::of)
                .collect(toList());
        tasrTransformationEngine.learn(TrainingSet.of(new HashSet<>(additionalFrequencyAwareWordPairs)));

        // Save and reload
        Serializer<TASRTransformationEngine, TASRTransformationEngineMessage> serializer = new Serializer<>(new TASRTransformationEngineConverter(), tasrTransformationEngine);
        Path file = Files.createTempFile("transformation-engine", "tasr");
        final TASRTransformationEngine rebuiltTASRTransformationEngine;
        try {
            serializer.serialize(tasrTransformationEngine, file);
            rebuiltTASRTransformationEngine = serializer.deserialize(file);
        }
        finally {
            Files.delete(file);
        }

        // Check forwards transformation
        allWordPairs.forEach(wordPair -> {
            Word input = wordPair.getLeftWord();
            TransformationEngineResponse expected = TransformationEngineResponse.singleton(wordPair.getRightWord());
            Optional<TransformationEngineResponse> response = rebuiltTASRTransformationEngine.transform(input);
            assertThat(response)
                    .withFailMessage("Invalid forwards transformation: %s --> %s instead of %s", input, response, expected)
                    .hasValue(expected);
        });

        // Check backwards transformation
        if (tasrTransformationEngine.isUnidirectional()) {
            UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> rebuiltTASRTransformationEngine.transformBack(allWordPairs.get(0).getRightWord()));
            assertThat(exception).hasMessage("Unidirectional TASR transformation engine can only transform words forwards but not backwards");
        }
        else {
            allWordPairs.forEach(wordPair -> {
                Word input = wordPair.getRightWord();
                if (skippedBackwardsInputs.contains(input)) {
                    return;
                }
                TransformationEngineResponse expected = TransformationEngineResponse.singleton(wordPair.getLeftWord());
                Optional<TransformationEngineResponse> response = rebuiltTASRTransformationEngine.transformBack(input);
                assertThat(response)
                        .withFailMessage("Invalid backwards transformation: %s --> %s instead of %s", input, response, expected)
                        .hasValue(expected);
            });
        }
    }

    private static List<WordPair> removeDuplicates(List<WordPair> wordPairs) {
        Collection<WordPair> nonRedundantWordPairs = wordPairs
                .stream()
                .collect(toMap(WordPair::getLeftWord, Function.identity(), (x, y) -> x))
                .values();
        return new ArrayList<>(nonRedundantWordPairs);
    }

}
