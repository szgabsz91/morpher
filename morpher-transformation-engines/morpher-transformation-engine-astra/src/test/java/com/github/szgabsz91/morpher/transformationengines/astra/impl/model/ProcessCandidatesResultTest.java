package com.github.szgabsz91.morpher.transformationengines.astra.impl.model;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.AtomicRuleCandidate;
import org.junit.jupiter.api.Test;

import java.util.List;

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

}
