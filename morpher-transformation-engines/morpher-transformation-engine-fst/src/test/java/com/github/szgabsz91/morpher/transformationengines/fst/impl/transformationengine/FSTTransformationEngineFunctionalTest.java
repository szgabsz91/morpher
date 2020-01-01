package com.github.szgabsz91.morpher.transformationengines.fst.impl.transformationengine;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.api.model.TransformationEngineResponse;
import com.github.szgabsz91.morpher.transformationengines.api.utils.TrainingDataRetriever;
import com.github.szgabsz91.morpher.transformationengines.fst.converters.FSTTransformationEngineConverter;
import com.github.szgabsz91.morpher.transformationengines.fst.protocolbuffers.FSTTransformationEngineMessage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FSTTransformationEngineFunctionalTest {

    private static Stream<Arguments> parameters() {
        AffixType affixType = AffixType.of("AFF");
        return Stream.of(new FSTTransformationEngine(false, affixType), new FSTTransformationEngine(true, affixType))
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(FSTTransformationEngine fstTransformationEngine) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(TrainingDataRetriever.getTrainingDataFile("CAS(ACC).csv"), StandardCharsets.UTF_8)) {
            List<WordPair> wordPairs = reader
                    .lines()
                    .limit(10000L)
                    .map(line -> line.split(","))
                    .map(lineParts -> WordPair.of(lineParts[0], lineParts[1]))
                    .collect(toList());
            wordPairs = removeDuplicates(wordPairs);
            List<WordPair> wordPairs1 = wordPairs.subList(0, 3000);
            List<WordPair> wordPairs2 = wordPairs.subList(3000, 6000);
            List<WordPair> wordPairs1_2 = new ArrayList<>(wordPairs1);
            wordPairs1_2.addAll(wordPairs2);
            List<WordPair> wordPairs3 = wordPairs.subList(6000, wordPairs.size());
            List<WordPair> wordPairs1_3 = new ArrayList<>(wordPairs1_2);
            wordPairs1_3.addAll(wordPairs3);

            assertFSTTransformationEngine(fstTransformationEngine, wordPairs1, wordPairs1);
            assertFSTTransformationEngine(fstTransformationEngine, wordPairs2, wordPairs1_2);
            assertFSTTransformationEngine(fstTransformationEngine, wordPairs3, wordPairs1_3);
        }
    }

    private static void assertFSTTransformationEngine(FSTTransformationEngine fstTransformationEngine, List<WordPair> additionalWordPairs, List<WordPair> allWordPairs) throws IOException {
        // Teach
        List<FrequencyAwareWordPair> additionalFrequencyAwareWordPairs = additionalWordPairs
                .stream()
                .map(FrequencyAwareWordPair::of)
                .collect(toList());
        fstTransformationEngine.learn(TrainingSet.of(new HashSet<>(additionalFrequencyAwareWordPairs)));

        // Check learnt word pairs
        List<WordPair> learntWordPairs = fstTransformationEngine.getWordPairs();
        assertThat(learntWordPairs).containsAll(allWordPairs);

        // Save and reload
        Serializer<FSTTransformationEngine, FSTTransformationEngineMessage> serializer = new Serializer<>(new FSTTransformationEngineConverter(), fstTransformationEngine);
        Path file = Files.createTempFile("transformation-engine", "fst");
        FSTTransformationEngine rebuiltFSTTransformationEngine;
        try {
            serializer.serialize(fstTransformationEngine, file);
            rebuiltFSTTransformationEngine = serializer.deserialize(file);
        }
        finally {
            Files.delete(file);
        }

        // Check forwards transformation
        allWordPairs.forEach(wordPair -> {
            Word input = wordPair.getLeftWord();
            TransformationEngineResponse expected = TransformationEngineResponse.singleton(wordPair.getRightWord());
            Optional<TransformationEngineResponse> response = rebuiltFSTTransformationEngine.transform(input);
            assertThat(response).hasValue(expected);
        });

        // Check backwards transformation
        if (fstTransformationEngine.isUnidirectional()) {
            UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> rebuiltFSTTransformationEngine.transformBack(allWordPairs.get(0).getRightWord()));
            assertThat(exception).hasMessage("Unidirectional FST transformation engine can only transform words forwards but not backwards");
        }
        else {
            allWordPairs.forEach(wordPair -> {
                Word input = wordPair.getRightWord();
                TransformationEngineResponse expected = TransformationEngineResponse.singleton(wordPair.getLeftWord());
                Optional<TransformationEngineResponse> response = rebuiltFSTTransformationEngine.transformBack(input);
                assertThat(response).hasValue(expected);
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
