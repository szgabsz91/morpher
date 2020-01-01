package com.github.szgabsz91.morpher.transformationengines.astra.converters;

import com.github.szgabsz91.morpher.transformationengines.astra.config.AtomicRuleFitnessCalculatorType;
import com.github.szgabsz91.morpher.transformationengines.astra.protocolbuffers.AtomicRuleFitnessCalculatorTypeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AtomicRuleFitnessCalculatorTypeConverterTest {

    private AtomicRuleFitnessCalculatorTypeConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new AtomicRuleFitnessCalculatorTypeConverter();
    }

    @Test
    public void testConvert() {
        assertThat(converter.convert(AtomicRuleFitnessCalculatorType.DEFAULT)).isEqualTo(AtomicRuleFitnessCalculatorTypeMessage.DEFAULT);
        assertThat(converter.convert(AtomicRuleFitnessCalculatorType.SMOOTH_LOCAL)).isEqualTo(AtomicRuleFitnessCalculatorTypeMessage.SMOOTH_LOCAL);
        assertThat(converter.convert(AtomicRuleFitnessCalculatorType.SMOOTH_GLOBAL)).isEqualTo(AtomicRuleFitnessCalculatorTypeMessage.SMOOTH_GLOBAL);
    }

    @Test
    public void testConvertBack() {
        assertThat(converter.convertBack(AtomicRuleFitnessCalculatorTypeMessage.DEFAULT)).isEqualTo(AtomicRuleFitnessCalculatorType.DEFAULT);
        assertThat(converter.convertBack(AtomicRuleFitnessCalculatorTypeMessage.SMOOTH_LOCAL)).isEqualTo(AtomicRuleFitnessCalculatorType.SMOOTH_LOCAL);
        assertThat(converter.convertBack(AtomicRuleFitnessCalculatorTypeMessage.SMOOTH_GLOBAL)).isEqualTo(AtomicRuleFitnessCalculatorType.SMOOTH_GLOBAL);
    }

}
