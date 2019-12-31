package com.github.szgabsz91.morpher.transformationengines.astra.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IWordConverter;
import com.github.szgabsz91.morpher.transformationengines.api.wordconverters.IdentityWordConverter;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.ASTRA;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.IASTRA;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.fitnesscalculators.atomicrule.DefaultAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.fitnesscalculators.atomicrule.IAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.fitnesscalculators.segment.DefaultSegmentFitnessCalculator;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.fitnesscalculators.segment.ISegmentFitnessCalculator;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.searchers.ISearcher;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.searchers.SequentialSearcher;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.transformationengine.ASTRATransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.astra.protocolbuffers.ASTRATransformationEngineMessage;
import com.github.szgabsz91.morpher.transformationengines.astra.protocolbuffers.SearcherTypeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ASTRATransformationEngineConverterTest {

    private ASTRATransformationEngineConverter converter;
    private IAtomicRuleFitnessCalculator atomicRuleFitnessCalculator;

    @BeforeEach
    public void setUp() {
        this.converter = new ASTRATransformationEngineConverter();

        final IWordConverter wordConverter = new IdentityWordConverter();
        final ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        final int averageSimilarityExponent = 2;
        this.atomicRuleFitnessCalculator = new DefaultAtomicRuleFitnessCalculator(
                wordConverter,
                characterRepository,
                averageSimilarityExponent
        );
    }

    @Test
    public void testConvertAndConvertBackAndParseWithoutMinimumContextLength() throws IOException {
        AffixType affixType = AffixType.of("AFF");
        final IWordConverter wordConverter = new IdentityWordConverter();
        final ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        ISearcher searcher = new SequentialSearcher(this.atomicRuleFitnessCalculator, true);
        ISegmentFitnessCalculator segmentFitnessCalculator = new DefaultSegmentFitnessCalculator();
        int minimalMatchingSegmentLength = 2;
        double fitnessThreshold = 0.5;
        int maximumNumberOfResponses = 1;
        IASTRA astra = new ASTRA(
                affixType,
                wordConverter,
                characterRepository,
                searcher,
                segmentFitnessCalculator,
                minimalMatchingSegmentLength,
                fitnessThreshold,
                maximumNumberOfResponses,
                null,
                null,
                null,
                null,
                null,
                null
        );
        ASTRATransformationEngine astraTransformationEngine = new ASTRATransformationEngine(astra);

        ASTRATransformationEngineMessage astraTransformationEngineMessage = this.converter.convert(astraTransformationEngine);
        assertThat(astraTransformationEngineMessage.getAstra().getAffixType()).isEqualTo(affixType.toString());
        assertThat(astraTransformationEngineMessage.getAstra().getSearcher().getType()).isEqualTo(SearcherTypeMessage.SEQUENTIAL);

        ASTRATransformationEngine result = this.converter.convertBack(astraTransformationEngineMessage);
        assertThat(result.getAstra().getAffixType()).isEqualTo(affixType);
        assertThat(result.getAstra().getSearcher()).isInstanceOf(SequentialSearcher.class);

        Path file = Files.createTempFile("transformation-engine", "astra");
        try {
            Serializer<ASTRATransformationEngine, ASTRATransformationEngineMessage> serializer = new Serializer<>(this.converter, astraTransformationEngine);
            serializer.serialize(astraTransformationEngine, file);
            ASTRATransformationEngineMessage resultingASTRATransformationEngineMessage = this.converter.parse(file);
            assertThat(resultingASTRATransformationEngineMessage).isEqualTo(astraTransformationEngineMessage);
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testConvertAndConvertBackAndParseWithMinimumContextLength() throws IOException {
        AffixType affixType = AffixType.of("AFF");
        final IWordConverter wordConverter = new IdentityWordConverter();
        final ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        ISearcher searcher = new SequentialSearcher(this.atomicRuleFitnessCalculator, true);
        ISegmentFitnessCalculator segmentFitnessCalculator = new DefaultSegmentFitnessCalculator();
        int minimalMatchingSegmentLength = 2;
        double fitnessThreshold = 0.5;
        int maximumNumberOfResponses = 1;
        int minimumContextLength = 3;
        int maximumNumberOfGeneratedAtomicRules = 1;
        IASTRA astra = new ASTRA(
                affixType,
                wordConverter,
                characterRepository,
                searcher,
                segmentFitnessCalculator,
                minimalMatchingSegmentLength,
                fitnessThreshold,
                maximumNumberOfResponses,
                null,
                null,
                null,
                minimumContextLength,
                maximumNumberOfGeneratedAtomicRules,
                null
        );
        ASTRATransformationEngine astraTransformationEngine = new ASTRATransformationEngine(astra);

        ASTRATransformationEngineMessage astraTransformationEngineMessage = this.converter.convert(astraTransformationEngine);
        assertThat(astraTransformationEngineMessage.getAstra().getAffixType()).isEqualTo(affixType.toString());
        assertThat(astraTransformationEngineMessage.getAstra().getSearcher().getType()).isEqualTo(SearcherTypeMessage.SEQUENTIAL);

        ASTRATransformationEngine result = this.converter.convertBack(astraTransformationEngineMessage);
        assertThat(result.getAstra().getAffixType()).isEqualTo(affixType);
        assertThat(result.getAstra().getSearcher()).isInstanceOf(SequentialSearcher.class);

        Path file = Files.createTempFile("transformation-engine", "astra");
        try {
            Serializer<ASTRATransformationEngine, ASTRATransformationEngineMessage> serializer = new Serializer<>(this.converter, astraTransformationEngine);
            serializer.serialize(astraTransformationEngine, file);
            ASTRATransformationEngineMessage resultingASTRATransformationEngineMessage = this.converter.parse(file);
            assertThat(resultingASTRATransformationEngineMessage).isEqualTo(astraTransformationEngineMessage);
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testConvertAndConvertBackAndParseWithMinimumSupportAndWordFrequencyAndAggregatedSupportThreshold() throws IOException {
        AffixType affixType = AffixType.of("AFF");
        final IWordConverter wordConverter = new IdentityWordConverter();
        final ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        ISearcher searcher = new SequentialSearcher(this.atomicRuleFitnessCalculator, true);
        ISegmentFitnessCalculator segmentFitnessCalculator = new DefaultSegmentFitnessCalculator();
        int minimalMatchingSegmentLength = 2;
        double fitnessThreshold = 0.5;
        int maximumNumberOfResponses = 1;
        int minimumSupportThreshold = 3;
        int minimumWordFrequencyThreshold = 4;
        int minimumAggregatedSupportThreshold = 5;
        IASTRA astra = new ASTRA(
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
                null,
                null,
                null
        );
        ASTRATransformationEngine astraTransformationEngine = new ASTRATransformationEngine(astra);

        ASTRATransformationEngineMessage astraTransformationEngineMessage = this.converter.convert(astraTransformationEngine);
        assertThat(astraTransformationEngineMessage.getAstra().getAffixType()).isEqualTo(affixType.toString());
        assertThat(astraTransformationEngineMessage.getAstra().getSearcher().getType()).isEqualTo(SearcherTypeMessage.SEQUENTIAL);

        ASTRATransformationEngine result = this.converter.convertBack(astraTransformationEngineMessage);
        assertThat(result.getAstra().getAffixType()).isEqualTo(affixType);
        assertThat(result.getAstra().getSearcher()).isInstanceOf(SequentialSearcher.class);

        Path file = Files.createTempFile("transformation-engine", "astra");
        try {
            Serializer<ASTRATransformationEngine, ASTRATransformationEngineMessage> serializer = new Serializer<>(this.converter, astraTransformationEngine);
            serializer.serialize(astraTransformationEngine, file);
            ASTRATransformationEngineMessage resultingASTRATransformationEngineMessage = this.converter.parse(file);
            assertThat(resultingASTRATransformationEngineMessage).isEqualTo(astraTransformationEngineMessage);
        }
        finally {
            Files.delete(file);
        }
    }

}
