package com.github.szgabsz91.morpher.transformationengines.fst.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FSTTransformationEngineConfigurationTest {

    @Test
    public void testConfiguration() {
        FSTTransformationEngineConfiguration configuration = new FSTTransformationEngineConfiguration();
        assertThat(configuration).isNotNull();
    }

}
