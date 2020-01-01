package com.github.szgabsz91.morpher.transformationengines.lattice.converters;

import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.Vowel;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.LipShape;
import com.github.szgabsz91.morpher.transformationengines.api.protocolbuffers.CharacterMessage;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.UnitNode;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.ZeroNode;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Context;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.NodeMessage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeConverterTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    NodeConverter nodeConverter = new NodeConverter();
                    nodeConverter.setCharacterRepository(characterRepository);
                    return Arguments.of(
                            characterRepository,
                            nodeConverter
                    );
                });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithSimpleNode(ICharacterRepository characterRepository, NodeConverter nodeConverter) {
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
                        null
                ),
                true
        );
        node.setLevel(1L);
        node.incrementFrequency();
        NodeMessage nodeMessage = nodeConverter.convert(node);

        assertThat(nodeMessage.getPrefixList()).hasSize(1);
        assertThat(nodeMessage.getPrefix(0).getType()).isEqualTo(Vowel.class.getCanonicalName());
        assertThat(nodeMessage.getPrefix(0).getLetter()).isEmpty();
        assertThat(nodeMessage.getPrefix(0).getAttributeList()).hasSize(1);
        assertThat(nodeMessage.getPrefix(0).getAttribute(0)).isEqualTo(LipShape.class.getName() + "." + LipShape.ROUNDED);
        assertThat(nodeMessage.getCoreList()).hasSize(1);
        assertThat(nodeMessage.getCore(0).getType()).isEqualTo(Vowel.class.getCanonicalName());
        assertThat(nodeMessage.getCore(0).getLetter()).isEmpty();
        assertThat(nodeMessage.getCore(0).getAttributeList()).hasSize(1);
        assertThat(nodeMessage.getCore(0).getAttribute(0)).isEqualTo(LipShape.class.getName() + "." + LipShape.UNROUNDED);
        assertThat(nodeMessage.getPostfixList()).hasSize(1);
        assertThat(nodeMessage.getPostfix(0).getType()).isEqualTo(Vowel.class.getCanonicalName());
        assertThat(nodeMessage.getPostfix(0).getLetter()).isEmpty();
        assertThat(nodeMessage.getPostfix(0).getAttributeList()).hasSize(1);
        assertThat(nodeMessage.getPostfix(0).getAttribute(0)).isEqualTo(Length.class.getName() + "." + Length.LONG);
        assertThat(nodeMessage.getFrontPosition().getPosition()).isEqualTo(0);
        assertThat(nodeMessage.getBackPosition().getPosition()).isEqualTo(1);
        assertThat(nodeMessage.getTransformationList()).hasSize(1);
        assertThat(nodeMessage.getTransformation(0).getType()).isEqualTo(Addition.class.getName());
        assertThat(nodeMessage.getTransformation(0).getChangeList()).hasSize(1);
        assertThat(nodeMessage.getTransformation(0).getChange(0)).isEqualTo(LipShape.class.getName() + "." + LipShape.ROUNDED);
        assertThat(nodeMessage.getInconsistent()).isTrue();
        assertThat(nodeMessage.getLevel()).isEqualTo(1L);
        assertThat(nodeMessage.getFrequency()).isEqualTo(1L);
        assertThat(nodeMessage.getType()).isEmpty();

        Node result = nodeConverter.convertBack(nodeMessage);
        assertThat(result).isEqualTo(node);
        assertThat(result.isInconsistent()).isEqualTo(node.isInconsistent());
        assertThat(result.getLevel()).isEqualTo(node.getLevel());
        assertThat(result.getFrequency()).isEqualTo(node.getFrequency());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithNullFrontAndBackPositions(ICharacterRepository characterRepository, NodeConverter nodeConverter) {
        Node node = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(),
                                List.of(),
                                null,
                                null
                        ),
                        List.of(),
                        characterRepository,
                        null
                )
        );
        NodeMessage nodeMessage = nodeConverter.convert(node);

        assertThat(nodeMessage.hasFrontPosition()).isFalse();
        assertThat(nodeMessage.hasBackPosition()).isFalse();

        Node result = nodeConverter.convertBack(nodeMessage);
        assertThat(result).isEqualTo(node);
        assertThat(result.isInconsistent()).isEqualTo(node.isInconsistent());
        assertThat(result.getLevel()).isEqualTo(node.getLevel());
        assertThat(result.getFrequency()).isEqualTo(node.getFrequency());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithNullTransformationList(ICharacterRepository characterRepository, NodeConverter nodeConverter) {
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
                        null,
                        characterRepository,
                        null
                ),
                true
        );
        node.setLevel(1L);
        node.incrementFrequency();
        NodeMessage nodeMessage = nodeConverter.convert(node);

        assertThat(nodeMessage.getPrefixList()).hasSize(1);
        assertThat(nodeMessage.getPrefix(0).getType()).isEqualTo(Vowel.class.getCanonicalName());
        assertThat(nodeMessage.getPrefix(0).getLetter()).isEmpty();
        assertThat(nodeMessage.getPrefix(0).getAttributeList()).hasSize(1);
        assertThat(nodeMessage.getPrefix(0).getAttribute(0)).isEqualTo(LipShape.class.getName() + "." + LipShape.ROUNDED);
        assertThat(nodeMessage.getCoreList()).hasSize(1);
        assertThat(nodeMessage.getCore(0).getType()).isEqualTo(Vowel.class.getCanonicalName());
        assertThat(nodeMessage.getCore(0).getLetter()).isEmpty();
        assertThat(nodeMessage.getCore(0).getAttributeList()).hasSize(1);
        assertThat(nodeMessage.getCore(0).getAttribute(0)).isEqualTo(LipShape.class.getName() + "." + LipShape.UNROUNDED);
        assertThat(nodeMessage.getPostfixList()).hasSize(1);
        assertThat(nodeMessage.getPostfix(0).getType()).isEqualTo(Vowel.class.getCanonicalName());
        assertThat(nodeMessage.getPostfix(0).getLetter()).isEmpty();
        assertThat(nodeMessage.getPostfix(0).getAttributeList()).hasSize(1);
        assertThat(nodeMessage.getPostfix(0).getAttribute(0)).isEqualTo(Length.class.getName() + "." + Length.LONG);
        assertThat(nodeMessage.getFrontPosition().getPosition()).isEqualTo(0);
        assertThat(nodeMessage.getBackPosition().getPosition()).isEqualTo(1);
        assertThat(nodeMessage.getTransformationList()).isEmpty();
        assertThat(nodeMessage.getInconsistent()).isTrue();
        assertThat(nodeMessage.getLevel()).isEqualTo(1L);
        assertThat(nodeMessage.getFrequency()).isEqualTo(1L);
        assertThat(nodeMessage.getType()).isEmpty();

        Node result = nodeConverter.convertBack(nodeMessage);
        node.getRule().setTransformations(List.of());
        assertThat(result).isEqualTo(node);
        assertThat(result.isInconsistent()).isEqualTo(node.isInconsistent());
        assertThat(result.getLevel()).isEqualTo(node.getLevel());
        assertThat(result.getFrequency()).isEqualTo(node.getFrequency());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithFullNode(ICharacterRepository characterRepository, NodeConverter nodeConverter) {
        Node expected = new UnitNode();
        expected.setLevel(1L);
        expected.setFrequency(1L);
        NodeMessage nodeMessage = nodeConverter.convert(expected);
        assertThat(nodeMessage.getPrefixList()).isEmpty();
        assertThat(nodeMessage.getCoreList()).isEmpty();
        assertThat(nodeMessage.getPostfixList()).isEmpty();
        assertThat(nodeMessage.hasFrontPosition()).isFalse();
        assertThat(nodeMessage.hasBackPosition()).isFalse();
        assertThat(nodeMessage.getTransformationList()).isEmpty();
        assertThat(nodeMessage.getInconsistent()).isFalse();
        assertThat(nodeMessage.getLevel()).isEqualTo(1L);
        assertThat(nodeMessage.getFrequency()).isEqualTo(1L);
        assertThat(nodeMessage.getType()).isEqualTo(UnitNode.class.getName());
        Node result = nodeConverter.convertBack(nodeMessage);
        assertThat(result).isEqualTo(expected);
        assertThat(result.isInconsistent()).isEqualTo(expected.isInconsistent());
        assertThat(result.getLevel()).isEqualTo(nodeMessage.getLevel());
        assertThat(result.getFrequency()).isEqualTo(nodeMessage.getFrequency());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithEmptyNode(ICharacterRepository characterRepository, NodeConverter nodeConverter) {
        Node expected = new ZeroNode();
        expected.setLevel(1L);
        expected.setFrequency(1L);
        NodeMessage nodeMessage = nodeConverter.convert(expected);
        assertThat(nodeMessage.getPrefixList()).isEmpty();
        assertThat(nodeMessage.getCoreList()).isEmpty();
        assertThat(nodeMessage.getPostfixList()).isEmpty();
        assertThat(nodeMessage.hasFrontPosition()).isFalse();
        assertThat(nodeMessage.hasBackPosition()).isFalse();
        assertThat(nodeMessage.getTransformationList()).isEmpty();
        assertThat(nodeMessage.getInconsistent()).isFalse();
        assertThat(nodeMessage.getLevel()).isEqualTo(1L);
        assertThat(nodeMessage.getFrequency()).isEqualTo(1L);
        assertThat(nodeMessage.getType()).isEqualTo(ZeroNode.class.getName());
        Node result = nodeConverter.convertBack(nodeMessage);
        assertThat(result).isEqualTo(expected);
        assertThat(result.isInconsistent()).isEqualTo(expected.isInconsistent());
        assertThat(result.getLevel()).isEqualTo(nodeMessage.getLevel());
        assertThat(result.getFrequency()).isEqualTo(nodeMessage.getFrequency());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testParse(ICharacterRepository characterRepository, NodeConverter nodeConverter) throws IOException {
        Path file = Paths.get("build/node.pb");
        NodeMessage nodeMessage = NodeMessage.newBuilder()
                .addCore(CharacterMessage.newBuilder().setLetter("x").build())
                .build();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(Files.newOutputStream(file))) {
            nodeMessage.writeTo(gzipOutputStream);
        }
        NodeMessage result = nodeConverter.parse(file);
        assertThat(result).isEqualTo(nodeMessage);
        Files.delete(file);
    }

}
