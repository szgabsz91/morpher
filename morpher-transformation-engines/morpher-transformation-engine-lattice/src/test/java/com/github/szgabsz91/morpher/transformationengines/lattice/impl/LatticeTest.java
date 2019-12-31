package com.github.szgabsz91.morpher.transformationengines.lattice.impl;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.Vowel;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IdentityWordConverter;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.ILatticeBuildListener;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.UnitNode;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.ZeroNode;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Context;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Removal;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Replacement;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.IWordPairProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.WordPairProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LatticeTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    IWordConverter wordConverter = new DoubleConsonantWordConverter();
                    return Arguments.of(
                            characterRepository,
                            wordConverter
                    );
                });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConstructor(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node unitNode = new UnitNode();
        Node zeroNode = new ZeroNode();
        Lattice lattice = new Lattice(unitNode, zeroNode, characterRepository, wordConverter);
        assertThat(lattice.getUnitNode()).isEqualTo(unitNode);
        assertThat(lattice.getZeroNode()).isEqualTo(zeroNode);
        assertThat(lattice.getCharacterRepository()).isSameAs(characterRepository);
        assertThat(lattice.getWordConverter()).isSameAs(wordConverter);
        assertThat(lattice.isEmpty()).isTrue();
        assertThat(lattice.size()).isEqualTo(2);
        assertThat(lattice.getBuildListener()).isNull();
        assertThat(lattice.getLatticeWalker()).isNotNull();
        lattice.setBuildListener(new ILatticeBuildListener() {
            @Override
            public boolean skipNodeInserting(Node node, Set<Node> children) {
                return false;
            }

            @Override
            public boolean onNodeBecomingInconsistent(Lattice lattice, Node node) {
                return false;
            }

            @Override
            public void onNodeInserted(Lattice lattice, Node node) {

            }
        });
        assertThat(lattice.getBuildListener()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetNextProcessingStatusWithNotYetExistentProcessingType(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        assertThat(lattice.getNextProcessingStatus(ProcessingType.CHILD_LIST_SEARCH)).isEqualTo(1L);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetNextProcessingStatusWithExistentProcessingType(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        assertThat(lattice.getNextProcessingStatus(ProcessingType.CHILD_LIST_SEARCH)).isEqualTo(1L);
        assertThat(lattice.getNextProcessingStatus(ProcessingType.CHILD_LIST_SEARCH)).isEqualTo(2L);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetInconsistentNodes(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node unitNode = new UnitNode();
        Node zeroNode = new ZeroNode();
        Node node = new Node(null, true, List.of(unitNode), List.of(zeroNode));
        Lattice lattice = new Lattice(unitNode, zeroNode, characterRepository, null);
        lattice.addNode(node);
        Node[] result = lattice.getInconsistentNodes();
        Node[] expected = new Node[] { node };
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testAddNodesWithNoDuplicates(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node node1 = new Node(
                new Rule(
                        Context.identity(),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        Node node2 = new Node(
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
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        lattice.addNodes(new Node[] { node1, node2 });
        assertThat(lattice.getNodes()).hasSize(2);
        assertThat(lattice.getNodes()).contains(node1, node2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testAddNodesWithDuplicates(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node node = new Node(
                new Rule(
                        Context.identity(),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> lattice.addNodes(new Node[] { node, node }));
        assertThat(exception.getMessage()).isEqualTo("A node with pattern " + node.getPattern() + " is already present, cannot add new " + "node " + node);
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(node);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testAddNodeWithNoDuplicates(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node node1 = new Node(
                new Rule(
                        Context.identity(),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        Node node2 = new Node(
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
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        lattice.addNode(node1);
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(node1);
        lattice.addNode(node2);
        assertThat(lattice.getNodes()).hasSize(2);
        assertThat(lattice.getNodes()).contains(node1, node2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testAddNodeWithDuplicates(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node node = new Node(
                new Rule(
                        Context.identity(),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        lattice.addNode(node);
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(node);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> lattice.addNode(node));
        assertThat(exception.getMessage()).isEqualTo("A node with pattern " + node.getPattern() + " is already present, cannot add new " + "node " + node);
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(node);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHasNodeWithUnitNode(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        Node node = new UnitNode();
        boolean result = lattice.hasNode(node);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHasNodeWithZeroNode(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        Node node = new ZeroNode();
        boolean result = lattice.hasNode(node);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHasNodeWithExistingPattern(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        Node node = new Node(Rule.identity());
        lattice.insertNode(node);
        boolean result = lattice.hasNode(node);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHasNodeWithNonExistingPattern(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        Node node1 = new Node(Rule.identity());
        Node node2 = new Node(
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
        lattice.insertNode(node1);
        boolean result = lattice.hasNode(node2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetNodesExceptFor(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node unitNode = new UnitNode();
        Node zeroNode = new ZeroNode();
        Node node1 = new Node(
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
                List.of(unitNode),
                List.of(zeroNode)
        );
        Node node2 = new Node(
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
                ),
                List.of(unitNode),
                List.of(zeroNode)
        );
        Lattice lattice = new Lattice(unitNode, zeroNode, characterRepository, null);
        lattice.addNodes(new Node[] { node1, node2 });
        Node[] result = lattice.getNodesExceptFor(node1)
                .toArray(Node[]::new);
        Node[] expected = new Node[] { node2 };
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testSize(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node unitNode = new UnitNode();
        Node zeroNode = new ZeroNode();
        Node node = mock(Node.class);
        Lattice lattice = new Lattice(unitNode, zeroNode, characterRepository, null);
        lattice.addNode(node);
        assertThat(lattice.size()).isEqualTo(3);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsEmptyWithEmptyLattice(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        boolean result = lattice.isEmpty();
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsEmptyWithNonEmptyLattice(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        lattice.addNode(new Node(Rule.identity()));
        boolean result = lattice.isEmpty();
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesWithNonMatchingWord(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Word word = Word.of("abc");
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        boolean result = lattice.matches(word);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesWithMatchingWord(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Word word = Word.of("abc");
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        Node emptyNode = new Node(null);
        Node matchingNode = new Node(new Rule(
                new Context(
                        List.of(characterRepository.getCharacter("a")),
                        List.of(characterRepository.getCharacter("b")),
                        List.of(characterRepository.getCharacter("c")),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(),
                characterRepository,
                wordConverter
        ));
        lattice.getUnitNode().addChild(emptyNode);
        lattice.getUnitNode().addChild(matchingNode);
        lattice.getUnitNode().removeChildren(Set.of(lattice.getZeroNode()));
        boolean result = lattice.matches(word);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testInsertNodeWithAnExistingNode(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node node = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        ));
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        lattice.addNode(node);
        boolean result = lattice.insertNode(node);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testInsertNodeWithNonExistentNode(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node node = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        ));
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        boolean result = lattice.insertNode(node);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testInsertNodeWithSettingAnExistingNodeInconsistentAndHavingALatticeBuildListener(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node node1 = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        ));
        Node node2 = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Removal(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        ));
        ILatticeBuildListener latticeBuildListener = mock(ILatticeBuildListener.class);
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        lattice.setBuildListener(latticeBuildListener);
        boolean result1 = lattice.insertNode(node1);
        assertThat(result1).isTrue();
        boolean result2 = lattice.insertNode(node2);
        assertThat(result2).isTrue();
        verify(latticeBuildListener).onNodeBecomingInconsistent(lattice, node1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testInsertNodeWithSettingAnExistingNodeInconsistentAndNotHavingALatticeBuildListener(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node node1 = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        ));
        Node node2 = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Removal(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        ));
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        boolean result1 = lattice.insertNode(node1);
        assertThat(result1).isTrue();
        boolean result2 = lattice.insertNode(node2);
        assertThat(result2).isTrue();
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(node1);
        assertThat(lattice.getNodes()).doesNotContain(node2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testInsertNodeWithExistingNode(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node a = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
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
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        ));

        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        lattice.getUnitNode().getChildren().remove(lattice.getZeroNode());
        lattice.getZeroNode().getParents().remove(lattice.getUnitNode());
        lattice.getUnitNode().addChild(a);
        lattice.getUnitNode().addChild(b);
        a.addChild(lattice.getZeroNode());
        b.addChild(lattice.getZeroNode());
        lattice.addNodes(new Node[] { a, b });

        assertThat(lattice.insertNode(a)).isFalse();
        assertThat(lattice.insertNode(b)).isFalse();
        assertThat(lattice.getNodes()).hasSize(2);
        assertThat(lattice.getNodes()).contains(a, b);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testInsertNodeWithBuilderSkippingTheNode(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        ILatticeBuildListener latticeBuildListener = mock(ILatticeBuildListener.class);
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        lattice.setBuildListener(latticeBuildListener);

        Node node = new Node(Rule.identity());
        when(latticeBuildListener.skipNodeInserting(node, Set.of(lattice.getZeroNode()))).thenReturn(true);

        boolean result = lattice.insertNode(node);
        assertThat(result).isTrue();
        assertThat(lattice.size()).isEqualTo(2);
        assertThat(lattice.getNodes()).isEmpty();
        verify(latticeBuildListener).skipNodeInserting(node, Set.of(lattice.getZeroNode()));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRemoveNode(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node[] nodes = new Node[] {
                new Node(new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("a")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        Set.of(Length.LONG),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )),
                new Node(new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("b")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        Set.of(Length.LONG),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )),
                new Node(new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("c")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        Set.of(Length.LONG),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                ))
        };
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        lattice.addNodes(nodes);
        lattice.getUnitNode().addChild(nodes[0]);
        lattice.getUnitNode().addChild(nodes[1]);
        lattice.getUnitNode().addChild(nodes[2]);
        lattice.getZeroNode().addParent(nodes[0]);
        lattice.getZeroNode().addParent(nodes[1]);
        lattice.getZeroNode().addParent(nodes[2]);
        lattice.getUnitNode().removeChildren(Set.of(lattice.getZeroNode()));
        lattice.getZeroNode().removeParents(Set.of(lattice.getUnitNode()));

        lattice.removeNode(nodes[0]);
        assertThat(lattice.getNodes()).hasSize(2);
        assertThat(lattice.getUnitNode().getChildren()).hasSize(2);
        assertThat(lattice.getUnitNode().getChildren()).containsSequence(nodes[1], nodes[2]);
        assertThat(nodes[1].getChildren()).hasSize(1);
        assertThat(nodes[1].getChildren()).containsSequence(lattice.getZeroNode());
        assertThat(lattice.getZeroNode().getParents()).hasSize(2);
        assertThat(lattice.getZeroNode().getParents()).containsSequence(nodes[1], nodes[2]);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRemoveNodeWithAddingNewRelationships(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        /*
         * Test lattice structure:
         * - 1
         *     - a
         *         - c
         *             - 0
         *     - b
         *         - 0
         * We are removing c and the resulting lattice should be:
         * - 1
         *     - a
         *         - 0
         *     - b
         *         - 0
         */
        Node unitNode = new UnitNode();
        Node a = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
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
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
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
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        ));
        Node zeroNode = new ZeroNode();
        Lattice lattice = new Lattice(unitNode, zeroNode, characterRepository, null);
        lattice.addNodes(new Node[] { a, b, c });
        unitNode.getChildren().remove(zeroNode);
        zeroNode.getParents().remove(unitNode);
        unitNode.addChild(a);
        unitNode.addChild(b);
        a.addChild(c);
        b.addChild(zeroNode);
        c.addChild(zeroNode);

        lattice.removeNode(c);
        assertThat(unitNode.getChildren()).hasSize(2);
        assertThat(unitNode.getChildren()).contains(a, b);
        assertThat(a.getChildren()).hasSize(1);
        assertThat(a.getChildren()).containsSequence(zeroNode);
        assertThat(c.getChildren()).hasSize(1);
        assertThat(c.getChildren()).containsSequence(zeroNode);
        assertThat(lattice.getNodes()).hasSize(2);
        assertThat(lattice.getNodes()).contains(a, b);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testFillDominantRulesWithNodeLevelsAlreadySet(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node unitNode = new UnitNode();
        Node zeroNode = new ZeroNode();
        zeroNode.setLevel(1L);
        LatticeWalker walker = mock(LatticeWalker.class);
        Lattice lattice = new Lattice(unitNode, zeroNode, characterRepository, null);
        lattice.setLatticeWalker(walker);
        assertThat(lattice.getUnitNode()).isEqualTo(unitNode);
        assertThat(lattice.getZeroNode()).isEqualTo(zeroNode);
        assertThat(lattice.isEmpty()).isTrue();
        assertThat(lattice.size()).isEqualTo(2);

        lattice.fillDominantRules();
        verify(walker).fillDominantRules();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testFillDominantRulesWithZeroNodeLevels(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node unitNode = new UnitNode();
        Node zeroNode = new ZeroNode();
        LatticeWalker walker = mock(LatticeWalker.class);
        Lattice lattice = new Lattice(unitNode, zeroNode, characterRepository, null);
        lattice.setLatticeWalker(walker);
        assertThat(lattice.getUnitNode()).isEqualTo(unitNode);
        assertThat(lattice.getZeroNode()).isEqualTo(zeroNode);
        assertThat(lattice.isEmpty()).isTrue();
        assertThat(lattice.size()).isEqualTo(2);

        lattice.fillDominantRules();
        verify(walker).fillNodeLevels();
        verify(walker).fillDominantRules();
    }

    @Test
    public void testFillLevels() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new IdentityWordConverter();
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        Node node = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("a")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Replacement(characterRepository.getCharacter("a"), characterRepository.getCharacter("b"), characterRepository)
                        ),
                        characterRepository,
                        wordConverter
                )
        );
        lattice.insertNode(node);
        lattice.fillNodeLevels();
        assertThat(lattice.getUnitNode().getLevel()).isEqualTo(0);
        assertThat(lattice.getUnitNode().getChildren()).hasSize(1);
        assertThat(lattice.getUnitNode().getChildren().get(0).getLevel()).isEqualTo(1);
        assertThat(lattice.getUnitNode().getChildren().get(0).getChildren()).hasSize(1);
        assertThat(lattice.getUnitNode().getChildren().get(0).getChildren().get(0).getLevel()).isEqualTo(2);
    }

    @Test
    public void testMatchWithMatchingChildrenWithHungarianSimpleCharacterRepository() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        Node a = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(),
                characterRepository,
                wordConverter
        ));
        Node e = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("e")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(),
                characterRepository,
                wordConverter
        ));

        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        lattice.getUnitNode().getChildren().remove(lattice.getZeroNode());
        lattice.getZeroNode().getParents().remove(lattice.getUnitNode());
        lattice.getUnitNode().addChild(a);
        lattice.getUnitNode().addChild(e);
        a.addChild(lattice.getZeroNode());
        e.addChild(lattice.getZeroNode());
        lattice.addNodes(new Node[] { a, e });

        Node result = lattice.match(Word.of("xiz"));
        assertThat(result).isEqualTo(lattice.getUnitNode());
    }

    @Test
    public void testMatchWithMatchingChildrenWithHungarianAttributedCharacterRepository() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        Node a = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        ));
        Node e = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("e")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        ));
        Node vowel = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(Vowel.create()),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        ));

        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        lattice.getUnitNode().getChildren().remove(lattice.getZeroNode());
        lattice.getZeroNode().getParents().remove(lattice.getUnitNode());
        lattice.getUnitNode().addChild(vowel);
        vowel.addChild(a);
        vowel.addChild(e);
        a.addChild(lattice.getZeroNode());
        e.addChild(lattice.getZeroNode());
        lattice.addNodes(new Node[] { vowel, a, e });

        Node result = lattice.match(Word.of("xaz"));
        assertThat(result).isEqualTo(vowel);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchWithUnknownWord(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node a = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        ));
        Node e = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("e")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        ));
        Node vowel = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(Vowel.create()),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                wordConverter
        ));

        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        lattice.getUnitNode().getChildren().remove(lattice.getZeroNode());
        lattice.getZeroNode().getParents().remove(lattice.getUnitNode());
        lattice.getUnitNode().addChild(vowel);
        vowel.addChild(a);
        vowel.addChild(e);
        a.addChild(lattice.getZeroNode());
        e.addChild(lattice.getZeroNode());
        lattice.addNodes(new Node[] { vowel, a, e });

        Node result = lattice.match(Word.of("xyz"));
        assertThat(result).isEqualTo(lattice.getUnitNode());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetNodeWithMostChildrenWithNonRootResult(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node a = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
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
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        ));
        Node vowel = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(Vowel.create()),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        ));

        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        lattice.getUnitNode().getChildren().remove(lattice.getZeroNode());
        lattice.getZeroNode().getParents().remove(lattice.getUnitNode());
        lattice.getUnitNode().addChild(vowel);
        vowel.addChild(a);
        vowel.addChild(e);
        a.addChild(lattice.getZeroNode());
        e.addChild(lattice.getZeroNode());
        lattice.addNodes(new Node[] { vowel, a, e });

        Node result = lattice.getNodeWithMostChildren();
        assertThat(result).isEqualTo(lattice.getUnitNode().getChildren().get(0));
        assertThat(result.getChildren()).hasSize(2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetNodeWithMostChildrenWithUnitResult(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node a = new Node(new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
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
                List.of(
                        new Addition(
                                Set.of(Length.LONG),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        ));

        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        lattice.getUnitNode().getChildren().remove(lattice.getZeroNode());
        lattice.getZeroNode().getParents().remove(lattice.getUnitNode());
        lattice.getUnitNode().addChild(a);
        lattice.getUnitNode().addChild(b);
        a.addChild(lattice.getZeroNode());
        b.addChild(lattice.getZeroNode());
        lattice.addNodes(new Node[] { a, b });

        Node result = lattice.getNodeWithMostChildren();
        assertThat(result).isEqualTo(lattice.getUnitNode());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetZeroLevel(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node unitNode = new UnitNode();
        Node zeroNode = new ZeroNode();
        long expected = 3L;
        LatticeWalker walker = mock(LatticeWalker.class);
        doAnswer(invocation -> {
            zeroNode.setLevel(expected);
            return null;
        }).when(walker).fillNodeLevels();
        Lattice lattice = new Lattice(unitNode, zeroNode, characterRepository, null);
        lattice.setLatticeWalker(walker);
        assertThat(lattice.getUnitNode()).isEqualTo(unitNode);
        assertThat(lattice.getZeroNode()).isEqualTo(zeroNode);
        assertThat(lattice.isEmpty()).isTrue();
        assertThat(lattice.size()).isEqualTo(2);

        long result = lattice.getZeroNodeLevel();
        assertThat(result).isEqualTo(expected);
        verify(walker).fillNodeLevels();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetMaximalConsistentNodes(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Set<Node> expected = Set.of(new UnitNode());
        LatticeWalker walker = mock(LatticeWalker.class);
        when(walker.getMaximalConsistentNodes()).thenReturn(expected);

        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        lattice.setLatticeWalker(walker);

        Set<Node> result = lattice.getMaximalConsistentNodes();

        assertThat(result).isEqualTo(expected);
        verify(walker).getMaximalConsistentNodes();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testInsertingInconsistentNodeWithNoListener(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node node = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(),
                                List.of(characterRepository.getCharacter("#")),
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
                true
        );
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        boolean result = lattice.insertNode(node);
        assertThat(result).isTrue();
        assertThat(lattice.size()).isEqualTo(3);
        assertThat(lattice.getUnitNode().getChildren()).hasSize(1);
        assertThat(lattice.getUnitNode().getChildren()).containsSequence(node);
        assertThat(lattice.getZeroNode().getParents()).hasSize(1);
        assertThat(lattice.getZeroNode().getParents()).containsSequence(node);
        assertThat(node.getParents()).hasSize(1);
        assertThat(node.getParents()).containsSequence(lattice.getUnitNode());
        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChildren()).containsSequence(lattice.getZeroNode());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testInsertingConsistentNodeWithNoListener(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node node1 = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(),
                                List.of(characterRepository.getCharacter("#")),
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
                                List.of(characterRepository.getCharacter("a")),
                                List.of(characterRepository.getCharacter("#")),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Replacement(
                                        characterRepository.getCharacter("a"), characterRepository.getCharacter("á"),
                                        characterRepository
                                ),
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
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, null);
        lattice.insertNode(node1);
        boolean result = lattice.insertNode(node2);
        assertThat(result).isTrue();
        assertThat(lattice.size()).isEqualTo(4);
        assertThat(lattice.getUnitNode().getChildren()).hasSize(1);
        assertThat(lattice.getUnitNode().getChildren()).containsSequence(node1);
        assertThat(node1.getChildren()).hasSize(1);
        assertThat(node1.getChildren()).containsSequence(node2);
        assertThat(node2.getChildren()).hasSize(1);
        assertThat(node2.getChildren()).containsSequence(lattice.getZeroNode());
        assertThat(lattice.getZeroNode().getParents()).hasSize(1);
        assertThat(lattice.getZeroNode().getParents()).containsSequence(node2);
        assertThat(node2.getParents()).hasSize(1);
        assertThat(node2.getParents()).containsSequence(node1);
        assertThat(node1.getParents()).hasSize(1);
        assertThat(node1.getParents()).containsSequence(lattice.getUnitNode());
    }

    @Test
    public void testMatchThatShouldReturnTheUnitNode() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                .costCalculator(new AttributeBasedCostCalculator())
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .build();

        /*
         * 1
         *     - édesség
         *     - szépség
         *     - éhség
         */
        Rule ruleSzepseg = wordPairProcessor.induceRules(WordPair.of(Word.of("szépség"), Word.of("szépséget"))).getRules().get(0);
        Rule ruleEhseg = wordPairProcessor.induceRules(WordPair.of(Word.of("éhség"), Word.of("éhséget"))).getRules().get(0);
        Rule ruleEdesseg = wordPairProcessor.induceRules(WordPair.of(Word.of("édesség"), Word.of("édességet"))).getRules().get(0);
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        lattice.getUnitNode().getChildren().clear();
        lattice.getUnitNode().addChild(new Node(ruleSzepseg));
        lattice.getUnitNode().addChild(new Node(ruleEhseg));
        lattice.getUnitNode().addChild(new Node(ruleEdesseg));

        Word word = Word.of("mézesség");
        Node result = lattice.match(word);
        assertThat(result.getRule()).isEqualTo(ruleEdesseg);
    }

}
