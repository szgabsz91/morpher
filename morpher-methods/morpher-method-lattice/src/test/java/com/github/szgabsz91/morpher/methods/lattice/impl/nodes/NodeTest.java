package com.github.szgabsz91.morpher.methods.lattice.impl.nodes;

import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.lattice.impl.ProcessingType;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Context;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Addition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodeTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConstructorWithRuleAndInhomogeneousAndParentsAndChildren(ICharacterRepository characterRepository) {
        Rule rule = mock(Rule.class);
        List<Node> parents = List.of(mock(Node.class));
        List<Node> children = List.of(mock(Node.class));
        Node node = new Node(rule, true, parents, children);
        assertThat(node.getRule()).isEqualTo(rule);
        assertThat(node.getParents()).isEqualTo(parents);
        assertThat(node.getChildren()).isEqualTo(children);
        assertThat(node.isInhomogeneous()).isTrue();
        assertThat(node.isHomogeneous()).isFalse();
        assertThat(node.getLevel()).isEqualTo(0L);
        assertThat(node.getFrequency()).isEqualTo(0L);
        assertThat(node.isFull()).isFalse();
        assertThat(node.isEmpty()).isFalse();
        assertThat(node.isAtomic()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConstructorWithRuleAndParentsAndChildren(ICharacterRepository characterRepository) {
        Rule rule = mock(Rule.class);
        List<Node> parents = List.of(mock(Node.class));
        List<Node> children = List.of(mock(Node.class));
        Node node = new Node(rule, parents, children);
        assertThat(node.getRule()).isEqualTo(rule);
        assertThat(node.getParents()).isEqualTo(parents);
        assertThat(node.getChildren()).isEqualTo(children);
        assertThat(node.isInhomogeneous()).isFalse();
        assertThat(node.isHomogeneous()).isTrue();
        assertThat(node.getLevel()).isEqualTo(0L);
        assertThat(node.getFrequency()).isEqualTo(0L);
        assertThat(node.isFull()).isFalse();
        assertThat(node.isEmpty()).isFalse();
        assertThat(node.isAtomic()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConstructorWithRuleAndInhomogeneous(ICharacterRepository characterRepository) {
        Rule rule = mock(Rule.class);
        Node node = new Node(rule, true);
        assertThat(node.getRule()).isEqualTo(rule);
        assertThat(node.getParents()).isEmpty();
        assertThat(node.getChildren()).isEmpty();
        assertThat(node.isInhomogeneous()).isTrue();
        assertThat(node.isHomogeneous()).isFalse();
        assertThat(node.getLevel()).isEqualTo(0L);
        assertThat(node.getFrequency()).isEqualTo(0L);
        assertThat(node.isFull()).isFalse();
        assertThat(node.isEmpty()).isFalse();
        assertThat(node.isAtomic()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConstructorWithRule(ICharacterRepository characterRepository) {
        Rule rule = mock(Rule.class);
        Node node = new Node(rule);
        assertThat(node.getRule()).isEqualTo(rule);
        assertThat(node.getParents()).isEmpty();
        assertThat(node.getChildren()).isEmpty();
        assertThat(node.isInhomogeneous()).isFalse();
        assertThat(node.isHomogeneous()).isTrue();
        assertThat(node.getLevel()).isEqualTo(0L);
        assertThat(node.getFrequency()).isEqualTo(0L);
        assertThat(node.isFull()).isFalse();
        assertThat(node.isEmpty()).isFalse();
        assertThat(node.isAtomic()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testOneArgConstructor(ICharacterRepository characterRepository) {
        Rule rule = new Rule(null, null, characterRepository, null);
        Node node = new Node(rule);
        assertThat(node.getRule()).isEqualTo(rule);
        assertThat(node.getParents()).isEmpty();
        assertThat(node.getChildren()).isEmpty();
        assertThat(node.getFrequency()).isEqualTo(0L);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testThreeArgConstructor(ICharacterRepository characterRepository) {
        Rule rule = new Rule(null, null, characterRepository, null);
        List<Node> parents = List.of(new FullNode());
        List<Node> children = List.of(new EmptyNode());
        Node node = new Node(rule, parents, children);
        assertThat(node.getRule()).isEqualTo(rule);
        assertThat(node.getParents()).isEqualTo(parents);
        assertThat(node.getChildren()).isEqualTo(children);
        assertThat(node.getFrequency()).isEqualTo(0L);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testSetRule(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        Rule rule = new Rule(null, null, characterRepository, null);
        node.setRule(rule);
        assertThat(node.getRule()).isEqualTo(rule);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testAddParent(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        node.addParent(new FullNode());
        node.addParent(new FullNode());
        assertThat(node.getParents()).hasSize(1);
        assertThat(node.getParents()).containsSequence(new FullNode());
        assertThat(node.getParents().get(0).getChildren()).hasSize(1);
        assertThat(node.getParents().get(0).getChildren()).containsSequence(node);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRemoveParents(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        node.addParent(new FullNode());
        node.addParent(new EmptyNode());
        node.removeParents(Set.of(new FullNode(), new EmptyNode()));
        assertThat(node.getParents()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testAddChild(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        node.addChild(new FullNode());
        node.addChild(new FullNode());
        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChildren()).containsSequence(new FullNode());
        assertThat(node.getChildren().get(0).getParents()).hasSize(1);
        assertThat(node.getChildren().get(0).getParents()).containsSequence(node);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRemoveChildren(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        node.addChild(new FullNode());
        node.addChild(new EmptyNode());
        node.removeChildren(Set.of(new FullNode(), new EmptyNode()));
        assertThat(node.getChildren()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsFull(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        assertThat(node.isFull()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsEmpty(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        assertThat(node.isEmpty()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsHomogeneous(ICharacterRepository characterRepository) {
        Node inhomogeneousNode1 = new Node(null, true);
        Node inhomogeneousNode2 = new Node(null, true);
        Node homogeneousNode = new Node(null, false);

        assertThat(inhomogeneousNode1.isHomogeneous()).isFalse();
        assertThat(inhomogeneousNode2.isHomogeneous()).isFalse();
        assertThat(homogeneousNode.isHomogeneous()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsInhomogeneous(ICharacterRepository characterRepository) {
        Node inhomogeneousNode1 = new Node(null, true);
        Node inhomogeneousNode2 = new Node(null, true);
        Node homogeneousNode = new Node(null, false);

        assertThat(inhomogeneousNode1.isInhomogeneous()).isTrue();
        assertThat(inhomogeneousNode2.isInhomogeneous()).isTrue();
        assertThat(homogeneousNode.isInhomogeneous()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testSetInhomogeneous(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        assertThat(node.isInhomogeneous()).isFalse();
        assertThat(node.isHomogeneous()).isTrue();

        node.setInhomogeneous();
        assertThat(node.isInhomogeneous()).isTrue();
        assertThat(node.isHomogeneous()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsAtomicWithMultipleChildren(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        node.getChildren().addAll(List.of(new Node(null), new Node(null)));
        boolean result = node.isAtomic();
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsAtomicWithOneNonLeafChild(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        node.getChildren().add(new Node(null));
        boolean result = node.isAtomic();
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsAtomicWithOneLeafChild(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        node.getChildren().add(new EmptyNode());
        boolean result = node.isAtomic();
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetPatternWithNullRule(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        List<ICharacter> pattern = node.getPattern();
        List<ICharacter> cachedPattern = node.getPattern();
        assertThat(pattern).isEmpty();
        assertThat(cachedPattern).isEqualTo(pattern);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetPatternWithNonNullRule(ICharacterRepository characterRepository) {
        Rule rule = new Rule(
                new Context(
                        List.of(characterRepository.getCharacter("a")),
                        List.of(characterRepository.getCharacter("b")),
                        List.of(characterRepository.getCharacter("c")),
                        null,
                        null
                ),
                null,
                characterRepository,
                null
        );
        Node node = new Node(rule);
        List<ICharacter> pattern = node.getPattern();
        List<ICharacter> cachedPattern = node.getPattern();
        assertThat(pattern.stream().map(ICharacter::toString).collect(joining())).isEqualTo("abc");
        assertThat(cachedPattern).isEqualTo(pattern);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsProcessedAndSetProcessingStatus(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        assertThat(node.isProcessed(ProcessingType.PARENT_LIST_SEARCH, 0L)).isTrue();
        assertThat(node.isProcessed(ProcessingType.PARENT_LIST_SEARCH, 2L)).isFalse();
        node.setProcessingStatus(ProcessingType.PARENT_LIST_SEARCH, 2L);
        assertThat(node.isProcessed(ProcessingType.PARENT_LIST_SEARCH, 2L)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testSetLevel(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        assertThat(node.getLevel()).isEqualTo(0L);
        node.setLevel(2L);
        assertThat(node.getLevel()).isEqualTo(2L);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIncrementFrequency(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        assertThat(node.getFrequency()).isEqualTo(0L);
        node.incrementFrequency();
        assertThat(node.getFrequency()).isEqualTo(1L);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testSetFrequency(ICharacterRepository characterRepository) {
        long expected = 100L;
        Node node = new Node(null);
        node.setFrequency(expected);
        assertThat(node.getFrequency()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testReset(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        node.addParent(Mockito.mock(Node.class));
        node.addChild(Mockito.mock(Node.class));
        node.setInhomogeneous();
        node.setProcessingStatus(ProcessingType.GENERAL_PROCESSING, 100L);
        node.setLevel(100L);
        node.setFrequency(100L);

        node.reset();

        assertThat(node.getParents()).isEmpty();
        assertThat(node.getChildren()).isEmpty();
        assertThat(node.isHomogeneous()).isTrue();
        assertThat(node.isProcessed(ProcessingType.GENERAL_PROCESSING, 1L)).isFalse();
        assertThat(node.getLevel()).isEqualTo(0L);
        assertThat(node.getFrequency()).isEqualTo(0L);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testShouldBecomeInhomogeneousWithInhomogeneousDescendant(ICharacterRepository characterRepository) {
        Node node1 = new Node(null, false);
        Node node2 = new Node(null, true);
        boolean result = node1.shouldBecomeInhomogeneousBasedOnDescendant(node2);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testShouldBecomeInhomogeneousWithStartCharacterInPrefixOfThisRule(ICharacterRepository characterRepository) {
        Node node1 = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getCharacter("$")),
                                List.of(),
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
                false
        );
        Node node2 = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(),
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
                false
        );
        boolean result = node1.shouldBecomeInhomogeneousBasedOnDescendant(node2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testEquals(ICharacterRepository characterRepository) {
        Rule rule1 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        null,
                        null
                ),
                List.of(),
                characterRepository,
                null
        );
        Node node1 = new Node(rule1);
        Rule rule2 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("b")),
                        List.of(),
                        null,
                        null
                ),
                List.of(),
                characterRepository,
                null
        );
        Node node2 = new Node(rule2);

        assertThat(node1).isEqualTo(node1);
        assertThat(node1).isNotEqualTo(null);
        assertThat(node1).isNotEqualTo("string");
        assertThat(node1).isNotEqualTo(node2);
        assertThat(new Node(null)).isEqualTo(new Node(null));
        assertThat(new Node(null)).isNotEqualTo(node1);
        assertThat(node1).isNotEqualTo(new Node(null));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHashCode(ICharacterRepository characterRepository) {
        Rule rule = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        null,
                        null
                ),
                List.of(),
                characterRepository,
                null
        );
        Node node = new Node(rule);

        int expected = 31 * rule.hashCode() + node.getPattern().hashCode();

        assertThat(node.hashCode()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testToStringWithNonEmptyNode(ICharacterRepository characterRepository) {
        String ruleString = "rule";
        Rule rule = mock(Rule.class);
        when(rule.toString()).thenReturn(ruleString);
        Node node = new Node(rule, true);
        assertThat(node).hasToString("Node[rule=" + ruleString + ", inhomogeneous=true]");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testToStringWithEmptyNode(ICharacterRepository characterRepository) {
        Node node = new Node(null, true);
        assertThat(node).hasToString("Node[rule=null, inhomogeneous=true]");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHierarchicalToString(ICharacterRepository characterRepository) {
        Rule rule = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        null,
                        null
                ),
                List.of(),
                characterRepository,
                null
        );
        Node node = new Node(rule, List.of(), List.of(new EmptyNode()));
        assertThat(node.hierarchicalToString(0))
                .isEqualTo(String.format(
                        "Node[rule=Rule[[[], [a], [], null, null], []], inhomogeneous=false] - [a]%n  EMPTY - []%n")
                );
    }

    @Test
    public void testGetPartialMatchingFactorWithEmptyCharacters() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        Node node = new Node(
                new Rule(
                        Context.identity(),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        List<ICharacter> characters = List.of();
        Integer result = node.getPartialMatchingFactor(characters);
        assertThat(result).isNull();
    }

    @Test
    public void testGetPartialMatchingFactorWithEmptyPattern() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        Node node = new Node(
                new Rule(
                        Context.identity(),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        List<ICharacter> characters = List.of(characterRepository.getCharacter("a"));
        Integer result = node.getPartialMatchingFactor(characters);
        assertThat(result).isNull();
    }

    @Test
    public void testGetPartialMatchingFactorWithPatternContainingOnlyAStartCharacter() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        Node node = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getStartCharacter()),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        List<ICharacter> characters = List.of(characterRepository.getCharacter("a"));
        Integer result = node.getPartialMatchingFactor(characters);
        assertThat(result).isNull();
    }

    @Test
    public void testGetPartialMatchingFactorWithPatternContainingOnlyAStartAndEndCharacter() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        Node node = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getStartCharacter()),
                                List.of(characterRepository.getEndCharacter()),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        List<ICharacter> characters = List.of(characterRepository.getCharacter("a"));
        Integer result = node.getPartialMatchingFactor(characters);
        assertThat(result).isNull();
    }

    @Test
    public void testGetPartialMatchingFactorWithBothStartAndEndSoundAndNoMatchingFirstCharacter() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        Node node = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getStartCharacter()),
                                List.of(characterRepository.getCharacter("a")),
                                List.of(characterRepository.getEndCharacter()),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        List<ICharacter> characters = List.of(characterRepository.getCharacter("b"));
        Integer result = node.getPartialMatchingFactor(characters);
        assertThat(result).isNull();
    }

    @Test
    public void testGetPartialMatchingFactorWithOnlyMatchingCharacters() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        Node node = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getStartCharacter()),
                                List.of(
                                        characterRepository.getCharacter("a"),
                                        characterRepository.getCharacter("b"),
                                        characterRepository.getCharacter("c")
                                ),
                                List.of(characterRepository.getEndCharacter()),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("d"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        Integer result = node.getPartialMatchingFactor(characters);
        assertThat(result).isEqualTo(10);
    }

    @Test
    public void testGetPartialMatchingFactorWithPartiallyMatchingLastCharacter() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        Node node = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getStartCharacter()),
                                List.of(
                                        characterRepository.getCharacter("a"),
                                        characterRepository.getCharacter("b"),
                                        characterRepository.getCharacter("c")
                                ),
                                List.of(characterRepository.getEndCharacter()),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("e"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        Integer result = node.getPartialMatchingFactor(characters);
        assertThat(result).isEqualTo(12);
    }

    @Test
    public void testGetPartialMatchingFactorWithFullMatch() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        Node node = new Node(
                new Rule(
                        new Context(
                                List.of(characterRepository.getStartCharacter()),
                                List.of(
                                        characterRepository.getCharacter("a"),
                                        characterRepository.getCharacter("b"),
                                        characterRepository.getCharacter("c")
                                ),
                                List.of(characterRepository.getEndCharacter()),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("b"),
                characterRepository.getCharacter("c")
        );
        Integer result = node.getPartialMatchingFactor(characters);
        assertThat(result).isEqualTo(14);
    }

}
