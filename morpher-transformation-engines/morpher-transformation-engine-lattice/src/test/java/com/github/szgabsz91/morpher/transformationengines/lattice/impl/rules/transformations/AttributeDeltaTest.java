package com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations;

import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.Length;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AttributeDeltaTest {

    @Test
    public void testConstructor() {
        AttributeDelta<Length> attributeDelta = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);
        assertThat(attributeDelta.getClazz()).isEqualTo(Length.class);
        assertThat(attributeDelta.getFrom()).isEqualTo(Length.LONG);
        assertThat(attributeDelta.getTo()).isEqualTo(Length.SHORT);
    }

    @Test
    public void testFactoryMethodWithRemoval() {
        AttributeDelta<Length> attributeDelta = AttributeDelta.remove(Length.class, Length.LONG);
        assertThat(attributeDelta.getClazz()).isEqualTo(Length.class);
        assertThat(attributeDelta.getFrom()).isEqualTo(Length.LONG);
        assertThat(attributeDelta.getTo()).isNull();
    }

    @Test
    public void testFactoryMethodWithAddition() {
        AttributeDelta<Length> attributeDelta = AttributeDelta.add(Length.class, Length.LONG);
        assertThat(attributeDelta.getClazz()).isEqualTo(Length.class);
        assertThat(attributeDelta.getFrom()).isNull();
        assertThat(attributeDelta.getTo()).isEqualTo(Length.LONG);
    }

    @Test
    public void testToString() {
        AttributeDelta<Length> attributeDelta = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);
        assertThat(attributeDelta).hasToString("Length: LONG -> SHORT");
    }

}
