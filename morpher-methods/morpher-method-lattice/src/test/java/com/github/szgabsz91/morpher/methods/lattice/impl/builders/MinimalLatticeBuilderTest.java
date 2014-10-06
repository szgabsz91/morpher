package com.github.szgabsz91.morpher.methods.lattice.impl.builders;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MinimalLatticeBuilderTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    IWordConverter wordConverter = new DoubleConsonantWordConverter();
                    ILatticeBuilder latticeBuilder = new MinimalLatticeBuilder(characterRepository, wordConverter);
                    return Arguments.of(
                            characterRepository,
                            wordConverter,
                            latticeBuilder
                    );
                });
    }

    @Test
    public void testConstructorWithInternalLatticeBuilders() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new IdentityWordConverter();
        HomogeneousLatticeBuilder homogeneousLatticeBuilder = new HomogeneousLatticeBuilder(characterRepository, wordConverter);
        FullLatticeBuilder fullLatticeBuilder = new FullLatticeBuilder(characterRepository, wordConverter);
        MinimalLatticeBuilder latticeBuilder = new MinimalLatticeBuilder(characterRepository, wordConverter, homogeneousLatticeBuilder, fullLatticeBuilder);
        assertThat(latticeBuilder.getHomogeneousLatticeBuilder()).isSameAs(homogeneousLatticeBuilder);
        assertThat(latticeBuilder.getFullLatticeBuilder()).isSameAs(fullLatticeBuilder);
        assertThat(latticeBuilder.getLattice().isEmpty()).isTrue();
    }

    @Test
    public void testConstructorWithInternalLatticeBuildersAndInitialLattice() {
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
        FullLatticeBuilder fullLatticeBuilder = new FullLatticeBuilder(characterRepository, wordConverter);
        MinimalLatticeBuilder latticeBuilder = new MinimalLatticeBuilder(characterRepository, wordConverter, homogeneousLatticeBuilder, fullLatticeBuilder, lattice);
        assertThat(latticeBuilder.getHomogeneousLatticeBuilder()).isSameAs(homogeneousLatticeBuilder);
        assertThat(latticeBuilder.getFullLatticeBuilder()).isSameAs(fullLatticeBuilder);
        assertThat(latticeBuilder.getLattice().isEmpty()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConstructorGettersAndReset(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            ILatticeBuilder latticeBuilder) {
        MinimalLatticeBuilder minimalLatticeBuilder = (MinimalLatticeBuilder) latticeBuilder;
        assertThat(minimalLatticeBuilder.getCharacterRepository()).hasSameClassAs(characterRepository);
        assertThat(minimalLatticeBuilder.getWordConverter()).hasSameClassAs(wordConverter);

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
        minimalLatticeBuilder.addRule(rule);
        assertThat(minimalLatticeBuilder.getLattice().size()).isGreaterThan(2);
        minimalLatticeBuilder.reset();
        assertThat(minimalLatticeBuilder.getLattice().size()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    @SuppressWarnings("unchecked")
    public void testAddRules1(ICharacterRepository characterRepository, IWordConverter wordConverter, ILatticeBuilder latticeBuilder) {
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
        latticeBuilder.addRules(rules);
        Lattice lattice = latticeBuilder.getLattice();
        assertThat(lattice.getNodes()).hasSize(1);
        assertThat(lattice.getNodes()).containsSequence(new Node(rules.iterator().next()));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(ICharacterRepository characterRepository, IWordConverter wordConverter, ILatticeBuilder latticeBuilder) {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> latticeBuilder.skipNodeInserting(null, null));
        assertThat(exception.getMessage()).isEqualTo("This builder should delegate the building process to the underlying builder instances, and thus should not listen to build events.");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testOnBecomingInhomogeneous(ICharacterRepository characterRepository, IWordConverter wordConverter, ILatticeBuilder latticeBuilder) {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> latticeBuilder.onNodeBecomingInhomogeneous(null, null));
        assertThat(exception.getMessage()).isEqualTo("This builder should delegate the building process to the underlying builder instances, and thus should not listen to build events.");
    }

    @Test
    public void testAddRules2() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ILatticeBuilder latticeBuilder = new MinimalLatticeBuilder(characterRepository, wordConverter);
        Set<Node> expected = Set.of(
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
        Set<Node> nodes = new HashSet<>(expected);
        nodes.add(
                new Node(
                        new Rule(
                                new Context(
                                        List.of(characterRepository.getCharacter("c")),
                                        List.of(),
                                        List.of(),
                                        Position.identity(),
                                        Position.identity()
                                ),
                                null,
                                characterRepository,
                                null
                        ),
                        true
                )
        );
        Set<Rule> rules = nodes
                .stream()
                .map(Node::getRule)
                .collect(toSet());
        latticeBuilder.addRules(rules);
        Lattice lattice = latticeBuilder.getLattice();
        assertThat(new HashSet<>(lattice.getNodes())).isEqualTo(nodes);
    }

}
