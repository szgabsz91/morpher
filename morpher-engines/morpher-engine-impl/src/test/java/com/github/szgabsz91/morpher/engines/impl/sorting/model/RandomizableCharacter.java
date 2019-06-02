package com.github.szgabsz91.morpher.engines.impl.sorting.model;

import java.util.Objects;

public class RandomizableCharacter {

    private final String character;
    private final boolean randomized;

    public RandomizableCharacter(String character, boolean randomized) {
        this.character = character;
        this.randomized = randomized;
    }

    public static RandomizableCharacter parse(String character) {
        if (character.toUpperCase().equals(character)) {
            return new RandomizableCharacter(character.toLowerCase(), true);
        }

        return new RandomizableCharacter(character, false);
    }

    public String getCharacter() {
        return character;
    }

    public boolean isRandomized() {
        return randomized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RandomizableCharacter that = (RandomizableCharacter) o;
        return randomized == that.randomized &&
                Objects.equals(character, that.character);
    }

    @Override
    public int hashCode() {
        return Objects.hash(character, randomized);
    }

    @Override
    public String toString() {
        return randomized ? character.toUpperCase() : character.toLowerCase();
    }

}
