package com.github.szgabsz91.morpher.methods.lattice.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IdentityWordConverter;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.AbstractLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.FullLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.ILatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.MaximalHomogeneousLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.MinimalLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.EmptyNode;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.FullNode;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Context;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Replacement;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.CharacterRepositoryTypeMessage;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.LatticeBuilderMessage;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.LatticeBuilderTypeMessage;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.WordConverterTypeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LatticeBuilderConverterTest {

    private LatticeBuilderConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new LatticeBuilderConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParseWithMinimalLatticeBuilderAndAdvancedComponents() throws IOException {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        MinimalLatticeBuilder minimalLatticeBuilder = new MinimalLatticeBuilder(characterRepository, wordConverter);
        minimalLatticeBuilder.addRules(Set.of(new Rule(
                new Context(
                        List.of(characterRepository.getCharacter("a")),
                        List.of(characterRepository.getCharacter("b")),
                        List.of(characterRepository.getCharacter("c")),
                        Position.identity(),
                        Position.of(1)
                ),
                List.of(new Replacement(
                        characterRepository.getCharacter("b"),
                        characterRepository.getCharacter("d"),
                        characterRepository
                )),
                characterRepository,
                wordConverter
        )));

        LatticeBuilderMessage latticeBuilderMessage = this.converter.convert(minimalLatticeBuilder);
        assertThat(latticeBuilderMessage.getCharacterRepositoryType()).isEqualTo(CharacterRepositoryTypeMessage.ATTRIBUTED);
        assertThat(latticeBuilderMessage.getWordConverterType()).isEqualTo(WordConverterTypeMessage.DOUBLE_CONSONANT);
        assertThat(latticeBuilderMessage.getLatticeBuilderType()).isEqualTo(LatticeBuilderTypeMessage.MINIMAL);
        assertThat(latticeBuilderMessage.getSkipFrequencyCalculation()).isFalse();
        assertThat(latticeBuilderMessage.getSkipDominantRuleSelection()).isFalse();
        assertThat(latticeBuilderMessage.hasInternalLatticeBuilder1()).isTrue();
        assertThat(latticeBuilderMessage.getInternalLatticeBuilder1().getLatticeBuilderType()).isEqualTo(LatticeBuilderTypeMessage.HOMOGENEOUS);
        assertThat(latticeBuilderMessage.hasInternalLatticeBuilder2()).isTrue();
        assertThat(latticeBuilderMessage.getInternalLatticeBuilder2().getLatticeBuilderType()).isEqualTo(LatticeBuilderTypeMessage.FULL);
        assertThat(latticeBuilderMessage.getLattice().getNodeCount()).isEqualTo(3);
        assertThat(latticeBuilderMessage.getLattice().getNeighborhoodCount()).isEqualTo(2);

        MinimalLatticeBuilder result = (MinimalLatticeBuilder) this.converter.convertBack(latticeBuilderMessage);
        assertThat(result.getCharacterRepository()).isInstanceOf(HungarianAttributedCharacterRepository.class);
        assertThat(result.getWordConverter()).isInstanceOf(DoubleConsonantWordConverter.class);
        assertThat(result.getHomogeneousLatticeBuilder()).isNotNull();
        assertThat(result.getFullLatticeBuilder()).isNotNull();
        assertThat(result.getLattice().size()).isEqualTo(3);
        Node node = result.getLattice().match(Word.of("abcabc"));
        assertThat(node).isNotNull();
        assertThat(node.getParents()).hasSize(1);
        assertThat(node.getParents().get(0)).isInstanceOf(FullNode.class);
        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChildren().get(0)).isInstanceOf(EmptyNode.class);

        Serializer<ILatticeBuilder, LatticeBuilderMessage> serializer = new Serializer<>(this.converter, minimalLatticeBuilder);
        Path file = Files.createTempFile("morpher", "lattice");
        try {
            serializer.serialize(minimalLatticeBuilder, file);
            LatticeBuilderMessage resultingLatticeBuilderMessage = this.converter.parse(file);
            assertThat(resultingLatticeBuilderMessage).isEqualTo(latticeBuilderMessage);
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testConvertAndConvertBackAndParseWithMaximalLatticeBuilderAndAdvancedComponents() throws IOException {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        MaximalHomogeneousLatticeBuilder maximalHomogeneousLatticeBuilder = new MaximalHomogeneousLatticeBuilder(characterRepository, wordConverter);
        maximalHomogeneousLatticeBuilder.addRules(Set.of(new Rule(
                new Context(
                        List.of(characterRepository.getCharacter("a")),
                        List.of(characterRepository.getCharacter("b")),
                        List.of(characterRepository.getCharacter("c")),
                        Position.identity(),
                        Position.of(1)
                ),
                List.of(new Replacement(
                        characterRepository.getCharacter("b"),
                        characterRepository.getCharacter("d"),
                        characterRepository
                )),
                characterRepository,
                wordConverter
        )));

        LatticeBuilderMessage latticeBuilderMessage = this.converter.convert(maximalHomogeneousLatticeBuilder);
        assertThat(latticeBuilderMessage.getCharacterRepositoryType()).isEqualTo(CharacterRepositoryTypeMessage.ATTRIBUTED);
        assertThat(latticeBuilderMessage.getWordConverterType()).isEqualTo(WordConverterTypeMessage.DOUBLE_CONSONANT);
        assertThat(latticeBuilderMessage.getLatticeBuilderType()).isEqualTo(LatticeBuilderTypeMessage.MAXIMAL_HOMOGENEOUS);
        assertThat(latticeBuilderMessage.getSkipFrequencyCalculation()).isFalse();
        assertThat(latticeBuilderMessage.getSkipDominantRuleSelection()).isFalse();
        assertThat(latticeBuilderMessage.hasInternalLatticeBuilder1()).isTrue();
        assertThat(latticeBuilderMessage.getInternalLatticeBuilder1().getLatticeBuilderType()).isEqualTo(LatticeBuilderTypeMessage.HOMOGENEOUS);
        assertThat(latticeBuilderMessage.hasInternalLatticeBuilder2()).isFalse();
        assertThat(latticeBuilderMessage.getLattice().getNodeCount()).isEqualTo(3);
        assertThat(latticeBuilderMessage.getLattice().getNeighborhoodCount()).isEqualTo(2);

        MaximalHomogeneousLatticeBuilder result = (MaximalHomogeneousLatticeBuilder) this.converter.convertBack(latticeBuilderMessage);
        assertThat(result.getCharacterRepository()).isInstanceOf(HungarianAttributedCharacterRepository.class);
        assertThat(result.getWordConverter()).isInstanceOf(DoubleConsonantWordConverter.class);
        assertThat(result.getHomogeneousLatticeBuilder()).isNotNull();
        assertThat(result.getLattice().size()).isEqualTo(3);
        Node node = result.getLattice().match(Word.of("abcabc"));
        assertThat(node).isNotNull();
        assertThat(node.getParents()).hasSize(1);
        assertThat(node.getParents().get(0)).isInstanceOf(FullNode.class);
        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChildren().get(0)).isInstanceOf(EmptyNode.class);

        Serializer<ILatticeBuilder, LatticeBuilderMessage> serializer = new Serializer<>(this.converter, maximalHomogeneousLatticeBuilder);
        Path file = Files.createTempFile("morpher", "lattice");
        try {
            serializer.serialize(maximalHomogeneousLatticeBuilder, file);
            LatticeBuilderMessage resultingLatticeBuilderMessage = this.converter.parse(file);
            assertThat(resultingLatticeBuilderMessage).isEqualTo(latticeBuilderMessage);
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testConvertAndConvertBackAndParseWithFullLatticeBuilderAndBasicComponents() throws IOException {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new IdentityWordConverter();
        FullLatticeBuilder fullLatticeBuilder = new FullLatticeBuilder(characterRepository, wordConverter);
        fullLatticeBuilder.addRules(Set.of(new Rule(
                new Context(
                        List.of(characterRepository.getCharacter("a")),
                        List.of(characterRepository.getCharacter("b")),
                        List.of(characterRepository.getCharacter("c")),
                        Position.identity(),
                        Position.of(1)
                ),
                List.of(new Replacement(
                        characterRepository.getCharacter("b"),
                        characterRepository.getCharacter("d"),
                        characterRepository
                )),
                characterRepository,
                wordConverter
        )));

        LatticeBuilderMessage latticeBuilderMessage = this.converter.convert(fullLatticeBuilder);
        assertThat(latticeBuilderMessage.getCharacterRepositoryType()).isEqualTo(CharacterRepositoryTypeMessage.SIMPLE);
        assertThat(latticeBuilderMessage.getWordConverterType()).isEqualTo(WordConverterTypeMessage.IDENTITY);
        assertThat(latticeBuilderMessage.getLatticeBuilderType()).isEqualTo(LatticeBuilderTypeMessage.FULL);
        assertThat(latticeBuilderMessage.getSkipFrequencyCalculation()).isFalse();
        assertThat(latticeBuilderMessage.getSkipDominantRuleSelection()).isFalse();
        assertThat(latticeBuilderMessage.hasInternalLatticeBuilder1()).isFalse();
        assertThat(latticeBuilderMessage.hasInternalLatticeBuilder2()).isFalse();
        assertThat(latticeBuilderMessage.getLattice().getNodeCount()).isEqualTo(3);
        assertThat(latticeBuilderMessage.getLattice().getNeighborhoodCount()).isEqualTo(2);

        FullLatticeBuilder result = (FullLatticeBuilder) this.converter.convertBack(latticeBuilderMessage);
        assertThat(result.getCharacterRepository()).isInstanceOf(HungarianSimpleCharacterRepository.class);
        assertThat(result.getWordConverter()).isInstanceOf(IdentityWordConverter.class);
        assertThat(result.getLattice().size()).isEqualTo(3);
        Node node = result.getLattice().match(Word.of("abcabc"));
        assertThat(node).isNotNull();
        assertThat(node.getParents()).hasSize(1);
        assertThat(node.getParents().get(0)).isInstanceOf(FullNode.class);
        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChildren().get(0)).isInstanceOf(EmptyNode.class);

        Serializer<ILatticeBuilder, LatticeBuilderMessage> serializer = new Serializer<>(this.converter, fullLatticeBuilder);
        Path file = Files.createTempFile("morpher", "lattice");
        LatticeBuilderMessage resultingLatticeBuilderMessage;
        try {
            serializer.serialize(fullLatticeBuilder, file);
            resultingLatticeBuilderMessage = this.converter.parse(file);
        }
        finally {
            Files.delete(file);
        }
        assertThat(resultingLatticeBuilderMessage).isEqualTo(latticeBuilderMessage);
    }

    @Test
    public void testConvertWithUnknownLatticeBuilderType() {
        ILatticeBuilder latticeBuilder = new UnknownLatticeBuilder(null, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.converter.convert(latticeBuilder));
        assertThat(exception).hasMessage("Unknown ILatticeBuilder implementation");
    }

    private static class UnknownLatticeBuilder extends AbstractLatticeBuilder {

        public UnknownLatticeBuilder(ICharacterRepository characterRepository, IWordConverter wordConverter) {
            super(characterRepository, wordConverter);
        }

        @Override
        public void addRules(Set<Rule> rules) {

        }

        @Override
        public boolean skipNodeInserting(Node node, Set<Node> children) {
            return false;
        }

    }

}
