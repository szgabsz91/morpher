package com.github.szgabsz91.morpher.transformationengines.astra.impl;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.languagehandlers.api.model.AnnotationTokenizerResult;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.HunmorphAnnotationTokenizer;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.astra.config.ASTRATransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.astra.config.SearcherType;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.AtomicRule;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.RuleGroup;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.searchers.SequentialSearcher;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.transformationengine.ASTRATransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.astra.utils.ExcludeDuringBuild;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@ExcludeDuringBuild
public class ASTRAUnsupervisedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ASTRAUnsupervisedTest.class);

    public static Stream<Arguments> parameters() {
        return Stream.of(
                new TestInput(
                        List.of("<PLUR>"),
                        List.of("<CAS<ACC>>"),
                        List.of("<PLUR>", "<CAS<ACC>>")
                ),
                new TestInput(
                        List.of("<PAST>"),
                        List.of("<VPLUR>"),
                        List.of("<PAST><PLUR>")
                )
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(TestInput testInput) throws IOException {
        // lemma --> <PLUR>
        // lemma --> <PAST>
        Set<Item> simpleItems1 = getSimpleItems(testInput.getAffixTypes1());
        printFirstItems(simpleItems1, "Simple items 1 (" + testInput.getAffixTypes1() + ")");
        // lemma --> <CAS<ACC>>
        // lemma --> <VPLUR>
        Set<Item> simpleItems2 = getSimpleItems(testInput.getAffixTypes2());
        printFirstItems(simpleItems2, "Simple items 2 (" + testInput.getAffixTypes2() + ")");
        // <PLUR> --> <CAS<ACC>>
        // <PAST> --> <VPLUR>
        Set<Item> agglutinativeItems = getCompoundItems(testInput.getAffixTypes3(), simpleItems1);
        printFirstItems(agglutinativeItems, "Agglutinative items (" + testInput.getAffixTypes3() + ")");
        // lemma --> <PLUR> + <CAS<ACC>>
        // lemma --> <PAST><PLUR>
        Set<Item> fusionalItems = getSimpleItems(testInput.getAffixTypes3());
        printFirstItems(fusionalItems, "Fusional items (" + testInput.getAffixTypes3() + ")");

        printAtomicRuleCount(agglutinativeItems, "Before removing existing atomic rules: {}");
        removeExistingAtomicRules(agglutinativeItems, simpleItems2);
        printAtomicRuleCount(agglutinativeItems, "After removing existing atomic rules: {}");

        Set<AtomicRule> simpleAtomicRules1 = reduce(simpleItems1);
        LOGGER.info("{}: {}, sum of lengths: {}", testInput.getAffixTypes1(), simpleAtomicRules1.size(), getLengthSum(simpleAtomicRules1));
        Set<AtomicRule> simpleAtomicRules2 = reduce(simpleItems2);
        LOGGER.info("{}: {}, sum of lengths: {}", testInput.getAffixTypes2(), simpleAtomicRules2.size(), getLengthSum(simpleAtomicRules2));
        Set<AtomicRule> agglutinativeAtomicRules = reduce(agglutinativeItems);
        LOGGER.info("{} agglutinative: {}, sum of lengths: {}", testInput.getAffixTypes3(), agglutinativeAtomicRules.size(), getLengthSum(agglutinativeAtomicRules));
        Set<AtomicRule> fusionalAtomicRules = reduce(fusionalItems);
        LOGGER.info("{} fusional: {}, sum of lengths: {}", testInput.getAffixTypes3(), fusionalAtomicRules.size(), getLengthSum(fusionalAtomicRules));
    }

    private static Set<Item> getSimpleItems(List<AffixType> affixTypes) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("../../morpher-language-handlers/data/ocamorph-results.csv"), StandardCharsets.UTF_8)) {
            Set<String> lines = reader
                    .lines()
                    .collect(toSet());
            HunmorphAnnotationTokenizer hunmorphAnnotationTokenizer = new HunmorphAnnotationTokenizer();
            return lines
                    .stream()
                    .map(line -> line.split(","))
                    .map(lineParts -> {
                        String grammaticalForm = lineParts[0];
                        String expression = lineParts[1];
                        int frequency = Integer.parseInt(lineParts[2]);
                        AnnotationTokenizerResult result = hunmorphAnnotationTokenizer.tokenize(expression, grammaticalForm, frequency);
                        if (result == null) {
                            return null;
                        }
                        return hunmorphAnnotationTokenizer.preprocess(result);
                    })
                    .filter(Objects::nonNull)
                    .filter(result -> {
                        if (result.getAffixTypes().size() != affixTypes.size() + 1) {
                            return false;
                        }

                        List<AffixType> affixTypeSublist = result.getAffixTypes().subList(1, affixTypes.size() + 1);
                        return affixTypeSublist.equals(affixTypes);
                    })
                    .map(result -> {
                        WordPair wordPair = WordPair.of(result.getLemma(), result.getGrammaticalForm());
                        ASTRATransformationEngineConfiguration astraTransformationEngineConfiguration = new ASTRATransformationEngineConfiguration.Builder()
                                .searcherType(SearcherType.SEQUENTIAL)
                                .build();
                        ASTRATransformationEngine astraTransformationEngine = new ASTRATransformationEngine(true, null, astraTransformationEngineConfiguration);
                        astraTransformationEngine.learn(TrainingSet.of(wordPair));
                        SequentialSearcher searcher = (SequentialSearcher) astraTransformationEngine.getAstra().getSearcher();
                        List<AtomicRule> atomicRules = searcher.getRuleGroups()
                                .stream()
                                .map(RuleGroup::getAtomicRules)
                                .flatMap(Collection::stream)
                                .sorted(comparing(atomicRule -> atomicRule.getContext().length()))
                                .collect(toList());
                        return new Item(result, wordPair, atomicRules);
                    })
                    .collect(toSet());
        }
    }

    private static Set<Item> getCompoundItems(List<AffixType> affixTypes, Set<Item> previousItems) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("../../morpher-language-handlers/data/ocamorph-results.csv"), StandardCharsets.UTF_8)) {
            Set<String> lines = reader
                    .lines()
                    .collect(toSet());
            HunmorphAnnotationTokenizer hunmorphAnnotationTokenizer = new HunmorphAnnotationTokenizer();
            return lines
                    .stream()
                    .map(line -> line.split(","))
                    .map(lineParts -> {
                        String grammaticalForm = lineParts[0];
                        String expression = lineParts[1];
                        int frequency = Integer.parseInt(lineParts[2]);
                        AnnotationTokenizerResult result = hunmorphAnnotationTokenizer.tokenize(expression, grammaticalForm, frequency);
                        if (result == null) {
                            return null;
                        }
                        return hunmorphAnnotationTokenizer.preprocess(result);
                    })
                    .filter(Objects::nonNull)
                    .filter(result -> {
                        if (result.getAffixTypes().size() != affixTypes.size() + 1) {
                            return false;
                        }

                        List<AffixType> affixTypeSublist = result.getAffixTypes().subList(1, affixTypes.size() + 1);
                        return affixTypeSublist.equals(affixTypes);
                    })
                    .map(result -> {
                        return previousItems
                                .stream()
                                .filter(i -> i.getAnnotationTokenizerResult().getLemma().equals(result.getLemma()))
                                .map(i -> WordPair.of(i.getWordPair().getRightWord().toString(), result.getGrammaticalForm()))
                                .findFirst()
                                .map(wordPair -> {
                                    ASTRATransformationEngineConfiguration astraTransformationEngineConfiguration = new ASTRATransformationEngineConfiguration.Builder()
                                            .searcherType(SearcherType.SEQUENTIAL)
                                            .build();
                                    ASTRATransformationEngine astraTransformationEngine = new ASTRATransformationEngine(true, null, astraTransformationEngineConfiguration);
                                    astraTransformationEngine.learn(TrainingSet.of(wordPair));
                                    SequentialSearcher searcher = (SequentialSearcher) astraTransformationEngine.getAstra().getSearcher();
                                    List<AtomicRule> atomicRules = searcher.getRuleGroups()
                                            .stream()
                                            .map(RuleGroup::getAtomicRules)
                                            .flatMap(Collection::stream)
                                            .sorted(comparing(atomicRule -> atomicRule.getContext().length()))
                                            .collect(toList());
                                    return new Item(result, wordPair, atomicRules);
                                });
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toSet());
        }
    }

    private static void removeExistingAtomicRules(Set<Item> compoundItems, Set<Item> simpleItems2) {
        compoundItems
                .stream()
                .map(Item::getAtomicRules)
                .forEach(atomicRules -> {
                    List<AtomicRule> atomicRulesToRemove = simpleItems2
                            .stream()
                            .map(Item::getAtomicRules)
                            .flatMap(Collection::stream)
                            .filter(atomicRules::contains)
                            .collect(toList());
                    atomicRules.removeAll(atomicRulesToRemove);
                });
    }

    private static Set<AtomicRule> reduce(Set<Item> items) {
        return items
                .stream()
                .map(item -> {
                    List<AtomicRule> atomicRules = item.getAtomicRules();

                    while (!atomicRules.isEmpty()) {
                        AtomicRule atomicRule = atomicRules.get(0);
                        boolean removable = items
                                .stream()
                                .filter(i -> i != item)
                                .map(Item::getAtomicRules)
                                .anyMatch(ars -> ars.contains(atomicRule));
                        if (!removable) {
                            return atomicRule;
                        }
                        atomicRules.remove(0);
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private static void printFirstItems(Set<Item> items, String title) {
        LOGGER.debug(title);
        items
                .stream()
                .limit(5L)
                .forEach(item -> LOGGER.debug("  - {}", item));
        LOGGER.debug("  - {}", items.size());
    }

    private static void printAtomicRuleCount(Set<Item> items, String template) {
        long count = items
                .stream()
                .map(Item::getAtomicRules)
                .mapToLong(Collection::size)
                .sum();
        LOGGER.debug(template, count);
    }

    private static long getLengthSum(Set<AtomicRule> atomicRules) {
        return atomicRules
                .stream()
                .mapToLong(atomicRule -> atomicRule.getContext().length() + atomicRule.getReplacementString().length())
                .sum();
    }

    private static class TestInput {

        private final List<AffixType> affixTypes1;
        private final List<AffixType> affixTypes2;
        private final List<AffixType> affixTypes3;

        private TestInput(List<String> affixTypes1, List<String> affixTypes2, List<String> affixTypes3) {
            this.affixTypes1 = convert(affixTypes1);
            this.affixTypes2 = convert(affixTypes2);
            this.affixTypes3 = convert(affixTypes3);
        }

        public List<AffixType> getAffixTypes1() {
            return affixTypes1;
        }

        public List<AffixType> getAffixTypes2() {
            return affixTypes2;
        }

        public List<AffixType> getAffixTypes3() {
            return affixTypes3;
        }

        @Override
        public String toString() {
            return "TestInput{" +
                    affixTypes1 +
                    ", " + affixTypes2 +
                    ", " + affixTypes3 +
                    '}';
        }

        private static List<AffixType> convert(List<String> affixTypeStrings) {
            return affixTypeStrings
                    .stream()
                    .map(AffixType::of)
                    .collect(toList());
        }

    }

    private static class Item {

        private final AnnotationTokenizerResult annotationTokenizerResult;
        private final WordPair wordPair;
        private final List<AtomicRule> atomicRules;

        private Item(AnnotationTokenizerResult annotationTokenizerResult, WordPair wordPair, List<AtomicRule> atomicRules) {
            this.annotationTokenizerResult = annotationTokenizerResult;
            this.wordPair = wordPair;
            this.atomicRules = atomicRules;
        }

        public AnnotationTokenizerResult getAnnotationTokenizerResult() {
            return annotationTokenizerResult;
        }

        public WordPair getWordPair() {
            return wordPair;
        }

        public List<AtomicRule> getAtomicRules() {
            return atomicRules;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Item item = (Item) o;
            return Objects.equals(wordPair, item.wordPair);
        }

        @Override
        public int hashCode() {
            return Objects.hash(wordPair);
        }

        @Override
        public String toString() {
            return "Item{" +
                    "annotationTokenizerResult=" + annotationTokenizerResult +
                    ", wordPair=" + wordPair +
                    ", atomicRules=" + atomicRules +
                    '}';
        }

    }

}
