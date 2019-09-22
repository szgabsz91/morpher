package com.github.szgabsz91.morpher.methods.lattice.impl.builders;

import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.lattice.impl.Lattice;
import com.github.szgabsz91.morpher.methods.lattice.impl.LatticeTestHelper;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.ITrainingSetProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.IWordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.TrainingSetProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.WordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.utils.ExcludeDuringBuild;
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
                    ILatticeBuilder fullLatticeBuilder = new FullLatticeBuilder(characterRepository, wordConverter);
                    ILatticeBuilder homogeneousLatticeBuilder = new HomogeneousLatticeBuilder(characterRepository, wordConverter);
                    ILatticeBuilder minimalLatticeBuilder = new MinimalLatticeBuilder(characterRepository, wordConverter);
                    ILatticeBuilder maximalHomogeneousLatticeBuilder = new MaximalHomogeneousLatticeBuilder(characterRepository, wordConverter);
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
                                fullLatticeBuilder,
                                homogeneousLatticeBuilder,
                                minimalLatticeBuilder,
                                maximalHomogeneousLatticeBuilder
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
            ILatticeBuilder fullLatticeBuilder,
            ILatticeBuilder homogeneousLatticeBuilder,
            ILatticeBuilder minimalLatticeBuilder,
            ILatticeBuilder maximalHomogeneousLatticeBuilder) {
        fullLatticeBuilder.addRules(rules);
        Lattice fullLattice = fullLatticeBuilder.getLattice();
        homogeneousLatticeBuilder.addRules(rules);
        Lattice homogeneousLattice = homogeneousLatticeBuilder.getLattice();
        minimalLatticeBuilder.addRules(rules);
        Lattice minimalLattice = minimalLatticeBuilder.getLattice();
        maximalHomogeneousLatticeBuilder.addRules(rules);
        Lattice maximalHomogeneousLattice = maximalHomogeneousLatticeBuilder.getLattice();
        Set<Node> fullLatticeNodes = new HashSet<>(fullLattice.getNodes());
        Set<Node> homogeneousLatticeNodes = new HashSet<>(homogeneousLattice.getNodes());
        Set<Node> minimalLatticeNodes = new HashSet<>(minimalLattice.getNodes());
        Set<Node> maximalHomogeneousLatticeNodes = new HashSet<>(maximalHomogeneousLattice.getNodes());
        Set<Node> droppedNodes = new HashSet<>(fullLatticeNodes);
        droppedNodes.removeAll(homogeneousLatticeNodes);
        Set<Node> reinsertedNodes = new HashSet<>(droppedNodes);
        reinsertedNodes.retainAll(minimalLatticeNodes);
        LOGGER.info("FullLatticeBuilder produced {} nodes", fullLatticeNodes.size());
        LOGGER.info("HomogeneousLatticeBuilder produced {} nodes", homogeneousLatticeNodes.size());
        LOGGER.info("MinimalLatticeBuilder produced {} nodes", minimalLatticeNodes.size());
        LOGGER.info("MaximalHomogeneousLatticeBuilder produced {} nodes", maximalHomogeneousLatticeNodes.size());
        LOGGER.info("HomogeneousLatticeBuilder dropped {} nodes", droppedNodes.size());
        LOGGER.info("MinimalLatticeBuilder reinserted {} nodes among the dropped nodes", reinsertedNodes.size());
    }

}
