package com.github.szgabsz91.morpher.methods.astra.impl;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.model.ProbabilisticWord;
import com.github.szgabsz91.morpher.methods.astra.impl.rules.AtomicRule;
import com.github.szgabsz91.morpher.methods.astra.impl.rules.RuleGroup;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.ParallelSearcher;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.PrefixTreeSearcher;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.SequentialSearcher;
import com.github.szgabsz91.morpher.methods.astra.impl.testutils.ASTRABuilder;
import com.github.szgabsz91.morpher.methods.astra.impl.testutils.RuleGroupFactory;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class ASTRATest {

    @Test
    public void testConstructorAndGetters() {
        AffixType affixType = AffixType.of("AFF");
        int minimumSupportThreshold = 2;
        int minimumWordFrequencyThreshold = 3;
        int minimumAggregatedSupportThreshold = 4;
        int minimumContextLength = 5;
        int maximumNumberOfGeneratedAtomicRules = 6;
        double maximumResponseProbabilityDifferenceThreshold = 7.8;
        ASTRA astra = new ASTRA(
                affixType,
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                ASTRABuilder.DEFAULT_SEARCHER,
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                ASTRABuilder.DEFAULT_FITNESS_THRESHOLD,
                ASTRABuilder.DEFAULT_MAXIMUM_NUMBER_OF_RESPONSES,
                minimumSupportThreshold,
                minimumWordFrequencyThreshold,
                minimumAggregatedSupportThreshold,
                minimumContextLength,
                maximumNumberOfGeneratedAtomicRules,
                maximumResponseProbabilityDifferenceThreshold
        );
        assertThat(astra.getAffixType()).isSameAs(affixType);
        assertThat(astra.getWordConverter()).isSameAs(ASTRABuilder.DEFAULT_WORD_CONVERTER);
        assertThat(astra.getCharacterRepository()).isSameAs(ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY);
        assertThat(astra.getSearcher()).isSameAs(ASTRABuilder.DEFAULT_SEARCHER);
        assertThat(astra.getSegmentFitnessCalculator()).isSameAs(ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR);
        assertThat(astra.getMinimalMatchingSegmentLength()).isEqualTo(ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH);
        assertThat(astra.getFitnessThreshold()).isEqualTo(ASTRABuilder.DEFAULT_FITNESS_THRESHOLD);
        assertThat(astra.getMaximumNumberOfResponses()).isEqualTo(ASTRABuilder.DEFAULT_MAXIMUM_NUMBER_OF_RESPONSES);
        assertThat(astra.getMinimumSupportThreshold()).isEqualTo(minimumSupportThreshold);
        assertThat(astra.getMinimumWordFrequencyThreshold()).isEqualTo(minimumWordFrequencyThreshold);
        assertThat(astra.getMinimumAggregatedSupportThreshold()).isEqualTo(minimumAggregatedSupportThreshold);
        assertThat(astra.getMinimumContextLength()).isEqualTo(minimumContextLength);
        assertThat(astra.getMaximumNumberOfGeneratedAtomicRules()).isEqualTo(maximumNumberOfGeneratedAtomicRules);
        assertThat(astra.getMaximumResponseProbabilityDifferenceThreshold()).isEqualTo(maximumResponseProbabilityDifferenceThreshold);
    }

    @Test
    public void testIsUnidirectional() {
        AffixType affixType = AffixType.of("AFF");
        IASTRA unidirectionalAstra = new ASTRA(
                affixType,
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new PrefixTreeSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, true),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                ASTRABuilder.DEFAULT_FITNESS_THRESHOLD,
                ASTRABuilder.DEFAULT_MAXIMUM_NUMBER_OF_RESPONSES,
                null,
                null,
                null,
                null,
                null,
                null
        );
        assertThat(unidirectionalAstra.isUnidirectional()).isTrue();
        IASTRA bidirectionalAstra = new ASTRA(
                affixType,
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new PrefixTreeSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                ASTRABuilder.DEFAULT_FITNESS_THRESHOLD,
                ASTRABuilder.DEFAULT_MAXIMUM_NUMBER_OF_RESPONSES,
                null,
                null,
                null,
                null,
                null,
                null
        );
        assertThat(bidirectionalAstra.isUnidirectional()).isFalse();
    }

    @Test
    public void testSize() {
        IASTRA astra = new ASTRABuilder()
                .affixType(AffixType.of("AFF"))
                .build();
        astra.learnWordPairs(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d"),
                FrequencyAwareWordPair.of("e", "f")
        ));
        assertThat(astra.size()).isEqualTo(3);
    }

    @Test
    public void testAddAtomicRulesAndInflect() {
        ASTRA astra = new ASTRABuilder()
                .affixType(AffixType.of("AFF"))
                .build();
        astra.addAtomicRules(List.of(
                new AtomicRule("a", "b", "d", "c", 1),
                new AtomicRule("d", "e", "g", "f", 1)
        ));

        // abc
        Word abc = Word.of("abc");
        Optional<MethodResponse> abcResponse = astra.inflect(abc);
        MethodResponse abcExpected = MethodResponse.singleton(Word.of("adc"));
        assertThat(abcResponse).hasValue(abcExpected);

        // def
        Word def = Word.of("def");
        Optional<MethodResponse> defResponse = astra.inflect(def);
        MethodResponse defExpected = MethodResponse.singleton(Word.of("dgf"));
        assertThat(defResponse).hasValue(defExpected);
    }

    @Test
    public void testAddAtomicRulesAndLemmatize() {
        ASTRA astra = new ASTRABuilder()
                .affixType(AffixType.of("AFF"))
                .build();
        astra.addAtomicRules(List.of(
                new AtomicRule("a", "b", "d", "c", 1),
                new AtomicRule("d", "e", "g", "f", 1)
        ));

        // adc
        Word adc = Word.of("adc");
        Optional<MethodResponse> adcResponse = astra.lemmatize(adc);
        MethodResponse adcExpected = MethodResponse.singleton(Word.of("abc"));
        assertThat(adcResponse).hasValue(adcExpected);

        // dgf
        Word dgf = Word.of("dgf");
        Optional<MethodResponse> dgfResponse = astra.lemmatize(dgf);
        MethodResponse dgfExpected = MethodResponse.singleton(Word.of("def"));
        assertThat(dgfResponse).hasValue(dgfExpected);
    }

    @Test
    public void testLearnWordPairs() {
        IASTRA astra = new ASTRABuilder()
                .affixType(AffixType.of("AFF"))
                .build();
        Set<FrequencyAwareWordPair> wordPairs = Set.of(
                FrequencyAwareWordPair.of("abc", "adc"),
                FrequencyAwareWordPair.of("def", "dgf")
        );
        astra.learnWordPairs(wordPairs);

        wordPairs.forEach(wordPair -> {
            Word rootForm = wordPair.getLeftWord();
            Word inflectedForm = wordPair.getRightWord();
            assertThat(astra.inflect(rootForm)).hasValue(MethodResponse.singleton(inflectedForm));
            assertThat(astra.lemmatize(inflectedForm)).hasValue(MethodResponse.singleton(rootForm));
        });
    }

    @Test
    public void testWithNoApplicableAtomicRules() {
        IASTRA astra = new ASTRABuilder()
                .affixType(AffixType.of("AFF"))
                .build();
        Set<FrequencyAwareWordPair> wordPairs = Set.of(
                FrequencyAwareWordPair.of("abc", "adc")
        );
        astra.learnWordPairs(wordPairs);

        Optional<MethodResponse> response = astra.inflect(Word.of("xyz"));
        assertThat(response).isEmpty();
    }

    @Test
    public void testWithPrefixRule() {
        AffixType affixType = AffixType.of("AFF");
        IASTRA astra = new ASTRA(
                affixType,
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new PrefixTreeSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                ASTRABuilder.DEFAULT_FITNESS_THRESHOLD,
                ASTRABuilder.DEFAULT_MAXIMUM_NUMBER_OF_RESPONSES,
                null,
                null,
                null,
                null,
                null,
                null
        );
        FrequencyAwareWordPair wordPair = FrequencyAwareWordPair.of("heg", "megheg");
        astra.learnWordPairs(Set.of(wordPair));

        Optional<MethodResponse> response = astra.lemmatize(wordPair.getRightWord());
        assertThat(response).hasValue(MethodResponse.singleton(wordPair.getLeftWord()));
    }

    @Test
    public void testWithCompoundRule() {
        AffixType affixType = AffixType.of("AFF");
        IASTRA astra = new ASTRA(
                affixType,
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new ParallelSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                0.4,
                2,
                null,
                null,
                null,
                null,
                null,
                null
        );
        FrequencyAwareWordPair wordPair = FrequencyAwareWordPair.of("apró", "legapróbb");
        astra.learnWordPairs(Set.of(wordPair));

        Optional<MethodResponse> response = astra.lemmatize(wordPair.getRightWord());
        assertThat(response).isPresent();
        MethodResponse methodResponse = response.get();
        assertThat(methodResponse.getResults())
                .extracting(ProbabilisticWord::getWord)
                .contains(wordPair.getLeftWord());
    }

    @Test
    public void testSimpleCase() {
        AffixType affixType = AffixType.of("AFF");
        IASTRA astra = new ASTRA(
                affixType,
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new SequentialSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                0.4,
                2,
                null,
                null,
                null,
                null,
                null,
                null
        );
        astra.learnWordPairs(Set.of(
                FrequencyAwareWordPair.of("xya", "xye"),
                FrequencyAwareWordPair.of("xda", "xdi")
        ));

        Optional<MethodResponse> optionalInflectionMethodResponse = astra.inflect(Word.of("ddda"));
        assertThat(optionalInflectionMethodResponse).isPresent();
        MethodResponse inflectionMethodResponse = optionalInflectionMethodResponse.get();
        List<ProbabilisticWord> inflectionResults = inflectionMethodResponse.getResults();
        assertThat(inflectionResults).hasSize(1);
        ProbabilisticWord inflectionOutput = inflectionResults.get(0);
        assertThat(inflectionOutput.getWord()).hasToString("dddi");
        assertThat(inflectionOutput.getProbability()).isEqualTo(0.5);

        Optional<MethodResponse> optionalLemmatizationMethodResponse = astra.lemmatize(Word.of("dddi"));
        assertThat(optionalLemmatizationMethodResponse).isPresent();
        MethodResponse lemmatizationMethodResponse = optionalLemmatizationMethodResponse.get();
        List<ProbabilisticWord> lemmatizationResults = lemmatizationMethodResponse.getResults();
        assertThat(lemmatizationResults).hasSize(1);
        ProbabilisticWord lemmatizationResult = lemmatizationResults.get(0);
        assertThat(lemmatizationResult.getWord()).hasToString("ddda");
        assertThat(lemmatizationResult.getProbability()).isEqualTo(0.5);
    }

    @Test
    public void testWithSuperlat() {
        AffixType affixType = AffixType.of("AFF");
        ASTRA astra = new ASTRA(
                affixType,
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new SequentialSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                0.1,
                Integer.MAX_VALUE,
                null,
                null,
                null,
                null,
                null,
                null
        );
        astra.getSearcher().addAtomicRules(Set.of(
                new AtomicRule("", "$", "$leg", "", 1),
                new AtomicRule("", "#", "abb#", "", 1),
                new AtomicRule("", "#", "ebb#", "", 1),
                new AtomicRule("", "#", "bb#", "", 1),
                new AtomicRule("", "#", "ibb#", "", 1)
        ));
        Optional<MethodResponse> methodResponse = astra.inflect(Word.of("kiugró"));
        assertThat(methodResponse).isPresent();
        Set<Word> resultingWords = methodResponse.get().getResults()
                .stream()
                .map(ProbabilisticWord::getWord)
                .collect(toSet());
        assertThat(resultingWords).contains(Word.of("legkiugróbb"));
    }

    @Test
    public void testWithMinimumSupportThreshold() {
        AffixType affixType = AffixType.of("AFF");
        int minimumSupportThreshold = 1;
        ASTRA astra = new ASTRA(
                affixType,
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new SequentialSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                0.1,
                Integer.MAX_VALUE,
                minimumSupportThreshold,
                null,
                null,
                null,
                null,
                null
        );
        astra.learnWordPairs(Set.of(
                FrequencyAwareWordPair.of("alma", "almát", 3),
                FrequencyAwareWordPair.of("malma", "malmát", 4)
        ));
        boolean minimumContextLengthPasses = astra.getSearcher().getRuleGroups()
                .stream()
                .map(RuleGroup::getAtomicRules)
                .flatMap(Collection::stream)
                .allMatch(atomicRule -> atomicRule.getSupport() > minimumSupportThreshold);
        assertThat(minimumContextLengthPasses).isTrue();
        assertThat(astra.getSearcher().getRuleGroups()).isNotEmpty();
    }

    @Test
    public void testWithMinimumWordFrequencyThreshold() {
        AffixType affixType = AffixType.of("AFF");
        int minimumWordFrequencyThreshold = 6;
        ASTRA astra = new ASTRA(
                affixType,
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new SequentialSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                0.1,
                Integer.MAX_VALUE,
                null,
                minimumWordFrequencyThreshold,
                null,
                null,
                null,
                null
        );
        astra.learnWordPairs(Set.of(
                FrequencyAwareWordPair.of("alma", "almát", 3),
                FrequencyAwareWordPair.of("malma", "malmát", 4)
        ));
        boolean minimumContextLengthPasses = astra.getSearcher().getRuleGroups()
                .stream()
                .map(RuleGroup::getAtomicRules)
                .flatMap(Collection::stream)
                .allMatch(atomicRule -> atomicRule.getWordFrequency() > minimumWordFrequencyThreshold);
        assertThat(minimumContextLengthPasses).isTrue();
        assertThat(astra.getSearcher().getRuleGroups()).isNotEmpty();
    }

    @Test
    public void testWithMinimumAggregatedSupportThreshold() {
        AffixType affixType = AffixType.of("AFF");
        int minimumAggregatedSupportThreshold = 13;
        ASTRA astra = new ASTRA(
                affixType,
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new SequentialSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                0.1,
                Integer.MAX_VALUE,
                null,
                null,
                minimumAggregatedSupportThreshold,
                null,
                null,
                null
        );
        astra.learnWordPairs(Set.of(
                FrequencyAwareWordPair.of("alma", "almát", 3),
                FrequencyAwareWordPair.of("malma", "malmát", 4)
        ));
        boolean minimumContextLengthPasses = astra.getSearcher().getRuleGroups()
                .stream()
                .map(RuleGroup::getAtomicRules)
                .flatMap(Collection::stream)
                .allMatch(atomicRule -> atomicRule.getAggregatedSupport() > minimumAggregatedSupportThreshold);
        assertThat(minimumContextLengthPasses).isTrue();
        assertThat(astra.getSearcher().getRuleGroups()).isNotEmpty();
    }

    @Test
    public void testWithSuperlatAndMinimumContextLength() {
        AffixType affixType = AffixType.of("AFF");
        int minimumContextLength = 3;
        ASTRA astra = new ASTRA(
                affixType,
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new SequentialSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                0.1,
                Integer.MAX_VALUE,
                null,
                null,
                null,
                minimumContextLength,
                null,
                null
        );
        astra.learnWordPairs(Set.of(FrequencyAwareWordPair.of("kiugró", "legkiugróbb")));
        boolean minimumContextLengthPasses = astra.getSearcher().getRuleGroups()
                .stream()
                .map(RuleGroup::getAtomicRules)
                .flatMap(Collection::stream)
                .allMatch(atomicRule -> atomicRule.getContext().length() >= minimumContextLength);
        assertThat(minimumContextLengthPasses).isTrue();
    }

    @Test
    public void testWithSuperlatAndNoMinimumContextLength() {
        AffixType affixType = AffixType.of("AFF");
        ASTRA astra = new ASTRA(
                affixType,
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new SequentialSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                0.1,
                Integer.MAX_VALUE,
                null,
                null,
                null,
                null,
                null,
                null
        );
        astra.learnWordPairs(Set.of(FrequencyAwareWordPair.of("kiugró", "legkiugróbb")));
        boolean minimumContextLengthPasses = astra.getSearcher().getRuleGroups()
                .stream()
                .map(RuleGroup::getAtomicRules)
                .flatMap(Collection::stream)
                .anyMatch(atomicRule -> atomicRule.getContext().length() < 3);
        assertThat(minimumContextLengthPasses).isTrue();
    }

    @Test
    public void testWithSuperlatAndMaximumNumberOfGeneratedAtomicRules() {
        AffixType affixType = AffixType.of("AFF");
        ASTRA astra = new ASTRA(
                affixType,
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new SequentialSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                0.1,
                Integer.MAX_VALUE,
                null,
                null,
                null,
                null,
                1,
                null
        );
        astra.learnWordPairs(Set.of(FrequencyAwareWordPair.of("kiugró", "legkiugróbb")));
        assertThat(astra.getSearcher().getRuleGroups()).containsExactlyInAnyOrder(
                RuleGroupFactory.createRuleGroup(new AtomicRule("", "#", "bb#", "", 1)),
                RuleGroupFactory.createRuleGroup(new AtomicRule("", "$", "$leg", "", 1))
        );
    }

    @Test
    public void testWithSuperlatAndMaximumNumberOfGeneratedAtomicRulesAndMinimumContextLength() {
        AffixType affixType = AffixType.of("AFF");
        ASTRA astra = new ASTRA(
                affixType,
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new SequentialSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                0.1,
                Integer.MAX_VALUE,
                null,
                null,
                null,
                3,
                2,
                null
        );
        astra.learnWordPairs(Set.of(FrequencyAwareWordPair.of("kiugró", "legkiugróbb")));
        assertThat(astra.getSearcher().getRuleGroups()).containsExactlyInAnyOrder(
                RuleGroupFactory.createRuleGroup(new AtomicRule("", "$", "$leg", "ki", 1)),
                RuleGroupFactory.createRuleGroup(new AtomicRule("", "$", "$leg", "kiu", 1)),
                RuleGroupFactory.createRuleGroup(new AtomicRule("ró", "#", "bb#", "", 1)),
                RuleGroupFactory.createRuleGroup(new AtomicRule("gró", "#", "bb#", "", 1))
        );
    }

    @Test
    public void testInflectWithEmptyFilteredAtomicRuleCandidates() {
        ASTRA astra = new ASTRA(
                AffixType.of("AFF"),
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new SequentialSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                1.0,
                Integer.MAX_VALUE,
                null,
                null,
                null,
                3,
                2,
                null
        );
        astra.learnWordPairs(Set.of(FrequencyAwareWordPair.of("alma", "almát")));
        Optional<MethodResponse> optionalMethodResponse = astra.inflect(Word.of("alma"));
        assertThat(optionalMethodResponse).isPresent();
        MethodResponse methodResponse = optionalMethodResponse.get();
        assertThat(methodResponse.getResults()).hasSize(1);
        ProbabilisticWord probabilisticWord = methodResponse.getResults().get(0);
        assertThat(probabilisticWord.getWord()).hasToString("almát");
    }

    @Test
    public void testInflectWithMaximumResponseProbabilityDifferenceThreshold() {
        ASTRA astra = new ASTRA(
                AffixType.of("AFF"),
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new SequentialSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                0.0,
                Integer.MAX_VALUE,
                null,
                null,
                null,
                3,
                2,
                0.1
        );
        astra.learnWordPairs(Set.of(FrequencyAwareWordPair.of("malma", "malmát")));
        astra.learnWordPairs(Set.of(FrequencyAwareWordPair.of("ma", "mat")));
        astra.learnWordPairs(Set.of(FrequencyAwareWordPair.of("alma", "almet")));
        Optional<MethodResponse> optionalMethodResponse = astra.inflect(Word.of("alma"));
        assertThat(optionalMethodResponse).isPresent();
        MethodResponse methodResponse = optionalMethodResponse.get();
        assertThat(methodResponse.getResults()).hasSize(2);
        Set<String> results = methodResponse.getResults()
                .stream()
                .map(ProbabilisticWord::getWord)
                .map(Word::toString)
                .collect(toSet());
        assertThat(results).containsExactlyInAnyOrder("almet", "almát");
    }

    @Test
    public void testSupportAndWordFrequencyCalculation() {
        ASTRA astra = new ASTRA(
                AffixType.of("AFF"),
                ASTRABuilder.DEFAULT_WORD_CONVERTER,
                ASTRABuilder.DEFAULT_CHARACTER_REPOSITORY,
                new ParallelSearcher(ASTRABuilder.DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false),
                ASTRABuilder.DEFAULT_SEGMENT_FITNESS_CALCULATOR,
                ASTRABuilder.DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH,
                0.0,
                Integer.MAX_VALUE,
                null,
                null,
                null,
                null,
                null,
                null
        );
        astra.learnWordPairs(Set.of(
                FrequencyAwareWordPair.of("alma", "almát", 2),
                FrequencyAwareWordPair.of("malma", "malmát", 1)
        ));
        ParallelSearcher searcher = (ParallelSearcher) astra.getSearcher();
        Set<AtomicRule> atomicRules = searcher.getRuleGroups()
                .stream()
                .map(RuleGroup::getAtomicRules)
                .flatMap(Collection::stream)
                .collect(toSet());
        assertThat(atomicRules).allMatch(atomicRule -> {
            if (atomicRule.getPrefix().equals("m") && atomicRule.getFrom().equals("a#") && atomicRule.getTo().equals("át#") && atomicRule.getPostfix().equals("")) {
                return atomicRule.getSupport() == 2 && atomicRule.getWordFrequency() == 3;
            }

            if (atomicRule.getPrefix().equals("$malm") && atomicRule.getFrom().equals("a#") && atomicRule.getTo().equals("át#") && atomicRule.getPostfix().equals("")) {
                return atomicRule.getSupport() == 1 && atomicRule.getWordFrequency() == 1;
            }

            if (atomicRule.getPrefix().equals("") && atomicRule.getFrom().equals("a#") && atomicRule.getTo().equals("át#") && atomicRule.getPostfix().equals("")) {
                return atomicRule.getSupport() == 2 && atomicRule.getWordFrequency() == 3;
            }

            if (atomicRule.getPrefix().equals("malm") && atomicRule.getFrom().equals("a#") && atomicRule.getTo().equals("át#") && atomicRule.getPostfix().equals("")) {
                return atomicRule.getSupport() == 1 && atomicRule.getWordFrequency() == 1;
            }

            if (atomicRule.getPrefix().equals("lm") && atomicRule.getFrom().equals("a#") && atomicRule.getTo().equals("át#") && atomicRule.getPostfix().equals("")) {
                return atomicRule.getSupport() == 2 && atomicRule.getWordFrequency() == 3;
            }

            if (atomicRule.getPrefix().equals("$alm") && atomicRule.getFrom().equals("a#") && atomicRule.getTo().equals("át#") && atomicRule.getPostfix().equals("")) {
                return atomicRule.getSupport() == 1 && atomicRule.getWordFrequency() == 2;
            }

            if (atomicRule.getPrefix().equals("alm") && atomicRule.getFrom().equals("a#") && atomicRule.getTo().equals("át#") && atomicRule.getPostfix().equals("")) {
                return atomicRule.getSupport() == 2 && atomicRule.getWordFrequency() == 3;
            }

            return false;
        });
    }

}
