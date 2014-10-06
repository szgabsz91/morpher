package com.github.szgabsz91.morpher.methods.lattice.impl;

import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.core.utils.ExcludeDuringBuild;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.ILatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.MinimalLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.ITrainingSetProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.IWordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.TrainingSetProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.WordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@ExcludeDuringBuild
public class LatticeMaximalHomogeneousNodesTest {

    private static final int WORD_PAIRS = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger(LatticeMaximalHomogeneousNodesTest.class);

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(ICharacterRepository characterRepository) throws IOException {
        List<WordPair> wordPairs = LatticeTestHelper.getList(WORD_PAIRS);
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .maximalContextSize(3)
                .build();
        ITrainingSetProcessor trainingSetProcessor = new TrainingSetProcessor(wordPairProcessor);
        Set<Rule> rules = trainingSetProcessor.induceRules(wordPairs);
        ILatticeBuilder latticeBuilder = new MinimalLatticeBuilder(characterRepository, wordConverter);
        latticeBuilder.addRules(rules);
        Lattice lattice = latticeBuilder.getLattice();
        Set<Node> maximalHomogeneousNodes = lattice.getMaximalHomogeneousNodes();
        LOGGER.info("Lattice contains {} maximal homogeneous nodes", maximalHomogeneousNodes.size());
        Path outputFile = Paths.get("build/maximal-homogeneous-nodes-cut-for-" + WORD_PAIRS + "-word-pairs-using-" + characterRepository.getClass().getSimpleName().toLowerCase() + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            for (Node maximalHomogeneousNode : maximalHomogeneousNodes) {
                LOGGER.info("{}", maximalHomogeneousNode);
                LatticeTestHelper.printVerbose(writer, maximalHomogeneousNode, 0);
            }
        }
    }

}
