package com.github.szgabsz91.morpher.methods.fst.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FSTMethodConfigurationTest {

    @Test
    public void testConfiguration() {
        FSTMethodConfiguration configuration = new FSTMethodConfiguration();
        assertThat(configuration).isNotNull();
    }

}
