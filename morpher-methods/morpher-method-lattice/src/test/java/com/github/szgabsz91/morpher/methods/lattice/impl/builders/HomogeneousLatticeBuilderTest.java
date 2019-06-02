package com.github.szgabsz91.morpher.methods.lattice.impl.builders;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IdentityWordConverter;
import com.github.szgabsz91.morpher.methods.lattice.impl.Lattice;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.EmptyNode;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.FullNode;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Context;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Replacement;
import com.github.szgabsz91.morpher.methods.lattice.impl.setoperations.IntersectionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.szgabsz91.morpher.methods.lattice.impl.setoperations.IntersectionCalculator.intersect;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class HomogeneousLatticeBuilderTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    IWordConverter wordConverter = new DoubleConsonantWordConverter();
                    return Arguments.of(
                            characterRepository,
                            new HomogeneousLatticeBuilder(characterRepository, wordConverter),
                            wordConverter
                    );
                });
    }

    @Test
    public void testConstructorWithFlags() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new IdentityWordConverter();
        HomogeneousLatticeBuilder latticeBuilder = new HomogeneousLatticeBuilder(characterRepository, wordConverter, true);
        assertThat(latticeBuilder.isSkipFrequencyCalculation()).isTrue();
        assertThat(latticeBuilder.getLattice().isEmpty()).isTrue();
    }

    @Test
    public void testConstructorWithFlagsAndInitialLattice() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new IdentityWordConverter();
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
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
        HomogeneousLatticeBuilder latticeBuilder = new HomogeneousLatticeBuilder(characterRepository, wordConverter, false, lattice);
        assertThat(latticeBuilder.isSkipFrequencyCalculation()).isFalse();
        assertThat(latticeBuilder.getLattice().isEmpty()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    @SuppressWarnings("unchecked")
    public void testConstructorGettersAndReset(
            ICharacterRepository characterRepository,
            ILatticeBuilder latticeBuilder,
            IWordConverter wordConverter) {
        HomogeneousLatticeBuilder homogeneousLatticeBuilder = (HomogeneousLatticeBuilder) latticeBuilder;
        assertThat(homogeneousLatticeBuilder.getCharacterRepository()).hasSameClassAs(characterRepository);
        assertThat(homogeneousLatticeBuilder.getWordConverter()).hasSameClassAs(wordConverter);

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
        homogeneousLatticeBuilder.addRule(rule);
        assertThat(homogeneousLatticeBuilder.getLattice().size()).isGreaterThan(2);
        homogeneousLatticeBuilder.reset();
        assertThat(homogeneousLatticeBuilder.getLattice().size()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    @SuppressWarnings("unchecked")
    public void testAddRules(ICharacterRepository characterRepository, ILatticeBuilder latticeBuilder, IWordConverter wordConverter) {
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
        latticeBuilder = new HomogeneousLatticeBuilder(characterRepository, wordConverter);
        latticeBuilder.addRules(rules);
        Lattice lattice = latticeBuilder.getLattice();
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(new Node(rules.iterator().next()));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testOnNodeBecomingInhomogeneousWithNodeContainingNoRule(ICharacterRepository characterRepository, ILatticeBuilder latticeBuilder, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
        Node node = new Node(null);
        lattice.addNode(node);

        latticeBuilder.onNodeBecomingInhomogeneous(lattice, node);
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(node);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testOnNodeBecomingInhomogeneousWithAtomicNode(ICharacterRepository characterRepository, ILatticeBuilder latticeBuilder, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
        lattice.getRoot().getChildren().remove(lattice.getLeaf());
        lattice.getLeaf().getParents().remove(lattice.getRoot());
        Node node = new Node(Rule.identity());
        lattice.getRoot().addChild(node);
        node.addChild(lattice.getLeaf());
        lattice.addNode(node);

        latticeBuilder.onNodeBecomingInhomogeneous(lattice, node);
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(node);
        assertThat(node.isInhomogeneous()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testOnNodeBecomingInhomogeneousWithNodeContainingARule(ICharacterRepository characterRepository, ILatticeBuilder latticeBuilder, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
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
        lattice.getRoot().getChildren().remove(lattice.getLeaf());
        lattice.getLeaf().getParents().remove(lattice.getRoot());
        lattice.getRoot().addChild(parent);
        parent.addChild(child);
        child.addChild(grandChild);
        grandChild.addChild(lattice.getLeaf());
        lattice.addNodes(new Node[] { parent, child, grandChild });

        latticeBuilder.onNodeBecomingInhomogeneous(lattice, child);
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(grandChild);
        assertThat(lattice.getLeaf().getParents()).hasSize(1);
        assertThat(lattice.getLeaf().getParents()).containsSequence(grandChild);
        assertThat(grandChild.getParents()).hasSize(1);
        assertThat(grandChild.getParents()).containsSequence(lattice.getRoot());
        assertThat(lattice.getRoot().getChildren()).hasSize(1);
        assertThat(lattice.getRoot().getChildren()).containsSequence(grandChild);
    }

    @Test
    public void testAddRulesWithHomogeneousIntersection() throws IntersectionException {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ILatticeBuilder latticeBuilder = new HomogeneousLatticeBuilder(characterRepository, wordConverter);

        Node a = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("a")),
                                List.of(characterRepository.getCharacter("#")),
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
        Node e = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("e")),
                                List.of(characterRepository.getCharacter("#")),
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
        Node intersection = intersect(a, e, characterRepository, wordConverter);
        Set<Rule> rules = Stream.of(a, e)
                .map(Node::getRule)
                .collect(toSet());
        latticeBuilder.addRules(rules);
        Lattice lattice = latticeBuilder.getLattice();
        assertThat(lattice.getRoot().getChildren()).hasSize(1);
        assertThat(lattice.getRoot().getChildren()).containsSequence(intersection);
        assertThat(lattice.getRoot().getChildren().get(0).getChildren()).hasSize(2);
        assertThat(lattice.getRoot().getChildren().get(0).getChildren()).contains(a, e);
        assertThat(lattice.getRoot().getChildren().get(0).getChildren().get(0).getChildren()).hasSize(1);
        assertThat(lattice.getRoot().getChildren().get(0).getChildren().get(0).getChildren()).containsSequence(lattice.getLeaf());
        assertThat(lattice.getRoot().getChildren().get(0).getChildren().get(1).getChildren()).hasSize(1);
        assertThat(lattice.getRoot().getChildren().get(0).getChildren().get(1).getChildren()).containsSequence(lattice.getLeaf());
    }

    @Test
    public void testMatchWithSiblingsInLattice() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new IdentityWordConverter();
        ILatticeBuilder latticeBuilder = new HomogeneousLatticeBuilder(characterRepository, wordConverter);
        List<Node> nodes = List.of(
                new Node(new Rule(
                        new Context(
                                List.of(),
                                List.of(characterRepository.getCharacter("a")),
                                List.of(),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(Set.of(Length.LONG), characterRepository)
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
                                new Addition(Set.of(Length.LONG), characterRepository)
                        ),
                        characterRepository,
                        null
                ))
        );
        Set<Rule> rules = nodes
                .stream()
                .map(Node::getRule)
                .collect(toSet());
        latticeBuilder.addRules(rules);
        Lattice lattice = latticeBuilder.getLattice();

        Node result = lattice.match(Word.of("xaz"));
        assertThat(result).isEqualTo(nodes.get(0));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testWithInhomogeneousCandidateIntersection(ICharacterRepository characterRepository, ILatticeBuilder latticeBuilder, IWordConverter wordConverter) {
        Node node1 = new Node(
                new Rule(
                        new Context(
                                List.of(
                                        characterRepository.getCharacter("$"),
                                        characterRepository.getCharacter("v"),
                                        characterRepository.getCharacter("a"),
                                        characterRepository.getCharacter("s"),
                                        characterRepository.getCharacter("r")
                                ),
                                List.of(
                                        characterRepository.getCharacter("ú"),
                                        characterRepository.getCharacter("d")
                                ),
                                List.of(characterRepository.getCharacter("#")),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Replacement(
                                        characterRepository.getCharacter("ú"),
                                        characterRepository.getCharacter("u"),
                                        characterRepository
                                ),
                                new Replacement(new HashSet<>(), characterRepository),
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("a").getAttributes()),
                                        characterRepository
                                ),
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )
        );
        Node node2 = new Node(
                new Rule(
                        new Context(
                                List.of(
                                        characterRepository.getCharacter("$"),
                                        characterRepository.getCharacter("f"),
                                        characterRepository.getCharacter("u"),
                                        characterRepository.getCharacter("t"),
                                        characterRepository.getCharacter("ó"),
                                        characterRepository.getCharacter("t")
                                ),
                                List.of(
                                        characterRepository.getCharacter("ű"),
                                        characterRepository.getCharacter("z")
                                ),
                                List.of(characterRepository.getCharacter("#")),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Replacement(
                                        characterRepository.getCharacter("ű"),
                                        characterRepository.getCharacter("ü"),
                                        characterRepository
                                ),
                                new Replacement(new HashSet<>(), characterRepository),
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("e").getAttributes()),
                                        characterRepository
                                ),
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )
        );
        Set<Rule> rules = Stream.of(node1, node2)
                .map(Node::getRule)
                .collect(toSet());
        latticeBuilder.addRules(rules);
        Lattice lattice = latticeBuilder.getLattice();
        assertThat(lattice.size()).isEqualTo(4);
        assertThat(lattice.getNodes()).hasSize(2);
        assertThat(lattice.getNodes()).contains(node1, node2);
        assertThat(lattice.getRoot().getChildren()).hasSize(2);
        assertThat(lattice.getRoot().getChildren()).contains(node1, node2);
        assertThat(lattice.getLeaf().getParents()).hasSize(2);
        assertThat(lattice.getLeaf().getParents()).contains(node1, node2);
    }

}
