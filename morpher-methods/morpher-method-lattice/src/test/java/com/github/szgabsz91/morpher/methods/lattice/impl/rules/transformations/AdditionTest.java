package com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations;

import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class AdditionTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConstructor(ICharacterRepository characterRepository) {
        Set<? extends IAttribute> expected = Set.of(Length.SHORT);
        Addition addition = new Addition(expected, characterRepository);
        Set<? extends IAttribute> result = addition.getAttributes();
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsInhomogeneous(ICharacterRepository characterRepository) {
        Addition inhomogeneousAddition = new Addition(Set.of(), characterRepository);
        assertThat(inhomogeneousAddition.isInhomogeneous()).isTrue();

        Addition homogeneousAddition = new Addition(Set.of(Length.SHORT), characterRepository);
        assertThat(homogeneousAddition.isInhomogeneous()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testPerform(ICharacterRepository characterRepository) {
        List<ICharacter> characters = new ArrayList<>(List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("e")
        ));
        Addition addition = new Addition(
                new HashSet<>(characterRepository.getCharacter("i").getAttributes()),
                characterRepository
        );

        assertThat(characters).hasSize(2);
        assertThat(characters).containsSequence(s("a", characterRepository), s("e", characterRepository));
        int result = addition.perform(characters, 1);
        assertThat(characters).hasSize(3);
        assertThat(characters).containsSequence(s("a", characterRepository), s("i", characterRepository), s("e", characterRepository));
        assertThat(result).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testEquals(ICharacterRepository characterRepository) {
        Addition addition1 = new Addition(Set.of(Length.SHORT), characterRepository);
        Addition addition2 = new Addition(Set.of(Length.SHORT), characterRepository);
        assertThat(addition1).isEqualTo(addition1);
        assertThat(addition1).isNotEqualTo(null);
        assertThat(addition1).isNotEqualTo("string");
        assertThat(addition1).isEqualTo(addition2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHashCode(ICharacterRepository characterRepository) {
        Set<IAttribute> attributes = Set.of(Length.SHORT);
        Addition addition = new Addition(attributes, characterRepository);
        assertThat(addition.hashCode()).isEqualTo(attributes.hashCode());
    }

    @Test
    public void testToString() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        Addition addition1 = new Addition(Set.of(Length.SHORT), characterRepository);
        assertThat(addition1).hasToString("ADD [SHORT]");

        Addition addition2 = new Addition(
                new HashSet<>(characterRepository.getCharacter("a").getAttributes()),
                characterRepository
        );
        assertThat(addition2).hasToString("ADD a");
    }

    private ICharacter s(String letter, ICharacterRepository characterRepository) {
        return characterRepository.getCharacter(letter);
    }

}
