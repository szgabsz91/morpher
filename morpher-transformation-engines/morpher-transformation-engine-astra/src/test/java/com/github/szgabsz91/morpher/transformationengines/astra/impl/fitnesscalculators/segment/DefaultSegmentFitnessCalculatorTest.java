package com.github.szgabsz91.morpher.transformationengines.astra.impl.fitnesscalculators.segment;

import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.Segment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultSegmentFitnessCalculatorTest {

    private ISegmentFitnessCalculator segmentFitnessCalculator;

    @BeforeEach
    public void setUp() {
        this.segmentFitnessCalculator = new DefaultSegmentFitnessCalculator();
    }

    @Test
    public void testCalculateWithSmallerLeftWordIndex() {
        Segment segment = new Segment(3, 6, "alm", "xyzabc");
        double result = this.segmentFitnessCalculator.calculate(segment);
        assertThat(result).isEqualTo(4.5);
    }

    @Test
    public void testCalculateWithSmallerRightWordIndex() {
        Segment segment = new Segment(6, 3, "alm", "xyzabc");
        double result = this.segmentFitnessCalculator.calculate(segment);
        assertThat(result).isEqualTo(4.5);
    }

    @Test
    public void testCalculateWithCustomWeights() {
        ISegmentFitnessCalculator segmentFitnessCalculator = new DefaultSegmentFitnessCalculator(0.1, 0.1, 0.8);
        Segment segment = new Segment(6, 3, "alm", "xyzabc");
        double result = segmentFitnessCalculator.calculate(segment);
        assertThat(result).isCloseTo(5.55, offset(0.0005));
    }

    @Test
    public void testConstructorWithInvalidWeights() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DefaultSegmentFitnessCalculator(0.1, 0.1, 0.9));
        assertThat(exception.getMessage()).isEqualTo("The sum of weights should be 1, but it was 1.1");
    }

}
