package com.github.szgabsz91.morpher.methods.lattice.impl;

import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.Vowel;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.LipShape;
import com.github.szgabsz91.morpher.methods.api.utils.TrainingDataRetriever;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.ILatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.MinimalLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.EmptyNode;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.FullNode;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Context;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.ITransformation;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.ITrainingSetProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.IWordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.TrainingSetProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.WordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
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
import static java.util.stream.Collectors.toSet;
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
         * - ROOT
         *     - a
         *         - i
         *             - c
         *                 - LEAF
         *             - o
         *                 - LEAF
         *     - e
         *         - i
         *             - c
         *                 - LEAF
         *     - b
         *         - LEAF
         */
        lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
        lattice.getRoot().getChildren().remove(lattice.getLeaf());
        lattice.getLeaf().getParents().remove(lattice.getRoot());
        lattice.addNodes(new Node[] { a, b, c, e, i });
        lattice.getRoot().addChild(a);
        lattice.getRoot().addChild(b);
        lattice.getRoot().addChild(e);
        a.addChild(i);
        e.addChild(i);
        b.addChild(lattice.getLeaf());
        i.addChild(c);
        i.addChild(o);
        o.addChild(lattice.getLeaf());
        c.addChild(lattice.getLeaf());
        latticeWalker = new LatticeWalker(lattice);

        Node result = latticeWalker.findSingleNode(
                node -> node.getRule() != null &&
                        !node.getRule().getContext().getCore().isEmpty() &&
                        HungarianAttributedCharacterRepository.VOWELS.contains(node.getRule().getContext().getCore().get(0).toString()),
                node -> node.getRule() != null && "i".equals(node.getRule().getContext().getCore().get(0).toString()),
                null
        );

        assertThat(result).isNotNull();
        assertThat(result).isNotInstanceOf(FullNode.class);
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
         * - ROOT
         *     - a
         *         - i
         *             - LEAF
         *     - e
         *         - i
         *             - LEAF
         *     - d
         *         - LEAF
         *     - o
         *         - LEAF
         */
        lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
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
        lattice.getRoot().getChildren().clear();
        lattice.getLeaf().getParents().clear();
        lattice.getRoot().addChild(a);
        lattice.getRoot().addChild(d);
        lattice.getRoot().addChild(e);
        lattice.getRoot().addChild(o);
        a.addChild(i);
        e.addChild(i);
        i.addChild(lattice.getLeaf());
        d.addChild(lattice.getLeaf());
        o.addChild(lattice.getLeaf());
        lattice.addNodes(new Node[] { a, e, i, d, o });

        Predicate<Node> nodePredicate =
                node ->
                        node.getRule() != null && node.getRule().getContext() != null &&
                                !node.getRule().getContext().getPrefix().isEmpty() &&
                                HungarianAttributedCharacterRepository.VOWELS.contains(node.getRule().getContext().getPrefix().get(0).toString());

        Set<Node> result = latticeWalker.walk(
                lattice.getRoot(),
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
                lattice.getRoot(),
                node -> true,
                Node::getChildren,
                ProcessingType.GENERAL_PROCESSING,
                lattice.getNextProcessingStatus(ProcessingType.GENERAL_PROCESSING),
                null
        );
        assertThat(result).hasSize(1);
        assertThat(result).containsSequence(lattice.getLeaf());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testWalkWithPostProcessor(ICharacterRepository characterRepository, IWordConverter wordConverter, Lattice lattice, LatticeWalker latticeWalker) {
        long expected = 100L;
        Set<Node> result = latticeWalker.walk(
                lattice.getRoot(),
                node -> true,
                Node::getChildren,
                ProcessingType.GENERAL_PROCESSING,
                lattice.getNextProcessingStatus(ProcessingType.GENERAL_PROCESSING),
                node -> node.setFrequency(expected)
        );
        assertThat(result).hasSize(1);
        assertThat(result).containsSequence(lattice.getLeaf());
        assertThat(lattice.getRoot().getFrequency()).isEqualTo(expected);
        assertThat(lattice.getLeaf().getFrequency()).isEqualTo(2L);
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

        assertThat(lattice.getRoot().getLevel()).isEqualTo(newLevel);
        assertThat(lattice.getLeaf().getLevel()).isEqualTo(newLevel);
        lattice.getNodes().forEach(node -> {
            assertThat(node.getLevel())
                    .withFailMessage(node.toString())
                    .isEqualTo(newLevel);
        });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testFillNodeLevels(ICharacterRepository characterRepository, IWordConverter wordConverter, Lattice lattice, LatticeWalker latticeWalker) {
        lattice.getRoot().setLevel(-1L);
        lattice.getLeaf().setLevel(-1L);
        lattice.getNodes().forEach(n -> n.setLevel(-1L));

        latticeWalker.fillNodeLevels();
        long result = lattice.getLeafLevel();
        assertThat(result).isEqualTo(4L);

        assertThat(lattice.getRoot().getLevel()).isEqualTo(0L);
        lattice.getNodes().forEach(this::assertNodeLevel);
        assertNodeLevel(lattice.getLeaf());
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
        Node inhomogeneousNode = new Node(
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
        inhomogeneousNode.setInhomogeneous();
        Node homogeneousNode1 = new Node(
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
        homogeneousNode1.setFrequency(2L);
        Node homogeneousNode2 = new Node(
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
        homogeneousNode2.setFrequency(1L);
        Node homogeneousNode3 = new Node(
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
        homogeneousNode3.setFrequency(3L);
        inhomogeneousNode.getChildren().addAll(List.of(homogeneousNode1, homogeneousNode2, homogeneousNode3));
        lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
        lattice.getRoot().getChildren().add(inhomogeneousNode);
        lattice.getLeaf().getParents().addAll(List.of(homogeneousNode1, homogeneousNode2, homogeneousNode3));
        lattice.addNodes(new Node[] { inhomogeneousNode, homogeneousNode1, homogeneousNode2, homogeneousNode3 });
        latticeWalker = new LatticeWalker(lattice);
        latticeWalker.fillDominantRules();
        assertThat(inhomogeneousNode.getRule().getTransformations()).isEqualTo(homogeneousNode3.getRule().getTransformations());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetMaximalHomogeneousNodes(ICharacterRepository characterRepository, IWordConverter wordConverter, Lattice lattice, LatticeWalker latticeWalker) {
        Set<Node> result = latticeWalker.getMaximalHomogeneousNodes();
        Set<Node> expected = Set.of(
                getNodeByPattern("ax", lattice, characterRepository),
                getNodeByPattern("f", lattice, characterRepository),
                getNodeByPattern("g", lattice, characterRepository)
        );
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testGetMaximalHomogeneousNodesRegression() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
        Node homogeneousParent = new Node(
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
        Node inhomogeneousParent = new Node(
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
        Node homogeneousChild = new Node(
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
        lattice.getRoot().getChildren().clear();
        lattice.getLeaf().getParents().clear();
        lattice.getRoot().addChild(homogeneousParent);
        lattice.getRoot().addChild(inhomogeneousParent);
        homogeneousParent.addChild(homogeneousChild);
        inhomogeneousParent.addChild(homogeneousChild);
        homogeneousChild.addChild(lattice.getLeaf());
        LatticeWalker latticeWalker = new LatticeWalker(lattice);
        Set<Node> result = latticeWalker.getMaximalHomogeneousNodes();
        assertThat(result).hasSize(1);
        assertThat(result).containsSequence(homogeneousParent);
    }

    @Test
    public void testImprovedSearchAlgorithm() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
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
        lattice.getRoot().getChildren().clear();
        lattice.getRoot().getChildren().addAll(List.of(node1, node2, node3));
        node1.getChildren().add(lattice.getLeaf());
        node2.getChildren().add(lattice.getLeaf());
        node3.getChildren().addAll(List.of(node31, node32));
        node31.getChildren().add(lattice.getLeaf());
        node32.getChildren().add(lattice.getLeaf());

        LatticeWalker latticeWalker = new LatticeWalker(lattice);
        Node result = latticeWalker.findSingleNode(
                n -> true,
                Node::isHomogeneous,
                null
        );
        assertThat(result).isEqualTo(node32);
    }

    @Test
    public void testGetMaximalHomogeneousDescendants() throws IOException {
        Set<WordPair> wordPairs;
        try (BufferedReader reader = Files.newBufferedReader(TrainingDataRetriever.getTrainingDataFile("CAS(ACC).csv"), StandardCharsets.UTF_8)) {
            wordPairs = reader.lines()
                    .limit(200L)
                    .map(line -> line.split(","))
                    .map(lineParts -> WordPair.of(lineParts[0], lineParts[1]))
                    .collect(toSet());
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
                    Node[] maximalHomogeneousDescendants = latticeWalker.getMaximalHomogeneousDescendants(node);

                    for (Node maximalHomogeneousDescendant : maximalHomogeneousDescendants) {
                        for (Node parent : maximalHomogeneousDescendant.getParents()) {
                            assertThat(parent.isHomogeneous())
                                    .withFailMessage(parent + " cannot be homogeneous so that " + maximalHomogeneousDescendant + " " + "can be a maximal homogeneous descendant")
                                    .isFalse();
                        }
                    }
                });
    }

    private static Lattice createLattice(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node root = new FullNode();
        Node inhomogeneous1 = new Node(
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
        Node inhomogeneous2 = new Node(
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
        Node inhomogeneous3 = new Node(
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
        Node inhomogeneous4 = new Node(
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
        Node inhomogeneous5 = new Node(
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
        Node inhomogeneous6 = new Node(
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
        Node leaf = new EmptyNode();

        root.setLevel(0L);
        inhomogeneous1.setLevel(1L);
        inhomogeneous2.setLevel(1L);
        inhomogeneous3.setLevel(2L);
        inhomogeneous4.setLevel(100L);
        inhomogeneous5.setLevel(200L);
        bc.setLevel(2L);
        ax.setLevel(2L);
        ad.setLevel(3L);
        leaf.setLevel(4L);

        inhomogeneous1.incrementFrequency();
        inhomogeneous1.incrementFrequency();
        inhomogeneous2.incrementFrequency();
        inhomogeneous3.incrementFrequency();
        inhomogeneous3.incrementFrequency();
        inhomogeneous3.incrementFrequency();
        inhomogeneous3.incrementFrequency();
        ax.incrementFrequency();
        ad.incrementFrequency();
        f.incrementFrequency();
        g.incrementFrequency();
        g.incrementFrequency();
        leaf.incrementFrequency();
        leaf.incrementFrequency();

        root.addChild(inhomogeneous1);
        root.addChild(inhomogeneous2);
        root.addChild(inhomogeneous4);
        root.addChild(inhomogeneous6);
        root.addChild(bc);
        inhomogeneous1.addChild(ax);
        inhomogeneous1.addChild(bc);
        inhomogeneous1.addChild(inhomogeneous3);
        inhomogeneous2.addChild(bc);
        inhomogeneous3.addChild(ad);
        inhomogeneous4.addChild(inhomogeneous5);
        inhomogeneous5.addChild(bc);
        inhomogeneous6.addChild(f);
        inhomogeneous6.addChild(g);
        ax.addChild(ad);
        ax.addChild(bc);
        ad.addChild(leaf);
        bc.addChild(leaf);
        f.addChild(leaf);
        g.addChild(leaf);

        Lattice lattice = new Lattice(root, leaf, characterRepository, wordConverter);
        root.getChildren().remove(leaf);
        leaf.getParents().remove(root);
        lattice.addNodes(new Node[]{
                inhomogeneous1, inhomogeneous2, inhomogeneous3, inhomogeneous4, inhomogeneous5,
                inhomogeneous6, ad, ax, bc, f, g
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
