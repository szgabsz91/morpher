package com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.tree;

import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.characters.ICharacter;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.Consonant;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.Vowel;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IWordConverter;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class WordPairProcessorTreeTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    IWordConverter wordConverter = new DoubleConsonantWordConverter();
                    return Arguments.of(
                            characterRepository,
                            wordConverter
                    );
                });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConstructor(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        WordPairProcessorTreeNode root = new WordPairProcessorTreeNode();
        WordPairProcessorTree tree = new WordPairProcessorTree(root, null, null, null, null);
        assertThat(tree.getRoot()).isEqualTo(root);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testBuildWithAllLeaves(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        WordPairProcessorTreeNode root = new WordPairProcessorTreeNode();
        WordPair wordPair = WordPair.of("abcdefgh", "xaccfefg");
        List<ICharacter> leftWordCharacters = wordConverter.convert(wordPair.getLeftWord(), characterRepository);
        List<ICharacter> rightWordCharacters = wordConverter.convert(wordPair.getRightWord(), characterRepository);
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        WordPairProcessorTree tree = new WordPairProcessorTree(root, leftWordCharacters, rightWordCharacters, characterRepository, costCalculator);
        List<WordPairProcessorTreeNode> leaves = tree.build(Integer.MAX_VALUE);
        assertThat(leaves).hasSize(265_729);
    }

    @Test
    public void testBuildWithFivesLeaves() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        WordPairProcessorTreeNode root = new WordPairProcessorTreeNode();
        WordPair wordPair = WordPair.of("abcdefgh", "xaccfefg");
        List<ICharacter> leftWordCharacters = wordConverter.convert(wordPair.getLeftWord(), characterRepository);
        List<ICharacter> rightWordCharacters = wordConverter.convert(wordPair.getRightWord(), characterRepository);
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        WordPairProcessorTree tree = new WordPairProcessorTree(root, leftWordCharacters, rightWordCharacters, characterRepository, costCalculator);
        List<WordPairProcessorTreeNode> leaves = tree.build(5);
        assertThat(leaves).hasSize(14);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testEquals(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        List<ICharacter> v = List.of(Vowel.create());
        List<ICharacter> c = List.of(Consonant.create());
        WordPairProcessorTree tree1 = new WordPairProcessorTree(new WordPairProcessorTreeNode(), v, v, null, null);
        WordPairProcessorTree tree2 = new WordPairProcessorTree(null, v, v, null, null);
        WordPairProcessorTree tree3 = new WordPairProcessorTree(new WordPairProcessorTreeNode(), c, v, null, null);
        WordPairProcessorTree tree4 = new WordPairProcessorTree(new WordPairProcessorTreeNode(), v, c, null, null);
        WordPairProcessorTree tree5 = new WordPairProcessorTree(new WordPairProcessorTreeNode(), v, v, null, null);

        assertThat(tree1.equals(tree1)).isTrue();
        assertThat(tree1.equals(null)).isFalse();
        assertThat(tree1).isNotEqualTo("string");
        assertThat(tree1).isNotEqualTo(tree2);
        assertThat(tree1).isNotEqualTo(tree3);
        assertThat(tree1).isNotEqualTo(tree4);
        assertThat(tree1).isEqualTo(tree5);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHashCode(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        WordPairProcessorTreeNode root = new WordPairProcessorTreeNode();
        List<ICharacter> v = List.of(Vowel.create());
        List<ICharacter> c = List.of(Consonant.create());
        WordPairProcessorTree tree = new WordPairProcessorTree(root, v, c, null, null);

        int expected = 31 * root.hashCode() + v.hashCode();
        expected = 31 * expected + c.hashCode();

        assertThat(tree.hashCode()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testToString(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        WordPairProcessorTreeNode root = new WordPairProcessorTreeNode();
        WordPairProcessorTree tree = new WordPairProcessorTree(root, null, null, null, null);
        assertThat(tree).hasToString(root.toString());
    }

}
