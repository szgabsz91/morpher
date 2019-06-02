package com.github.szgabsz91.morpher.methods.astra.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.methods.astra.impl.ASTRA;
import com.github.szgabsz91.morpher.methods.astra.impl.IASTRA;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.atomicrule.DefaultAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.atomicrule.IAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.segment.DefaultSegmentFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.segment.ISegmentFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.method.ASTRAMethod;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.ISearcher;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.SequentialSearcher;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.ASTRAMethodMessage;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.SearcherTypeMessage;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IdentityWordConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ASTRAMethodConverterTest {

    private ASTRAMethodConverter converter;
    private IAtomicRuleFitnessCalculator atomicRuleFitnessCalculator;

    @BeforeEach
    public void setUp() {
        this.converter = new ASTRAMethodConverter();

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
        ASTRAMethod astraMethod = new ASTRAMethod(astra);

        ASTRAMethodMessage astraMethodMessage = this.converter.convert(astraMethod);
        assertThat(astraMethodMessage.getAstra().getAffixType()).isEqualTo(affixType.toString());
        assertThat(astraMethodMessage.getAstra().getSearcher().getType()).isEqualTo(SearcherTypeMessage.SEQUENTIAL);

        ASTRAMethod result = this.converter.convertBack(astraMethodMessage);
        assertThat(result.getAstra().getAffixType()).isEqualTo(affixType);
        assertThat(result.getAstra().getSearcher()).isInstanceOf(SequentialSearcher.class);

        Path file = Files.createTempFile("morpher", "astra");
        try {
            Serializer<ASTRAMethod, ASTRAMethodMessage> serializer = new Serializer<>(this.converter, astraMethod);
            serializer.serialize(astraMethod, file);
            ASTRAMethodMessage resultingASTRAMethodMessage = this.converter.parse(file);
            assertThat(resultingASTRAMethodMessage).isEqualTo(astraMethodMessage);
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
        ASTRAMethod astraMethod = new ASTRAMethod(astra);

        ASTRAMethodMessage astraMethodMessage = this.converter.convert(astraMethod);
        assertThat(astraMethodMessage.getAstra().getAffixType()).isEqualTo(affixType.toString());
        assertThat(astraMethodMessage.getAstra().getSearcher().getType()).isEqualTo(SearcherTypeMessage.SEQUENTIAL);

        ASTRAMethod result = this.converter.convertBack(astraMethodMessage);
        assertThat(result.getAstra().getAffixType()).isEqualTo(affixType);
        assertThat(result.getAstra().getSearcher()).isInstanceOf(SequentialSearcher.class);

        Path file = Files.createTempFile("morpher", "astra");
        try {
            Serializer<ASTRAMethod, ASTRAMethodMessage> serializer = new Serializer<>(this.converter, astraMethod);
            serializer.serialize(astraMethod, file);
            ASTRAMethodMessage resultingASTRAMethodMessage = this.converter.parse(file);
            assertThat(resultingASTRAMethodMessage).isEqualTo(astraMethodMessage);
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
        ASTRAMethod astraMethod = new ASTRAMethod(astra);

        ASTRAMethodMessage astraMethodMessage = this.converter.convert(astraMethod);
        assertThat(astraMethodMessage.getAstra().getAffixType()).isEqualTo(affixType.toString());
        assertThat(astraMethodMessage.getAstra().getSearcher().getType()).isEqualTo(SearcherTypeMessage.SEQUENTIAL);

        ASTRAMethod result = this.converter.convertBack(astraMethodMessage);
        assertThat(result.getAstra().getAffixType()).isEqualTo(affixType);
        assertThat(result.getAstra().getSearcher()).isInstanceOf(SequentialSearcher.class);

        Path file = Files.createTempFile("morpher", "astra");
        try {
            Serializer<ASTRAMethod, ASTRAMethodMessage> serializer = new Serializer<>(this.converter, astraMethod);
            serializer.serialize(astraMethod, file);
            ASTRAMethodMessage resultingASTRAMethodMessage = this.converter.parse(file);
            assertThat(resultingASTRAMethodMessage).isEqualTo(astraMethodMessage);
        }
        finally {
            Files.delete(file);
        }
    }

}
