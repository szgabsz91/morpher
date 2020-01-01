package com.github.szgabsz91.morpher.transformationengines.lattice.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IdentityWordConverter;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.AbstractLatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.CompleteLatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.ILatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.MaximalConsistentLatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.MinimalLatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.UnitNode;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.ZeroNode;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Context;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Replacement;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.CharacterRepositoryTypeMessage;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.LatticeBuilderMessage;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.LatticeBuilderTypeMessage;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.WordConverterTypeMessage;
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
        assertThat(latticeBuilderMessage.getInternalLatticeBuilder1().getLatticeBuilderType()).isEqualTo(LatticeBuilderTypeMessage.CONSISTENT);
        assertThat(latticeBuilderMessage.hasInternalLatticeBuilder2()).isTrue();
        assertThat(latticeBuilderMessage.getInternalLatticeBuilder2().getLatticeBuilderType()).isEqualTo(LatticeBuilderTypeMessage.COMPLETE);
        assertThat(latticeBuilderMessage.getLattice().getNodeCount()).isEqualTo(3);
        assertThat(latticeBuilderMessage.getLattice().getNeighborhoodCount()).isEqualTo(2);

        MinimalLatticeBuilder result = (MinimalLatticeBuilder) this.converter.convertBack(latticeBuilderMessage);
        assertThat(result.getCharacterRepository()).isInstanceOf(HungarianAttributedCharacterRepository.class);
        assertThat(result.getWordConverter()).isInstanceOf(DoubleConsonantWordConverter.class);
        assertThat(result.getConsistentLatticeBuilder()).isNotNull();
        assertThat(result.getCompleteLatticeBuilder()).isNotNull();
        assertThat(result.getLattice().size()).isEqualTo(3);
        Node node = result.getLattice().match(Word.of("abcabc"));
        assertThat(node).isNotNull();
        assertThat(node.getParents()).hasSize(1);
        assertThat(node.getParents().get(0)).isInstanceOf(UnitNode.class);
        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChildren().get(0)).isInstanceOf(ZeroNode.class);

        Serializer<ILatticeBuilder, LatticeBuilderMessage> serializer = new Serializer<>(this.converter, minimalLatticeBuilder);
        Path file = Files.createTempFile("transformation-engine", "lattice");
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
    public void testConvertAndConvertBackAndParseWithMaximalConsistentLatticeBuilderAndAdvancedComponents() throws IOException {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        MaximalConsistentLatticeBuilder maximalConsistentLatticeBuilder = new MaximalConsistentLatticeBuilder(characterRepository, wordConverter);
        maximalConsistentLatticeBuilder.addRules(Set.of(new Rule(
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

        LatticeBuilderMessage latticeBuilderMessage = this.converter.convert(maximalConsistentLatticeBuilder);
        assertThat(latticeBuilderMessage.getCharacterRepositoryType()).isEqualTo(CharacterRepositoryTypeMessage.ATTRIBUTED);
        assertThat(latticeBuilderMessage.getWordConverterType()).isEqualTo(WordConverterTypeMessage.DOUBLE_CONSONANT);
        assertThat(latticeBuilderMessage.getLatticeBuilderType()).isEqualTo(LatticeBuilderTypeMessage.MAXIMAL_CONSISTENT);
        assertThat(latticeBuilderMessage.getSkipFrequencyCalculation()).isFalse();
        assertThat(latticeBuilderMessage.getSkipDominantRuleSelection()).isFalse();
        assertThat(latticeBuilderMessage.hasInternalLatticeBuilder1()).isTrue();
        assertThat(latticeBuilderMessage.getInternalLatticeBuilder1().getLatticeBuilderType()).isEqualTo(LatticeBuilderTypeMessage.CONSISTENT);
        assertThat(latticeBuilderMessage.hasInternalLatticeBuilder2()).isFalse();
        assertThat(latticeBuilderMessage.getLattice().getNodeCount()).isEqualTo(3);
        assertThat(latticeBuilderMessage.getLattice().getNeighborhoodCount()).isEqualTo(2);

        MaximalConsistentLatticeBuilder result = (MaximalConsistentLatticeBuilder) this.converter.convertBack(latticeBuilderMessage);
        assertThat(result.getCharacterRepository()).isInstanceOf(HungarianAttributedCharacterRepository.class);
        assertThat(result.getWordConverter()).isInstanceOf(DoubleConsonantWordConverter.class);
        assertThat(result.getConsistentLatticeBuilder()).isNotNull();
        assertThat(result.getLattice().size()).isEqualTo(3);
        Node node = result.getLattice().match(Word.of("abcabc"));
        assertThat(node).isNotNull();
        assertThat(node.getParents()).hasSize(1);
        assertThat(node.getParents().get(0)).isInstanceOf(UnitNode.class);
        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChildren().get(0)).isInstanceOf(ZeroNode.class);

        Serializer<ILatticeBuilder, LatticeBuilderMessage> serializer = new Serializer<>(this.converter, maximalConsistentLatticeBuilder);
        Path file = Files.createTempFile("transformation-engine", "lattice");
        try {
            serializer.serialize(maximalConsistentLatticeBuilder, file);
            LatticeBuilderMessage resultingLatticeBuilderMessage = this.converter.parse(file);
            assertThat(resultingLatticeBuilderMessage).isEqualTo(latticeBuilderMessage);
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testConvertAndConvertBackAndParseWithCompleteLatticeBuilderAndBasicComponents() throws IOException {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new IdentityWordConverter();
        CompleteLatticeBuilder completeLatticeBuilder = new CompleteLatticeBuilder(characterRepository, wordConverter);
        completeLatticeBuilder.addRules(Set.of(new Rule(
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

        LatticeBuilderMessage latticeBuilderMessage = this.converter.convert(completeLatticeBuilder);
        assertThat(latticeBuilderMessage.getCharacterRepositoryType()).isEqualTo(CharacterRepositoryTypeMessage.SIMPLE);
        assertThat(latticeBuilderMessage.getWordConverterType()).isEqualTo(WordConverterTypeMessage.IDENTITY);
        assertThat(latticeBuilderMessage.getLatticeBuilderType()).isEqualTo(LatticeBuilderTypeMessage.COMPLETE);
        assertThat(latticeBuilderMessage.getSkipFrequencyCalculation()).isFalse();
        assertThat(latticeBuilderMessage.getSkipDominantRuleSelection()).isFalse();
        assertThat(latticeBuilderMessage.hasInternalLatticeBuilder1()).isFalse();
        assertThat(latticeBuilderMessage.hasInternalLatticeBuilder2()).isFalse();
        assertThat(latticeBuilderMessage.getLattice().getNodeCount()).isEqualTo(3);
        assertThat(latticeBuilderMessage.getLattice().getNeighborhoodCount()).isEqualTo(2);

        CompleteLatticeBuilder result = (CompleteLatticeBuilder) this.converter.convertBack(latticeBuilderMessage);
        assertThat(result.getCharacterRepository()).isInstanceOf(HungarianSimpleCharacterRepository.class);
        assertThat(result.getWordConverter()).isInstanceOf(IdentityWordConverter.class);
        assertThat(result.getLattice().size()).isEqualTo(3);
        Node node = result.getLattice().match(Word.of("abcabc"));
        assertThat(node).isNotNull();
        assertThat(node.getParents()).hasSize(1);
        assertThat(node.getParents().get(0)).isInstanceOf(UnitNode.class);
        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChildren().get(0)).isInstanceOf(ZeroNode.class);

        Serializer<ILatticeBuilder, LatticeBuilderMessage> serializer = new Serializer<>(this.converter, completeLatticeBuilder);
        Path file = Files.createTempFile("transformation-engine", "lattice");
        LatticeBuilderMessage resultingLatticeBuilderMessage;
        try {
            serializer.serialize(completeLatticeBuilder, file);
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
