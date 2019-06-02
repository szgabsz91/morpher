package com.github.szgabsz91.morpher.methods.astra.impl.testutils;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.methods.astra.config.ASTRAMethodConfiguration;
import com.github.szgabsz91.morpher.methods.astra.impl.ASTRA;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.atomicrule.DefaultAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.atomicrule.IAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.segment.DefaultSegmentFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.segment.ISegmentFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.ISearcher;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.SequentialSearcher;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IdentityWordConverter;

public class ASTRABuilder {

    public static final IWordConverter DEFAULT_WORD_CONVERTER = new IdentityWordConverter();
    public static final ICharacterRepository DEFAULT_CHARACTER_REPOSITORY = HungarianSimpleCharacterRepository.get();
    public static final IAtomicRuleFitnessCalculator DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR = new DefaultAtomicRuleFitnessCalculator(
            DEFAULT_WORD_CONVERTER,
            DEFAULT_CHARACTER_REPOSITORY,
            2
    );
    public static final ISearcher DEFAULT_SEARCHER = new SequentialSearcher(DEFAULT_ATOMIC_RULE_FITNESS_CALCULATOR, false);
    public static final ISegmentFitnessCalculator DEFAULT_SEGMENT_FITNESS_CALCULATOR = new DefaultSegmentFitnessCalculator();
    public static final int DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH = 2;
    public static final double DEFAULT_FITNESS_THRESHOLD = ASTRAMethodConfiguration.Builder.DEFAULT_FITNESS_THRESHOLD;
    public static final int DEFAULT_MAXIMUM_NUMBER_OF_RESPONSES = ASTRAMethodConfiguration.Builder.DEFAULT_MAXIMUM_NUMBER_OF_RESPONSES;

    private AffixType affixType;
    private IWordConverter wordConverter;
    private ICharacterRepository characterRepository;
    private ISearcher searcher;
    private ISegmentFitnessCalculator segmentFitnessCalculator;
    private int minimalMatchingSegmentLength;
    private double fitnessThreshold;
    private int maximumNumberOfResponses;
    private Integer minimumSupportThreshold;
    private Integer minimumWordFrequencyThreshold;
    private Integer minimumAggregatedSupportThreshold;
    private Integer minimumContextLength;
    private Integer maximumNumberOfGeneratedAtomicRules;
    private Double maximumResponseProbabilityDifferenceThreshold;

    public ASTRABuilder() {
        this.wordConverter = DEFAULT_WORD_CONVERTER;
        this.characterRepository = DEFAULT_CHARACTER_REPOSITORY;
        this.searcher = DEFAULT_SEARCHER;
        this.segmentFitnessCalculator = DEFAULT_SEGMENT_FITNESS_CALCULATOR;
        this.minimalMatchingSegmentLength = DEFAULT_MINIMAL_MATCHING_SEGMENT_LENGTH;
        this.fitnessThreshold = DEFAULT_FITNESS_THRESHOLD;
        this.maximumNumberOfResponses = DEFAULT_MAXIMUM_NUMBER_OF_RESPONSES;
    }

    public ASTRABuilder affixType(final AffixType affixType) {
        this.affixType = affixType;
        return this;
    }

    public ASTRABuilder wordConverter(final IWordConverter wordConverter) {
        this.wordConverter = wordConverter;
        return this;
    }

    public ASTRABuilder characterRepository(final ICharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
        return this;
    }

    public ASTRABuilder searcher(final ISearcher searcher) {
        this.searcher = searcher;
        return this;
    }

    public ASTRABuilder segmentFitnessCalculator(final ISegmentFitnessCalculator segmentFitnessCalculator) {
        this.segmentFitnessCalculator = segmentFitnessCalculator;
        return this;
    }

    public ASTRABuilder minimalMatchingSegmentLength(final int minimalMatchingSegmentLength) {
        this.minimalMatchingSegmentLength = minimalMatchingSegmentLength;
        return this;
    }

    public ASTRABuilder fitnessThreshold(final double fitnessThreshold) {
        this.fitnessThreshold = fitnessThreshold;
        return this;
    }

    public ASTRABuilder maximumNumberOfResponses(final int maximumNumberOfResponses) {
        this.maximumNumberOfResponses = maximumNumberOfResponses;
        return this;
    }

    public ASTRABuilder minimumSupportThreshold(final int minimumSupportThreshold) {
        this.minimumSupportThreshold = minimumSupportThreshold;
        return this;
    }

    public ASTRABuilder minimumWordFrequencyThreshold(final int minimumWordFrequencyThreshold) {
        this.minimumWordFrequencyThreshold = minimumWordFrequencyThreshold;
        return this;
    }

    public ASTRABuilder minimumAggregatedSupportThreshold(final int minimumAggregatedSupportThreshold) {
        this.minimumAggregatedSupportThreshold = minimumAggregatedSupportThreshold;
        return this;
    }

    public ASTRABuilder minimumContextLength(final int minimumContextLength) {
        this.minimumContextLength = minimumContextLength;
        return this;
    }

    public ASTRABuilder maximumNumberOfGeneratedAtomicRules(final int maximumNumberOfGeneratedAtomicRules) {
        this.maximumNumberOfGeneratedAtomicRules = maximumNumberOfGeneratedAtomicRules;
        return this;
    }

    public ASTRABuilder maximumResponseProbabilityDifferenceThreshold(final Double maximumResponseProbabilityDifferenceThreshold) {
        this.maximumResponseProbabilityDifferenceThreshold = maximumResponseProbabilityDifferenceThreshold;
        return this;
    }

    public ASTRA build() {
        if (this.affixType == null) {
            throw new IllegalStateException("The affixType property must not be null");
        }

        return new ASTRA(
                affixType,
                wordConverter,
                characterRepository,
                searcher,
                segmentFitnessCalculator,
                minimalMatchingSegmentLength,
                fitnessThreshold,
                maximumNumberOfResponses,
                minimumSupportThreshold,
                minimumWordFrequencyThreshold,
                minimumAggregatedSupportThreshold,
                minimumContextLength,
                maximumNumberOfGeneratedAtomicRules,
                maximumResponseProbabilityDifferenceThreshold
        );
    }

}
