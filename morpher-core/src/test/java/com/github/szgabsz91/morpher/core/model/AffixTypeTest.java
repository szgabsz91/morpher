package com.github.szgabsz91.morpher.core.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AffixTypeTest {

    @Test
    public void testOfAndGetter() {
        String token = "TOK";
        AffixType affixType = AffixType.of(token);
        assertThat(affixType.toString()).isEqualTo(token);
    }

    @Test
    public void testToString() {
        AffixType affixType = AffixType.of("AFF");
        assertThat(affixType).hasToString(affixType.toString());
    }

}
