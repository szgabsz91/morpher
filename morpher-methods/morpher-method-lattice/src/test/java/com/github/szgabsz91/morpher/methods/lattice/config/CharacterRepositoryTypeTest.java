package com.github.szgabsz91.morpher.methods.lattice.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CharacterRepositoryTypeTest {

    @Test
    public void testValueOf() {
        assertThat(CharacterRepositoryType.valueOf("SIMPLE")).isEqualTo(CharacterRepositoryType.SIMPLE);
        assertThat(CharacterRepositoryType.valueOf("ATTRIBUTED")).isEqualTo(CharacterRepositoryType.ATTRIBUTED);
    }

}
