package com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IdentityWordConverter;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.ITransformation;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Replacement;
import com.github.szgabsz91.morpher.methods.lattice.impl.setoperations.IntersectionException;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.DefaultCostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainingSetProcessorTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    IWordConverter wordConverter = new DoubleConsonantWordConverter();
                    ICostCalculator costCalculator = new AttributeBasedCostCalculator();
                    IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                            .characterRepository(characterRepository)
                            .wordConverter(wordConverter)
                            .costCalculator(costCalculator)
                            .build();
                    ITrainingSetProcessor trainingSetProcessor = new TrainingSetProcessor(wordPairProcessor);
                    return Arguments.of(
                            characterRepository,
                            trainingSetProcessor
                    );
                });
    }

    @Test
    public void testConstructorWithEmptyFrequencyMap() {
        IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(HungarianSimpleCharacterRepository.get())
                .wordConverter(new IdentityWordConverter())
                .costCalculator(new DefaultCostCalculator())
                .build();
        TrainingSetProcessor trainingSetProcessor = new TrainingSetProcessor(wordPairProcessor);
        assertThat(trainingSetProcessor.getFrequencyMap()).isEmpty();
    }

    @Test
    public void testConstructorWithNonEmptyFrequencyMap() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(new IdentityWordConverter())
                .costCalculator(new DefaultCostCalculator())
                .build();
        Map<List<ITransformation>, Long> frequencyMap = Map.of(
                List.of(new Replacement(
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("b"),
                        characterRepository
                )),
                1L
        );
        TrainingSetProcessor trainingSetProcessor = new TrainingSetProcessor(wordPairProcessor, frequencyMap);
        assertThat(trainingSetProcessor.getFrequencyMap()).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testInduceRulesWithoutAmbiguousWordPairs(
            ICharacterRepository characterRepository,
            ITrainingSetProcessor trainingSetProcessor) {
        List<WordPair> wordPairs = List.of(WordPair.of("alma", "almát"));
        List<Rule> rules = new ArrayList<>(trainingSetProcessor.induceRules(wordPairs));
        assertThat(rules).hasSize(1);
        Rule rule = rules.get(0);
        Word result = rule.transform(wordPairs.get(0).getLeftWord());
        assertThat(result).isEqualTo(wordPairs.get(0).getRightWord());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testInduceRulesWithAmbiguousWordPairs(
            ICharacterRepository characterRepository,
            ITrainingSetProcessor trainingSetProcessor) throws IntersectionException {
        List<WordPair> wordPairs = List.of(
                WordPair.of("karot", "karotot"),
                WordPair.of("xyz", "xyzot")
        );
        List<Rule> rules = new ArrayList<>(trainingSetProcessor.induceRules(wordPairs));
        assertThat(rules).hasSize(2);
        assertRule(rules.get(0), wordPairs);
        assertRule(rules.get(1), wordPairs);
    }

    private static void assertRule(Rule rule, List<WordPair> wordPairs) {
        int wordPairIndex = rule.getContext().getPrefix().get(1).toString().equals("x") ? 1 : 0;
        WordPair wordPair = wordPairs.get(wordPairIndex);
        Word result = rule.transform(wordPair.getLeftWord());
        assertThat(result).isEqualTo(wordPair.getRightWord());
    }

}
