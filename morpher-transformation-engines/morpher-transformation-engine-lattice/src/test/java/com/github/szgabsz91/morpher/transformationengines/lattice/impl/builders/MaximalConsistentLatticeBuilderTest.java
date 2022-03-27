package com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders;

import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
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
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.setoperations.IntersectionException;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.szgabsz91.morpher.transformationengines.lattice.impl.setoperations.IntersectionCalculator.intersect;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MaximalConsistentLatticeBuilderTest {

    @Test
    public void testConstructorWithConsistentLatticeBuilder() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new IdentityWordConverter();
        ConsistentLatticeBuilder consistentLatticeBuilder = new ConsistentLatticeBuilder(characterRepository, wordConverter);
        MaximalConsistentLatticeBuilder maximalConsistentLatticeBuilder = new MaximalConsistentLatticeBuilder(characterRepository, wordConverter, consistentLatticeBuilder);
        assertThat(maximalConsistentLatticeBuilder.getConsistentLatticeBuilder()).isSameAs(consistentLatticeBuilder);
        assertThat(maximalConsistentLatticeBuilder.getLattice().isEmpty()).isTrue();
    }

    @Test
    public void testConstructorWithConsistentLatticeBuilderAndInitialLattice() {
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
        ConsistentLatticeBuilder consistentLatticeBuilder = new ConsistentLatticeBuilder(characterRepository, wordConverter);
        MaximalConsistentLatticeBuilder maximalConsistentLatticeBuilder = new MaximalConsistentLatticeBuilder(characterRepository, wordConverter, consistentLatticeBuilder, lattice);
        assertThat(maximalConsistentLatticeBuilder.getConsistentLatticeBuilder()).isSameAs(consistentLatticeBuilder);
        assertThat(maximalConsistentLatticeBuilder.getLattice().isEmpty()).isFalse();
    }

    @Test
    public void testConstructorGettersAndReset() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        MaximalConsistentLatticeBuilder maximalConsistentLatticeBuilder = new MaximalConsistentLatticeBuilder(characterRepository, wordConverter);

        assertThat(maximalConsistentLatticeBuilder.getCharacterRepository()).hasSameClassAs(characterRepository);
        assertThat(maximalConsistentLatticeBuilder.getWordConverter()).hasSameClassAs(wordConverter);

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
        maximalConsistentLatticeBuilder.addRule(rule);
        assertThat(maximalConsistentLatticeBuilder.getLattice().size()).isGreaterThan(2);
        maximalConsistentLatticeBuilder.reset();
        assertThat(maximalConsistentLatticeBuilder.getLattice().size()).isEqualTo(2);
    }

    @Test
    public void testAddRulesWithHungarianAttributedCharacterRepository() throws IntersectionException {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        MaximalConsistentLatticeBuilder maximalConsistentLatticeBuilder = new MaximalConsistentLatticeBuilder(characterRepository, wordConverter);

        Node maximalConsistentNode1 = new Node(
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
        Node maximalConsistentNode2 = new Node(
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
        Node inconsistentIntersection = intersect(maximalConsistentNode1, maximalConsistentNode2, characterRepository, wordConverter);
        Set<Rule> maximalConsistentRules = Stream.of(maximalConsistentNode1, maximalConsistentNode2)
                .map(Node::getRule)
                .collect(toUnmodifiableSet());
        maximalConsistentLatticeBuilder.addRules(maximalConsistentRules);
        Lattice lattice = maximalConsistentLatticeBuilder.getLattice();
        assertThat(inconsistentIntersection.isInconsistent()).isTrue();
        assertThat(lattice.size()).isEqualTo(4);
        assertThat(lattice.getNodes()).hasSize(2);
        assertThat(lattice.getNodes()).contains(maximalConsistentNode1, maximalConsistentNode2);
    }

    @Test
    public void testAddRulesWithHungarianSimpleCharacterRepository() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        MaximalConsistentLatticeBuilder maximalConsistentLatticeBuilder = new MaximalConsistentLatticeBuilder(characterRepository, wordConverter);

        Node maximalConsistentNode1 = new Node(
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
        Node maximalConsistentNode2 = new Node(
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
        Set<Rule> maximalConsistentRules = Stream.of(maximalConsistentNode1, maximalConsistentNode2)
                .map(Node::getRule)
                .collect(toUnmodifiableSet());
        maximalConsistentLatticeBuilder.addRules(maximalConsistentRules);
        Lattice lattice = maximalConsistentLatticeBuilder.getLattice();
        assertThat(lattice.size()).isEqualTo(4);
        assertThat(lattice.getNodes()).hasSize(2);
        assertThat(lattice.getNodes()).contains(maximalConsistentNode1, maximalConsistentNode2);
    }

    @Test
    public void testSkipNodeInserting() {
        MaximalConsistentLatticeBuilder maximalConsistentLatticeBuilder = new MaximalConsistentLatticeBuilder(null, null);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> maximalConsistentLatticeBuilder.skipNodeInserting(null, null));
        assertThat(exception).hasMessage("This builder should delegate the building process to the underlying builder instances, and thus should not listen to build events.");
    }

    @Test
    public void testOnNodeBecomingInconsistent() {
        MaximalConsistentLatticeBuilder maximalConsistentLatticeBuilder = new MaximalConsistentLatticeBuilder(null, null);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> maximalConsistentLatticeBuilder.onNodeBecomingInconsistent(null, null));
        assertThat(exception).hasMessage("This builder should delegate the building process to the underlying builder instances, and thus should not listen to build events.");
    }

}
