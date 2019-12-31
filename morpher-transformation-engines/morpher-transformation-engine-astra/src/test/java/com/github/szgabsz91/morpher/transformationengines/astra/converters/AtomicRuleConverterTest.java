package com.github.szgabsz91.morpher.transformationengines.astra.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.AtomicRule;
import com.github.szgabsz91.morpher.transformationengines.astra.protocolbuffers.AtomicRuleMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class AtomicRuleConverterTest {

    private AtomicRuleConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new AtomicRuleConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParse() throws IOException {
        AtomicRule atomicRule = new AtomicRule("prefix", "changingSubstring", "replacementString", "postfix", 5);

        AtomicRuleMessage atomicRuleMessage = this.converter.convert(atomicRule);
        assertThat(atomicRuleMessage.getPrefix()).isEqualTo(atomicRule.getPrefix());
        assertThat(atomicRuleMessage.getChangingSubstring()).isEqualTo(atomicRule.getChangingSubstring());
        assertThat(atomicRuleMessage.getReplacementString()).isEqualTo(atomicRule.getReplacementString());
        assertThat(atomicRuleMessage.getPostfix()).isEqualTo(atomicRule.getPostfix());
        assertThat(atomicRuleMessage.getSupport()).isEqualTo(atomicRule.getSupport());

        AtomicRule result = this.converter.convertBack(atomicRuleMessage);
        assertThat(result.getPrefix()).isEqualTo(atomicRule.getPrefix());
        assertThat(result.getChangingSubstring()).isEqualTo(atomicRule.getChangingSubstring());
        assertThat(result.getReplacementString()).isEqualTo(atomicRule.getReplacementString());
        assertThat(result.getPostfix()).isEqualTo(atomicRule.getPostfix());
        assertThat(result.getSupport()).isEqualTo(atomicRule.getSupport());

        Path file = Files.createTempFile("transformation-engine", "astra");
        try {
            Serializer<AtomicRule, AtomicRuleMessage> serializer = new Serializer<>(this.converter, atomicRule);
            serializer.serialize(atomicRule, file);
            AtomicRuleMessage resultingAtomicRuleMessage = this.converter.parse(file);
            assertThat(resultingAtomicRuleMessage).isEqualTo(atomicRuleMessage);
        }
        finally {
            Files.delete(file);
        }
    }

}
