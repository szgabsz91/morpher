package com.github.szgabsz91.morpher.transformationengines.dictionary.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DictionaryTransformationEngineConfigurationTest {

    @Test
    public void testConstructor() {
        DictionaryTransformationEngineConfiguration configuration = new DictionaryTransformationEngineConfiguration();
        assertThat(configuration).isNotNull();
    }

}
