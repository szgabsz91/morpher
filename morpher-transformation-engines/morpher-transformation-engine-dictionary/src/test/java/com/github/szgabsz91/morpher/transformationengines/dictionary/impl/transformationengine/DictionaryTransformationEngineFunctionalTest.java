package com.github.szgabsz91.morpher.transformationengines.dictionary.impl.transformationengine;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.api.model.TransformationEngineResponse;
import com.github.szgabsz91.morpher.transformationengines.api.utils.TrainingDataRetriever;
import com.github.szgabsz91.morpher.transformationengines.dictionary.converters.DictionaryTransformationEngineConverter;
import com.github.szgabsz91.morpher.transformationengines.dictionary.protocolbuffers.DictionaryTransformationEngineMessage;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public class DictionaryTransformationEngineFunctionalTest {

    @Test
    public void test() throws IOException {
        AffixType affixType = AffixType.of("AFF");
        DictionaryTransformationEngine dictionaryTransformationEngine = new DictionaryTransformationEngine(affixType);

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

            assertDictionaryTransformationEngine(dictionaryTransformationEngine, wordPairs1, wordPairs1);
            assertDictionaryTransformationEngine(dictionaryTransformationEngine, wordPairs2, wordPairs1_2);
            assertDictionaryTransformationEngine(dictionaryTransformationEngine, wordPairs3, wordPairs1_3);
        }
    }

    private static void assertDictionaryTransformationEngine(DictionaryTransformationEngine dictionaryTransformationEngine, List<WordPair> additionalWordPairs, List<WordPair> allWordPairs) throws IOException {
        // Teach
        List<FrequencyAwareWordPair> additionalFrequencyAwareWordPairs = additionalWordPairs
                .stream()
                .map(FrequencyAwareWordPair::of)
                .collect(toList());
        dictionaryTransformationEngine.learn(TrainingSet.of(new HashSet<>(additionalFrequencyAwareWordPairs)));

        // Save and reload
        Serializer<DictionaryTransformationEngine, DictionaryTransformationEngineMessage> serializer = new Serializer<>(new DictionaryTransformationEngineConverter(), dictionaryTransformationEngine);
        Path file = Files.createTempFile("transformation-engine", "dictionary");
        DictionaryTransformationEngine rebuiltDictionaryTransformationEngine;
        try {
            serializer.serialize(dictionaryTransformationEngine, file);
            rebuiltDictionaryTransformationEngine = serializer.deserialize(file);
        }
        finally {
            Files.delete(file);
        }

        // Check forwards and backwards transformation
        allWordPairs.forEach(wordPair -> {
            Word rootForm = wordPair.getLeftWord();
            Word inflectedForm = wordPair.getRightWord();
            assertThat(rebuiltDictionaryTransformationEngine.transform(rootForm)).hasValue(TransformationEngineResponse.singleton(inflectedForm));
            assertThat(rebuiltDictionaryTransformationEngine.transformBack(inflectedForm)).hasValue(TransformationEngineResponse.singleton(rootForm));
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
