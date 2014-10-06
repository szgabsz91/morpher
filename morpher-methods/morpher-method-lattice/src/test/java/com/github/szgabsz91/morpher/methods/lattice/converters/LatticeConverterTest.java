package com.github.szgabsz91.morpher.methods.lattice.converters;

import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.Vowel;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.LipShape;
import com.github.szgabsz91.morpher.methods.api.protocolbuffers.CharacterMessage;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.lattice.impl.Lattice;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.EmptyNode;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.FullNode;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Context;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.LatticeMessage;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.NeighborhoodListMessage;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.NodeMessage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class LatticeConverterTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    IWordConverter wordConverter = new DoubleConsonantWordConverter();
                    LatticeConverter latticeConverter = new LatticeConverter();
                    return Arguments.of(
                            characterRepository,
                            wordConverter,
                            latticeConverter
                    );
                });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversion(ICharacterRepository characterRepository, IWordConverter wordConverter, LatticeConverter latticeConverter) {
        Lattice lattice = new Lattice(new FullNode(), new EmptyNode(), characterRepository, wordConverter);
        Node node = new Node(
                new Rule(
                        new Context(
                                List.of(
                                        Vowel.create(LipShape.ROUNDED)
                                ),
                                List.of(
                                        Vowel.create(LipShape.UNROUNDED)
                                ),
                                List.of(
                                        Vowel.create(Length.LONG)
                                ),
                                Position.identity(),
                                Position.of(1)
                        ),
                        List.of(
                                new Addition(
                                        Set.of(LipShape.ROUNDED),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        wordConverter
                ),
                List.of(lattice.getRoot()),
                List.of(lattice.getLeaf())
        );
        lattice.addNode(node);
        node.setLevel(1L);
        node.incrementFrequency();
        lattice.getLeaf().setLevel(2L);
        lattice.getLeaf().incrementFrequency();
        lattice.getLeaf().incrementFrequency();
        LatticeMessage latticeMessage = latticeConverter.convert(lattice);

        assertThat(latticeMessage.getNodeList()).hasSize(3);
        // Root
        NodeMessage rootMessage = latticeMessage.getNode(0);
        assertThat(rootMessage.getPrefixList()).isEmpty();
        assertThat(rootMessage.getCoreList()).isEmpty();
        assertThat(rootMessage.getPostfixList()).isEmpty();
        assertThat(rootMessage.hasFrontPosition()).isFalse();
        assertThat(rootMessage.hasBackPosition()).isFalse();
        assertThat(rootMessage.getTransformationList()).isEmpty();
        assertThat(rootMessage.getInhomogeneous()).isFalse();
        assertThat(rootMessage.getLevel()).isEqualTo(0L);
        assertThat(rootMessage.getFrequency()).isEqualTo(0L);
        assertThat(rootMessage.getType()).isEqualTo(FullNode.class.getName());
        // Leaf
        NodeMessage leafMessage = latticeMessage.getNode(1);
        assertThat(leafMessage.getPrefixList()).isEmpty();
        assertThat(leafMessage.getCoreList()).isEmpty();
        assertThat(leafMessage.getPostfixList()).isEmpty();
        assertThat(leafMessage.hasFrontPosition()).isFalse();
        assertThat(leafMessage.hasBackPosition()).isFalse();
        assertThat(leafMessage.getTransformationList()).isEmpty();
        assertThat(leafMessage.getInhomogeneous()).isFalse();
        assertThat(leafMessage.getLevel()).isEqualTo(2L);
        assertThat(leafMessage.getFrequency()).isEqualTo(2L);
        assertThat(leafMessage.getType()).isEqualTo(EmptyNode.class.getName());
        // Node
        NodeMessage nodeMessage = latticeMessage.getNode(2);
        assertThat(nodeMessage.getPrefixList()).hasSize(1);
        assertThat(nodeMessage.getPrefix(0).getLetter()).isEmpty();
        assertThat(nodeMessage.getPrefix(0).getAttributeList()).hasSize(1);
        assertThat(nodeMessage.getPrefix(0).getAttribute(0)).isEqualTo(LipShape.class.getName() + "." + LipShape.ROUNDED);
        assertThat(nodeMessage.getCoreList()).hasSize(1);
        assertThat(nodeMessage.getCore(0).getLetter()).isEmpty();
        assertThat(nodeMessage.getCore(0).getAttributeList()).hasSize(1);
        assertThat(nodeMessage.getCore(0).getAttribute(0)).isEqualTo(LipShape.class.getName() + "." + LipShape.UNROUNDED);
        assertThat(nodeMessage.getPostfixList()).hasSize(1);
        assertThat(nodeMessage.getPostfix(0).getLetter()).isEmpty();
        assertThat(nodeMessage.getPostfix(0).getAttributeList()).hasSize(1);
        assertThat(nodeMessage.getPostfix(0).getAttribute(0)).isEqualTo(Length.class.getName() + "." + Length.LONG);
        assertThat(nodeMessage.getFrontPosition().getPosition()).isEqualTo(0);
        assertThat(nodeMessage.getBackPosition().getPosition()).isEqualTo(1);
        assertThat(nodeMessage.getTransformationList()).hasSize(1);
        assertThat(nodeMessage.getTransformation(0).getType()).isEqualTo(Addition.class.getName());
        assertThat(nodeMessage.getTransformation(0).getChangeList()).hasSize(1);
        assertThat(nodeMessage.getTransformation(0).getChange(0)).isEqualTo(LipShape.class.getName() + "." + LipShape.ROUNDED);
        assertThat(nodeMessage.getInhomogeneous()).isFalse();
        assertThat(nodeMessage.getLevel()).isEqualTo(1L);
        assertThat(nodeMessage.getFrequency()).isEqualTo(1L);
        assertThat(nodeMessage.getType()).isEmpty();
        assertThat(latticeMessage.getNeighborhoodMap().entrySet()).hasSize(2);
        assertThat(latticeMessage.getNeighborhoodMap().get(0).getNeighborList()).hasSize(1);
        assertThat(latticeMessage.getNeighborhoodMap().get(0).getNeighbor(0)).isEqualTo(1);
        assertThat(latticeMessage.getNeighborhoodMap().get(2).getNeighborList()).hasSize(1);
        assertThat(latticeMessage.getNeighborhoodMap().get(2).getNeighbor(0)).isEqualTo(1);

        Lattice result = latticeConverter.convertBack(latticeMessage);
        assertThat(new HashSet<>(result.getNodes())).isEqualTo(new HashSet<>(lattice.getNodes()));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testParse(ICharacterRepository characterRepository, IWordConverter wordConverter, LatticeConverter latticeConverter) throws IOException {
        Path file = Paths.get("build/lattice.pb");
        LatticeMessage latticeMessage = LatticeMessage.newBuilder()
                .addNode(NodeMessage.newBuilder().addCore(CharacterMessage.newBuilder().setLetter("x").build()))
                .addNode(NodeMessage.newBuilder().addCore(CharacterMessage.newBuilder().setLetter("y").build()))
                .putNeighborhood(0, NeighborhoodListMessage.newBuilder().addNeighbor(1).build())
                .build();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(Files.newOutputStream(file))) {
            latticeMessage.writeTo(gzipOutputStream);
        }
        LatticeMessage result = latticeConverter.parse(file);
        assertThat(result).isEqualTo(latticeMessage);
        Files.delete(file);
    }

}
