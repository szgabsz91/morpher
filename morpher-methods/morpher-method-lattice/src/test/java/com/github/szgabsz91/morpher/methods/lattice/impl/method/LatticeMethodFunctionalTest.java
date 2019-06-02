package com.github.szgabsz91.morpher.methods.lattice.impl.method;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.IMorpherMethod;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.github.szgabsz91.morpher.methods.api.utils.TrainingDataRetriever;
import com.github.szgabsz91.morpher.methods.lattice.config.CharacterRepositoryType;
import com.github.szgabsz91.morpher.methods.lattice.config.CostCalculatorType;
import com.github.szgabsz91.morpher.methods.lattice.config.LatticeBuilderType;
import com.github.szgabsz91.morpher.methods.lattice.config.LatticeMethodConfiguration;
import com.github.szgabsz91.morpher.methods.lattice.config.WordConverterType;
import com.github.szgabsz91.morpher.methods.lattice.converters.LatticeMethodConverter;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.LatticeMethodMessage;
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

public class LatticeMethodFunctionalTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LatticeMethodFunctionalTest.class);

    public static Stream<Arguments> parameters() {
        return Stream.of(
                createConfiguration(LatticeBuilderType.FULL, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, Integer.MAX_VALUE),
                createConfiguration(LatticeBuilderType.HOMOGENEOUS, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, Integer.MAX_VALUE)
        ).map(Arguments::of);
    }

    private static LatticeMethodConfiguration createConfiguration(
            LatticeBuilderType latticeBuilderType,
            WordConverterType wordConverterType,
            CostCalculatorType costCalculatorType,
            CharacterRepositoryType characterRepositoryType,
            int maximalContextSize) {
        return new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(latticeBuilderType)
                .wordConverterType(wordConverterType)
                .costCalculatorType(costCalculatorType)
                .characterRepositoryType(characterRepositoryType)
                .maximalContextSize(maximalContextSize)
                .build();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(LatticeMethodConfiguration configuration) throws IOException {
        LOGGER.debug("Using configuration: {}", configuration);

        AffixType affixType = AffixType.of("AFF");
        LatticeAbstractMethodFactory latticeAbstractMethodFactory = new LatticeAbstractMethodFactory(configuration);
        Supplier<IMorpherMethod<?>> supplier = latticeAbstractMethodFactory.getBidirectionalFactory(affixType);
        LatticeMethod latticeMethod = (LatticeMethod) supplier.get();

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

            assertLatticeMethod(latticeMethod, wordPairs1, wordPairs1);
            assertLatticeMethod(latticeMethod, wordPairs2, wordPairs1_2);
            assertLatticeMethod(latticeMethod, wordPairs3, wordPairs1_3);
        }
    }

    private static void assertLatticeMethod(LatticeMethod latticeMethod, List<WordPair> additionalWordPairs, List<WordPair> allWordPairs) throws IOException {
        // Teach
        List<FrequencyAwareWordPair> additionalFrequencyAwareWordPairs = additionalWordPairs
                .stream()
                .map(FrequencyAwareWordPair::of)
                .collect(toList());
        latticeMethod.learn(TrainingSet.of(new HashSet<>(additionalFrequencyAwareWordPairs)));

        // Save and reload
        Serializer<LatticeMethod, LatticeMethodMessage> serializer = new Serializer<>(new LatticeMethodConverter(), latticeMethod);
        Path file = Files.createTempFile("morpher", "lattice");
        LatticeMethod rebuiltLatticeMethod;
        try {
            serializer.serialize(latticeMethod, file);
            rebuiltLatticeMethod = serializer.deserialize(file);
        }
        finally {
            Files.delete(file);
        }

        // Check inflection
        allWordPairs.forEach(wordPair -> {
            Word input = wordPair.getLeftWord();
            MethodResponse expected = MethodResponse.singleton(wordPair.getRightWord());
            Optional<MethodResponse> response = rebuiltLatticeMethod.inflect(input);
            assertThat(response)
                    .withFailMessage("Inflection error: %s --> %s instead of %s", input, response, expected)
                    .hasValue(expected);
        });

        // Check lemmatization
        allWordPairs.forEach(wordPair -> {
            Word input = wordPair.getRightWord();
            MethodResponse expected = MethodResponse.singleton(wordPair.getLeftWord());
            Optional<MethodResponse> response = rebuiltLatticeMethod.lemmatize(input);
            assertThat(response)
                    .withFailMessage("Lemmatization error: %s --> %s instead of %s", input, response, expected)
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
