package com.github.szgabsz91.morpher.methods.astra.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.methods.astra.impl.rules.AtomicRule;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.AtomicRuleMessage;
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
        AtomicRule atomicRule = new AtomicRule("prefix", "from", "to", "postfix", 5);

        AtomicRuleMessage atomicRuleMessage = this.converter.convert(atomicRule);
        assertThat(atomicRuleMessage.getPrefix()).isEqualTo(atomicRule.getPrefix());
        assertThat(atomicRuleMessage.getFrom()).isEqualTo(atomicRule.getFrom());
        assertThat(atomicRuleMessage.getTo()).isEqualTo(atomicRule.getTo());
        assertThat(atomicRuleMessage.getPostfix()).isEqualTo(atomicRule.getPostfix());
        assertThat(atomicRuleMessage.getSupport()).isEqualTo(atomicRule.getSupport());

        AtomicRule result = this.converter.convertBack(atomicRuleMessage);
        assertThat(result.getPrefix()).isEqualTo(atomicRule.getPrefix());
        assertThat(result.getFrom()).isEqualTo(atomicRule.getFrom());
        assertThat(result.getTo()).isEqualTo(atomicRule.getTo());
        assertThat(result.getPostfix()).isEqualTo(atomicRule.getPostfix());
        assertThat(result.getSupport()).isEqualTo(atomicRule.getSupport());

        Path file = Files.createTempFile("morpher", "astra");
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
