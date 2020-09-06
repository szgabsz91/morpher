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
    public void testEquals() {
        AffixType affixType1 = AffixType.of("AFF");
        AffixType affixType2 = AffixType.of("AFF2");
        AffixType affixType3 = AffixType.of("AFF");

        assertThat(affixType1.equals(affixType1)).isTrue();
        assertThat(affixType1.equals(null)).isFalse();
        assertThat(affixType1).isNotEqualTo("string");
        assertThat(affixType1).isNotEqualTo(affixType2);
        assertThat(affixType1).isEqualTo(affixType3);
    }

    @Test
    public void testHashCode() {
        AffixType affixType = AffixType.of("AFF");
        int result = affixType.hashCode();
        assertThat(result).isEqualTo(affixType.toString().hashCode());
    }

    @Test
    public void testToString() {
        AffixType affixType = AffixType.of("AFF");
        assertThat(affixType).hasToString(affixType.toString());
    }

}
