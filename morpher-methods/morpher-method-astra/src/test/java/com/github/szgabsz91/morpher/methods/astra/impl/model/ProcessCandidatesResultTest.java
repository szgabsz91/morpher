package com.github.szgabsz91.morpher.methods.astra.impl.model;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.methods.astra.impl.rules.AtomicRuleCandidate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessCandidatesResultTest {

    @Test
    public void testConstructorAndGetters() {
        FitnessAwareWord fitnessAwareWord = FitnessAwareWord.of(Word.of("word"), 1.0);
        List<AtomicRuleCandidate> atomicRuleCandidates = List.of();
        ProcessCandidatesResult result = new ProcessCandidatesResult(fitnessAwareWord, atomicRuleCandidates);
        assertThat(result.getFitnessAwareWord()).isEqualTo(fitnessAwareWord);
        assertThat(result.getAtomicRuleCandidates()).isEqualTo(atomicRuleCandidates);
    }

    @Test
    public void testEquals() {
        ProcessCandidatesResult result1 = new ProcessCandidatesResult(FitnessAwareWord.of(Word.of("word"), 1.0), List.of());
        ProcessCandidatesResult result2 = new ProcessCandidatesResult(FitnessAwareWord.of(Word.of("word"), 1.0), List.of());
        ProcessCandidatesResult result3 = new ProcessCandidatesResult(FitnessAwareWord.of(Word.of("word2"), 1.0), List.of());
        ProcessCandidatesResult result4 = new ProcessCandidatesResult(FitnessAwareWord.of(Word.of("word"), 1.0), List.of(new AtomicRuleCandidate(null, null, 1.0)));

        assertThat(result1).isEqualTo(result1);
        assertThat(result1).isEqualTo(result2);
        assertThat(result1).isNotEqualTo(null);
        assertThat(result1).isNotEqualTo("string");
        assertThat(result1).isNotEqualTo(result3);
        assertThat(result1).isNotEqualTo(result4);
    }

    @Test
    public void testHashCode() {
        FitnessAwareWord fitnessAwareWord = FitnessAwareWord.of(Word.of("word"), 1.0);
        List<AtomicRuleCandidate> atomicRuleCandidates = List.of();
        ProcessCandidatesResult result = new ProcessCandidatesResult(fitnessAwareWord, atomicRuleCandidates);
        assertThat(result.hashCode()).isEqualTo(Objects.hash(fitnessAwareWord, atomicRuleCandidates));
    }

    @Test
    public void testToString() {
        FitnessAwareWord fitnessAwareWord = FitnessAwareWord.of(Word.of("word"), 1.0);
        List<AtomicRuleCandidate> atomicRuleCandidates = List.of();
        ProcessCandidatesResult result = new ProcessCandidatesResult(fitnessAwareWord, atomicRuleCandidates);
        assertThat(result).hasToString("ProcessCandidatesResult[fitnessAwareWord=" + fitnessAwareWord + ", atomicRuleCandidates=" + atomicRuleCandidates + ']');
    }

}
