package com.github.szgabsz91.morpher.methods.tasr.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TASRMethodConfigurationTest {

    @Test
    public void testConstructor() {
        TASRMethodConfiguration configuration = new TASRMethodConfiguration();
        assertThat(configuration).isNotNull();
    }

}
