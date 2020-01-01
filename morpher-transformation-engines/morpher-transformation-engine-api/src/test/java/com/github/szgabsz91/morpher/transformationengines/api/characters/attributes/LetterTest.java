package com.github.szgabsz91.morpher.transformationengines.api.characters.attributes;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LetterTest {

    @Test
    public void testValueOf() {
        for (Letter letter : Letter.values()) {
            String toString = letter.toString()
                    .replaceAll("á", "a_")
                    .replaceAll("é", "e_")
                    .replaceAll("í", "i_")
                    .replaceAll("ó", "o_")
                    .replaceAll("ö", "o__")
                    .replaceAll("ő", "o___")
                    .replaceAll("ú", "u_")
                    .replaceAll("ü", "u__")
                    .replaceAll("ű", "u___")
                    .replace("$", "START_SYMBOL")
                    .replace("#", "END_SYMBOL")
                    .toUpperCase();
            Letter recreatedLetter = Letter.valueOf(toString);
            assertThat(recreatedLetter).isEqualTo(letter);
        }
    }

    @Test
    public void testFactory() {
        for (Letter letter : Letter.values()) {
            String toString = letter.toString();
            Letter recreatedLetter = Letter.factory(toString);
            assertThat(recreatedLetter).isEqualTo(letter);
        }
    }

}
