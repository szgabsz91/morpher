package com.github.szgabsz91.morpher.transformationengines.astra.impl.rules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SegmentTest {

    @Test
    public void testConstructorAndGetters() {
        int leftWordIndex = 1;
        int rightWordIndex = 2;
        String leftWordSubstring = "a";
        String rightWordSubstring = "b";

        Segment segment = new Segment(leftWordIndex, rightWordIndex, leftWordSubstring, rightWordSubstring);

        assertThat(segment.getLeftWordIndex()).isEqualTo(leftWordIndex);
        assertThat(segment.getRightWordIndex()).isEqualTo(rightWordIndex);
        assertThat(segment.getLeftWordSubstring()).isEqualTo(leftWordSubstring);
        assertThat(segment.getRightWordSubstring()).isEqualTo(rightWordSubstring);
        assertThat(segment.getFitness()).isNull();
    }

    @Test
    public void testIsVariantWithVariantSegment() {
        Segment segment = new Segment(0, 0, "a", "b");
        assertThat(segment.isVariant()).isTrue();
    }

    @Test
    public void testIsVariantWithInvariantSegment() {
        Segment segment = new Segment(0, 0, "a", "a");
        assertThat(segment.isVariant()).isFalse();
    }

    @Test
    public void testIsInvariantWithVariantSegment() {
        Segment segment = new Segment(0, 0, "a", "b");
        assertThat(segment.isInvariant()).isFalse();
    }

    @Test
    public void testSetFitness() {
        Segment segment = new Segment(0, 0, "a", "a");
        assertThat(segment.getFitness()).isNull();

        double fitness = 1.0;
        segment.setFitness(fitness);

        assertThat(segment.getFitness()).isEqualTo(fitness);
    }

    @Test
    public void testIsInvariantWithInvariantSegment() {
        Segment segment = new Segment(0, 0, "a", "a");
        assertThat(segment.isInvariant()).isTrue();
    }

    @Test
    public void testCompareToWithNullThisFitness() {
        Segment segment1 = new Segment(0, 0, "a", "a");
        Segment segment2 = new Segment(0, 0, "a", "a");
        segment2.setFitness(1.0);
        NullPointerException exception = assertThrows(NullPointerException.class, () -> segment1.compareTo(segment2));
        assertThat(exception.getMessage()).isEqualTo("Fitness cannot be null for sorting " + segment1 + " and " + segment2);
    }

    @Test
    public void testCompareToWithNullOtherFitness() {
        Segment segment1 = new Segment(0, 0, "a", "a");
        Segment segment2 = new Segment(0, 0, "a", "a");
        segment1.setFitness(1.0);
        NullPointerException exception = assertThrows(NullPointerException.class, () -> segment1.compareTo(segment2));
        assertThat(exception.getMessage()).isEqualTo("Fitness cannot be null for sorting " + segment1 + " and " + segment2);
    }

    @Test
    public void testCompareToWithEqualFitness() {
        Segment segment1 = new Segment(0, 0, "a", "a");
        Segment segment2 = new Segment(0, 0, "a", "a");
        segment1.setFitness(1.0);
        segment2.setFitness(1.0);
        int result = segment1.compareTo(segment2);
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void testCompareToWithLessThisFitness() {
        Segment segment1 = new Segment(0, 0, "a", "a");
        Segment segment2 = new Segment(0, 0, "a", "a");
        segment1.setFitness(0.0);
        segment2.setFitness(1.0);
        int result = segment1.compareTo(segment2);
        assertThat(result).isGreaterThan(0);
    }

    @Test
    public void testCompareToWithGreaterThisFitness() {
        Segment segment1 = new Segment(0, 0, "a", "a");
        Segment segment2 = new Segment(0, 0, "a", "a");
        segment1.setFitness(1.0);
        segment2.setFitness(0.0);
        int result = segment1.compareTo(segment2);
        assertThat(result).isLessThan(0);
    }

    @Test
    public void testEquals() {
        Segment segment1 = new Segment(1, 2, "a", "b");
        Segment segment2 = new Segment(3, 2, "a", "b");
        Segment segment3 = new Segment(1, 3, "a", "b");
        Segment segment4 = new Segment(1, 2, "c", "b");
        Segment segment5 = new Segment(1, 2, "a", "c");
        Segment segment6 = new Segment(1, 2, "a", "b");

        assertThat(segment1).isEqualTo(segment1);
        assertThat(segment1).isEqualTo(segment6);
        assertThat(segment1).isNotEqualTo(null);
        assertThat(segment1).isNotEqualTo("string");
        assertThat(segment1).isNotEqualTo(segment2);
        assertThat(segment1).isNotEqualTo(segment3);
        assertThat(segment1).isNotEqualTo(segment4);
        assertThat(segment1).isNotEqualTo(segment5);
    }

    @Test
    public void testHashCode() {
        Segment segment = new Segment(1, 2, "a", "b");
        int result = segment.hashCode();

        int expected = 31 * segment.getLeftWordIndex() + segment.getRightWordIndex();
        expected = 31 * expected + segment.getLeftWordSubstring().hashCode();
        expected = 31 * expected + segment.getRightWordSubstring().hashCode();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToStringWithNullFitness() {
        Segment segment = new Segment(1, 2, "a", "b");
        assertThat(segment).hasToString("[1, 2, a, b, null]");
    }

    @Test
    public void testToStringWithNonNullFitness() {
        Segment segment = new Segment(1, 2, "a", "b");
        segment.setFitness(3.0);
        assertThat(segment).hasToString("[1, 2, a, b, 3.0]");
    }

}
