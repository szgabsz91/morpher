package com.github.szgabsz91.morpher.engines.impl.impl.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.analyzeragents.api.model.ProbabilisticAffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.methods.api.model.ProbabilisticWord;
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

    @Test
    public void testEquals() {
        StepCandidate stepCandidate1 = new StepCandidate(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), Word.of("input"), new StepCandidate(ProbabilisticAffixType.of(AffixType.of("CAS"), 0.5), Word.of("in")));
        stepCandidate1.setOutput(ProbabilisticWord.of(Word.of("output"), 0.5));
        StepCandidate stepCandidate2 = new StepCandidate(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), Word.of("input"), new StepCandidate(ProbabilisticAffixType.of(AffixType.of("CAS"), 0.5), Word.of("in"), new StepCandidate(null, Word.of("x"))));
        stepCandidate2.setOutput(ProbabilisticWord.of(Word.of("output"), 0.5));
        StepCandidate stepCandidate3 = new StepCandidate(ProbabilisticAffixType.of(AffixType.of("AFF2"), 0.5), Word.of("input"), new StepCandidate(ProbabilisticAffixType.of(AffixType.of("CAS"), 0.5), Word.of("in")));
        stepCandidate3.setOutput(ProbabilisticWord.of(Word.of("output"), 0.5));
        StepCandidate stepCandidate4 = new StepCandidate(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), Word.of("input2"), new StepCandidate(ProbabilisticAffixType.of(AffixType.of("CAS"), 0.5), Word.of("in")));
        stepCandidate4.setOutput(ProbabilisticWord.of(Word.of("output"), 0.5));
        StepCandidate stepCandidate5 = new StepCandidate(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), Word.of("input"), new StepCandidate(ProbabilisticAffixType.of(AffixType.of("CAS"), 0.5), Word.of("in2")));
        stepCandidate5.setOutput(ProbabilisticWord.of(Word.of("output"), 0.5));
        StepCandidate stepCandidate6 = new StepCandidate(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), Word.of("input"), new StepCandidate(ProbabilisticAffixType.of(AffixType.of("CAS"), 0.5), Word.of("in")));
        stepCandidate6.setOutput(ProbabilisticWord.of(Word.of("output2"), 0.5));
        StepCandidate stepCandidate7 = new StepCandidate(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), Word.of("input"), null);
        stepCandidate7.setOutput(ProbabilisticWord.of(Word.of("output"), 0.5));
        StepCandidate stepCandidate8 = new StepCandidate(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), Word.of("input"), new StepCandidate(ProbabilisticAffixType.of(AffixType.of("CAS"), 0.5), Word.of("in")));
        StepCandidate stepCandidate9 = new StepCandidate(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), Word.of("input"), new StepCandidate(ProbabilisticAffixType.of(AffixType.of("CAS"), 0.5), Word.of("in")));
        stepCandidate9.setOutput(ProbabilisticWord.of(Word.of("output"), 0.5));
        StepCandidate stepCandidate10 = new StepCandidate(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), Word.of("input"), null);
        stepCandidate10.setOutput(ProbabilisticWord.of(Word.of("output"), 0.5));
        StepCandidate stepCandidate11 = new StepCandidate(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5), Word.of("input"), new StepCandidate(ProbabilisticAffixType.of(AffixType.of("CAS"), 0.5), Word.of("in")));
        stepCandidate11.setOutput(ProbabilisticWord.of(Word.of("output"), 0.5));

        assertThat(stepCandidate1).isEqualTo(stepCandidate1);
        assertThat(stepCandidate1).isNotEqualTo(null);
        assertThat(stepCandidate1).isNotEqualTo("string");
        assertThat(stepCandidate1).isNotEqualTo(stepCandidate2);
        assertThat(stepCandidate1).isNotEqualTo(stepCandidate3);
        assertThat(stepCandidate1).isNotEqualTo(stepCandidate3);
        assertThat(stepCandidate1).isNotEqualTo(stepCandidate4);
        assertThat(stepCandidate1).isNotEqualTo(stepCandidate5);
        assertThat(stepCandidate1).isNotEqualTo(stepCandidate6);
        assertThat(stepCandidate7).isNotEqualTo(stepCandidate1);
        assertThat(stepCandidate1).isNotEqualTo(stepCandidate7);
        assertThat(stepCandidate8).isNotEqualTo(stepCandidate1);
        assertThat(stepCandidate10).isNotEqualTo(stepCandidate11);
        assertThat(stepCandidate1).isEqualTo(stepCandidate9);
    }

    @Test
    public void testHashCode() {
        ProbabilisticAffixType probabilisticAffixType = ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5);
        Word input = Word.of("input");
        StepCandidate parent = new StepCandidate(probabilisticAffixType, input);
        ProbabilisticWord output = ProbabilisticWord.of(Word.of("output"), 0.5);
        StepCandidate stepCandidate = new StepCandidate(probabilisticAffixType, input, parent);
        stepCandidate.setOutput(output);
        int result = stepCandidate.hashCode();

        int expected;
        expected = probabilisticAffixType.hashCode();
        expected = 31 * expected + input.hashCode();
        expected = 31 * expected + stepCandidate.getLevel();
        expected = 31 * expected + parent.hashCode();
        expected = 31 * expected + output.hashCode();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        ProbabilisticAffixType probabilisticAffixType = ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5);
        Word input = Word.of("input");
        StepCandidate parent = new StepCandidate(null, null, null);
        ProbabilisticWord output = ProbabilisticWord.of(Word.of("output"), 0.5);
        StepCandidate stepCandidate = new StepCandidate(probabilisticAffixType, input, parent);
        stepCandidate.setOutput(output);
        assertThat(stepCandidate).hasToString(
                "StepCandidate[" +
                        "probabilisticAffixType=" + probabilisticAffixType +
                        ", input=" + input +
                        ", level=1" +
                        ", parent=" + parent +
                        ", output=" + output +
                        ']'
        );
    }

}
