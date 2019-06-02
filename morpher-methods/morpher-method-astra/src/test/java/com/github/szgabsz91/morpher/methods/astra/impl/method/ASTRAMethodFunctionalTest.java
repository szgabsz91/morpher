package com.github.szgabsz91.morpher.methods.astra.impl.method;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.methods.api.IMorpherMethod;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.github.szgabsz91.morpher.methods.api.utils.TrainingDataRetriever;
import com.github.szgabsz91.morpher.methods.astra.config.ASTRAMethodConfiguration;
import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import com.github.szgabsz91.morpher.methods.astra.converters.ASTRAMethodConverter;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.ASTRAMethodMessage;
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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public class ASTRAMethodFunctionalTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ASTRAMethodFunctionalTest.class);

    public static Stream<Arguments> parameters() {
        return Stream.of(
                new ASTRAMethodConfiguration.Builder()
                        .searcherType(SearcherType.SEQUENTIAL)
                        .build(),
                new ASTRAMethodConfiguration.Builder()
                        .searcherType(SearcherType.PARALLEL)
                        .build(),
                new ASTRAMethodConfiguration.Builder()
                        .searcherType(SearcherType.PREFIX_TREE)
                        .build()
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(ASTRAMethodConfiguration configuration) throws IOException {
        LOGGER.debug("Using configuration: {}", configuration);

        ASTRAAbstractMethodFactory astraAbstractMethodFactory = new ASTRAAbstractMethodFactory(configuration);
        Supplier<IMorpherMethod<?>> supplier = astraAbstractMethodFactory.getBidirectionalFactory(AffixType.of("<CAS<ACC>>"));
        ASTRAMethod astraMethod = (ASTRAMethod) supplier.get();

        try (BufferedReader reader = Files.newBufferedReader(TrainingDataRetriever.getTrainingDataFile("CAS(ACC).csv"), StandardCharsets.UTF_8)) {
            List<FrequencyAwareWordPair> wordPairs = reader
                    .lines()
                    .limit(1000L)
                    .map(line -> line.split(","))
                    .map(lineParts -> FrequencyAwareWordPair.of(lineParts[0], lineParts[1], Integer.parseInt(lineParts[2])))
                    .collect(toList());
            wordPairs = removeDuplicates(wordPairs);
            List<FrequencyAwareWordPair> wordPairs1 = wordPairs.subList(0, wordPairs.size() / 3);
            List<FrequencyAwareWordPair> wordPairs2 = wordPairs.subList(wordPairs.size() / 3, 2 * wordPairs.size() / 3);
            List<FrequencyAwareWordPair> wordPairs1_2 = new ArrayList<>(wordPairs1);
            wordPairs1_2.addAll(wordPairs2);
            List<FrequencyAwareWordPair> wordPairs3 = wordPairs.subList(2 * wordPairs.size() / 3, wordPairs.size());
            List<FrequencyAwareWordPair> wordPairs1_3 = new ArrayList<>(wordPairs1_2);
            wordPairs1_3.addAll(wordPairs3);

            astraMethod = assertASTRAMethod(astraMethod, wordPairs1, wordPairs1);
            astraMethod = assertASTRAMethod(astraMethod, wordPairs2, wordPairs1_2);
            astraMethod = assertASTRAMethod(astraMethod, wordPairs3, wordPairs1_3);
        }
    }

    private static ASTRAMethod assertASTRAMethod(ASTRAMethod astraMethod, List<FrequencyAwareWordPair> additionalWordPairs, List<FrequencyAwareWordPair> allWordPairs) throws IOException {
        // Teach
        astraMethod.learn(TrainingSet.of(new HashSet<>(additionalWordPairs)));

        // Save and reload
        Serializer<ASTRAMethod, ASTRAMethodMessage> serializer = new Serializer<>(new ASTRAMethodConverter(), astraMethod);
        Path file = Files.createTempFile("morpher", "astra");
        ASTRAMethod rebuiltASTRAMethod;
        try {
            serializer.serialize(astraMethod, file);
            rebuiltASTRAMethod = serializer.deserialize(file);
        }
        finally {
            Files.delete(file);
        }

        // Check inflection
        allWordPairs.forEach(wordPair -> {
            Word input = wordPair.getLeftWord();
            MethodResponse expected = MethodResponse.singleton(wordPair.getRightWord());
            Optional<MethodResponse> response = rebuiltASTRAMethod.inflect(input);
            assertThat(response)
                    .withFailMessage("Inflection error: %s --> %s instead of %s", input, response, expected)
                    .hasValue(expected);
        });

        // Check lemmatization
        allWordPairs.forEach(wordPair -> {
            Word input = wordPair.getRightWord();
            MethodResponse expected = MethodResponse.singleton(wordPair.getLeftWord());
            Optional<MethodResponse> response = rebuiltASTRAMethod.lemmatize(input);
            assertThat(response)
                    .withFailMessage("Lemmatization error: %s --> %s instead of %s", input, response, expected)
                    .hasValue(expected);
        });

        return astraMethod;
    }

    private static List<FrequencyAwareWordPair> removeDuplicates(List<FrequencyAwareWordPair> wordPairs) {
        return new ArrayList<>(wordPairs
                .stream()
                .collect(toMap(FrequencyAwareWordPair::getLeftWord, Function.identity(), (x, y) -> x))
                .values());
    }

}
