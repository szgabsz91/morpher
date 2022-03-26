package com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.tree;

import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.ITransformation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class WordPairProcessorTreeNodeTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConstructor(ICharacterRepository characterRepository) {
        ITransformation transformation = new Addition(Set.of(), characterRepository);
        WordPairProcessorTreeNode parent = new WordPairProcessorTreeNode();
        WordPairProcessorTreeNode node = new WordPairProcessorTreeNode(1, 2, transformation, 3, 4, parent);
        assertThat(node.getCurrentIndexInStartWord()).isEqualTo(1);
        assertThat(node.getCurrentIndexInEndWord()).isEqualTo(2);
        assertThat(node.getTransformation()).isEqualTo(transformation);
        assertThat(node.getScoreSoFar()).isEqualTo(3);
        assertThat(node.getLevel()).isEqualTo(4);
        assertThat(node.getParent()).isEqualTo(parent);
        assertThat(node.getChildren()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testDefaultConstructor(ICharacterRepository characterRepository) {
        WordPairProcessorTreeNode node = new WordPairProcessorTreeNode();
        assertThat(node.getCurrentIndexInStartWord()).isEqualTo(0);
        assertThat(node.getCurrentIndexInEndWord()).isEqualTo(0);
        assertThat(node.getTransformation()).isNull();
        assertThat(node.getScoreSoFar()).isEqualTo(0);
        assertThat(node.getLevel()).isEqualTo(0);
        assertThat(node.getParent()).isNull();
        assertThat(node.getChildren()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testAddChild(ICharacterRepository characterRepository) {
        WordPairProcessorTreeNode node = new WordPairProcessorTreeNode();
        WordPairProcessorTreeNode child = new WordPairProcessorTreeNode();
        node.addChild(child);
        node.addChild(child);
        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChildren()).containsSequence(child);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsRoot(ICharacterRepository characterRepository) {
        WordPairProcessorTreeNode nonRoot = new WordPairProcessorTreeNode(0, 0, null, 0, 0, new WordPairProcessorTreeNode());
        assertThat(nonRoot.isRoot()).isFalse();

        WordPairProcessorTreeNode root = new WordPairProcessorTreeNode();
        assertThat(root.isRoot()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsLeaf(ICharacterRepository characterRepository) {
        WordPairProcessorTreeNode leaf = new WordPairProcessorTreeNode();
        assertThat(leaf.isLeaf()).isTrue();

        WordPairProcessorTreeNode nonLeaf = new WordPairProcessorTreeNode();
        nonLeaf.addChild(new WordPairProcessorTreeNode());
        assertThat(nonLeaf.isLeaf()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testTraversePreOrder(ICharacterRepository characterRepository) {
        WordPairProcessorTreeNode child1 = new WordPairProcessorTreeNode(0, 0, null, 0, 1, null);
        WordPairProcessorTreeNode child2 = new WordPairProcessorTreeNode(0, 0, null, 0, 2, null);
        WordPairProcessorTreeNode root = new WordPairProcessorTreeNode(0, 0, null, 0, 0, null);
        root.addChild(child1);
        root.addChild(child2);

        List<Integer> levels = new ArrayList<>();
        root.traversePreOrder(node -> levels.add(node.getLevel()));

        assertThat(levels).hasSize(3);
        assertThat(levels).containsSequence(0, 1, 2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testTraversePostOrder(ICharacterRepository characterRepository) {
        WordPairProcessorTreeNode child1 = new WordPairProcessorTreeNode(0, 0, null, 0, 1, null);
        WordPairProcessorTreeNode child2 = new WordPairProcessorTreeNode(0, 0, null, 0, 2, null);
        WordPairProcessorTreeNode root = new WordPairProcessorTreeNode(0, 0, null, 0, 0, null);
        root.addChild(child1);
        root.addChild(child2);

        List<Integer> levels = new ArrayList<>();
        root.traversePostOrder(node -> levels.add(node.getLevel()));

        assertThat(levels).hasSize(3);
        assertThat(levels).containsSequence(1, 2, 0);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testCompareToWithDifferentScores(ICharacterRepository characterRepository) {
        WordPairProcessorTreeNode node1 = new WordPairProcessorTreeNode(0, 0, null, 0, 0, null);
        WordPairProcessorTreeNode node2 = new WordPairProcessorTreeNode(0, 0, null, 1, 0, null);
        int result = node1.compareTo(node2);
        assertThat(result).isLessThan(0);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testCompareToWithSameScores(ICharacterRepository characterRepository) {
        WordPairProcessorTreeNode node1 = new WordPairProcessorTreeNode(0, 0, null, 0, 0, null);
        WordPairProcessorTreeNode node2 = new WordPairProcessorTreeNode(0, 0, null, 0, 1, null);
        int result = node1.compareTo(node2);
        assertThat(result).isGreaterThan(0);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testToString(ICharacterRepository characterRepository) {
        assertThat(new WordPairProcessorTreeNode()).hasToString("[]");
        assertThat(new WordPairProcessorTreeNode(0, 1, new Addition(new HashSet<>(characterRepository.getCharacter("e").getAttributes()), characterRepository), 2, 3, null)).hasToString("ADD e");
    }

}
