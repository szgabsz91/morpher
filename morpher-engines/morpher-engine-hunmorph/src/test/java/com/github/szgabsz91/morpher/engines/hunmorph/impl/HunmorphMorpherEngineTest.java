package com.github.szgabsz91.morpher.engines.hunmorph.impl;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Corpus;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.engines.api.model.AnalysisInput;
import com.github.szgabsz91.morpher.engines.api.model.InflectionInput;
import com.github.szgabsz91.morpher.engines.api.model.InflectionOrderedInput;
import com.github.szgabsz91.morpher.engines.api.model.Mode;
import com.github.szgabsz91.morpher.engines.api.model.MorpherEngineResponse;
import com.github.szgabsz91.morpher.engines.api.model.PreanalyzedTrainingItems;
import com.github.szgabsz91.morpher.engines.api.model.ProbabilisticStep;
import com.github.szgabsz91.morpher.engines.hunmorph.HunmorphMorpherEngine;
import com.github.szgabsz91.morpher.languagehandlers.api.model.LemmaMap;
import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessageV3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HunmorphMorpherEngineTest {

    private HunmorphMorpherEngine engine;

    @BeforeEach
    public void setUp() {
        this.engine = new HunmorphMorpherEngine(false);
    }

    @AfterEach
    public void tearDown() {
        this.engine.close();
    }

    @Test
    public void testIsEager() {
        boolean result = this.engine.isEager();
        assertThat(result).isTrue();
    }

    @Test
    public void testIsLazy() {
        boolean result = this.engine.isLazy();
        assertThat(result).isFalse();
    }

    @Test
    public void testLearnWithCorpus() {
        this.engine.learn(Corpus.of(Word.of("almát")));
    }

    @Test
    public void testLearnWithPreanalyzedTrainingItems() {
        this.engine.learn(PreanalyzedTrainingItems.of(Set.of()));
    }

    @Test
    public void testLearnWithLemmaMap() {
        this.engine.learn(LemmaMap.of(Map.of()));
    }

    @Test
    public void testInflectWithUnorderedInflectionInput() {
        InflectionInput inflectionInput = new InflectionInput(Word.of("input"), Set.of());
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.engine.inflect(inflectionInput));
        assertThat(exception).hasMessage("Inflection is not supported by HunmorphMorpherEngine");
    }

    @Test
    public void testInflectWithOrderedInflectionInput() {
        InflectionOrderedInput inflectionOrderedInput = new InflectionOrderedInput(Word.of("input"), List.of());
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.engine.inflect(inflectionOrderedInput));
        assertThat(exception).hasMessage("Inflection is not supported by HunmorphMorpherEngine");
    }

    @Test
    public void testAnalyze() {
        Word input = Word.of("almákat");
        AnalysisInput analysisInput = AnalysisInput.of(input);
        List<MorpherEngineResponse> response = this.engine.analyze(analysisInput);
        assertThat(response).hasSize(1);
        MorpherEngineResponse morpherEngineResponse = response.get(0);
        assertThat(morpherEngineResponse.getMode()).isEqualTo(Mode.ANALYSIS);
        assertThat(morpherEngineResponse.getInput()).isEqualTo(input);
        assertThat(morpherEngineResponse.getOutput()).hasToString("alma");
        assertThat(morpherEngineResponse.getAffixTypeChainProbability()).isOne();
        assertThat(morpherEngineResponse.getPos().getAffixType()).isEqualTo(AffixType.of("/NOUN"));
        assertThat(morpherEngineResponse.getPos().getProbability()).isOne();
        assertThat(morpherEngineResponse.getSteps()).containsExactly(
                new ProbabilisticStep(input, input, AffixType.of("<CAS<ACC>>"), 1.0, 1.0, 1.0),
                new ProbabilisticStep(input, Word.of("alma"), AffixType.of("<PLUR>"), 1.0, 1.0, 1.0)
        );
        assertThat(morpherEngineResponse.getAggregatedWeight()).isOne();
    }

    @Test
    public void testAnalyzeWithGuessMode() {
        Word input = Word.of("habablát");
        AnalysisInput analysisInput = AnalysisInput.of(input);
        HunmorphMorpherEngine hunmorphMorpherEngine = new HunmorphMorpherEngine(true);
        List<MorpherEngineResponse> responses = hunmorphMorpherEngine.analyze(analysisInput);
        assertThat(responses).hasSize(4);
        List<String> outputs = responses
                .stream()
                .map(MorpherEngineResponse::getOutput)
                .map(Word::toString)
                .collect(toList());
        assertThat(outputs).containsExactlyInAnyOrder("hababl", "hababla", "habablá", "habablát");
    }

    @Test
    public void testGetSupportedAffixTypes() {
        List<AffixType> affixTypes = this.engine.getSupportedAffixTypes();
        assertThat(affixTypes).hasSize(324);
    }

    @Test
    public void testIsDirty() {
        boolean dirty = this.engine.isDirty();
        assertThat(dirty).isFalse();
    }

    @Test
    public void testClean() {
        this.engine.clean();
    }

    @Test
    public void testToMessage() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.engine.toMessage());
        assertThat(exception).hasMessage("Saving is not supported by HunmorphMorpherEngine");
    }

    @Test
    public void testFromMessageWithGeneratedMessageV3() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.engine.fromMessage((GeneratedMessageV3) null));
        assertThat(exception).hasMessage("Loading is not supported by HunmorphMorpherEngine");
    }

    @Test
    public void testFromMessageWithAny() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.engine.fromMessage((Any) null));
        assertThat(exception).hasMessage("Loading is not supported by HunmorphMorpherEngine");
    }

    @Test
    public void testSaveTo() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.engine.saveTo(null));
        assertThat(exception).hasMessage("Saving is not supported by HunmorphMorpherEngine");
    }

    @Test
    public void testLoadFrom() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.engine.loadFrom(null));
        assertThat(exception).hasMessage("Loading is not supported by HunmorphMorpherEngine");
    }

}
