package com.github.szgabsz91.morpher.methods.astra.impl;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.utils.TrainingDataRetriever;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IdentityWordConverter;
import com.github.szgabsz91.morpher.methods.astra.config.AtomicRuleFitnessCalculatorType;
import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import com.github.szgabsz91.morpher.methods.astra.impl.factories.ComponentFactory;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.atomicrule.DefaultAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.SequentialSearcher;
import com.github.szgabsz91.morpher.methods.astra.impl.testutils.ASTRABuilder;
import com.github.szgabsz91.morpher.methods.astra.utils.ExcludeDuringBuild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

@ExcludeDuringBuild
public class ASTRAFunctionalTest {

    private Set<FrequencyAwareWordPair> wordPairs;

    public static Stream<Arguments> parameters() {
        AffixType affixType = AffixType.of("<CAS<ACC>>");
        IASTRA astraWithoutDoubleConsonantSupportAndWithoutAttributes = new ASTRABuilder()
                .affixType(affixType)
                .wordConverter(new IdentityWordConverter())
                .characterRepository(HungarianSimpleCharacterRepository.get())
                .searcher(new SequentialSearcher(new DefaultAtomicRuleFitnessCalculator(new IdentityWordConverter(), HungarianSimpleCharacterRepository.get(), 2), false))
                .build();
        IASTRA astraWithoutDoubleConsonantSupportAndWithAttributes = new ASTRABuilder()
                .affixType(affixType)
                .wordConverter(new IdentityWordConverter())
                .characterRepository(HungarianAttributedCharacterRepository.get())
                .searcher(new SequentialSearcher(new DefaultAtomicRuleFitnessCalculator(new IdentityWordConverter(), HungarianAttributedCharacterRepository.get(), 2), false))
                .build();
        IASTRA astraWithDoubleConsonantSupportAndWithoutAttributes = new ASTRABuilder()
                .affixType(affixType)
                .wordConverter(new DoubleConsonantWordConverter())
                .characterRepository(HungarianSimpleCharacterRepository.get())
                .searcher(new SequentialSearcher(new DefaultAtomicRuleFitnessCalculator(new DoubleConsonantWordConverter(), HungarianSimpleCharacterRepository.get(), 2), false))
                .build();
        IASTRA astraWithDoubleConsonantSupportAndWithAttributes = new ASTRABuilder()
                .affixType(affixType)
                .wordConverter(new DoubleConsonantWordConverter())
                .characterRepository(HungarianAttributedCharacterRepository.get())
                .searcher(new SequentialSearcher(new DefaultAtomicRuleFitnessCalculator(new DoubleConsonantWordConverter(), HungarianAttributedCharacterRepository.get(), 2), false))
                .build();
        IASTRA astraWithParallelSearcher = new ASTRABuilder()
                .affixType(affixType)
                .searcher(ComponentFactory.createSearcher(SearcherType.PARALLEL, false, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0))
                .build();
        IASTRA astraWithPrefixTreeSearcher = new ASTRABuilder()
                .affixType(affixType)
                .searcher(ComponentFactory.createSearcher(SearcherType.PREFIX_TREE, false, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0))
                .build();

        return Stream.of(
                astraWithoutDoubleConsonantSupportAndWithoutAttributes,
                astraWithoutDoubleConsonantSupportAndWithAttributes,
                astraWithDoubleConsonantSupportAndWithoutAttributes,
                astraWithDoubleConsonantSupportAndWithAttributes,
                astraWithParallelSearcher,
                astraWithPrefixTreeSearcher
        ).map(Arguments::of);
    }

    @BeforeEach
    public void setUp() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(TrainingDataRetriever.getTrainingDataFile("CAS(ACC).csv"))) {
            Map<Word, List<FrequencyAwareWordPair>> mapping = reader
                    .lines()
                    .limit(3000L)
                    .map(line -> line.split(","))
                    .map(lineParts -> FrequencyAwareWordPair.of(lineParts[0], lineParts[1], Integer.parseInt(lineParts[2])))
                    .collect(groupingBy(FrequencyAwareWordPair::getLeftWord));

            this.wordPairs = mapping.values()
                    .stream()
                    .map(list -> list.get(0))
                    .collect(toSet());
        }
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(IASTRA astra) {
        astra.learnWordPairs(this.wordPairs);
        List<FrequencyAwareWordPair> wordPairList = new ArrayList<>(wordPairs);

        for (int index = 0; index < wordPairList.size(); index++) {
            FrequencyAwareWordPair wordPair = wordPairList.get(index);
            Word rootForm = wordPair.getLeftWord();
            Word inflectedForm = wordPair.getRightWord();
            Optional<MethodResponse> inflectionResponse = astra.inflect(rootForm);
            assertThat(inflectionResponse)
                    .withFailMessage("#" + index + ": " + rootForm + " --> " + inflectionResponse + " (" + inflectedForm + ")")
                    .hasValue(MethodResponse.singleton(inflectedForm));
            Optional<MethodResponse> lemmatizationResponse = astra.lemmatize(inflectedForm);
            assertThat(lemmatizationResponse)
                    .withFailMessage("#" + index + ": " + inflectedForm + " --> " + lemmatizationResponse + " (" + rootForm + ")")
                    .hasValue(MethodResponse.singleton(rootForm));
        }
    }

}
