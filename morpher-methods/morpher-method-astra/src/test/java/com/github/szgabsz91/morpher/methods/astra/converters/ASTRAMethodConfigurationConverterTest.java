package com.github.szgabsz91.morpher.methods.astra.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.methods.astra.config.ASTRAMethodConfiguration;
import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.ASTRAMethodConfigurationMessage;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.SearcherTypeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ASTRAMethodConfigurationConverterTest {

    private ASTRAMethodConfigurationConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new ASTRAMethodConfigurationConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParseWithNoMinimumContextLength() throws IOException {
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .exponentialFactor(0.0)
                .build();

        ASTRAMethodConfigurationMessage message = this.converter.convert(configuration);
        assertThat(message).isNotNull();
        assertThat(message.getSearcherType()).isEqualTo(SearcherTypeMessage.PARALLEL);
        assertThat(message.getExponentialFactor()).isEqualTo(configuration.getExponentialFactor());
        assertThat(message.hasMinimumContextLength()).isFalse();

        ASTRAMethodConfiguration result = this.converter.convertBack(message);
        assertThat(result).isNotNull();
        assertThat(result.getSearcherType()).isEqualTo(configuration.getSearcherType());
        assertThat(result.getExponentialFactor()).isEqualTo(configuration.getExponentialFactor());
        assertThat(result.getMinimumContextLength()).isNull();

        Path file = Files.createTempFile("morpher", "astra");
        try {
            Serializer<ASTRAMethodConfiguration, ASTRAMethodConfigurationMessage> serializer = new Serializer<>(this.converter, configuration);
            serializer.serialize(configuration, file);
            ASTRAMethodConfigurationMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testConvertAndConvertBackAndParseWithMinimumContextLength() throws IOException {
        int minimumContextLength = 3;
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .minimumContextLength(minimumContextLength)
                .exponentialFactor(2.0)
                .build();

        ASTRAMethodConfigurationMessage message = this.converter.convert(configuration);
        assertThat(message).isNotNull();
        assertThat(message.getSearcherType()).isEqualTo(SearcherTypeMessage.PARALLEL);
        assertThat(message.getExponentialFactor()).isEqualTo(configuration.getExponentialFactor());
        assertThat(message.getMinimumContextLength().getValue()).isEqualTo(minimumContextLength);

        ASTRAMethodConfiguration result = this.converter.convertBack(message);
        assertThat(result).isNotNull();
        assertThat(result.getSearcherType()).isEqualTo(configuration.getSearcherType());
        assertThat(result.getMinimumContextLength()).isEqualTo(minimumContextLength);
        assertThat(result.getExponentialFactor()).isEqualTo(configuration.getExponentialFactor());

        Path file = Files.createTempFile("morpher", "astra");
        try {
            Serializer<ASTRAMethodConfiguration, ASTRAMethodConfigurationMessage> serializer = new Serializer<>(this.converter, configuration);
            serializer.serialize(configuration, file);
            ASTRAMethodConfigurationMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testConvertAndConvertBackAndParseWithMaximumNumberOfGeneratedAtomicRules() throws IOException {
        int maximumNumberOfAtomicRules = 1;
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .maximumNumberOfGeneratedAtomicRules(maximumNumberOfAtomicRules)
                .build();

        ASTRAMethodConfigurationMessage message = this.converter.convert(configuration);
        assertThat(message).isNotNull();
        assertThat(message.getSearcherType()).isEqualTo(SearcherTypeMessage.PARALLEL);
        assertThat(message.getExponentialFactor()).isEqualTo(configuration.getExponentialFactor());
        assertThat(message.hasMinimumContextLength()).isFalse();
        assertThat(message.hasMaximumNumberOfGeneratedAtomicRules()).isTrue();
        assertThat(message.getMaximumNumberOfGeneratedAtomicRules().getValue()).isEqualTo(maximumNumberOfAtomicRules);

        ASTRAMethodConfiguration result = this.converter.convertBack(message);
        assertThat(result).isNotNull();
        assertThat(result.getSearcherType()).isEqualTo(configuration.getSearcherType());
        assertThat(result.getExponentialFactor()).isEqualTo(configuration.getExponentialFactor());
        assertThat(result.getMinimumContextLength()).isNull();
        assertThat(result.getMaximumNumberOfGeneratedAtomicRules()).isEqualTo(maximumNumberOfAtomicRules);

        Path file = Files.createTempFile("morpher", "astra");
        try {
            Serializer<ASTRAMethodConfiguration, ASTRAMethodConfigurationMessage> serializer = new Serializer<>(this.converter, configuration);
            serializer.serialize(configuration, file);
            ASTRAMethodConfigurationMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
        }
    }

}
