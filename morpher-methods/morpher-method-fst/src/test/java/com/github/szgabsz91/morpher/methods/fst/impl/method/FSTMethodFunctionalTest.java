package com.github.szgabsz91.morpher.methods.fst.impl.method;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.github.szgabsz91.morpher.methods.api.utils.TrainingDataRetriever;
import com.github.szgabsz91.morpher.methods.fst.converters.FSTMethodConverter;
import com.github.szgabsz91.morpher.methods.fst.protocolbuffers.FSTMethodMessage;
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

public class FSTMethodFunctionalTest {

    private static Stream<Arguments> parameters() {
        AffixType affixType = AffixType.of("AFF");
        return Stream.of(new FSTMethod(false, affixType), new FSTMethod(true, affixType))
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(FSTMethod fstMethod) throws IOException {
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

            assertFSTMethod(fstMethod, wordPairs1, wordPairs1);
            assertFSTMethod(fstMethod, wordPairs2, wordPairs1_2);
            assertFSTMethod(fstMethod, wordPairs3, wordPairs1_3);
        }
    }

    private static void assertFSTMethod(FSTMethod fstMethod, List<WordPair> additionalWordPairs, List<WordPair> allWordPairs) throws IOException {
        // Teach
        List<FrequencyAwareWordPair> additionalFrequencyAwareWordPairs = additionalWordPairs
                .stream()
                .map(FrequencyAwareWordPair::of)
                .collect(toList());
        fstMethod.learn(TrainingSet.of(new HashSet<>(additionalFrequencyAwareWordPairs)));

        // Check learnt word pairs
        List<WordPair> learntWordPairs = fstMethod.getWordPairs();
        assertThat(learntWordPairs).containsAll(allWordPairs);

        // Save and reload
        Serializer<FSTMethod, FSTMethodMessage> serializer = new Serializer<>(new FSTMethodConverter(), fstMethod);
        Path file = Files.createTempFile("morpher", "fst");
        FSTMethod rebuiltFSTMethod;
        try {
            serializer.serialize(fstMethod, file);
            rebuiltFSTMethod = serializer.deserialize(file);
        }
        finally {
            Files.delete(file);
        }

        // Check inflection
        allWordPairs.forEach(wordPair -> {
            Word input = wordPair.getLeftWord();
            MethodResponse expected = MethodResponse.singleton(wordPair.getRightWord());
            Optional<MethodResponse> response = rebuiltFSTMethod.inflect(input);
            assertThat(response).hasValue(expected);
        });

        // Check lemmatization
        if (fstMethod.isUnidirectional()) {
            UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> rebuiltFSTMethod.lemmatize(allWordPairs.get(0).getRightWord()));
            assertThat(exception).hasMessage("Unidirectional FST method can only inflect but not lemmatize");
        }
        else {
            allWordPairs.forEach(wordPair -> {
                Word input = wordPair.getRightWord();
                MethodResponse expected = MethodResponse.singleton(wordPair.getLeftWord());
                Optional<MethodResponse> response = rebuiltFSTMethod.lemmatize(input);
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
