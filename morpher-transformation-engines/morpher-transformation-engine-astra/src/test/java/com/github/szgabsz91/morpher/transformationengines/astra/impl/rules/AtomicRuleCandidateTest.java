package com.github.szgabsz91.morpher.transformationengines.astra.impl.rules;

import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.componentaccessors.IAtomicRuleComponentAccessor;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.componentaccessors.StraightAtomicRuleComponentAccessor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AtomicRuleCandidateTest {

    @Test
    public void testConstructorAndGetters() {
        IAtomicRuleComponentAccessor atomicRuleComponentAccessor = StraightAtomicRuleComponentAccessor.get();
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        double fitness = 1.0;
        AtomicRuleCandidate atomicRuleCandidate = new AtomicRuleCandidate(atomicRuleComponentAccessor, atomicRule, fitness);
        assertThat(atomicRuleCandidate.getAtomicRuleComponentAccessor()).isSameAs(atomicRuleComponentAccessor);
        assertThat(atomicRuleCandidate.getAtomicRule()).isEqualTo(atomicRule);
        assertThat(atomicRuleCandidate.getFitness()).isEqualTo(fitness);
    }

    @Test
    public void testCompareToWithLessThan() {
        IAtomicRuleComponentAccessor atomicRuleComponentAccessor = StraightAtomicRuleComponentAccessor.get();
        AtomicRuleCandidate atomicRuleCandidate1 = new AtomicRuleCandidate(atomicRuleComponentAccessor, null, 2.0);
        AtomicRuleCandidate atomicRuleCandidate2 = new AtomicRuleCandidate(atomicRuleComponentAccessor, null, 1.0);
        int result = atomicRuleCandidate1.compareTo(atomicRuleCandidate2);
        assertThat(result).isLessThan(0);
    }

    @Test
    public void testCompareToWithEqual() {
        IAtomicRuleComponentAccessor atomicRuleComponentAccessor = StraightAtomicRuleComponentAccessor.get();
        AtomicRuleCandidate atomicRuleCandidate1 = new AtomicRuleCandidate(atomicRuleComponentAccessor, null, 1.0);
        AtomicRuleCandidate atomicRuleCandidate2 = new AtomicRuleCandidate(atomicRuleComponentAccessor, null, 1.0);
        int result = atomicRuleCandidate1.compareTo(atomicRuleCandidate2);
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void testCompareToWithGreaterThan() {
        IAtomicRuleComponentAccessor atomicRuleComponentAccessor = StraightAtomicRuleComponentAccessor.get();
        AtomicRuleCandidate atomicRuleCandidate1 = new AtomicRuleCandidate(atomicRuleComponentAccessor, null, 1.0);
        AtomicRuleCandidate atomicRuleCandidate2 = new AtomicRuleCandidate(atomicRuleComponentAccessor, null, 2.0);
        int result = atomicRuleCandidate1.compareTo(atomicRuleCandidate2);
        assertThat(result).isGreaterThan(0);
    }

    @Test
    public void testEquals() {
        IAtomicRuleComponentAccessor atomicRuleComponentAccessor = StraightAtomicRuleComponentAccessor.get();
        AtomicRuleCandidate atomicRuleCandidate1 = new AtomicRuleCandidate(atomicRuleComponentAccessor, new AtomicRule("a", "b", "c", "d", 1), 1.0);
        AtomicRuleCandidate atomicRuleCandidate2 = new AtomicRuleCandidate(atomicRuleComponentAccessor, new AtomicRule("b", "b", "c", "d", 1), 1.0);
        AtomicRuleCandidate atomicRuleCandidate3 = new AtomicRuleCandidate(atomicRuleComponentAccessor, new AtomicRule("a", "b", "c", "d", 1), 2.0);
        AtomicRuleCandidate atomicRuleCandidate4 = new AtomicRuleCandidate(atomicRuleComponentAccessor, new AtomicRule("a", "b", "c", "d", 1), 1.0);

        assertThat(atomicRuleCandidate1.equals(atomicRuleCandidate1)).isTrue();
        assertThat(atomicRuleCandidate1).isEqualTo(atomicRuleCandidate4);
        assertThat(atomicRuleCandidate1).isNotEqualTo(null);
        assertThat(atomicRuleCandidate1).isNotEqualTo("string");
        assertThat(atomicRuleCandidate1).isNotEqualTo(atomicRuleCandidate2);
        assertThat(atomicRuleCandidate1).isNotEqualTo(atomicRuleCandidate3);
    }

    @Test
    public void testHashCode() {
        IAtomicRuleComponentAccessor atomicRuleComponentAccessor = StraightAtomicRuleComponentAccessor.get();
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        double fitness = 1.0;
        AtomicRuleCandidate atomicRuleCandidate = new AtomicRuleCandidate(atomicRuleComponentAccessor, atomicRule, fitness);
        int result = atomicRuleCandidate.hashCode();

        int expected;
        long temp = Double.doubleToLongBits(fitness);
        expected = atomicRule.hashCode();
        expected = 31 * expected + (int) (temp ^ (temp >>> 32));

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToStringWithNullFitness() {
        IAtomicRuleComponentAccessor atomicRuleComponentAccessor = StraightAtomicRuleComponentAccessor.get();
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        double fitness = 1.0;
        AtomicRuleCandidate atomicRuleCandidate = new AtomicRuleCandidate(atomicRuleComponentAccessor, atomicRule, fitness);
        assertThat(atomicRuleCandidate).hasToString("AtomicRuleCandidate[" + atomicRule + ", " + fitness + "]");
    }

}
