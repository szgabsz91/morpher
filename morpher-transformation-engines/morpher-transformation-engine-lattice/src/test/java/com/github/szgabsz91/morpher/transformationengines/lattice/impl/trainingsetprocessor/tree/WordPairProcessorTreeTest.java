package com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.tree;

import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.characters.ICharacter;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
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
    public void testToString(ICharacterRepository characterRepository, IWordConverter wordConverter) {
        WordPairProcessorTreeNode root = new WordPairProcessorTreeNode();
        WordPairProcessorTree tree = new WordPairProcessorTree(root, null, null, null, null);
        assertThat(tree).hasToString(root.toString());
    }

}
