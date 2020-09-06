package com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations;

import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.LipShape;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Context;
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
    public void testEquals() {
        AttributeDelta<Length> attributeDelta1 = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);

        assertThat(attributeDelta1.equals(attributeDelta1)).isTrue();
        assertThat(attributeDelta1.equals(null)).isFalse();
        assertThat(attributeDelta1).isNotEqualTo(new Context(null, null, null, null, null));
        assertThat(attributeDelta1).isNotEqualTo(new AttributeDelta<>(LipShape.class, LipShape.ROUNDED, LipShape.UNROUNDED));
        assertThat(attributeDelta1).isNotEqualTo(new AttributeDelta<>(Length.class, Length.SHORT, Length.SHORT));
        assertThat(attributeDelta1).isNotEqualTo(new AttributeDelta<>(Length.class, Length.LONG, Length.LONG));
        assertThat(attributeDelta1).isEqualTo(new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT));

        // Null from
        AttributeDelta<Length> attributeDelta2 = AttributeDelta.add(Length.class, Length.LONG);
        assertThat(attributeDelta2).isNotEqualTo(attributeDelta1);
        AttributeDelta<Length> attributeDelta3 = AttributeDelta.add(Length.class, Length.LONG);
        assertThat(attributeDelta2).isEqualTo(attributeDelta3);
        AttributeDelta<Length> attributeDelta4 = AttributeDelta.remove(Length.class, Length.LONG);
        assertThat(attributeDelta2).isNotEqualTo(attributeDelta4);

        // Null to
        assertThat(attributeDelta4).isNotEqualTo(attributeDelta1);
    }

    @Test
    public void testHashCode() {
        AttributeDelta<Length> attributeDelta = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);

        int expected = 31 * attributeDelta.getClazz().hashCode() + attributeDelta.getFrom().hashCode();
        expected = 31 * expected + attributeDelta.getTo().hashCode();

        assertThat(attributeDelta.hashCode()).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        AttributeDelta<Length> attributeDelta = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);
        assertThat(attributeDelta).hasToString("Length: LONG -> SHORT");
    }

}
