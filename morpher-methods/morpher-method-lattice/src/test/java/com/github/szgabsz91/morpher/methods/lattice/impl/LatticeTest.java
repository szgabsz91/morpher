package com.github.szgabsz91.morpher.methods.lattice.impl;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.Vowel;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IdentityWordConverter;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.ILatticeBuildListener;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.EmptyNode;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.FullNode;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Context;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Removal;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Replacement;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.IWordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.WordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
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
        Node root = new FullNode();
        Node leaf = new EmptyNode();
        Lattice lattice = new Lattice(root, leaf, characterRepository, wordConverter);
        assertThat(lattice.getRoot()).isEqualTo(root);
        assertThat(lattice.getLeaf()).isEqualTo(leaf);
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
            public boolean onNodeBecomingInhomogeneous(Lattice lattice, Node node) {
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
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        assertThat(lattice.getNextProcessingStatus(ProcessingType.CHILD_LIST_SEARCH)).isEqualTo(1L);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetNextProcessingStatusWithExistentProcessingType(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        assertThat(lattice.getNextProcessingStatus(ProcessingType.CHILD_LIST_SEARCH)).isEqualTo(1L);
        assertThat(lattice.getNextProcessingStatus(ProcessingType.CHILD_LIST_SEARCH)).isEqualTo(2L);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetInhomogeneousNodes(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node root = new FullNode();
        Node leaf = new EmptyNode();
        Node node = new Node(null, true, List.of(root), List.of(leaf));
        Lattice lattice = new Lattice(root, leaf, characterRepository, null);
        lattice.addNode(node);
        Node[] result = lattice.getInhomogeneousNodes();
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
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
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
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
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
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
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
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
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
    public void testHasNodeWithFullNode(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        Node node = new FullNode();
        boolean result = lattice.hasNode(node);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHasNodeWithEmptyNode(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        Node node = new EmptyNode();
        boolean result = lattice.hasNode(node);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHasNodeWithExistingPattern(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        Node node = new Node(Rule.identity());
        lattice.insertNode(node);
        boolean result = lattice.hasNode(node);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHasNodeWithNonExistingPattern(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
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
        Node root = new FullNode();
        Node leaf = new EmptyNode();
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
                List.of(root),
                List.of(leaf)
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
                List.of(root),
                List.of(leaf)
        );
        Lattice lattice = new Lattice(root, leaf, characterRepository, null);
        lattice.addNodes(new Node[] { node1, node2 });
        Node[] result = lattice.getNodesExceptFor(node1)
                .toArray(Node[]::new);
        Node[] expected = new Node[] { node2 };
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testSize(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node root = new FullNode();
        Node leaf = new EmptyNode();
        Node node = mock(Node.class);
        Lattice lattice = new Lattice(root, leaf, characterRepository, null);
        lattice.addNode(node);
        assertThat(lattice.size()).isEqualTo(3);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsEmptyWithEmptyLattice(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        boolean result = lattice.isEmpty();
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsEmptyWithNonEmptyLattice(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        lattice.addNode(new Node(Rule.identity()));
        boolean result = lattice.isEmpty();
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesWithNonMatchingWord(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Word word = Word.of("abc");
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
        boolean result = lattice.matches(word);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testMatchesWithMatchingWord(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Word word = Word.of("abc");
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
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
        lattice.getRoot().addChild(emptyNode);
        lattice.getRoot().addChild(matchingNode);
        lattice.getRoot().removeChildren(Set.of(lattice.getLeaf()));
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
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
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
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        boolean result = lattice.insertNode(node);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testInsertNodeWithSettingAnExistingNodeInhomogeneousAndHavingALatticeBuildListener(ICharacterRepository characterRepository, IWordConverter wordConverter) {
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
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        lattice.setBuildListener(latticeBuildListener);
        boolean result1 = lattice.insertNode(node1);
        assertThat(result1).isTrue();
        boolean result2 = lattice.insertNode(node2);
        assertThat(result2).isTrue();
        verify(latticeBuildListener).onNodeBecomingInhomogeneous(lattice, node1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testInsertNodeWithSettingAnExistingNodeInhomogeneousAndNotHavingALatticeBuildListener(ICharacterRepository characterRepository, IWordConverter wordConverter) {
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
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
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

        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        lattice.getRoot().getChildren().remove(lattice.getLeaf());
        lattice.getLeaf().getParents().remove(lattice.getRoot());
        lattice.getRoot().addChild(a);
        lattice.getRoot().addChild(b);
        a.addChild(lattice.getLeaf());
        b.addChild(lattice.getLeaf());
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
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        lattice.setBuildListener(latticeBuildListener);

        Node node = new Node(Rule.identity());
        when(latticeBuildListener.skipNodeInserting(node, Set.of(lattice.getLeaf()))).thenReturn(true);

        boolean result = lattice.insertNode(node);
        assertThat(result).isTrue();
        assertThat(lattice.size()).isEqualTo(2);
        assertThat(lattice.getNodes()).isEmpty();
        verify(latticeBuildListener).skipNodeInserting(node, Set.of(lattice.getLeaf()));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRemoveNode(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node[] nodes = new Node[]{
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
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        lattice.addNodes(nodes);
        lattice.getRoot().addChild(nodes[0]);
        lattice.getRoot().addChild(nodes[1]);
        lattice.getRoot().addChild(nodes[2]);
        lattice.getLeaf().addParent(nodes[0]);
        lattice.getLeaf().addParent(nodes[1]);
        lattice.getLeaf().addParent(nodes[2]);
        lattice.getRoot().removeChildren(Set.of(lattice.getLeaf()));
        lattice.getLeaf().removeParents(Set.of(lattice.getRoot()));

        lattice.removeNode(nodes[0]);
        assertThat(lattice.getNodes()).hasSize(2);
        assertThat(lattice.getRoot().getChildren()).hasSize(2);
        assertThat(lattice.getRoot().getChildren()).containsSequence(nodes[1], nodes[2]);
        assertThat(nodes[1].getChildren()).hasSize(1);
        assertThat(nodes[1].getChildren()).containsSequence(lattice.getLeaf());
        assertThat(lattice.getLeaf().getParents()).hasSize(2);
        assertThat(lattice.getLeaf().getParents()).containsSequence(nodes[1], nodes[2]);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRemoveNodeWithAddingNewRelationships(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        /*
         * Test lattice structure:
         * - ROOT
         *     - a
         *         - c
         *             - leaf
         *     - b
         *         - leaf
         * We are removing c and the resulting lattice should be:
         * - ROOT
         *     - a
         *         - leaf
         *     - b
         *         - leaf
         */
        Node root = new FullNode();
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
        Node leaf = new EmptyNode();
        Lattice lattice = new Lattice(root, leaf, characterRepository, null);
        lattice.addNodes(new Node[] { a, b, c });
        root.getChildren().remove(leaf);
        leaf.getParents().remove(root);
        root.addChild(a);
        root.addChild(b);
        a.addChild(c);
        b.addChild(leaf);
        c.addChild(leaf);

        lattice.removeNode(c);
        assertThat(root.getChildren()).hasSize(2);
        assertThat(root.getChildren()).contains(a, b);
        assertThat(a.getChildren()).hasSize(1);
        assertThat(a.getChildren()).containsSequence(leaf);
        assertThat(c.getChildren()).hasSize(1);
        assertThat(c.getChildren()).containsSequence(leaf);
        assertThat(lattice.getNodes()).hasSize(2);
        assertThat(lattice.getNodes()).contains(a, b);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testFillDominantRulesWithNodeLevelsAlreadySet(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node root = new FullNode();
        Node leaf = new EmptyNode();
        leaf.setLevel(1L);
        LatticeWalker walker = mock(LatticeWalker.class);
        Lattice lattice = new Lattice(root, leaf, characterRepository, null);
        lattice.setLatticeWalker(walker);
        assertThat(lattice.getRoot()).isEqualTo(root);
        assertThat(lattice.getLeaf()).isEqualTo(leaf);
        assertThat(lattice.isEmpty()).isTrue();
        assertThat(lattice.size()).isEqualTo(2);

        lattice.fillDominantRules();
        verify(walker).fillDominantRules();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testFillDominantRulesWithEmptyNodeLevels(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node root = new FullNode();
        Node leaf = new EmptyNode();
        LatticeWalker walker = mock(LatticeWalker.class);
        Lattice lattice = new Lattice(root, leaf, characterRepository, null);
        lattice.setLatticeWalker(walker);
        assertThat(lattice.getRoot()).isEqualTo(root);
        assertThat(lattice.getLeaf()).isEqualTo(leaf);
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
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
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
        assertThat(lattice.getRoot().getLevel()).isEqualTo(0);
        assertThat(lattice.getRoot().getChildren()).hasSize(1);
        assertThat(lattice.getRoot().getChildren().get(0).getLevel()).isEqualTo(1);
        assertThat(lattice.getRoot().getChildren().get(0).getChildren()).hasSize(1);
        assertThat(lattice.getRoot().getChildren().get(0).getChildren().get(0).getLevel()).isEqualTo(2);
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

        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
        lattice.getRoot().getChildren().remove(lattice.getLeaf());
        lattice.getLeaf().getParents().remove(lattice.getRoot());
        lattice.getRoot().addChild(a);
        lattice.getRoot().addChild(e);
        a.addChild(lattice.getLeaf());
        e.addChild(lattice.getLeaf());
        lattice.addNodes(new Node[] { a, e });

        Node result = lattice.match(Word.of("xiz"));
        assertThat(result).isEqualTo(lattice.getRoot());
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

        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
        lattice.getRoot().getChildren().remove(lattice.getLeaf());
        lattice.getLeaf().getParents().remove(lattice.getRoot());
        lattice.getRoot().addChild(vowel);
        vowel.addChild(a);
        vowel.addChild(e);
        a.addChild(lattice.getLeaf());
        e.addChild(lattice.getLeaf());
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

        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
        lattice.getRoot().getChildren().remove(lattice.getLeaf());
        lattice.getLeaf().getParents().remove(lattice.getRoot());
        lattice.getRoot().addChild(vowel);
        vowel.addChild(a);
        vowel.addChild(e);
        a.addChild(lattice.getLeaf());
        e.addChild(lattice.getLeaf());
        lattice.addNodes(new Node[] { vowel, a, e });

        Node result = lattice.match(Word.of("xyz"));
        assertThat(result).isEqualTo(lattice.getRoot());
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

        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        lattice.getRoot().getChildren().remove(lattice.getLeaf());
        lattice.getLeaf().getParents().remove(lattice.getRoot());
        lattice.getRoot().addChild(vowel);
        vowel.addChild(a);
        vowel.addChild(e);
        a.addChild(lattice.getLeaf());
        e.addChild(lattice.getLeaf());
        lattice.addNodes(new Node[] { vowel, a, e });

        Node result = lattice.getNodeWithMostChildren();
        assertThat(result).isEqualTo(lattice.getRoot().getChildren().get(0));
        assertThat(result.getChildren()).hasSize(2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetNodeWithMostChildrenWithRootResult(ICharacterRepository characterRepository, IWordConverter wordConverter) {
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

        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        lattice.getRoot().getChildren().remove(lattice.getLeaf());
        lattice.getLeaf().getParents().remove(lattice.getRoot());
        lattice.getRoot().addChild(a);
        lattice.getRoot().addChild(b);
        a.addChild(lattice.getLeaf());
        b.addChild(lattice.getLeaf());
        lattice.addNodes(new Node[] { a, b });

        Node result = lattice.getNodeWithMostChildren();
        assertThat(result).isEqualTo(lattice.getRoot());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetLeafLevel(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Node root = new FullNode();
        Node leaf = new EmptyNode();
        long expected = 3L;
        LatticeWalker walker = mock(LatticeWalker.class);
        doAnswer(invocation -> {
            leaf.setLevel(expected);
            return null;
        }).when(walker).fillNodeLevels();
        Lattice lattice = new Lattice(root, leaf, characterRepository, null);
        lattice.setLatticeWalker(walker);
        assertThat(lattice.getRoot()).isEqualTo(root);
        assertThat(lattice.getLeaf()).isEqualTo(leaf);
        assertThat(lattice.isEmpty()).isTrue();
        assertThat(lattice.size()).isEqualTo(2);

        long result = lattice.getLeafLevel();
        assertThat(result).isEqualTo(expected);
        verify(walker).fillNodeLevels();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetMaximalHomogeneousNodes(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        Set<Node> expected = Set.of(new FullNode());
        LatticeWalker walker = mock(LatticeWalker.class);
        when(walker.getMaximalHomogeneousNodes()).thenReturn(expected);

        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        lattice.setLatticeWalker(walker);

        Set<Node> result = lattice.getMaximalHomogeneousNodes();

        assertThat(result).isEqualTo(expected);
        verify(walker).getMaximalHomogeneousNodes();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testInsertingInhomogeneousNodeWithNoListener(ICharacterRepository characterRepository, IWordConverter wordConverter) {
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
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        boolean result = lattice.insertNode(node);
        assertThat(result).isTrue();
        assertThat(lattice.size()).isEqualTo(3);
        assertThat(lattice.getRoot().getChildren()).hasSize(1);
        assertThat(lattice.getRoot().getChildren()).containsSequence(node);
        assertThat(lattice.getLeaf().getParents()).hasSize(1);
        assertThat(lattice.getLeaf().getParents()).containsSequence(node);
        assertThat(node.getParents()).hasSize(1);
        assertThat(node.getParents()).containsSequence(lattice.getRoot());
        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChildren()).containsSequence(lattice.getLeaf());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testInsertingHomogeneousNodeWithNoListener(ICharacterRepository characterRepository, IWordConverter wordConverter) {
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
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, null);
        lattice.insertNode(node1);
        boolean result = lattice.insertNode(node2);
        assertThat(result).isTrue();
        assertThat(lattice.size()).isEqualTo(4);
        assertThat(lattice.getRoot().getChildren()).hasSize(1);
        assertThat(lattice.getRoot().getChildren()).containsSequence(node1);
        assertThat(node1.getChildren()).hasSize(1);
        assertThat(node1.getChildren()).containsSequence(node2);
        assertThat(node2.getChildren()).hasSize(1);
        assertThat(node2.getChildren()).containsSequence(lattice.getLeaf());
        assertThat(lattice.getLeaf().getParents()).hasSize(1);
        assertThat(lattice.getLeaf().getParents()).containsSequence(node2);
        assertThat(node2.getParents()).hasSize(1);
        assertThat(node2.getParents()).containsSequence(node1);
        assertThat(node1.getParents()).hasSize(1);
        assertThat(node1.getParents()).containsSequence(lattice.getRoot());
    }

    @Test
    public void testMatchThatShouldReturnTheFullNode() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                .costCalculator(new AttributeBasedCostCalculator())
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .build();

        /*
         * FULL
         *     - édesség
         *     - szépség
         *     - éhség
         */
        Rule ruleSzepseg = wordPairProcessor.induceRules(WordPair.of(Word.of("szépség"), Word.of("szépséget"))).getRules().get(0);
        Rule ruleEhseg = wordPairProcessor.induceRules(WordPair.of(Word.of("éhség"), Word.of("éhséget"))).getRules().get(0);
        Rule ruleEdesseg = wordPairProcessor.induceRules(WordPair.of(Word.of("édesség"), Word.of("édességet"))).getRules().get(0);
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
        lattice.getRoot().getChildren().clear();
        lattice.getRoot().addChild(new Node(ruleSzepseg));
        lattice.getRoot().addChild(new Node(ruleEhseg));
        lattice.getRoot().addChild(new Node(ruleEdesseg));

        Word word = Word.of("mézesség");
        Node result = lattice.match(word);
        assertThat(result.getRule()).isEqualTo(ruleEdesseg);
    }

}
