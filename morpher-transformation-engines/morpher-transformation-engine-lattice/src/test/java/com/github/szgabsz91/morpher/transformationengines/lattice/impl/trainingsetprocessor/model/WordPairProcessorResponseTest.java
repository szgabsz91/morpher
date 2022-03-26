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
    public void testToString(ICharacterRepository characterRepository) {
        WordPairProcessorTree tree = mock(WordPairProcessorTree.class);
        List<WordPairProcessorTreeNode> leaves = List.of(mock(WordPairProcessorTreeNode.class));
        List<Rule> rules = List.of(mock(Rule.class));
        WordPairProcessorResponse wordPairProcessorResponse = new WordPairProcessorResponse(tree, leaves, rules);

        assertThat(wordPairProcessorResponse).hasToString(tree.toString());
    }

}
