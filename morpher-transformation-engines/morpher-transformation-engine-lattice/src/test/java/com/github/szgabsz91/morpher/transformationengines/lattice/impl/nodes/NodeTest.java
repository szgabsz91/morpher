package com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes;

import com.github.szgabsz91.morpher.transformationengines.api.characters.ICharacter;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.ProcessingType;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Context;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Addition;
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
    public void testConstructorWithRuleAndInconsistentAndParentsAndChildren(ICharacterRepository characterRepository) {
        Rule rule = mock(Rule.class);
        List<Node> parents = List.of(mock(Node.class));
        List<Node> children = List.of(mock(Node.class));
        Node node = new Node(rule, true, parents, children);
        assertThat(node.getRule()).isEqualTo(rule);
        assertThat(node.getParents()).isEqualTo(parents);
        assertThat(node.getChildren()).isEqualTo(children);
        assertThat(node.isInconsistent()).isTrue();
        assertThat(node.isConsistent()).isFalse();
        assertThat(node.getLevel()).isEqualTo(0L);
        assertThat(node.getFrequency()).isEqualTo(0L);
        assertThat(node.isUnit()).isFalse();
        assertThat(node.isZero()).isFalse();
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
        assertThat(node.isInconsistent()).isFalse();
        assertThat(node.isConsistent()).isTrue();
        assertThat(node.getLevel()).isEqualTo(0L);
        assertThat(node.getFrequency()).isEqualTo(0L);
        assertThat(node.isUnit()).isFalse();
        assertThat(node.isZero()).isFalse();
        assertThat(node.isAtomic()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConstructorWithRuleAndInconsistent(ICharacterRepository characterRepository) {
        Rule rule = mock(Rule.class);
        Node node = new Node(rule, true);
        assertThat(node.getRule()).isEqualTo(rule);
        assertThat(node.getParents()).isEmpty();
        assertThat(node.getChildren()).isEmpty();
        assertThat(node.isInconsistent()).isTrue();
        assertThat(node.isConsistent()).isFalse();
        assertThat(node.getLevel()).isEqualTo(0L);
        assertThat(node.getFrequency()).isEqualTo(0L);
        assertThat(node.isUnit()).isFalse();
        assertThat(node.isZero()).isFalse();
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
        assertThat(node.isInconsistent()).isFalse();
        assertThat(node.isConsistent()).isTrue();
        assertThat(node.getLevel()).isEqualTo(0L);
        assertThat(node.getFrequency()).isEqualTo(0L);
        assertThat(node.isUnit()).isFalse();
        assertThat(node.isZero()).isFalse();
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
        List<Node> parents = List.of(new UnitNode());
        List<Node> children = List.of(new ZeroNode());
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
        node.addParent(new UnitNode());
        node.addParent(new UnitNode());
        assertThat(node.getParents()).hasSize(1);
        assertThat(node.getParents()).containsSequence(new UnitNode());
        assertThat(node.getParents().get(0).getChildren()).hasSize(1);
        assertThat(node.getParents().get(0).getChildren()).containsSequence(node);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRemoveParents(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        node.addParent(new UnitNode());
        node.addParent(new ZeroNode());
        node.removeParents(Set.of(new UnitNode(), new ZeroNode()));
        assertThat(node.getParents()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testAddChild(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        node.addChild(new UnitNode());
        node.addChild(new UnitNode());
        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChildren()).containsSequence(new UnitNode());
        assertThat(node.getChildren().get(0).getParents()).hasSize(1);
        assertThat(node.getChildren().get(0).getParents()).containsSequence(node);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRemoveChildren(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        node.addChild(new UnitNode());
        node.addChild(new ZeroNode());
        node.removeChildren(Set.of(new UnitNode(), new ZeroNode()));
        assertThat(node.getChildren()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsUnit(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        assertThat(node.isUnit()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsZero(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        assertThat(node.isZero()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsConsistent(ICharacterRepository characterRepository) {
        Node inconsistentNode1 = new Node(null, true);
        Node inconsistentNode2 = new Node(null, true);
        Node consistentNode = new Node(null, false);

        assertThat(inconsistentNode1.isConsistent()).isFalse();
        assertThat(inconsistentNode2.isConsistent()).isFalse();
        assertThat(consistentNode.isConsistent()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsInconsistent(ICharacterRepository characterRepository) {
        Node inconsistentNode1 = new Node(null, true);
        Node inconsistentNode2 = new Node(null, true);
        Node consistentNode = new Node(null, false);

        assertThat(inconsistentNode1.isInconsistent()).isTrue();
        assertThat(inconsistentNode2.isInconsistent()).isTrue();
        assertThat(consistentNode.isInconsistent()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testSetInconsistent(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        assertThat(node.isInconsistent()).isFalse();
        assertThat(node.isConsistent()).isTrue();

        node.setInconsistent();
        assertThat(node.isInconsistent()).isTrue();
        assertThat(node.isConsistent()).isFalse();
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
    public void testIsAtomicWithOneNonZeroChild(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        node.getChildren().add(new Node(null));
        boolean result = node.isAtomic();
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsAtomicWithOneZeroChild(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        node.getChildren().add(new ZeroNode());
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
        node.setInconsistent();
        node.setProcessingStatus(ProcessingType.GENERAL_PROCESSING, 100L);
        node.setLevel(100L);
        node.setFrequency(100L);

        node.reset();

        assertThat(node.getParents()).isEmpty();
        assertThat(node.getChildren()).isEmpty();
        assertThat(node.isConsistent()).isTrue();
        assertThat(node.isProcessed(ProcessingType.GENERAL_PROCESSING, 1L)).isFalse();
        assertThat(node.getLevel()).isEqualTo(0L);
        assertThat(node.getFrequency()).isEqualTo(0L);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testShouldBecomeInconsistentWithInconsistentDescendant(ICharacterRepository characterRepository) {
        Node node1 = new Node(null, false);
        Node node2 = new Node(null, true);
        boolean result = node1.shouldBecomeInconsistentBasedOnDescendant(node2);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testShouldBecomeInconsistentWithStartCharacterInPrefixOfThisRule(ICharacterRepository characterRepository) {
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
        boolean result = node1.shouldBecomeInconsistentBasedOnDescendant(node2);
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

        assertThat(node1.equals(node1)).isTrue();
        assertThat(node1.equals(null)).isFalse();
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
    public void testToStringWithNonZeroNode(ICharacterRepository characterRepository) {
        String ruleString = "rule";
        Rule rule = mock(Rule.class);
        when(rule.toString()).thenReturn(ruleString);
        Node node = new Node(rule, true);
        assertThat(node).hasToString("Node[rule=" + ruleString + ", inconsistent=true]");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testToStringWithZeroNode(ICharacterRepository characterRepository) {
        Node node = new Node(null, true);
        assertThat(node).hasToString("Node[rule=null, inconsistent=true]");
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
        Node node = new Node(rule, List.of(), List.of(new ZeroNode()));
        assertThat(node.hierarchicalToString(0))
                .isEqualTo(String.format(
                        "Node[rule=Rule[[[], [a], [], null, null], []], inconsistent=false] - [a]%n  0 - []%n")
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
