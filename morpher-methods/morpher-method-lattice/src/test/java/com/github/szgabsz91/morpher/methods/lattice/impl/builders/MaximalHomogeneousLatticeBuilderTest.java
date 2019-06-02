package com.github.szgabsz91.morpher.methods.lattice.impl.builders;

import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
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
import com.github.szgabsz91.morpher.methods.lattice.impl.setoperations.IntersectionException;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.szgabsz91.morpher.methods.lattice.impl.setoperations.IntersectionCalculator.intersect;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MaximalHomogeneousLatticeBuilderTest {

    @Test
    public void testConstructorWithHomogeneousLatticeBuilder() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new IdentityWordConverter();
        HomogeneousLatticeBuilder homogeneousLatticeBuilder = new HomogeneousLatticeBuilder(characterRepository, wordConverter);
        MaximalHomogeneousLatticeBuilder latticeBuilder = new MaximalHomogeneousLatticeBuilder(characterRepository, wordConverter, homogeneousLatticeBuilder);
        assertThat(latticeBuilder.getHomogeneousLatticeBuilder()).isSameAs(homogeneousLatticeBuilder);
        assertThat(latticeBuilder.getLattice().isEmpty()).isTrue();
    }

    @Test
    public void testConstructorWithHomogeneousLatticeBuilderAndInitialLattice() {
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
        HomogeneousLatticeBuilder homogeneousLatticeBuilder = new HomogeneousLatticeBuilder(characterRepository, wordConverter);
        MaximalHomogeneousLatticeBuilder latticeBuilder = new MaximalHomogeneousLatticeBuilder(characterRepository, wordConverter, homogeneousLatticeBuilder, lattice);
        assertThat(latticeBuilder.getHomogeneousLatticeBuilder()).isSameAs(homogeneousLatticeBuilder);
        assertThat(latticeBuilder.getLattice().isEmpty()).isFalse();
    }

    @Test
    public void testConstructorGettersAndReset() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        MaximalHomogeneousLatticeBuilder latticeBuilder = new MaximalHomogeneousLatticeBuilder(characterRepository, wordConverter);

        assertThat(latticeBuilder.getCharacterRepository()).hasSameClassAs(characterRepository);
        assertThat(latticeBuilder.getWordConverter()).hasSameClassAs(wordConverter);

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
        latticeBuilder.addRule(rule);
        assertThat(latticeBuilder.getLattice().size()).isGreaterThan(2);
        latticeBuilder.reset();
        assertThat(latticeBuilder.getLattice().size()).isEqualTo(2);
    }

    @Test
    public void testAddRulesWithHungarianAttributedCharacterRepository() throws IntersectionException {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ILatticeBuilder latticeBuilder = new MaximalHomogeneousLatticeBuilder(characterRepository, wordConverter);

        Node maximalHomogeneousNode1 = new Node(
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
                )
        );
        Node maximalHomogeneousNode2 = new Node(
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
        Node inhomogeneousIntersection = intersect(maximalHomogeneousNode1, maximalHomogeneousNode2, characterRepository, wordConverter);
        Set<Rule> maximalHomogeneousRules = Stream.of(maximalHomogeneousNode1, maximalHomogeneousNode2)
                .map(Node::getRule)
                .collect(toSet());
        latticeBuilder.addRules(maximalHomogeneousRules);
        Lattice lattice = latticeBuilder.getLattice();
        assertThat(inhomogeneousIntersection.isInhomogeneous()).isTrue();
        assertThat(lattice.size()).isEqualTo(4);
        assertThat(lattice.getNodes()).hasSize(2);
        assertThat(lattice.getNodes()).contains(maximalHomogeneousNode1, maximalHomogeneousNode2);
    }

    @Test
    public void testAddRulesWithHungarianSimpleCharacterRepository() throws IntersectionException {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ILatticeBuilder latticeBuilder = new MaximalHomogeneousLatticeBuilder(characterRepository, wordConverter);

        Node maximalHomogeneousNode1 = new Node(
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
                )
        );
        Node maximalHomogeneousNode2 = new Node(
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
        Set<Rule> maximalHomogeneousRules = Stream.of(maximalHomogeneousNode1, maximalHomogeneousNode2)
                .map(Node::getRule)
                .collect(toSet());
        latticeBuilder.addRules(maximalHomogeneousRules);
        Lattice lattice = latticeBuilder.getLattice();
        assertThat(lattice.size()).isEqualTo(4);
        assertThat(lattice.getNodes()).hasSize(2);
        assertThat(lattice.getNodes()).contains(maximalHomogeneousNode1, maximalHomogeneousNode2);
    }

    @Test
    public void testSkipNodeInserting() {
        ILatticeBuilder latticeBuilder = new MaximalHomogeneousLatticeBuilder(null, null);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> latticeBuilder.skipNodeInserting(null, null));
        assertThat(exception).hasMessage("This builder should delegate the building process to the underlying builder instances, and thus should not listen to build events.");
    }

    @Test
    public void testOnNodeBecomingInhomogeneous() {
        ILatticeBuilder latticeBuilder = new MaximalHomogeneousLatticeBuilder(null, null);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> latticeBuilder.onNodeBecomingInhomogeneous(null, null));
        assertThat(exception).hasMessage("This builder should delegate the building process to the underlying builder instances, and thus should not listen to build events.");
    }

}
