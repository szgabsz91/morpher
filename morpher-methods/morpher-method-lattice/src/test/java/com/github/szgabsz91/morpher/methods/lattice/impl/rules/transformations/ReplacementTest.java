package com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations;

import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.ISound;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.Vowel;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.HorizontalTonguePosition;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.LipShape;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.VerticalTonguePosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

public class ReplacementTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConstructorWithAttributeDeltas(ICharacterRepository characterRepository) {
        @SuppressWarnings("unchecked")
        Set<AttributeDelta<? super IAttribute>> attributeDeltas = Set.of(
                new AttributeDelta(Length.class, Length.LONG, Length.SHORT),
                new AttributeDelta(LipShape.class, LipShape.ROUNDED, LipShape.UNROUNDED)
        );
        Replacement replacement = new Replacement(attributeDeltas, characterRepository);
        assertThat(replacement.getAttributeDeltas()).isEqualTo(attributeDeltas);
    }

    @Test
    public void testConstructorWithFromAndTo() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        ICharacter from = characterRepository.getCharacter("a");
        ICharacter to = characterRepository.getCharacter("á");
        Replacement replacement = new Replacement(from, to, characterRepository);
        @SuppressWarnings("unchecked")
        Set<AttributeDelta<? super IAttribute>> expected = Set.of(
                new AttributeDelta(Length.class, Length.SHORT, Length.LONG)
        );
        assertThat(replacement.getAttributeDeltas()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsInhomogeneous(ICharacterRepository characterRepository) {
        Replacement inhomogeneousReplacement = new Replacement(Set.of(), characterRepository);
        assertThat(inhomogeneousReplacement.isInhomogeneous()).isTrue();

        @SuppressWarnings("unchecked")
        Replacement homogeneousReplacement = new Replacement(
                new HashSet(List.of(Length.class, Length.LONG, Length.SHORT)),
                characterRepository
        );
        assertThat(homogeneousReplacement.isInhomogeneous()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testPerformWithNormalLetterReplacement(ICharacterRepository characterRepository) {
        ICharacter from = characterRepository.getCharacter("e");
        ICharacter to = characterRepository.getCharacter("é");
        Replacement replacement = new Replacement(from, to, characterRepository);
        List<ICharacter> characters = Arrays.asList(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("e"),
                characterRepository.getCharacter("i")
        );
        assertThat(characters).hasSize(3);
        assertThat(characters).containsSequence(s("a", characterRepository), s("e", characterRepository), s("i", characterRepository));
        int result = replacement.perform(characters, 1);
        assertThat(characters).hasSize(3);
        assertThat(characters).containsSequence(s("a", characterRepository), s("é", characterRepository), s("i", characterRepository));
        assertThat(result).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testPerformWithInvalidAttributeTypeInAttributeDelta(ICharacterRepository characterRepository) {
        ISound from = Vowel.create(LipShape.UNROUNDED, HorizontalTonguePosition.FRONT, VerticalTonguePosition.SEMI_OPEN);
        @SuppressWarnings("unchecked")
        Replacement replacement = new Replacement(
                Set.of(
                        new AttributeDelta(Length.class, Length.SHORT, Length.LONG)
                ),
                characterRepository
        );
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("a"),
                from,
                characterRepository.getCharacter("i")
        );
        assertThat(characters).hasSize(3);
        assertThat(characters).containsSequence(s("a", characterRepository), from, s("i", characterRepository));
        int result = replacement.perform(characters, 1);
        assertThat(characters).hasSize(3);
        assertThat(characters).containsSequence(s("a", characterRepository), from, s("i", characterRepository));
        assertThat(result).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testPerformWithIndexOverflow(ICharacterRepository characterRepository) {
        int index = 0;
        Replacement replacement = new Replacement(Set.of(), characterRepository);
        int result = replacement.perform(List.of(), index);
        assertThat(result).isEqualTo(index);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testEquals(ICharacterRepository characterRepository) {
        @SuppressWarnings("unchecked")
        Replacement replacement1 = new Replacement(
                new HashSet(List.of(
                        new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT),
                        new AttributeDelta<>(LipShape.class, LipShape.ROUNDED, LipShape.UNROUNDED)
                )),
                characterRepository
        );
        @SuppressWarnings("unchecked")
        Replacement replacement2 = new Replacement(
                Set.of(
                        new AttributeDelta(Length.class, Length.LONG, Length.SHORT)
                ),
                characterRepository
        );

        assertThat(replacement1).isEqualTo(replacement1);
        assertThat(replacement1).isNotEqualTo(null);
        assertThat(replacement1).isNotEqualTo("string");
        assertThat(replacement1).isNotEqualTo(replacement2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHashCode(ICharacterRepository characterRepository) {
        @SuppressWarnings("unchecked")
        Replacement replacement = new Replacement(
                new HashSet(List.of(
                        new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT),
                        new AttributeDelta<>(LipShape.class, LipShape.ROUNDED, LipShape.UNROUNDED)
                )),
                characterRepository
        );
        assertThat(replacement.hashCode()).isEqualTo(replacement.getAttributeDeltas().hashCode());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testToStringWithInvariantReplacement(ICharacterRepository characterRepository) {
        Replacement replacement = new Replacement(Set.of(), characterRepository);
        assertThat(replacement).hasToString("INVARIANT_REPLACEMENT");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testToStringWithVariantReplacement(ICharacterRepository characterRepository) {
        @SuppressWarnings("unchecked")
        Replacement replacement = new Replacement(
                new HashSet(List.of(
                        new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT),
                        new AttributeDelta<>(LipShape.class, LipShape.ROUNDED, LipShape.UNROUNDED)
                )),
                characterRepository
        );
        String expected = replacement.getAttributeDeltas()
                .stream()
                .map(AttributeDelta::toString)
                .collect(joining(", "));
        assertThat(replacement).hasToString(expected);
    }

    @Test
    public void testPerformWithIllegalArgumentExceptionFromCharacterRepository() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        Replacement replacement = new Replacement(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("s"),
                characterRepository
        );
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("e"),
                characterRepository.getCharacter("s")
        );
        int result = replacement.perform(characters, 0);
        assertThat(characters).hasSize(2);
        assertThat(characters).containsSequence(s("e", characterRepository), s("s", characterRepository));
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void testPerformWithInvalidResultingCharacter() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        @SuppressWarnings("unchecked")
        Replacement replacement = new Replacement(
                Set.of(
                        new AttributeDelta(
                                LipShape.class,
                                LipShape.UNROUNDED,
                                LipShape.ROUNDED
                        )
                ),
                characterRepository
        );
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("e"),
                characterRepository.getCharacter("s")
        );
        int result = replacement.perform(characters, 0);
        assertThat(characters).hasSize(2);
        assertThat(characters).containsSequence(s("e", characterRepository), s("s", characterRepository));
        assertThat(result).isEqualTo(0);
    }

    private ICharacter s(String letter, ICharacterRepository characterRepository) {
        return characterRepository.getCharacter(letter);
    }

}
