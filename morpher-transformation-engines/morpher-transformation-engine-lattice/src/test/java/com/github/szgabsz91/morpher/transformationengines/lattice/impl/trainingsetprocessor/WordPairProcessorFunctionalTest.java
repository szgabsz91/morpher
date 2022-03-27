package com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.utils.TrainingDataRetriever;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IWordConverter;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.testutils.LazyLogger;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.model.WordPairProcessorResponse;
import com.github.szgabsz91.morpher.transformationengines.lattice.utils.ExcludeDuringBuild;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExcludeDuringBuild
public class WordPairProcessorFunctionalTest {

    private static final int DEFAULT_LIMIT = 1000;

    @TestFactory
    public Stream<DynamicTest> testFactory() throws IOException {
        Path folder = TrainingDataRetriever.getTrainingDataFile("CAS(ACC).csv").getParent();
        return Files.walk(folder)
                .filter(file -> Files.isRegularFile(file))
                .filter(file -> file.getFileName().toString().endsWith(".csv"))
                .flatMap(file -> {
                    String filename = file.getFileName().toString();
                    return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                            .flatMap(characterRepository -> {
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
                                List<DynamicTest> dynamicTests = new LinkedList<>();
                                dynamicTests.add(DynamicTest.dynamicTest(
                                        getClass().getSimpleName() + "." + filename.substring(0, filename.lastIndexOf(".")) + " with " + characterRepository.getClass().getSimpleName(),
                                        () -> testNormally(file, wordPairProcessor, cutWordPairProcessor)
                                ));
                                if (filename.endsWith("CAS(ACC).csv")) {
                                    dynamicTests.add(DynamicTest.dynamicTest(
                                            getClass().getSimpleName() + "." + filename.substring(0, filename
                                                    .lastIndexOf(".")) + " with " + characterRepository.getClass() .getSimpleName() + " (extended)",
                                            () -> testExtended(file, wordPairProcessor, cutWordPairProcessor)
                                    ));
                                }
                                return dynamicTests.stream();
                            });
                });
    }

    private void testNormally(Path file, IWordPairProcessor wordPairProcessor, IWordPairProcessor cutWordPairProcessor) throws IOException {
        String filename = file.getFileName().toString();
        String affixAnnotation = filename.substring(0, filename.lastIndexOf(".")).replace('(', '<').replace(')', '>');
        List<WordPair> normalWordPairs = getWordPairs(file);
        LazyLogger lazyLogger = new LazyLogger(LoggerFactory.getLogger(WordPairProcessorFunctionalTest.class), 60);
        for (WordPair wordPair : normalWordPairs) {
            lazyLogger.log(
                    "Testing WordPairProcessor for {} (normal)",
                    "Still testing WordPairProcessor for {} (normal)",
                    affixAnnotation
            );
            testWithWordPairProcessor(wordPair, wordPairProcessor);
            testWithWordPairProcessor(wordPair, cutWordPairProcessor);
        }
    }

    private static void testExtended(Path file, IWordPairProcessor wordPairProcessor, IWordPairProcessor cutWordPairProcessor) throws IOException {
        String filename = file.getFileName().toString();
        String affixAnnotation = filename.substring(0, filename.lastIndexOf(".")).replace('(', '<').replace(')', '>');
        List<WordPair> normalWordPairs = getWordPairs(file);
        WordPair[] extendedWordPairs = normalWordPairs
                .stream()
                .map(wordPair -> {
                    RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder()
                            .withinRange('a', 'z')
                            .build();
                    String randomSuffix = randomStringGenerator.generate(3);
                    return WordPair.of(wordPair.getLeftWord() + randomSuffix, wordPair.getRightWord() + randomSuffix);
                })
                .toArray(WordPair[]::new);
        LazyLogger lazyLogger = new LazyLogger(LoggerFactory.getLogger(WordPairProcessorFunctionalTest.class), 60);
        for (WordPair wordPair : extendedWordPairs) {
            lazyLogger.log(
                    "Testing WordPairProcessor for {} (extended)",
                    "Still testing WordPairProcessor for {} (extended)",
                    affixAnnotation
            );
            testWithWordPairProcessor(wordPair, wordPairProcessor);
            testWithWordPairProcessor(wordPair, cutWordPairProcessor);
        }
    }

    private static void testWithWordPairProcessor(WordPair wordPair, IWordPairProcessor wordPairProcessor) {
        WordPairProcessorResponse response = wordPairProcessor.induceRules(wordPair);
        for (Rule rule : response.getRules()) {
            Word input = wordPair.getLeftWord();
            Word expected = wordPair.getRightWord();
            assertThat(rule.transform(input))
                    .withFailMessage(rule + " failed for transform()")
                    .isEqualTo(expected);
            assertThat(rule.transformFromFront(input))
                    .withFailMessage(rule + " failed for transformFromFront()")
                    .isEqualTo(expected);
            assertThat(rule.transformFromBack(input))
                    .withFailMessage(rule + " failed for transformFromBack()")
                    .isEqualTo(expected);
        }
    }

    private static List<WordPair> getWordPairs(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return reader
                    .lines()
                    .limit(DEFAULT_LIMIT)
                    .map(line -> line.split(","))
                    .map(lineParts -> WordPair.of(lineParts[0], lineParts[1]))
                    .toList();
        }
    }

}
