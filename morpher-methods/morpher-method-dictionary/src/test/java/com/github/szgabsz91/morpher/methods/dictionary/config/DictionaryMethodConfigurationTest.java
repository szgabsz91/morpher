package com.github.szgabsz91.morpher.methods.dictionary.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DictionaryMethodConfigurationTest {

    @Test
    public void testConstructor() {
        DictionaryMethodConfiguration configuration = new DictionaryMethodConfiguration();
        assertThat(configuration).isNotNull();
    }

}
