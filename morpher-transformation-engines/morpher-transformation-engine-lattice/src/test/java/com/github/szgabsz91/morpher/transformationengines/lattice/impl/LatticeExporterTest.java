package com.github.szgabsz91.morpher.transformationengines.lattice.impl;

import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IWordConverter;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.ILatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.MinimalLatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.ITrainingSetProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.IWordPairProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.TrainingSetProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.WordPairProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
import com.github.szgabsz91.morpher.transformationengines.lattice.utils.ExcludeDuringBuild;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@ExcludeDuringBuild
public class LatticeExporterTest {

    private static final int WORD_PAIRS = 200;

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    IWordConverter wordConverter = new DoubleConsonantWordConverter();
                    ICostCalculator costCalculator = new AttributeBasedCostCalculator();
                    IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                            .characterRepository(characterRepository)
                            .wordConverter(wordConverter)
                            .costCalculator(costCalculator)
                            .build();
                    ITrainingSetProcessor trainingSetProcessor = new TrainingSetProcessor(wordPairProcessor);
                    ILatticeBuilder latticeBuilder = new MinimalLatticeBuilder(characterRepository, wordConverter);

                    try {
                        List<WordPair> wordPairs = LatticeTestHelper.getList(WORD_PAIRS);

                        return Arguments.of(
                                characterRepository,
                                wordPairs,
                                trainingSetProcessor,
                                latticeBuilder
                        );
                    }
                    catch (IOException e) {
                        throw new RuntimeException("Training data could not be read", e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testNormally(
            ICharacterRepository characterRepository,
            List<WordPair> wordPairs,
            ITrainingSetProcessor trainingSetProcessor,
            ILatticeBuilder latticeBuilder) throws IOException {
        String filename = "build/lattice-nodes-for-" +
                WORD_PAIRS + "-word-pairs-with-" +
                characterRepository.getClass().getSimpleName().toLowerCase() + ".txt";
        Set<Rule> rules = trainingSetProcessor.induceRules(wordPairs);
        latticeBuilder.addRules(rules);
        Lattice lattice = latticeBuilder.getLattice();
        LatticeTestHelper.print(lattice, Paths.get(filename));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testReversed(
            ICharacterRepository characterRepository,
            List<WordPair> wordPairs,
            ITrainingSetProcessor trainingSetProcessor,
            ILatticeBuilder latticeBuilder) throws IOException {
        String filename = "build/lattice-nodes-reversed-for-" +
                WORD_PAIRS + "-word-pairs-with-" +
                characterRepository.getClass().getSimpleName().toLowerCase() + ".txt";
        wordPairs = wordPairs
                .stream()
                .map(wordPair -> WordPair.of(wordPair.getRightWord(), wordPair.getLeftWord()))
                .collect(toList());
        Set<Rule> rules = trainingSetProcessor.induceRules(wordPairs);
        latticeBuilder.addRules(rules);
        Lattice lattice = latticeBuilder.getLattice();
        LatticeTestHelper.print(lattice, Paths.get(filename));
    }

}
