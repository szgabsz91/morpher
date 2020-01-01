package com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.model;

import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.ITransformation;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.Removal;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.tree.WordPairProcessorTree;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.tree.WordPairProcessorTreeNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class WordPairProcessorResponseTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetters(ICharacterRepository characterRepository) {
        WordPairProcessorTree tree = mock(WordPairProcessorTree.class);
        List<WordPairProcessorTreeNode> leaves = List.of(mock(WordPairProcessorTreeNode.class));
        List<Rule> rules = List.of(mock(Rule.class));
        WordPairProcessorResponse wordPairProcessorResponse = new WordPairProcessorResponse(tree, leaves, rules);

        assertThat(wordPairProcessorResponse.getTree()).isEqualTo(tree);
        assertThat(wordPairProcessorResponse.getLeaves()).isEqualTo(leaves);
        assertThat(wordPairProcessorResponse.getRules()).isEqualTo(rules);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetRuleWithOnlyOneRule(ICharacterRepository characterRepository) {
        Rule rule = mock(Rule.class);
        List<Rule> rules = List.of(rule);
        WordPairProcessorResponse wordPairProcessorResponse = new WordPairProcessorResponse(null, null, rules);

        assertThat(wordPairProcessorResponse.getRule(null)).isEqualTo(rule);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testGetRuleWithMultipleRules(ICharacterRepository characterRepository) {
        Rule rule1 = new Rule(
                null,
                List.of(new Addition(Set.of(), characterRepository)),
                characterRepository,
                null
        );
        Rule rule2 = new Rule(
                null,
                List.of(new Addition(Set.of(), characterRepository), new Removal(Set.of(), characterRepository)),
                characterRepository,
                null
        );
        Rule rule3 = new Rule(
                null,
                List.of(new Removal(Set.of(), characterRepository)),
                characterRepository,
                null
        );
        List<Rule> rules = List.of(rule1, rule2, rule3);

        Map<List<ITransformation>, Long> frequencyMap = new HashMap<>();
        frequencyMap.put(rule1.getTransformations(), 1L);
        frequencyMap.put(rule2.getTransformations(), 2L);
        frequencyMap.put(rule3.getTransformations(), 0L);

        WordPairProcessorResponse wordPairProcessorResponse = new WordPairProcessorResponse(null, null, rules);

        Rule result = wordPairProcessorResponse.getRule(frequencyMap);
        assertThat(result).isEqualTo(rule2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testEquals(ICharacterRepository characterRepository) {
        WordPairProcessorTree tree = mock(WordPairProcessorTree.class);
        WordPairProcessorTree tree2 = mock(WordPairProcessorTree.class);
        List<WordPairProcessorTreeNode> leaves = List.of(mock(WordPairProcessorTreeNode.class));
        List<WordPairProcessorTreeNode> leaves2 = List.of(mock(WordPairProcessorTreeNode.class));
        List<Rule> rules = List.of(mock(Rule.class));
        List<Rule> rules2 = List.of(mock(Rule.class));

        WordPairProcessorResponse wordPairProcessorResponse1 = new WordPairProcessorResponse(tree, leaves, rules);
        WordPairProcessorResponse wordPairProcessorResponse2 = new WordPairProcessorResponse(tree, leaves, rules);
        WordPairProcessorResponse wordPairProcessorResponse3 = new WordPairProcessorResponse(null, leaves, rules);
        WordPairProcessorResponse wordPairProcessorResponse4 = new WordPairProcessorResponse(tree, null, rules);
        WordPairProcessorResponse wordPairProcessorResponse5 = new WordPairProcessorResponse(tree2, leaves, rules);
        WordPairProcessorResponse wordPairProcessorResponse6 = new WordPairProcessorResponse(tree, leaves2, rules);
        WordPairProcessorResponse wordPairProcessorResponse7 = new WordPairProcessorResponse(tree, leaves, rules2);

        assertThat(wordPairProcessorResponse1).isEqualTo(wordPairProcessorResponse2);
        assertThat(wordPairProcessorResponse1).isEqualTo(wordPairProcessorResponse1);
        assertThat(wordPairProcessorResponse1).isNotEqualTo(null);
        assertThat(wordPairProcessorResponse1).isNotEqualTo("string");
        assertThat(wordPairProcessorResponse1).isNotEqualTo(wordPairProcessorResponse3);
        assertThat(wordPairProcessorResponse1).isNotEqualTo(wordPairProcessorResponse4);
        assertThat(wordPairProcessorResponse1).isNotEqualTo(wordPairProcessorResponse5);
        assertThat(wordPairProcessorResponse1).isNotEqualTo(wordPairProcessorResponse6);
        assertThat(wordPairProcessorResponse1).isNotEqualTo(wordPairProcessorResponse7);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHashCode(ICharacterRepository characterRepository) {
        WordPairProcessorTree tree = mock(WordPairProcessorTree.class);
        List<WordPairProcessorTreeNode> leaves = List.of(mock(WordPairProcessorTreeNode.class));
        List<Rule> rules = List.of(mock(Rule.class));
        WordPairProcessorResponse wordPairProcessorResponse = new WordPairProcessorResponse(tree, leaves, rules);

        int expected = 31 * tree.hashCode() + leaves.hashCode();
        expected = 31 * expected + rules.hashCode();

        assertThat(wordPairProcessorResponse.hashCode()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testToString(ICharacterRepository characterRepository) {
        WordPairProcessorTree tree = mock(WordPairProcessorTree.class);
        List<WordPairProcessorTreeNode> leaves = List.of(mock(WordPairProcessorTreeNode.class));
        List<Rule> rules = List.of(mock(Rule.class));
        WordPairProcessorResponse wordPairProcessorResponse = new WordPairProcessorResponse(tree, leaves, rules);

        assertThat(wordPairProcessorResponse).hasToString(tree.toString());
    }

}
