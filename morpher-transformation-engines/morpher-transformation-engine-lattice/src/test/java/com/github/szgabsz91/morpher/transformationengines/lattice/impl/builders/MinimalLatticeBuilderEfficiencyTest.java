package com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders;

import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IWordConverter;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.Lattice;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.LatticeTestHelper;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.Node;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@ExcludeDuringBuild
public class MinimalLatticeBuilderEfficiencyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinimalLatticeBuilderEfficiencyTest.class);

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    IWordConverter wordConverter = new DoubleConsonantWordConverter();
                    ILatticeBuilder completeLatticeBuilder = new CompleteLatticeBuilder(characterRepository, wordConverter);
                    ILatticeBuilder consistentLatticeBuilder = new ConsistentLatticeBuilder(characterRepository, wordConverter);
                    ILatticeBuilder minimalLatticeBuilder = new MinimalLatticeBuilder(characterRepository, wordConverter);
                    ILatticeBuilder maximalConsistentLatticeBuilder = new MaximalConsistentLatticeBuilder(characterRepository, wordConverter);
                    ICostCalculator costCalculator = new AttributeBasedCostCalculator();
                    IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                            .characterRepository(characterRepository)
                            .wordConverter(wordConverter)
                            .costCalculator(costCalculator)
                            .build();
                    ITrainingSetProcessor trainingSetProcessor = new TrainingSetProcessor(wordPairProcessor);
                    try {
                        List<WordPair> wordPairs = LatticeTestHelper.getWindow(1);
                        Set<Rule> rules = trainingSetProcessor.induceRules(wordPairs);

                        return Arguments.of(
                                rules,
                                completeLatticeBuilder,
                                consistentLatticeBuilder,
                                minimalLatticeBuilder,
                                maximalConsistentLatticeBuilder
                        );
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(
            Set<Rule> rules,
            ILatticeBuilder completeLatticeBuilder,
            ILatticeBuilder consistentLatticeBuilder,
            ILatticeBuilder minimalLatticeBuilder,
            ILatticeBuilder maximalConsistentLatticeBuilder) {
        completeLatticeBuilder.addRules(rules);
        Lattice completeLattice = completeLatticeBuilder.getLattice();
        consistentLatticeBuilder.addRules(rules);
        Lattice consistentLattice = consistentLatticeBuilder.getLattice();
        minimalLatticeBuilder.addRules(rules);
        Lattice minimalLattice = minimalLatticeBuilder.getLattice();
        maximalConsistentLatticeBuilder.addRules(rules);
        Lattice maximalConsistentLattice = maximalConsistentLatticeBuilder.getLattice();
        Set<Node> completeLatticeNodes = new HashSet<>(completeLattice.getNodes());
        Set<Node> consistentLatticeNodes = new HashSet<>(consistentLattice.getNodes());
        Set<Node> minimalLatticeNodes = new HashSet<>(minimalLattice.getNodes());
        Set<Node> maximalConsistentLatticeNodes = new HashSet<>(maximalConsistentLattice.getNodes());
        Set<Node> droppedNodes = new HashSet<>(completeLatticeNodes);
        droppedNodes.removeAll(consistentLatticeNodes);
        Set<Node> reinsertedNodes = new HashSet<>(droppedNodes);
        reinsertedNodes.retainAll(minimalLatticeNodes);
        LOGGER.info("CompleteLatticeBuilder produced {} nodes", completeLattice.size());
        LOGGER.info("ConsistentLatticeBuilder produced {} nodes", consistentLattice.size());
        LOGGER.info("MinimalLatticeBuilder produced {} nodes", minimalLatticeNodes.size());
        LOGGER.info("MaximalConsistentLatticeBuilder produced {} nodes", maximalConsistentLatticeNodes.size());
        LOGGER.info("ConsistentLatticeBuilder dropped {} nodes", droppedNodes.size());
        LOGGER.info("MinimalLatticeBuilder reinserted {} nodes among the dropped nodes", reinsertedNodes.size());
    }

}
