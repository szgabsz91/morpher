package com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.characters.ICharacter;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IdentityWordConverter;
import com.github.szgabsz91.morpher.transformationengines.lattice.converters.LatticeConverter;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.Lattice;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.UnitNode;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.ZeroNode;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Context;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.testutils.IOUtils;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.IWordPairProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.WordPairProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;

public class CompleteLatticeBuilderTest {

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
                    IWordPairProcessor cutWordPairProcessor = new WordPairProcessor.Builder()
                            .characterRepository(characterRepository)
                            .wordConverter(wordConverter)
                            .costCalculator(costCalculator)
                            .maximalContextSize(3)
                            .build();
                    CompleteLatticeBuilder completeLatticeBuilder = new CompleteLatticeBuilder(characterRepository, wordConverter);
                    return Arguments.of(
                            characterRepository,
                            wordConverter,
                            completeLatticeBuilder,
                            wordPairProcessor,
                            cutWordPairProcessor
                    );
                });
    }

    @Test
    public void testConstructorWithFlags() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new IdentityWordConverter();
        CompleteLatticeBuilder completeLatticeBuilder = new CompleteLatticeBuilder(characterRepository, wordConverter, true, true);
        assertThat(completeLatticeBuilder.isSkipFrequencyCalculation()).isTrue();
        assertThat(completeLatticeBuilder.isSkipDominantRuleSelection()).isTrue();
        assertThat(completeLatticeBuilder.getLattice().isEmpty()).isTrue();
    }

    @Test
    public void testConstructorWithFlagsAndInitialLattice() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new IdentityWordConverter();
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        Rule rule = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
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
        );
        Node node = new Node(rule);
        lattice.addNode(node);
        CompleteLatticeBuilder completeLatticeBuilder = new CompleteLatticeBuilder(characterRepository, wordConverter, false, false, lattice);
        assertThat(completeLatticeBuilder.isSkipFrequencyCalculation()).isFalse();
        assertThat(completeLatticeBuilder.isSkipDominantRuleSelection()).isFalse();
        assertThat(completeLatticeBuilder.getLattice().isEmpty()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    @SuppressWarnings("unchecked")
    public void testConstructorGettersAndReset(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            ILatticeBuilder latticeBuilder,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) {
        CompleteLatticeBuilder completeLatticeBuilder = (CompleteLatticeBuilder) latticeBuilder;
        assertThat(completeLatticeBuilder.getCharacterRepository()).hasSameClassAs(characterRepository);
        assertThat(completeLatticeBuilder.getWordConverter()).hasSameClassAs(wordConverter);

        Rule rule = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
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
        );
        completeLatticeBuilder.addRule(rule);
        assertThat(completeLatticeBuilder.getLattice().size()).isGreaterThan(2);
        completeLatticeBuilder.reset();
        assertThat(completeLatticeBuilder.getLattice().size()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    @SuppressWarnings("unchecked")
    public void testAddRules(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            ILatticeBuilder latticeBuilder,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) {
        Set<Rule> rules = Set.of(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("a")),
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
        CompleteLatticeBuilder completeLatticeBuilder = new CompleteLatticeBuilder(characterRepository, new IdentityWordConverter());
        completeLatticeBuilder.addRules(rules);
        Lattice lattice = completeLatticeBuilder.getLattice();
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(new Node(rules.iterator().next()));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testOnNodeBecomingInconsistentWithNodeContainingNoRule(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            ILatticeBuilder latticeBuilder,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) {
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        Node node = new Node(null);
        lattice.addNode(node);

        latticeBuilder.onNodeBecomingInconsistent(lattice, node);
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(node);
        assertThat(node.isInconsistent()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testOnNodeBecomingInconsistentWithAtomicNode(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            ILatticeBuilder latticeBuilder,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) {
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        lattice.getUnitNode().getChildren().remove(lattice.getZeroNode());
        lattice.getZeroNode().getParents().remove(lattice.getUnitNode());
        Node node = new Node(Rule.identity());
        lattice.getUnitNode().addChild(node);
        node.addChild(lattice.getZeroNode());
        lattice.addNode(node);

        latticeBuilder.onNodeBecomingInconsistent(lattice, node);
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(node);
        assertThat(node.isInconsistent()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testOnNodeBecomingInconsistentWithNodeContainingARule(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            ILatticeBuilder latticeBuilder,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) {
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        Node parent = new Node(
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
                )
        );
        Node child = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("b")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        Node grandChild = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("c")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        lattice.getUnitNode().getChildren().remove(lattice.getZeroNode());
        lattice.getZeroNode().getParents().remove(lattice.getUnitNode());
        lattice.getUnitNode().addChild(parent);
        parent.addChild(child);
        child.addChild(grandChild);
        grandChild.addChild(lattice.getZeroNode());
        lattice.addNodes(new Node[] { parent, child, grandChild });

        latticeBuilder.onNodeBecomingInconsistent(lattice, child);
        assertThat(lattice.getNodes()).hasSize(3);
        assertThat(lattice.getNodes()).contains(parent, child, grandChild);
        assertThat(parent.isInconsistent()).isTrue();
        assertThat(child.isInconsistent()).isTrue();
        assertThat(grandChild.isInconsistent()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testAddRulesWithoutTruncation(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            ILatticeBuilder latticeBuilder,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) throws IOException {
        WordPair wordPair1 = WordPair.of("alma", "almát");
        WordPair wordPair2 = WordPair.of("malma", "malmát");

        Set<Rule> rules = new HashSet<>();
        rules.add(wordPairProcessor.induceRules(wordPair1).getRules().get(0));
        rules.add(wordPairProcessor.induceRules(wordPair2).getRules().get(0));

        latticeBuilder.addRules(rules);
        Lattice originalLattice = latticeBuilder.getLattice();
        LatticeConverter latticeConverter = new LatticeConverter();
        latticeConverter.setCharacterRepository(characterRepository);
        latticeConverter.setWordConverter(wordConverter);
        Lattice lattice = IOUtils.serializeAndDeserialize(
                originalLattice,
                new Serializer<>(latticeConverter, originalLattice)
        );

        Node unitNode = lattice.getUnitNode();
        assertThat(unitNode.isUnit()).isTrue();
        assertThat(unitNode.isZero()).isFalse();
        assertThat(unitNode.getParents()).isEmpty();
        assertThat(unitNode.getChildren()).hasSize(1);
        assertThat(unitNode.getRule()).isNull();

        Node intersection = unitNode.getChildren().get(0);
        assertThat(intersection.isUnit()).isFalse();
        assertThat(intersection.isZero()).isFalse();
        assertThat(intersection.getParents()).hasSize(1);
        assertThat(intersection.getChildren()).hasSize(2);
        assertThat(charactersToString(intersection.getRule().getContext().getPrefix(), characterRepository)).isEqualTo("alm");
        assertThat(charactersToString(intersection.getRule().getContext().getCore(), characterRepository)).isEqualTo("a");
        assertThat(charactersToString(intersection.getRule().getContext().getPostfix(), characterRepository)).isEqualTo("#");
        assertThat(intersection.getRule().getContext().getFrontPosition().getIndex()).isEqualTo(0);
        assertThat(intersection.getRule().getContext().getBackPosition().getIndex()).isEqualTo(0);

        Node alma = intersection.getChildren()
                .stream()
                .filter(n -> charactersToString(n.getRule().getContext().getPrefix(), characterRepository).equals("$alm"))
                .findFirst()
                .get();
        assertThat(alma.isUnit()).isFalse();
        assertThat(alma.isZero()).isFalse();
        assertThat(alma.getParents()).hasSize(1);
        assertThat(alma.getChildren()).hasSize(1);
        assertThat(charactersToString(alma.getRule().getContext().getPrefix(), characterRepository)).isEqualTo("$alm");
        assertThat(charactersToString(alma.getRule().getContext().getCore(), characterRepository)).isEqualTo("a");
        assertThat(charactersToString(alma.getRule().getContext().getPostfix(), characterRepository)).isEqualTo("#");
        assertThat(alma.getRule().getContext().getFrontPosition().getIndex()).isEqualTo(0);
        assertThat(alma.getRule().getContext().getBackPosition().getIndex()).isEqualTo(0);
        assertThat(alma.getChildren()).hasSize(1);
        assertThat(alma.getChildren()).containsSequence(new ZeroNode());

        Node malma = intersection.getChildren()
                .stream()
                .filter(n -> charactersToString(n.getRule().getContext().getPrefix(), characterRepository).equals("$malm"))
                .findFirst()
                .get();
        assertThat(malma.isUnit()).isFalse();
        assertThat(malma.isZero()).isFalse();
        assertThat(malma.getParents()).hasSize(1);
        assertThat(malma.getChildren()).hasSize(1);
        assertThat(charactersToString(malma.getRule().getContext().getPrefix(), characterRepository)).isEqualTo("$malm");
        assertThat(charactersToString(malma.getRule().getContext().getCore(), characterRepository)).isEqualTo("a");
        assertThat(charactersToString(malma.getRule().getContext().getPostfix(), characterRepository)).isEqualTo("#");
        assertThat(malma.getRule().getContext().getFrontPosition().getIndex()).isEqualTo(0);
        assertThat(malma.getRule().getContext().getBackPosition().getIndex()).isEqualTo(0);
        assertThat(malma.getChildren()).hasSize(1);
        assertThat(malma.getChildren()).containsSequence(new ZeroNode());

        Node zeroNode = malma.getChildren().get(0);
        assertThat(zeroNode.isUnit()).isFalse();
        assertThat(zeroNode.isZero()).isTrue();
        assertThat(zeroNode.getParents()).hasSize(2);
        assertThat(zeroNode.getChildren()).isEmpty();
        assertThat(zeroNode.getRule()).isEqualTo(Rule.identity());

        // Test match
        Node node1 = lattice.match(wordPair1.getLeftWord());
        Rule rule1 = node1.getRule();
        assertThat(rule1.transform(wordPair1.getLeftWord())).isEqualTo(wordPair1.getRightWord());
        assertThat(rule1.transformFromFront(wordPair1.getLeftWord())).isEqualTo(wordPair1.getRightWord());
        assertThat(rule1.transformFromBack(wordPair1.getLeftWord())).isEqualTo(wordPair1.getRightWord());

        Node node2 = lattice.match(wordPair2.getLeftWord());
        Rule rule2 = node2.getRule();
        assertThat(rule2.transform(wordPair2.getLeftWord())).isEqualTo(wordPair2.getRightWord());
        assertThat(rule2.transformFromFront(wordPair2.getLeftWord())).isEqualTo(wordPair2.getRightWord());
        assertThat(rule2.transformFromBack(wordPair2.getLeftWord())).isEqualTo(wordPair2.getRightWord());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testAddRulesWithTruncation(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            ILatticeBuilder latticeBuilder,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) throws IOException {
        WordPair wordPair1 = WordPair.of("alma", "almát");
        WordPair wordPair2 = WordPair.of("malma", "malmát");

        Set<Rule> rules = new HashSet<>();
        rules.add(cutWordPairProcessor.induceRules(wordPair1).getRules().get(0));
        rules.add(cutWordPairProcessor.induceRules(wordPair2).getRules().get(0));

        latticeBuilder.addRules(rules);
        Lattice originalLattice = latticeBuilder.getLattice();
        LatticeConverter latticeConverter = new LatticeConverter();
        latticeConverter.setCharacterRepository(characterRepository);
        latticeConverter.setWordConverter(wordConverter);
        Lattice lattice = IOUtils.serializeAndDeserialize(
                originalLattice,
                new Serializer<>(latticeConverter, originalLattice)
        );

        Node unitNode = lattice.getUnitNode();
        assertThat(unitNode.isUnit()).isTrue();
        assertThat(unitNode.isZero()).isFalse();
        assertThat(unitNode.getParents()).isEmpty();
        assertThat(unitNode.getChildren()).hasSize(1);
        assertThat(unitNode.getRule()).isNull();

        Node alma = unitNode.getChildren().get(0);
        assertThat(alma.isUnit()).isFalse();
        assertThat(alma.isZero()).isFalse();
        assertThat(alma.getParents()).hasSize(1);
        assertThat(alma.getChildren()).hasSize(1);
        assertThat(charactersToString(alma.getRule().getContext().getPrefix(), characterRepository)).isEqualTo("alm");
        assertThat(charactersToString(alma.getRule().getContext().getCore(), characterRepository)).isEqualTo("a");
        assertThat(charactersToString(alma.getRule().getContext().getPostfix(), characterRepository)).isEqualTo("#");
        assertThat(alma.getRule().getContext().getFrontPosition().getIndex()).isEqualTo(0);
        assertThat(alma.getRule().getContext().getBackPosition().getIndex()).isEqualTo(0);
        assertThat(alma.getChildren()).hasSize(1);
        assertThat(alma.getChildren()).containsSequence(new ZeroNode());

        Node zeroNode = alma.getChildren().get(0);
        assertThat(zeroNode.isUnit()).isFalse();
        assertThat(zeroNode.isZero()).isTrue();
        assertThat(zeroNode.getParents()).hasSize(1);
        assertThat(zeroNode.getChildren()).isEmpty();
        assertThat(zeroNode.getRule()).isEqualTo(Rule.identity());

        // Test match
        Node node1 = lattice.match(wordPair1.getLeftWord());
        Node node2 = lattice.match(wordPair2.getLeftWord());
        Rule rule1 = node1.getRule();
        Rule rule2 = node2.getRule();
        assertThat(rule1.transform(wordPair1.getLeftWord())).isEqualTo(wordPair1.getRightWord());
        assertThat(rule1.transformFromFront(wordPair1.getLeftWord())).isEqualTo(wordPair1.getRightWord());
        assertThat(rule1.transformFromBack(wordPair1.getLeftWord())).isEqualTo(wordPair1.getRightWord());
        assertThat(rule2.transform(wordPair2.getLeftWord())).isEqualTo(wordPair2.getRightWord());
        assertThat(rule2.transformFromFront(wordPair2.getLeftWord())).isEqualTo(wordPair2.getRightWord());
        assertThat(rule2.transformFromBack(wordPair2.getLeftWord())).isEqualTo(wordPair2.getRightWord());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testWithCollidingRules(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            ILatticeBuilder latticeBuilder,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) throws IOException {
        List<WordPair> wordPairs = new ArrayList<>();
        wordPairs.add(WordPair.of("törvénygyűjtemény", "törvénygyűjteményt"));
        wordPairs.add(WordPair.of("növény", "növényt"));
        Set<Rule> rules = wordPairs
                .stream()
                .map(wordPair -> cutWordPairProcessor.induceRules(wordPair).getRules().get(0))
                .collect(toUnmodifiableSet());
        latticeBuilder.addRules(rules);
        Lattice originalLattice = latticeBuilder.getLattice();
        LatticeConverter latticeConverter = new LatticeConverter();
        latticeConverter.setCharacterRepository(characterRepository);
        latticeConverter.setWordConverter(wordConverter);
        Lattice lattice = IOUtils.serializeAndDeserialize(
                originalLattice,
                new Serializer<>(latticeConverter, originalLattice)
        );

        for (WordPair wordPair : wordPairs) {
            Word input = wordPair.getLeftWord();
            Word expected = wordPair.getRightWord();
            Node node = lattice.match(input);
            Rule rule = node.getRule();
            Word output = rule.transform(input);
            assertThat(output)
                    .withFailMessage(String.format("Word Pair #%d: %s - %s", wordPairs.indexOf(wordPair), wordPair.getLeftWord(), wordPair.getRightWord()))
                    .isEqualTo(expected);
        }
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testWithNoFrequencyCalculation(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            ILatticeBuilder latticeBuilder,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) {
        Set<Rule> rules = Set.of(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("a")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                ),
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("e")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )
        );
        CompleteLatticeBuilder completeLatticeBuilder = new CompleteLatticeBuilder(characterRepository, wordConverter, true, true);
        completeLatticeBuilder.addRules(rules);
        Lattice lattice = completeLatticeBuilder.getLattice();
        assertThat(lattice.getUnitNode().getFrequency()).isEqualTo(0L);
        assertThat(lattice.getZeroNode().getFrequency()).isEqualTo(0L);
        lattice.getNodes().forEach(node -> assertThat(node.getFrequency()).isEqualTo(0L));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testSkipNodeInsertingWithNodeBecomingInconsistent(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            ILatticeBuilder latticeBuilder,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) {
        Node node = new Node(Rule.identity());
        assertThat(node.isInconsistent()).isFalse();
        Set<Node> children = Set.of(new Node(Rule.identity()));
        children.iterator().next().setInconsistent();
        boolean result = latticeBuilder.skipNodeInserting(node, children);
        assertThat(node.isInconsistent()).isTrue();
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testSkipNodeInsertingWithNodeNotBecomingInconsistent(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            ILatticeBuilder latticeBuilder,
            IWordPairProcessor wordPairProcessor,
            IWordPairProcessor cutWordPairProcessor) {
        Node node = new Node(Rule.identity());
        assertThat(node.isInconsistent()).isFalse();
        Set<Node> children = Set.of(new Node(Rule.identity()));
        boolean result = latticeBuilder.skipNodeInserting(node, children);
        assertThat(node.isInconsistent()).isFalse();
        assertThat(result).isFalse();
    }

    private static String charactersToString(List<ICharacter> characters, ICharacterRepository characterRepository) {
        return characters
                .stream()
                .map(characterRepository::getLetter)
                .collect(joining());
    }

}
