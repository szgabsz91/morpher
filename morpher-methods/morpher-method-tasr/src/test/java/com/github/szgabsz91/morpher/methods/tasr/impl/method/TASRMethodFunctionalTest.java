package com.github.szgabsz91.morpher.methods.tasr.impl.method;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.github.szgabsz91.morpher.methods.tasr.converters.TASRMethodConverter;
import com.github.szgabsz91.morpher.methods.tasr.protocolbuffers.TASRMethodMessage;
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

public class TASRMethodFunctionalTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TASRMethodFunctionalTest.class);

    private static Stream<Arguments> parameters() {
        AffixType affixType = AffixType.of("AFF");
        return Stream.of(new TASRMethod(false, affixType), new TASRMethod(true, affixType))
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testWithUnidirectionalMethod(TASRMethod tasrMethod) throws IOException {
        LOGGER.debug("Testing with {} TASR method", tasrMethod.isUnidirectional() ? "unidirectional" : "bidirectional");

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

            Set<Word> skippedLemmatizationInputs = Set.of(
                    Word.of("wed")
            );
            assertTASRMethod(tasrMethod, wordPairs1, wordPairs1, skippedLemmatizationInputs);
            assertTASRMethod(tasrMethod, wordPairs2, wordPairs1_2, skippedLemmatizationInputs);
            assertTASRMethod(tasrMethod, wordPairs3, wordPairs1_3, skippedLemmatizationInputs);
        }
    }

    private static void assertTASRMethod(TASRMethod tasrMethod, List<WordPair> additionalWordPairs, List<WordPair> allWordPairs, Set<Word> skippedLemmatizationInputs) throws IOException {
        // Teach
        List<FrequencyAwareWordPair> additionalFrequencyAwareWordPairs = additionalWordPairs
                .stream()
                .map(FrequencyAwareWordPair::of)
                .collect(toList());
        tasrMethod.learn(TrainingSet.of(new HashSet<>(additionalFrequencyAwareWordPairs)));

        // Save and reload
        Serializer<TASRMethod, TASRMethodMessage> serializer = new Serializer<>(new TASRMethodConverter(), tasrMethod);
        Path file = Files.createTempFile("morpher", "tasr");
        final TASRMethod rebuiltTASRMethod;
        try {
            serializer.serialize(tasrMethod, file);
            rebuiltTASRMethod = serializer.deserialize(file);
        }
        finally {
            Files.delete(file);
        }

        // Check inflection
        allWordPairs.forEach(wordPair -> {
            Word input = wordPair.getLeftWord();
            MethodResponse expected = MethodResponse.singleton(wordPair.getRightWord());
            Optional<MethodResponse> response = rebuiltTASRMethod.inflect(input);
            assertThat(response)
                    .withFailMessage("Invalid inflection: %s --> %s instead of %s", input, response, expected)
                    .hasValue(expected);
        });

        // Check lemmatization
        if (tasrMethod.isUnidirectional()) {
            UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> rebuiltTASRMethod.lemmatize(allWordPairs.get(0).getRightWord()));
            assertThat(exception).hasMessage("Unidirectional TASR method can only inflect but not lemmatize");
        }
        else {
            allWordPairs.forEach(wordPair -> {
                Word input = wordPair.getRightWord();
                if (skippedLemmatizationInputs.contains(input)) {
                    return;
                }
                MethodResponse expected = MethodResponse.singleton(wordPair.getLeftWord());
                Optional<MethodResponse> response = rebuiltTASRMethod.lemmatize(input);
                assertThat(response)
                        .withFailMessage("Invalid lemmatization: %s --> %s instead of %s", input, response, expected)
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
