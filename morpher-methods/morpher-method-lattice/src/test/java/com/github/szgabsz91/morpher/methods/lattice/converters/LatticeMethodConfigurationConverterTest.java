package com.github.szgabsz91.morpher.methods.lattice.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.methods.lattice.config.CharacterRepositoryType;
import com.github.szgabsz91.morpher.methods.lattice.config.CostCalculatorType;
import com.github.szgabsz91.morpher.methods.lattice.config.LatticeBuilderType;
import com.github.szgabsz91.morpher.methods.lattice.config.LatticeMethodConfiguration;
import com.github.szgabsz91.morpher.methods.lattice.config.WordConverterType;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.CharacterRepositoryTypeMessage;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.CostCalculatorTypeMessage;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.LatticeBuilderTypeMessage;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.LatticeMethodConfigurationMessage;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.WordConverterTypeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class LatticeMethodConfigurationConverterTest {

    private LatticeMethodConfigurationConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new LatticeMethodConfigurationConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParse() throws IOException {
        LatticeMethodConfiguration configuration = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(5)
                .build();

        LatticeMethodConfigurationMessage message = this.converter.convert(configuration);
        assertThat(message).isNotNull();
        assertThat(message.getLatticeBuilderType()).isEqualTo(LatticeBuilderTypeMessage.MINIMAL);
        assertThat(message.getWordConverterType()).isEqualTo(WordConverterTypeMessage.IDENTITY);
        assertThat(message.getCostCalculatorType()).isEqualTo(CostCalculatorTypeMessage.DEFAULT);
        assertThat(message.getCharacterRepositoryType()).isEqualTo(CharacterRepositoryTypeMessage.SIMPLE);
        assertThat(message.getMaximalContextSize()).isEqualTo(configuration.getMaximalContextSize());

        LatticeMethodConfiguration result = this.converter.convertBack(message);
        assertThat(result).isNotNull();
        assertThat(result.getLatticeBuilderType()).isEqualTo(configuration.getLatticeBuilderType());
        assertThat(result.getWordConverterType()).isEqualTo(configuration.getWordConverterType());
        assertThat(result.getCostCalculatorType()).isEqualTo(configuration.getCostCalculatorType());
        assertThat(result.getCharacterRepositoryType()).isEqualTo(configuration.getCharacterRepositoryType());
        assertThat(result.getMaximalContextSize()).isEqualTo(configuration.getMaximalContextSize());

        Path file = Files.createTempFile("morpher", "lattice");
        try {
            Serializer<LatticeMethodConfiguration, LatticeMethodConfigurationMessage> serializer = new Serializer<>(this.converter, configuration);
            serializer.serialize(configuration, file);
            LatticeMethodConfigurationMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
        }
    }

}
