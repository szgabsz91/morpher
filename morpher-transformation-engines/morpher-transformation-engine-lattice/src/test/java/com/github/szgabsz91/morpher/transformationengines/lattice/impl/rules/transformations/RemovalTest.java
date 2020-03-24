package com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations;

import com.github.szgabsz91.morpher.transformationengines.api.characters.ICharacter;
import com.github.szgabsz91.morpher.transformationengines.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.consonant.SoundProductionPlace;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.consonant.Voice;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel.VerticalTonguePosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class RemovalTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConstructor(ICharacterRepository characterRepository) {
        Set<? extends IAttribute> expected = Set.of(Length.SHORT);
        Removal removal = new Removal(expected, characterRepository);
        Set<? extends IAttribute> result = removal.getAttributes();
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsInconsistent(ICharacterRepository characterRepository) {
        Removal inconsistentRemoval = new Removal(Set.of(), characterRepository);
        assertThat(inconsistentRemoval.isInconsistent()).isTrue();

        Removal consistentRemoval = new Removal(Set.of(Length.SHORT), characterRepository);
        assertThat(consistentRemoval.isInconsistent()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testPerformWithFullRemoval(ICharacterRepository characterRepository) {
        List<ICharacter> characters = new ArrayList<>(List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("e"),
                characterRepository.getCharacter("i")
        ));
        Removal removal = new Removal(new HashSet<>(characters.get(1).getAttributes()), characterRepository);

        assertThat(characters).hasSize(3);
        assertThat(characters).containsSequence(s("a", characterRepository), s("e", characterRepository), s("i", characterRepository));
        int result = removal.perform(characters, 1);
        assertThat(characters).hasSize(2);
        assertThat(characters).containsSequence(s("a", characterRepository), s("i", characterRepository));
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void testPerformWithRemovingSomeAttributes() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        Set<IAttribute> attributes = new HashSet<>(characterRepository.getCharacter("a").getAttributes());
        attributes.add(SoundProductionPlace.BILABIAL);
        List<ICharacter> characters = Arrays.asList(
                new CustomCharacter(attributes),
                characterRepository.getCharacter("e"),
                characterRepository.getCharacter("i")
        );
        Removal removal = new Removal(
                Set.of(SoundProductionPlace.BILABIAL),
                characterRepository
        );

        assertThat(characters).hasSize(3);
        assertThat(characters).containsSequence(new CustomCharacter(attributes), s("e", characterRepository), s("i", characterRepository));
        int result = removal.perform(characters, 0);
        assertThat(characters).hasSize(3);
        assertThat(characters.get(0)).hasToString("a");
        assertThat(characters.get(1)).hasToString("e");
        assertThat(characters.get(2)).hasToString("i");
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void testPerformWithRemovingSomeAttributesButGettingAnInvalidCharacter() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("e"),
                characterRepository.getCharacter("i")
        );
        Removal removal = new Removal(
                Set.of(Length.SHORT),
                characterRepository
        );

        assertThat(characters).hasSize(3);
        assertThat(characters).containsSequence(s("a", characterRepository), s("e", characterRepository), s("i", characterRepository));
        int result = removal.perform(characters, 0);
        assertThat(characters).hasSize(3);
        assertThat(characters.get(0)).hasToString("a");
        assertThat(characters.get(1)).hasToString("e");
        assertThat(characters.get(2)).hasToString("i");
        assertThat(result).isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testPerformWithLeavingCharacterIntact(ICharacterRepository characterRepository) {
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("s"),
                characterRepository.getCharacter("i")
        );
        Removal removal = new Removal(Set.of(VerticalTonguePosition.SEMI_OPEN), characterRepository);

        assertThat(characters).hasSize(3);
        assertThat(characters).containsSequence(s("a", characterRepository), s("s", characterRepository), s("i", characterRepository));
        int result = removal.perform(characters, 1);
        assertThat(characters).hasSize(3);
        assertThat(characters).containsSequence(s("a", characterRepository), s("s", characterRepository), s("i", characterRepository));
        assertThat(result).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testPerformWithNoAttributeModificationNecessary(ICharacterRepository characterRepository) {
        List<ICharacter> characters = List.of(
                characterRepository.getCharacter("a"),
                characterRepository.getCharacter("e"),
                characterRepository.getCharacter("i")
        );
        Removal removal = new Removal(Set.of(Voice.UNVOICED), characterRepository);

        assertThat(characters).hasSize(3);
        assertThat(characters).containsSequence(s("a", characterRepository), s("e", characterRepository), s("i", characterRepository));
        int result = removal.perform(characters, 1);
        assertThat(characters).hasSize(3);
        assertThat(characters).containsSequence(s("a", characterRepository), s("e", characterRepository), s("i", characterRepository));
        assertThat(result).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testPerformWithIndexOverflow(ICharacterRepository characterRepository) {
        int index = 0;
        Removal removal = new Removal(Set.of(), characterRepository);
        int result = removal.perform(List.of(), index);
        assertThat(result).isEqualTo(index);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testEquals(ICharacterRepository characterRepository) {
        Removal removal1 = new Removal(Set.of(Length.SHORT), characterRepository);
        Removal removal2 = new Removal(Set.of(Length.SHORT), characterRepository);

        assertThat(removal1.equals(removal1)).isTrue();
        assertThat(removal1).isNotEqualTo(null);
        assertThat(removal1).isNotEqualTo("string");
        assertThat(removal1).isEqualTo(removal2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHashCode(ICharacterRepository characterRepository) {
        Set<IAttribute> attributes = Set.of(Length.SHORT);
        Removal removal = new Removal(attributes, characterRepository);
        assertThat(removal.hashCode()).isEqualTo(attributes.hashCode());
    }

    @Test
    public void testToString() {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        Removal removal1 = new Removal(Set.of(Length.SHORT), characterRepository);
        assertThat(removal1).hasToString("REMOVE [SHORT]");

        Removal removal2 = new Removal(
                new HashSet<>(characterRepository.getCharacter("a").getAttributes()),
                characterRepository
        );
        assertThat(removal2).hasToString("REMOVE a");
    }

    private ICharacter s(String letter, ICharacterRepository characterRepository) {
        return characterRepository.getCharacter(letter);
    }

    private static final class CustomCharacter implements ICharacter {

        private final Set<IAttribute> attributes;

        private CustomCharacter(Set<IAttribute> attributes) {
            this.attributes = attributes;
        }

        @Override
        public IAttribute get(Class<? extends IAttribute> clazz) {
            return null;
        }

        @Override
        public Collection<? extends IAttribute> getAttributes() {
            return attributes;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean isStart() {
            return false;
        }

        @Override
        public boolean isEnd() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CustomCharacter that = (CustomCharacter) o;

            return attributes.equals(that.attributes);

        }

        @Override
        public int hashCode() {
            return attributes.hashCode();
        }

    }

}
