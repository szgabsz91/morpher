package com.github.szgabsz91.morpher.engines.impl.impl.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.languagehandlers.api.model.ProbabilisticAffixType;
import com.github.szgabsz91.morpher.transformationengines.api.model.ProbabilisticWord;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StepCandidateTest {

    @Test
    public void testCompleteConstructorAndGettersWithChildOfRoot() {
        ProbabilisticAffixType probabilisticAffixType = ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5);
        Word input = Word.of("input");
        ProbabilisticWord output = ProbabilisticWord.of(Word.of("output"), 0.5);
        StepCandidate parent = new StepCandidate(null, null, null);
        StepCandidate stepCandidate = new StepCandidate(probabilisticAffixType, input, parent, output);
        assertThat(stepCandidate.getProbabilisticAffixType()).isEqualTo(probabilisticAffixType);
        assertThat(stepCandidate.getAffixType()).isEqualTo(probabilisticAffixType.getAffixType());
        assertThat(stepCandidate.getProbability()).isEqualTo(probabilisticAffixType.getProbability());
        assertThat(stepCandidate.getInput()).isEqualTo(input);
        assertThat(stepCandidate.getLevel()).isEqualTo(1);
        assertThat(stepCandidate.getParent()).isSameAs(parent);
        assertThat(stepCandidate.getOutput()).isSameAs(output);
    }

    @Test
    public void testCompleteConstructorAndGettersWithChildOfNonRoot() {
        ProbabilisticAffixType probabilisticAffixType = ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5);
        Word input = Word.of("input");
        ProbabilisticWord output = ProbabilisticWord.of(Word.of("output"), 0.5);
        StepCandidate parent = new StepCandidate(null, null, new StepCandidate(null, null));
        StepCandidate stepCandidate = new StepCandidate(probabilisticAffixType, input, parent, output);
        assertThat(stepCandidate.getProbabilisticAffixType()).isEqualTo(probabilisticAffixType);
        assertThat(stepCandidate.getAffixType()).isEqualTo(probabilisticAffixType.getAffixType());
        assertThat(stepCandidate.getProbability()).isEqualTo(probabilisticAffixType.getProbability());
        assertThat(stepCandidate.getInput()).isEqualTo(input);
        assertThat(stepCandidate.getLevel()).isEqualTo(2);
        assertThat(stepCandidate.getParent()).isSameAs(parent);
        assertThat(stepCandidate.getOutput()).isSameAs(output);
    }

    @Test
    public void testMiddleConstructorAndGetters() {
        ProbabilisticAffixType probabilisticAffixType = ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5);
        Word input = Word.of("input");
        StepCandidate parent = new StepCandidate(null, null, null);
        StepCandidate stepCandidate = new StepCandidate(probabilisticAffixType, input, parent);
        assertThat(stepCandidate.getProbabilisticAffixType()).isEqualTo(probabilisticAffixType);
        assertThat(stepCandidate.getAffixType()).isEqualTo(probabilisticAffixType.getAffixType());
        assertThat(stepCandidate.getProbability()).isEqualTo(probabilisticAffixType.getProbability());
        assertThat(stepCandidate.getInput()).isEqualTo(input);
        assertThat(stepCandidate.getLevel()).isEqualTo(1);
        assertThat(stepCandidate.getParent()).isSameAs(parent);
        assertThat(stepCandidate.getOutput()).isNull();
    }

    @Test
    public void testSimpleConstructorAndGetters() {
        ProbabilisticAffixType probabilisticAffixType = ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5);
        Word input = Word.of("input");
        StepCandidate stepCandidate = new StepCandidate(probabilisticAffixType, input);
        assertThat(stepCandidate.getProbabilisticAffixType()).isEqualTo(probabilisticAffixType);
        assertThat(stepCandidate.getAffixType()).isEqualTo(probabilisticAffixType.getAffixType());
        assertThat(stepCandidate.getProbability()).isEqualTo(probabilisticAffixType.getProbability());
        assertThat(stepCandidate.getInput()).isEqualTo(input);
        assertThat(stepCandidate.getLevel()).isZero();
        assertThat(stepCandidate.getParent()).isNull();
        assertThat(stepCandidate.getOutput()).isNull();
    }

    @Test
    public void testCopyConstructorWithExistingParent() {
        ProbabilisticAffixType probabilisticAffixType = ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5);
        Word input = Word.of("input");
        StepCandidate parent = new StepCandidate(probabilisticAffixType, Word.of("x"), null);
        StepCandidate stepCandidate = new StepCandidate(probabilisticAffixType, input, parent);
        StepCandidate result = new StepCandidate(stepCandidate);
        assertThat(result.getProbabilisticAffixType()).isEqualTo(stepCandidate.getProbabilisticAffixType());
        assertThat(result.getAffixType()).isEqualTo(stepCandidate.getAffixType());
        assertThat(result.getProbability()).isEqualTo(stepCandidate.getProbability());
        assertThat(result.getInput()).isEqualTo(stepCandidate.getInput());
        assertThat(result.getLevel()).isEqualTo(stepCandidate.getLevel());
        assertThat(result.getParent()).isEqualTo(stepCandidate.getParent());
        assertThat(result.getOutput()).isEqualTo(stepCandidate.getOutput());
    }

    @Test
    public void testCopyConstructorWithNonExistentParent() {
        ProbabilisticAffixType probabilisticAffixType = ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5);
        Word input = Word.of("input");
        StepCandidate stepCandidate = new StepCandidate(probabilisticAffixType, input, null);
        StepCandidate result = new StepCandidate(stepCandidate);
        assertThat(result.getProbabilisticAffixType()).isEqualTo(stepCandidate.getProbabilisticAffixType());
        assertThat(result.getAffixType()).isEqualTo(stepCandidate.getAffixType());
        assertThat(result.getProbability()).isEqualTo(stepCandidate.getProbability());
        assertThat(result.getInput()).isEqualTo(stepCandidate.getInput());
        assertThat(result.getLevel()).isEqualTo(stepCandidate.getLevel());
        assertThat(result.getParent()).isNull();
        assertThat(result.getOutput()).isEqualTo(stepCandidate.getOutput());
    }

    @Test
    public void testSetOutputAndGetOutput() {
        ProbabilisticWord output = ProbabilisticWord.of(Word.of("output"), 0.5);
        StepCandidate stepCandidate = new StepCandidate(null, null);
        stepCandidate.setOutput(output);
        assertThat(stepCandidate.getOutput()).isEqualTo(output);
    }

}
