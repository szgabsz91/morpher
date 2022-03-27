package com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IdentityWordConverter;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.Lattice;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.UnitNode;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.ZeroNode;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Context;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Replacement;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.setoperations.IntersectionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.szgabsz91.morpher.transformationengines.lattice.impl.setoperations.IntersectionCalculator.intersect;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsistentLatticeBuilderTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    IWordConverter wordConverter = new DoubleConsonantWordConverter();
                    return Arguments.of(
                            characterRepository,
                            new ConsistentLatticeBuilder(characterRepository, wordConverter),
                            wordConverter
                    );
                });
    }

    @Test
    public void testConstructorWithFlags() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new IdentityWordConverter();
        ConsistentLatticeBuilder consistentLatticeBuilder = new ConsistentLatticeBuilder(characterRepository, wordConverter, true);
        assertThat(consistentLatticeBuilder.isSkipFrequencyCalculation()).isTrue();
        assertThat(consistentLatticeBuilder.getLattice().isEmpty()).isTrue();
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
        ConsistentLatticeBuilder consistentLatticeBuilder = new ConsistentLatticeBuilder(characterRepository, wordConverter, false, lattice);
        assertThat(consistentLatticeBuilder.isSkipFrequencyCalculation()).isFalse();
        assertThat(consistentLatticeBuilder.getLattice().isEmpty()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    @SuppressWarnings("unchecked")
    public void testConstructorGettersAndReset(
            ICharacterRepository characterRepository,
            ILatticeBuilder latticeBuilder,
            IWordConverter wordConverter) {
        ConsistentLatticeBuilder consistentLatticeBuilder = (ConsistentLatticeBuilder) latticeBuilder;
        assertThat(consistentLatticeBuilder.getCharacterRepository()).hasSameClassAs(characterRepository);
        assertThat(consistentLatticeBuilder.getWordConverter()).hasSameClassAs(wordConverter);

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
        consistentLatticeBuilder.addRule(rule);
        assertThat(consistentLatticeBuilder.getLattice().size()).isGreaterThan(2);
        consistentLatticeBuilder.reset();
        assertThat(consistentLatticeBuilder.getLattice().size()).isEqualTo(2);
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
        ConsistentLatticeBuilder consistentLatticeBuilder = new ConsistentLatticeBuilder(characterRepository, wordConverter);
        consistentLatticeBuilder.addRules(rules);
        Lattice lattice = consistentLatticeBuilder.getLattice();
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(new Node(rules.iterator().next()));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testOnNodeBecomingInconsistentWithNodeContainingNoRule(ICharacterRepository characterRepository, ILatticeBuilder latticeBuilder, IWordConverter wordConverter) {
        Lattice lattice = new Lattice(new UnitNode(), new ZeroNode(), characterRepository, wordConverter);
        Node node = new Node(null);
        lattice.addNode(node);

        latticeBuilder.onNodeBecomingInconsistent(lattice, node);
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(node);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testOnNodeBecomingInconsistentWithAtomicNode(ICharacterRepository characterRepository, ILatticeBuilder latticeBuilder, IWordConverter wordConverter) {
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
    public void testOnNodeBecomingInconsistentWithNodeContainingARule(ICharacterRepository characterRepository, ILatticeBuilder latticeBuilder, IWordConverter wordConverter) {
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
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(grandChild);
        assertThat(lattice.getZeroNode().getParents()).hasSize(1);
        assertThat(lattice.getZeroNode().getParents()).containsSequence(grandChild);
        assertThat(grandChild.getParents()).hasSize(1);
        assertThat(grandChild.getParents()).containsSequence(lattice.getUnitNode());
        assertThat(lattice.getUnitNode().getChildren()).hasSize(1);
        assertThat(lattice.getUnitNode().getChildren()).containsSequence(grandChild);
    }

    @Test
    public void testAddRulesWithConsistentIntersection() throws IntersectionException {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ConsistentLatticeBuilder consistentLatticeBuilder = new ConsistentLatticeBuilder(characterRepository, wordConverter);

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
                .collect(toUnmodifiableSet());
        consistentLatticeBuilder.addRules(rules);
        Lattice lattice = consistentLatticeBuilder.getLattice();
        assertThat(lattice.getUnitNode().getChildren()).hasSize(1);
        assertThat(lattice.getUnitNode().getChildren()).containsSequence(intersection);
        assertThat(lattice.getUnitNode().getChildren().get(0).getChildren()).hasSize(2);
        assertThat(lattice.getUnitNode().getChildren().get(0).getChildren()).contains(a, e);
        assertThat(lattice.getUnitNode().getChildren().get(0).getChildren().get(0).getChildren()).hasSize(1);
        assertThat(lattice.getUnitNode().getChildren().get(0).getChildren().get(0).getChildren()).containsSequence(lattice.getZeroNode());
        assertThat(lattice.getUnitNode().getChildren().get(0).getChildren().get(1).getChildren()).hasSize(1);
        assertThat(lattice.getUnitNode().getChildren().get(0).getChildren().get(1).getChildren()).containsSequence(lattice.getZeroNode());
    }

    @Test
    public void testMatchWithSiblingsInLattice() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new IdentityWordConverter();
        ConsistentLatticeBuilder consistentLatticeBuilder = new ConsistentLatticeBuilder(characterRepository, wordConverter);
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
                .collect(toUnmodifiableSet());
        consistentLatticeBuilder.addRules(rules);
        Lattice lattice = consistentLatticeBuilder.getLattice();

        Node result = lattice.match(Word.of("xaz"));
        assertThat(result).isEqualTo(nodes.get(0));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testWithInconsistentCandidateIntersection(ICharacterRepository characterRepository, ILatticeBuilder latticeBuilder, IWordConverter wordConverter) {
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
                .collect(toUnmodifiableSet());
        latticeBuilder.addRules(rules);
        Lattice lattice = latticeBuilder.getLattice();
        assertThat(lattice.size()).isEqualTo(4);
        assertThat(lattice.getNodes()).hasSize(2);
        assertThat(lattice.getNodes()).contains(node1, node2);
        assertThat(lattice.getUnitNode().getChildren()).hasSize(2);
        assertThat(lattice.getUnitNode().getChildren()).contains(node1, node2);
        assertThat(lattice.getZeroNode().getParents()).hasSize(2);
        assertThat(lattice.getZeroNode().getParents()).contains(node1, node2);
    }

}
