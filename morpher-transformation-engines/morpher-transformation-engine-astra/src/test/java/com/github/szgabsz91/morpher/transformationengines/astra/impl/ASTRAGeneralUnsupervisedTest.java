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
import org.junit.Before;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.paukov.combinatorics3.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Map.entry;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@ExcludeDuringBuild
public class ASTRAGeneralUnsupervisedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ASTRAGeneralUnsupervisedTest.class);

    private static Set<AffixTypeCombinationAffixType> affixTypeCombinationAffixes;

    public static Stream<Arguments> parameters() {
        return Stream.of(
                /*new TestInput(
                        AffixType.of("<PLUR>"),
                        AffixType.of("<CAS<DAT>>"),
                        AffixType.of("<POSS<1>>")
                ),
                new TestInput(
                        AffixType.of("<PLUR>"),
                        AffixType.of("<CAS<ACC>>")
                ),
                new TestInput(
                        List.of("<PAST>"),
                        List.of("<VPLUR>"),
                        List.of("<PAST><PLUR>")
                )*/
                new TestInput(
                        Map.ofEntries(entry(
                                AffixType.of("<PAST><PLUR>"),
                                AffixType.of("<PAST><VPLUR>")
                        )),
                        AffixType.of("<PAST>"),
                        AffixType.of("<VPLUR>")
                )
        ).map(Arguments::of);
    }

    @Before
    public void setUp() {
        affixTypeCombinationAffixes = null;
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(TestInput testInput) throws IOException {
        LOGGER.info("Testing {}", testInput);
        Map<List<AffixType>, ProcessResult> processResultMap = new HashMap<>();
        List<AnnotationTokenizerResult> annotationTokenizerResults = filterAnnotationTokenizerResults(testInput);
        List<Set<AffixType>> affixTypeCombinations = getAffixTypeCombinations(testInput);

        for (Set<AffixType> affixTypeCombination : affixTypeCombinations) {
            process(affixTypeCombination, processResultMap, annotationTokenizerResults);
        }
    }

    private static List<AnnotationTokenizerResult> filterAnnotationTokenizerResults(TestInput testInput) throws IOException {
        Set<AffixType> expectedAffixTypes = testInput.getAffixTypes();

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
                        result = hunmorphAnnotationTokenizer.preprocess(result);
                        if (result == null) {
                            return null;
                        }
                        return convertBack(result, expectedAffixTypes, testInput.getAffixTypeConversionMap());
                    })
                    .filter(Objects::nonNull)
                    .filter(result -> {
                        List<AffixType> affixTypeList = new ArrayList<>(result.getAffixTypes());
                        affixTypeList.remove(0);
                        Set<AffixType> affixTypeSet = new HashSet<>(affixTypeList);
                        return expectedAffixTypes.containsAll(affixTypeSet);
                    })
                    .collect(toList());
        }
    }

    private static List<Set<AffixType>> getAffixTypeCombinations(TestInput testInput) {
        Set<AffixType> affixTypes = testInput.getAffixTypes();
        List<Set<AffixType>> affixTypeCombinations = new ArrayList<>();

        for (int i = 1; i <= affixTypes.size(); i++) {
            List<HashSet<AffixType>> affixTypeCombination = Generator.combination(affixTypes)
                    .simple(i)
                    .stream()
                    .map(HashSet::new)
                    .collect(toList());
            affixTypeCombinations.addAll(affixTypeCombination);
        }

        affixTypeCombinations.sort(Comparator.comparingInt(Set::size));
        LOGGER.info("Affix type combinations:");
        affixTypeCombinations.forEach(affixTypeCombination -> LOGGER.info("    - {}", affixTypeCombination));

        return affixTypeCombinations;
    }

    private void process(Set<AffixType> affixTypeCombination, Map<List<AffixType>, ProcessResult> processResultMap, List<AnnotationTokenizerResult> annotationTokenizerResults) {
        LOGGER.info("Processing {}", affixTypeCombination);

        if (affixTypeCombination.size() == 1) {
            ProcessResult fusionalProcessResult = generateFusionalProcessResult(affixTypeCombination, annotationTokenizerResults);
            LOGGER.info("    - First level fusional result: {}", fusionalProcessResult);
            fusionalProcessResult.reduce();
            processResultMap.put(new ArrayList<>(affixTypeCombination), fusionalProcessResult);
            LOGGER.info("    - Process result map:");
            processResultMap.forEach((affixTypes, processResult) -> LOGGER.info("        - {} --> {}", affixTypes, processResult));
            return;
        }

        ProcessResult fusionalProcessResult = generateFusionalProcessResult(affixTypeCombination, annotationTokenizerResults);
        Stream<ProcessResult> agglutinativeProcessResultStream = processResultMap.values()
                .stream()
                .filter(processResult -> affixTypeCombination.containsAll(processResult.getSemanticRelations()))
                .map(parentProcessResult -> {
                    Set<AffixType> parentSemanticRelations = parentProcessResult.getSemanticRelations();
                    Set<AffixType> missingSemanticRelations = new HashSet<>(affixTypeCombination);
                    missingSemanticRelations.removeAll(parentSemanticRelations);
                    String missingAffixTypeString = missingSemanticRelations
                            .stream()
                            .map(AffixType::toString)
                            .sorted()
                            .collect(joining());
                    AffixType missingAffixType = AffixType.of(missingAffixTypeString);
                    List<AffixType> affixTypes = new ArrayList<>(parentProcessResult.getAffixTypes());
                    affixTypes.add(missingAffixType);
                    LOGGER.info("    - Generating agglutinative result for {} from {}", affixTypeCombination, parentProcessResult);
                    ProcessResult processResult = generateAgglutinativeProcessResult(affixTypeCombination, affixTypes, parentProcessResult, annotationTokenizerResults);
                    ProcessResult processResultToRemove = processResultMap.get(List.of(missingAffixType));
                    processResult.removeParentAtomicRules(processResultToRemove, missingAffixType);
                    return processResult;
                });
        List<ProcessResult> processResultCandidates = Stream.concat(Stream.of(fusionalProcessResult), agglutinativeProcessResultStream)
                .peek(ProcessResult::reduce)
                .sorted()
                .collect(toList());
        processResultCandidates.forEach(processResultCandidate -> LOGGER.info("    - Candidate: {}", processResultCandidate));

        ProcessResult winningProcessResult = processResultCandidates.get(0);
        LOGGER.info("    - Winning candidate: {}", winningProcessResult);
        processResultMap.put(winningProcessResult.getAffixTypes(), winningProcessResult);
        LOGGER.info("    - Process result map:");
        processResultMap.forEach((affixTypes, processResult) -> LOGGER.info("        - {} --> {}", affixTypes, processResult));
    }

    private ProcessResult generateFusionalProcessResult(Set<AffixType> affixTypesToProcess, List<AnnotationTokenizerResult> annotationTokenizerResults) {
        Set<Item> items = annotationTokenizerResults
                .stream()
                .filter(annotationTokenizerResult -> {
                    List<AffixType> affixTypeList = new ArrayList<>(annotationTokenizerResult.getAffixTypes());
                    affixTypeList.remove(0);
                    Set<AffixType> affixTypeSet = new HashSet<>(affixTypeList);
                    return affixTypeSet.equals(affixTypesToProcess);
                })
                .map(result -> {
                    WordPair wordPair = WordPair.of(result.getLemma(), result.getGrammaticalForm());
                    ASTRATransformationEngineConfiguration astraTransformationEngineConfiguration = new ASTRATransformationEngineConfiguration.Builder()
                            .searcherType(SearcherType.SEQUENTIAL)
                            .build();
                    ASTRATransformationEngine astraTransformationEngine = new ASTRATransformationEngine(true, AffixType.of("AFF"), astraTransformationEngineConfiguration);
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
        String affixTypeString = affixTypesToProcess
                .stream()
                .map(AffixType::toString)
                .sorted()
                .collect(joining());
        AffixType affixType = AffixType.of(affixTypeString);
        return new ProcessResult(affixTypesToProcess, List.of(affixType), items);
    }

    private ProcessResult generateAgglutinativeProcessResult(Set<AffixType> semanticRelations, List<AffixType> affixTypesToProcess, ProcessResult parentProcessResult, List<AnnotationTokenizerResult> annotationTokenizerResults) {
        Wrapper<Integer> printedWordPairCounter = new Wrapper<>(0);

        Set<Item> items = annotationTokenizerResults
                .stream()
                .filter(annotationTokenizerResult -> {
                    List<AffixType> affixTypeList = new ArrayList<>(annotationTokenizerResult.getAffixTypes());
                    affixTypeList.remove(0);
                    Set<AffixType> affixTypeSet = new HashSet<>(affixTypeList);
                    return affixTypeSet.equals(semanticRelations);
                })
                .flatMap(result -> {
                    return parentProcessResult.getItems()
                            .stream()
                            .filter(item -> item.getAnnotationTokenizerResult().getLemma().equals(result.getLemma()))
                            .map(item -> WordPair.of(item.getWordPair().getRightWord().toString(), result.getGrammaticalForm()))
                            .distinct()
                            .map(wordPair -> {
                                if (printedWordPairCounter.get() < 5) {
                                    LOGGER.info("        - Word pair: {}", wordPair);
                                    printedWordPairCounter.set(counter -> counter + 1);
                                }
                                ASTRATransformationEngineConfiguration astraTransformationEngineConfiguration = new ASTRATransformationEngineConfiguration.Builder()
                                        .searcherType(SearcherType.SEQUENTIAL)
                                        .build();
                                ASTRATransformationEngine astraTransformationEngine = new ASTRATransformationEngine(true, AffixType.of("AFF"), astraTransformationEngineConfiguration);
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
                .collect(toSet());
        return new ProcessResult(semanticRelations, affixTypesToProcess, items);
    }

    private static AnnotationTokenizerResult convertBack(AnnotationTokenizerResult annotationTokenizerResult, Set<AffixType> affixTypes, Map<AffixType, AffixType> affixTypeConversionMap) {
        annotationTokenizerResult = convertBackConversion(annotationTokenizerResult, affixTypeConversionMap);
        return convertBackCombinedAffixTypes(annotationTokenizerResult, affixTypes);
    }

    private static AnnotationTokenizerResult convertBackCombinedAffixTypes(AnnotationTokenizerResult annotationTokenizerResult, Set<AffixType> affixTypes) {
        if (affixTypeCombinationAffixes == null) {
            affixTypeCombinationAffixes = IntStream.range(2, affixTypes.size() + 1)
                    .mapToObj(i -> {
                        return Generator.combination(affixTypes)
                                .simple(i)
                                .stream()
                                .map(HashSet::new)
                                .flatMap(combination -> {
                                    return Generator.permutation(combination)
                                            .simple()
                                            .stream()
                                            .map(affixTypeList -> {
                                                return affixTypeList
                                                        .stream()
                                                        .map(AffixType::toString)
                                                        .collect(joining());
                                            })
                                            .map(AffixType::of)
                                            .map(affixType -> new AffixTypeCombinationAffixType(affixType, combination));
                                })
                                .collect(toSet());
                    })
                    .flatMap(Collection::stream)
                    .collect(toSet());
        }

        List<AffixType> originalAffixTypes = annotationTokenizerResult.getAffixTypes();
        boolean shouldConvert = originalAffixTypes
                .stream()
                .anyMatch(originalAffixType -> {
                    return affixTypeCombinationAffixes
                            .stream()
                            .map(AffixTypeCombinationAffixType::getCombinedAffixType)
                            .collect(toSet())
                            .contains(originalAffixType);
                });

        if (!shouldConvert) {
            return annotationTokenizerResult;
        }

        List<Set<AffixType>> convertedAffixTypeSets = originalAffixTypes
                .stream()
                .map(originalAffixType -> {
                    return affixTypeCombinationAffixes
                            .stream()
                            .filter(affixTypeCombinationAffixType -> affixTypeCombinationAffixType.getCombinedAffixType().equals(originalAffixType))
                            .findFirst()
                            .map(AffixTypeCombinationAffixType::getPrimitiveAffixTypes)
                            .orElse(Set.of(originalAffixType));
                })
                .collect(toList());
        List<AffixType> convertedAffixTypes = new ArrayList<>();
        for (Set<AffixType> affixTypeSet : convertedAffixTypeSets) {
            convertedAffixTypes.addAll(affixTypeSet);
        }

        AnnotationTokenizerResult convertedResult = new AnnotationTokenizerResult(annotationTokenizerResult.getExpression(), annotationTokenizerResult.getGrammaticalForm(), annotationTokenizerResult.getLemma(), annotationTokenizerResult.getFrequency());
        convertedResult.getAffixTypes().addAll(convertedAffixTypes);
        return convertedResult;
    }

    private static AnnotationTokenizerResult convertBackConversion(AnnotationTokenizerResult annotationTokenizerResult, Map<AffixType, AffixType> affixTypeConversionMap) {
        if (affixTypeConversionMap == null) {
            return annotationTokenizerResult;
        }

        List<AffixType> affixTypes = annotationTokenizerResult.getAffixTypes();
        List<AffixType> convertedAffixTypes = new ArrayList<>(affixTypes);

        if (affixTypeConversionMap.size() > 1) {
            throw new IllegalStateException("Cannot handle multiple conversions");
        }

        AffixType from = affixTypeConversionMap.keySet().iterator().next();
        AffixType to = affixTypeConversionMap.values().iterator().next();

        for (int i = 0; i < convertedAffixTypes.size(); i++) {
            if (convertedAffixTypes.get(i).equals(from)) {
                convertedAffixTypes.set(i, to);
            }
        }

        if (affixTypes.equals(convertedAffixTypes)) {
            return annotationTokenizerResult;
        }

        AnnotationTokenizerResult convertedAnnotationTokenResult = new AnnotationTokenizerResult(annotationTokenizerResult.getExpression(), annotationTokenizerResult.getGrammaticalForm(), annotationTokenizerResult.getLemma(), annotationTokenizerResult.getFrequency());
        convertedAnnotationTokenResult.getAffixTypes().addAll(convertedAffixTypes);
        return convertedAnnotationTokenResult;
    }

    public static class TestInput {

        private final Map<AffixType, AffixType> affixTypeConversionMap;
        private final Set<AffixType> affixTypes;

        private TestInput(Map<AffixType, AffixType> affixTypeConversionMap, AffixType... affixTypes) {
            this.affixTypeConversionMap = affixTypeConversionMap;
            this.affixTypes = Set.of(affixTypes);
        }

        public Map<AffixType, AffixType> getAffixTypeConversionMap() {
            return affixTypeConversionMap;
        }

        public Set<AffixType> getAffixTypes() {
            return affixTypes;
        }

        @Override
        public String toString() {
            return "TestInput{" +
                    "affixTypeConversionMap=" + affixTypeConversionMap +
                    ", affixTypes=" + affixTypes +
                    '}';
        }

    }

    public static class Item {

        private final AnnotationTokenizerResult annotationTokenizerResult;
        private final WordPair wordPair;
        private final List<AtomicRule> atomicRules;

        public Item(AnnotationTokenizerResult annotationTokenizerResult, WordPair wordPair, List<AtomicRule> atomicRules) {
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

    public static class ProcessResult implements Comparable<ProcessResult> {

        private final Set<AffixType> semanticRelations;
        private final List<AffixType> affixTypes;
        private final Set<Item> items;
        private Set<AtomicRule> atomicRules;
        private long minimalDescriptionLength;

        public ProcessResult(Set<AffixType> semanticRelations, List<AffixType> affixTypes, Set<Item> items) {
            this.semanticRelations = semanticRelations;
            this.affixTypes = affixTypes;
            this.items = items;
            this.atomicRules = null;
            this.minimalDescriptionLength = -1;
        }

        public Set<AffixType> getSemanticRelations() {
            return semanticRelations;
        }

        public List<AffixType> getAffixTypes() {
            return affixTypes;
        }

        public Set<Item> getItems() {
            return items;
        }

        public Set<AtomicRule> getAtomicRules() {
            return atomicRules;
        }

        public long getMinimalDescriptionLength() {
            return minimalDescriptionLength;
        }

        @Override
        public int compareTo(ProcessResult other) {
            return Long.compare(this.minimalDescriptionLength, other.minimalDescriptionLength);
        }

        public void removeParentAtomicRules(ProcessResult processResultToRemove, AffixType missingAffixType) {
            if (this.atomicRules != null) {
                throw new IllegalStateException("Atomic rules already calculated for " + this.affixTypes + ", has size of " + this.atomicRules.size());
            }
            if (this.minimalDescriptionLength >= 0) {
                throw new IllegalStateException("Minimal description length already calculated for " + this.affixTypes + ": " + this.minimalDescriptionLength);
            }

            if (processResultToRemove == null) {
                LOGGER.info("    - Cannot remove process result for {}, as it was not winning", missingAffixType);
                return;
            }

            Set<AtomicRule> parentAtomicRules = processResultToRemove.getItems()
                    .stream()
                    .map(Item::getAtomicRules)
                    .flatMap(Collection::stream)
                    .collect(toSet());

            List<AffixType> parentAffixTypes = processResultToRemove.getAffixTypes();
            long originalAtomicRuleCount = this.items
                    .stream()
                    .map(Item::getAtomicRules)
                    .mapToLong(Collection::size)
                    .sum();
            this.items.forEach(item -> item.getAtomicRules().removeAll(parentAtomicRules));
            long newAtomicRuleCount = this.items
                    .stream()
                    .map(Item::getAtomicRules)
                    .mapToLong(Collection::size)
                    .sum();
            LOGGER.info("    - Removing parent atomic rules {} from {} --> Number of atomic rules reduced from {} to {}", parentAffixTypes, this.affixTypes, originalAtomicRuleCount, newAtomicRuleCount);
        }

        public void reduce() {
            if (this.atomicRules != null) {
                throw new IllegalStateException("Atomic rules already calculated for " + this.affixTypes + ", has size of " + this.atomicRules.size());
            }
            if (this.minimalDescriptionLength >= 0) {
                throw new IllegalStateException("Minimal description length already calculated for " + this.affixTypes + ": " + this.minimalDescriptionLength);
            }

            this.atomicRules = this.items
                    .stream()
                    .map(item -> {
                        List<AtomicRule> atomicRules = new ArrayList<>(item.getAtomicRules());

                        while (!atomicRules.isEmpty()) {
                            AtomicRule atomicRule = atomicRules.get(0);
                            boolean removable = this.items
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
            long originalAtomicRuleCount = this.items
                    .stream()
                    .map(Item::getAtomicRules)
                    .mapToLong(Collection::size)
                    .sum();
            this.minimalDescriptionLength = this.atomicRules
                    .stream()
                    .mapToLong(atomicRule -> atomicRule.getContext().length() + atomicRule.getReplacementString().length())
                    .sum();
            LOGGER.info("    - Reducing {} --> Number of atomic rules reduced from {} to {}, minimal description length: {}", this.affixTypes, originalAtomicRuleCount, this.atomicRules.size(), this.minimalDescriptionLength);
        }

        @Override
        public String toString() {
            return "ProcessResult{" +
                    "semanticRelations=" + semanticRelations +
                    ", affixTypes=" + affixTypes +
                    ", " + items.size() + " items" +
                    ", " + (atomicRules == null ? 0 : atomicRules.size()) + " atomic rules" +
                    ", minimalDescriptionLength=" + minimalDescriptionLength +
                    '}';
        }

    }

    public static class Wrapper<T> {

        private T value;

        public Wrapper(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

        public void set(Function<T, T> nextValueCalculator) {
            this.value = nextValueCalculator.apply(this.value);
        }

    }

    public static class AffixTypeCombinationAffixType {

        private final AffixType combinedAffixType;
        private final Set<AffixType> primitiveAffixTypes;

        public AffixTypeCombinationAffixType(AffixType combinedAffixType, Set<AffixType> primitiveAffixTypes) {
            this.combinedAffixType = combinedAffixType;
            this.primitiveAffixTypes = primitiveAffixTypes;
        }

        public AffixType getCombinedAffixType() {
            return combinedAffixType;
        }

        public Set<AffixType> getPrimitiveAffixTypes() {
            return primitiveAffixTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AffixTypeCombinationAffixType that = (AffixTypeCombinationAffixType) o;
            return Objects.equals(combinedAffixType, that.combinedAffixType) &&
                    Objects.equals(primitiveAffixTypes, that.primitiveAffixTypes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(combinedAffixType, primitiveAffixTypes);
        }

        @Override
        public String toString() {
            return "AffixTypeCombinationAffixType{" +
                    "combinedAffixType=" + combinedAffixType +
                    ", primitiveAffixTypes=" + primitiveAffixTypes +
                    '}';
        }

    }

}
