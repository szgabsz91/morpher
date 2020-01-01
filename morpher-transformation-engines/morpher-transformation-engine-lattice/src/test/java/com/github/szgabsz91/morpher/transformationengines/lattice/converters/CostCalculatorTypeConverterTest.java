package com.github.szgabsz91.morpher.transformationengines.lattice.converters;

import com.github.szgabsz91.morpher.transformationengines.lattice.config.CostCalculatorType;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.ITransformation;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.DefaultCostCalculator;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.CostCalculatorTypeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CostCalculatorTypeConverterTest {

    private CostCalculatorTypeConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new CostCalculatorTypeConverter();
    }

    @Test
    public void testConvert() {
        assertThat(this.converter.convert(CostCalculatorType.ATTRIBUTE_BASED)).isEqualTo(CostCalculatorTypeMessage.ATTRIBUTE_BASED);
        assertThat(this.converter.convert(CostCalculatorType.DEFAULT)).isEqualTo(CostCalculatorTypeMessage.DEFAULT);
    }

    @Test
    public void testFromImplementation() {
        assertThat(this.converter.fromImplementation(new AttributeBasedCostCalculator())).isEqualTo(CostCalculatorTypeMessage.ATTRIBUTE_BASED);
        assertThat(this.converter.fromImplementation(new DefaultCostCalculator())).isEqualTo(CostCalculatorTypeMessage.DEFAULT);
        assertThat(this.converter.fromImplementation(new CustomCostCalculator())).isEqualTo(CostCalculatorTypeMessage.DEFAULT);
    }

    @Test
    public void testConvertBack() {
        assertThat(this.converter.convertBack(CostCalculatorTypeMessage.ATTRIBUTE_BASED)).isEqualTo(CostCalculatorType.ATTRIBUTE_BASED);
        assertThat(this.converter.convertBack(CostCalculatorTypeMessage.DEFAULT)).isEqualTo(CostCalculatorType.DEFAULT);
    }

    @Test
    public void testToImplementation() {
        assertThat(this.converter.toImplementation(CostCalculatorTypeMessage.ATTRIBUTE_BASED)).isInstanceOf(AttributeBasedCostCalculator.class);
        assertThat(this.converter.toImplementation(CostCalculatorTypeMessage.DEFAULT)).isInstanceOf(DefaultCostCalculator.class);
    }

    private static class CustomCostCalculator implements ICostCalculator {

        @Override
        public int calculateCost(ITransformation transformation) {
            return 0;
        }

    }

}
