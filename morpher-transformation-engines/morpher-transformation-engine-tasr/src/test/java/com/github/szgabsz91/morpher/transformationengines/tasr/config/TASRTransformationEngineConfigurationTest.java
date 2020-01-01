package com.github.szgabsz91.morpher.transformationengines.tasr.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TASRTransformationEngineConfigurationTest {

    @Test
    public void testConstructor() {
        TASRTransformationEngineConfiguration configuration = new TASRTransformationEngineConfiguration();
        assertThat(configuration).isNotNull();
    }

}
