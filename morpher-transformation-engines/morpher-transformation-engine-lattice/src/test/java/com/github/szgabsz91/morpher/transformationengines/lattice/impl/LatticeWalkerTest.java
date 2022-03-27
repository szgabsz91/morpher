package com.github.szgabsz91.morpher.transformationengines.lattice.impl;

import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.characters.ICharacter;
import com.github.szgabsz91.morpher.transformationengines.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.Vowel;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.LipShape;
import com.github.szgabsz91.morpher.transformationengines.api.utils.TrainingDataRetriever;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IWordConverter;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.ILatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.MinimalLatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.UnitNode;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.ZeroNode;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Context;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.ITransformation;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.ITrainingSetProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.IWordPairProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.TrainingSetProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.WordPairProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;

public class LatticeWalkerTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    IWordConverter wordConverter = new DoubleConsonantWordConverter();
                    Lattice lattice = createLattice(characterRepository, wordConverter);
                    LatticeWalker latticeWalker = new LatticeWalker(lattice);
                    return Arguments.of(
                            characterRepository,
                            wordConverter,
                            lattice,
                            latticeWalker
                    );
                });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testFindSingleNode(ICharacterRepository characterRepository, IWordConverter wordConverter, Lattice lattice, LatticeWalker latticeWalker) {
        Node a = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        ));
        Node b = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("b")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        ));
        Node c = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("c")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        ));
        Node e = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("e")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        ));
        Node i = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("i")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        ));
        Node o = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("o")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        ));

        /*
         * - 1
         *     - a
         *         - i
         *             - c
         *                 - 0
         *             - o
         *                 - 0
         *     - e
         *         - i
         *             - c
         *                 - 0
         *     - b
         *         - 0
         */
        lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        lattice.getUnitNode().getChildren().remove(lattice.getZeroNode());
        lattice.getZeroNode().getParents().remove(lattice.getUnitNode());
        lattice.addNodes(new Node[] { a, b, c, e, i });
        lattice.getUnitNode().addChild(a);
        lattice.getUnitNode().addChild(b);
        lattice.getUnitNode().addChild(e);
        a.addChild(i);
        e.addChild(i);
        b.addChild(lattice.getZeroNode());
        i.addChild(c);
        i.addChild(o);
        o.addChild(lattice.getZeroNode());
        c.addChild(lattice.getZeroNode());
        latticeWalker = new LatticeWalker(lattice);

        Node result = latticeWalker.findSingleNode(
                node -> node.getRule() != null &&
                        !node.getRule().getContext().getCore().isEmpty() &&
                        HungarianAttributedCharacterRepository.VOWELS.contains(node.getRule().getContext().getCore().get(0).toString()),
                node -> node.getRule() != null && "i".equals(node.getRule().getContext().getCore().get(0).toString()),
                null
        );

        assertThat(result).isNotNull();
        assertThat(result).isNotInstanceOf(UnitNode.class);
        assertThat(result.getRule()).isNotNull();
        assertThat(result.getRule().getContext()).isNotNull();
        assertThat(result.getRule().getContext().getCore()).isNotNull();
        assertThat(result.getRule().getContext().getCore()).hasSize(1);
        assertThat(result.getRule().getContext().getCore()).containsSequence(s("i", characterRepository));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testWalk(ICharacterRepository characterRepository, IWordConverter wordConverter, Lattice lattice, LatticeWalker latticeWalker) {
        /*
         * - 1
         *     - a
         *         - i
         *             - 0
         *     - e
         *         - i
         *             - 0
         *     - d
         *         - 0
         *     - o
         *         - 0
         */
        lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        latticeWalker = new LatticeWalker(lattice);

        Node a = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("a")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        null,
                        characterRepository,
                        null
                )
        );
        Node e = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("e")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        null,
                        characterRepository,
                        null
                )
        );
        Node i = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("i")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        null,
                        characterRepository,
                        null
                )
        );
        Node d = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("d")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        null,
                        characterRepository,
                        null
                )
        );
        Node o = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("o")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        null,
                        characterRepository,
                        null
                )
        );
        lattice.getUnitNode().getChildren().clear();
        lattice.getZeroNode().getParents().clear();
        lattice.getUnitNode().addChild(a);
        lattice.getUnitNode().addChild(d);
        lattice.getUnitNode().addChild(e);
        lattice.getUnitNode().addChild(o);
        a.addChild(i);
        e.addChild(i);
        i.addChild(lattice.getZeroNode());
        d.addChild(lattice.getZeroNode());
        o.addChild(lattice.getZeroNode());
        lattice.addNodes(new Node[] { a, e, i, d, o });

        Predicate<Node> nodePredicate =
                node ->
                        node.getRule() != null && node.getRule().getContext() != null &&
                                !node.getRule().getContext().getPrefix().isEmpty() &&
                                HungarianAttributedCharacterRepository.VOWELS.contains(node.getRule().getContext().getPrefix().get(0).toString());

        Set<Node> result = latticeWalker.walk(
                lattice.getUnitNode(),
                nodePredicate,
                Node::getChildren,
                ProcessingType.CHILD_LIST_SEARCH,
                lattice.getNextProcessingStatus(ProcessingType.CHILD_LIST_SEARCH)
        );
        Set<Node> expected = Set.of(
                getNodeByContextStart("i", lattice, characterRepository),
                getNodeByContextStart("o", lattice, characterRepository)
        );

        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testWalkWithNoPostProcessor(ICharacterRepository characterRepository, IWordConverter wordConverter, Lattice lattice, LatticeWalker latticeWalker) {
        Set<Node> result = latticeWalker.walk(
                lattice.getUnitNode(),
                node -> true,
                Node::getChildren,
                ProcessingType.GENERAL_PROCESSING,
                lattice.getNextProcessingStatus(ProcessingType.GENERAL_PROCESSING),
                null
        );
        assertThat(result).hasSize(1);
        assertThat(result).containsSequence(lattice.getZeroNode());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testWalkWithPostProcessor(ICharacterRepository characterRepository, IWordConverter wordConverter, Lattice lattice, LatticeWalker latticeWalker) {
        long expected = 100L;
        Set<Node> result = latticeWalker.walk(
                lattice.getUnitNode(),
                node -> true,
                Node::getChildren,
                ProcessingType.GENERAL_PROCESSING,
                lattice.getNextProcessingStatus(ProcessingType.GENERAL_PROCESSING),
                node -> node.setFrequency(expected)
        );
        assertThat(result).hasSize(1);
        assertThat(result).containsSequence(lattice.getZeroNode());
        assertThat(lattice.getUnitNode().getFrequency()).isEqualTo(expected);
        assertThat(lattice.getZeroNode().getFrequency()).isEqualTo(2L);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testProcessNodesOnce(ICharacterRepository characterRepository, IWordConverter wordConverter, Lattice lattice, LatticeWalker latticeWalker) {
        long newLevel = 500L;
        latticeWalker.processNodesOnce(node -> {
            if (node.getLevel() == newLevel) {
                node.setLevel(node.getLevel() + 1);
            }
            else {
                node.setLevel(newLevel);
            }
        });

        assertThat(lattice.getUnitNode().getLevel()).isEqualTo(newLevel);
        assertThat(lattice.getZeroNode().getLevel()).isEqualTo(newLevel);
        lattice.getNodes().forEach(node -> {
            assertThat(node.getLevel())
                    .withFailMessage(node.toString())
                    .isEqualTo(newLevel);
        });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testFillNodeLevels(ICharacterRepository characterRepository, IWordConverter wordConverter, Lattice lattice, LatticeWalker latticeWalker) {
        lattice.getUnitNode().setLevel(-1L);
        lattice.getZeroNode().setLevel(-1L);
        lattice.getNodes().forEach(n -> n.setLevel(-1L));

        latticeWalker.fillNodeLevels();
        long result = lattice.getZeroNodeLevel();
        assertThat(result).isEqualTo(4L);

        assertThat(lattice.getUnitNode().getLevel()).isEqualTo(0L);
        lattice.getNodes().forEach(this::assertNodeLevel);
        assertNodeLevel(lattice.getZeroNode());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testFillDominantRules(ICharacterRepository characterRepository, IWordConverter wordConverter, Lattice lattice, LatticeWalker latticeWalker) {
        Node ax = this.getNodeByContextStart("ax", lattice, characterRepository);
        ax.incrementFrequency();
        ax.incrementFrequency();
        ax.incrementFrequency();
        ax.incrementFrequency();

        latticeWalker.fillDominantRules();

        assertTransformation("e", "c", lattice, characterRepository);
        assertTransformation("d", "b", lattice, characterRepository);
        assertTransformation("y", "b", lattice, characterRepository);
        assertTransformation("w", "b", lattice, characterRepository);
        assertTransformation("z", "y", lattice, characterRepository);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testFillDominantRulesWithHigherCoverage(ICharacterRepository characterRepository, IWordConverter wordConverter, Lattice lattice, LatticeWalker latticeWalker) {
        Node inconsistentNode = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("a")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        null,
                        characterRepository,
                        null
                )
        );
        inconsistentNode.setInconsistent();
        Node consistentNode1 = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("b")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("a").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )
        );
        consistentNode1.setFrequency(2L);
        Node consistentNode2 = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("c")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("b").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )
        );
        consistentNode2.setFrequency(1L);
        Node consistentNode3 = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("d")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("c").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )
        );
        consistentNode3.setFrequency(3L);
        inconsistentNode.getChildren().addAll(List.of(consistentNode1, consistentNode2, consistentNode3));
        lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        lattice.getUnitNode().getChildren().add(inconsistentNode);
        lattice.getZeroNode().getParents().addAll(List.of(consistentNode1, consistentNode2, consistentNode3));
        lattice.addNodes(new Node[] { inconsistentNode, consistentNode1, consistentNode2, consistentNode3 });
        latticeWalker = new LatticeWalker(lattice);
        latticeWalker.fillDominantRules();
        assertThat(inconsistentNode.getRule().getTransformations()).isEqualTo(consistentNode3.getRule().getTransformations());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetMaximalConsistentNodes(ICharacterRepository characterRepository, IWordConverter wordConverter, Lattice lattice, LatticeWalker latticeWalker) {
        Set<Node> result = latticeWalker.getMaximalConsistentNodes();
        Set<Node> expected = Set.of(
                getNodeByPattern("ax", lattice, characterRepository),
                getNodeByPattern("f", lattice, characterRepository),
                getNodeByPattern("g", lattice, characterRepository)
        );
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testGetMaximalConsistentNodesRegression() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        Node consistentParent = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("a")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                ),
                false
        );
        Node inconsistentParent = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("e")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                ),
                true
        );
        Node consistentChild = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("i")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                ),
                false
        );
        lattice.getUnitNode().getChildren().clear();
        lattice.getZeroNode().getParents().clear();
        lattice.getUnitNode().addChild(consistentParent);
        lattice.getUnitNode().addChild(inconsistentParent);
        consistentParent.addChild(consistentChild);
        inconsistentParent.addChild(consistentChild);
        consistentChild.addChild(lattice.getZeroNode());
        LatticeWalker latticeWalker = new LatticeWalker(lattice);
        Set<Node> result = latticeWalker.getMaximalConsistentNodes();
        assertThat(result).hasSize(1);
        assertThat(result).containsSequence(consistentParent);
    }

    @Test
    public void testImprovedSearchAlgorithm() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        Node node1 = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(Vowel.create(Length.SHORT)),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                ),
                true
        );
        Node node2 = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(Vowel.create(Length.SHORT, LipShape.ROUNDED)),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                ),
                true
        );
        Node node3 = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("a")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                ),
                true
        );
        Node node31 = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(
                                        characterRepository.getCharacter("t"),
                                        characterRepository.getCharacter("a")
                                ),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                ),
                true
        );
        Node node32 = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(
                                        characterRepository.getCharacter("v"),
                                        characterRepository.getCharacter("a")
                                ),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                ),
                false
        );
        lattice.getUnitNode().getChildren().clear();
        lattice.getUnitNode().getChildren().addAll(List.of(node1, node2, node3));
        node1.getChildren().add(lattice.getZeroNode());
        node2.getChildren().add(lattice.getZeroNode());
        node3.getChildren().addAll(List.of(node31, node32));
        node31.getChildren().add(lattice.getZeroNode());
        node32.getChildren().add(lattice.getZeroNode());

        LatticeWalker latticeWalker = new LatticeWalker(lattice);
        Node result = latticeWalker.findSingleNode(
                n -> true,
                Node::isConsistent,
                null
        );
        assertThat(result).isEqualTo(node32);
    }

    @Test
    public void testGetMaximalConsistentDescendants() throws IOException {
        Set<WordPair> wordPairs;
        try (BufferedReader reader = Files.newBufferedReader(TrainingDataRetriever.getTrainingDataFile("CAS(ACC).csv"), StandardCharsets.UTF_8)) {
            wordPairs = reader.lines()
                    .limit(200L)
                    .map(line -> line.split(","))
                    .map(lineParts -> WordPair.of(lineParts[0], lineParts[1]))
                    .collect(toUnmodifiableSet());
        }
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
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
        LatticeWalker latticeWalker = new LatticeWalker(lattice);
        lattice.getNodes()
                .forEach(node -> {
                    Node[] maximalConsistentDescendants = latticeWalker.getMaximalConsistentDescendants(node);

                    for (Node maximalConsistentDescendant : maximalConsistentDescendants) {
                        for (Node parent : maximalConsistentDescendant.getParents()) {
                            assertThat(parent.isConsistent())
                                    .withFailMessage(parent + " cannot be consistent so that " + maximalConsistentDescendant + " " + "can be a maximal consistent descendant")
                                    .isFalse();
                        }
                    }
                });
    }

    private static Lattice createLattice(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node unitNode = new UnitNode();
        Node inconsistent1 = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("e")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        null,
                        characterRepository,
                        null
                ),
                true
        );
        Node inconsistent2 = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("d")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        null,
                        characterRepository,
                        null
                ),
                true
        );
        Node inconsistent3 = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("x")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        null,
                        characterRepository,
                        null
                ),
                true
        );
        Node inconsistent4 = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("y")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        null,
                        characterRepository,
                        null
                ),
                true
        );
        Node inconsistent5 = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("w")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        null,
                        characterRepository,
                        null
                ),
                true
        );
        Node inconsistent6 = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("z")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        null,
                        characterRepository,
                        null
                ),
                true
        );
        Node ax = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("a"), characterRepository.getCharacter("x")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("c").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )
        );
        Node ad = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("a"), characterRepository.getCharacter("d")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("a").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )
        );
        Node bc = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("b"), characterRepository.getCharacter("c")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("b").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )
        );
        Node f = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("f")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("x").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )
        );
        Node g = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("g")),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("y").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )
        );
        Node zeroNode = new ZeroNode();

        unitNode.setLevel(0L);
        inconsistent1.setLevel(1L);
        inconsistent2.setLevel(1L);
        inconsistent3.setLevel(2L);
        inconsistent4.setLevel(100L);
        inconsistent5.setLevel(200L);
        bc.setLevel(2L);
        ax.setLevel(2L);
        ad.setLevel(3L);
        zeroNode.setLevel(4L);

        inconsistent1.incrementFrequency();
        inconsistent1.incrementFrequency();
        inconsistent2.incrementFrequency();
        inconsistent3.incrementFrequency();
        inconsistent3.incrementFrequency();
        inconsistent3.incrementFrequency();
        inconsistent3.incrementFrequency();
        ax.incrementFrequency();
        ad.incrementFrequency();
        f.incrementFrequency();
        g.incrementFrequency();
        g.incrementFrequency();
        zeroNode.incrementFrequency();
        zeroNode.incrementFrequency();

        unitNode.addChild(inconsistent1);
        unitNode.addChild(inconsistent2);
        unitNode.addChild(inconsistent4);
        unitNode.addChild(inconsistent6);
        unitNode.addChild(bc);
        inconsistent1.addChild(ax);
        inconsistent1.addChild(bc);
        inconsistent1.addChild(inconsistent3);
        inconsistent2.addChild(bc);
        inconsistent3.addChild(ad);
        inconsistent4.addChild(inconsistent5);
        inconsistent5.addChild(bc);
        inconsistent6.addChild(f);
        inconsistent6.addChild(g);
        ax.addChild(ad);
        ax.addChild(bc);
        ad.addChild(zeroNode);
        bc.addChild(zeroNode);
        f.addChild(zeroNode);
        g.addChild(zeroNode);

        Lattice lattice = new Lattice(unitNode, zeroNode, characterRepository, wordConverter);
        unitNode.getChildren().remove(zeroNode);
        zeroNode.getParents().remove(unitNode);
        lattice.addNodes(new Node[]{
                inconsistent1, inconsistent2, inconsistent3, inconsistent4, inconsistent5,
                inconsistent6, ad, ax, bc, f, g
        });

        return lattice;
    }

    private Node getNodeByPattern(String pattern, Lattice lattice, ICharacterRepository characterRepository) {
        return lattice.getNodes()
                .stream()
                .filter(n -> n.getPattern().stream().map(characterRepository::getLetter).collect(joining()).equals(pattern))
                .findFirst()
                .get();
    }

    private Node getNodeByContextStart(String start, Lattice lattice, ICharacterRepository characterRepository) {
        if (start == null) {
            return getNodeByContextStart("ax", lattice, characterRepository);
        }

        return lattice.getNodes()
                .stream()
                .filter(n -> n.getPattern().stream().map(characterRepository::getLetter).collect(joining()).startsWith(start))
                .findFirst()
                .get();
    }

    private void assertTransformation(String start, String addedCharacter, Lattice lattice, ICharacterRepository characterRepository) {
        Node node = getNodeByContextStart(start, lattice, characterRepository);
        Rule rule = node.getRule();
        assertThat(rule).isNotNull();
        List<ITransformation> transformations = rule.getTransformations();
        assertThat(transformations).isNotNull();
        assertThat(transformations).isNotEmpty();
        assertThat(transformations).hasSize(1);
        ITransformation transformation = transformations.get(0);
        assertThat(transformation).isInstanceOf(Addition.class);
        Addition addition = (Addition) transformation;
        Set<? extends IAttribute> attributes = addition.getAttributes();
        assertThat(attributes).isNotEmpty();
        ICharacter character = characterRepository.getCharacter(attributes);
        assertThat(character).isNotNull();
        assertThat(character).hasToString(addedCharacter);
    }

    private void assertNodeLevel(Node node) {
        long nodeLevel = node.getLevel();
        long parentLevel = node.getParents()
                .stream()
                .mapToLong(Node::getLevel)
                .max()
                .getAsLong();
        long expectedNodeLevel = parentLevel + 1L;
        assertThat(nodeLevel).isEqualTo(expectedNodeLevel);
    }

    private ICharacter s(String letter, ICharacterRepository characterRepository) {
        return characterRepository.getCharacter(letter);
    }

}
