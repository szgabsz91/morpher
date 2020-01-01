package com.github.szgabsz91.morpher.transformationengines.tasr.converters;

import com.github.szgabsz91.morpher.transformationengines.tasr.impl.rules.SuffixRule;
import com.github.szgabsz91.morpher.transformationengines.tasr.protocolbuffers.SuffixRuleMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SuffixRuleConverterTest {

    private SuffixRuleConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new SuffixRuleConverter();
    }

    @Test
    public void testConvert() {
        SuffixRule suffixRule = new SuffixRule("a", "b");
        suffixRule.incrementFrequency();
        SuffixRuleMessage suffixRuleMessage = this.converter.convert(suffixRule);
        assertThat(suffixRuleMessage.getLeftHandSuffix()).isEqualTo(suffixRule.getLeftHandSuffix());
        assertThat(suffixRuleMessage.getRightHandSuffix()).isEqualTo(suffixRule.getRightHandSuffix());
        assertThat(suffixRuleMessage.getFrequency()).isEqualTo(suffixRule.getFrequency());
    }

    @Test
    public void testConvertBack() {
        SuffixRuleMessage suffixRuleMessage = SuffixRuleMessage.newBuilder()
                .setLeftHandSuffix("a")
                .setRightHandSuffix("b")
                .setFrequency(100)
                .build();
        SuffixRule suffixRule = this.converter.convertBack(suffixRuleMessage);
        assertThat(suffixRule.getLeftHandSuffix()).isEqualTo(suffixRuleMessage.getLeftHandSuffix());
        assertThat(suffixRule.getRightHandSuffix()).isEqualTo(suffixRuleMessage.getRightHandSuffix());
        assertThat(suffixRule.getFrequency()).isEqualTo(suffixRuleMessage.getFrequency());
    }

    @Test
    public void testParse() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.converter.parse(null));
        assertThat(exception).hasMessage("Suffix rules cannot be saved and loaded individually");
    }

}
