package com.github.szgabsz91.morpher.methods.lattice.impl.rules;

import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ContextTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIdentity(ICharacterRepository characterRepository) {
        Context identity = Context.identity();
        assertThat(identity.getPrefix().isEmpty()).isTrue();
        assertThat(identity.getCore()).isEmpty();
        assertThat(identity.getPostfix()).isEmpty();
        assertThat(identity.getFrontPosition()).isEqualTo(Position.identity());
        assertThat(identity.getBackPosition()).isEqualTo(Position.identity());
        assertThat(identity.isIdentity()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConstructor(ICharacterRepository characterRepository) {
        Context context = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                Position.of(1)
        );
        assertThat(context.getPrefix()).hasSize(1);
        assertThat(context.getPrefix()).containsSequence(s("a", characterRepository));
        assertThat(context.getCore()).hasSize(1);
        assertThat(context.getCore()).containsSequence(s("b", characterRepository));
        assertThat(context.getPostfix()).hasSize(1);
        assertThat(context.getPostfix()).containsSequence(s("c", characterRepository));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsIdentityWithNonEmptyPrefix(ICharacterRepository characterRepository) {
        Context context = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(),
                List.of(),
                Position.identity(),
                Position.identity()
        );
        assertThat(context.isIdentity()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsIdentityWithNonEmptyCore(ICharacterRepository characterRepository) {
        Context context = new Context(
                List.of(),
                List.of(characterRepository.getCharacter("a")),
                List.of(),
                Position.identity(),
                Position.identity()
        );
        assertThat(context.isIdentity()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsIdentityWithNonEmptyPostfix(ICharacterRepository characterRepository) {
        Context context = new Context(
                List.of(),
                List.of(),
                List.of(characterRepository.getCharacter("a")),
                Position.identity(),
                Position.identity()
        );
        assertThat(context.isIdentity()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsIdentityWithNullFrontPosition(ICharacterRepository characterRepository) {
        Context context = new Context(
                List.of(),
                List.of(),
                List.of(),
                null,
                Position.identity()
        );
        assertThat(context.isIdentity()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsIdentityWithNonIdentityFrontPosition(ICharacterRepository characterRepository) {
        Context context = new Context(
                List.of(),
                List.of(),
                List.of(),
                Position.of(1),
                Position.identity()
        );
        assertThat(context.isIdentity()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsIdentityWithNullBackPosition(ICharacterRepository characterRepository) {
        Context context = new Context(
                List.of(),
                List.of(),
                List.of(),
                Position.identity(),
                null
        );
        assertThat(context.isIdentity()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsIdentityWithNonIdentityBackPosition(ICharacterRepository characterRepository) {
        Context context = new Context(
                List.of(),
                List.of(),
                List.of(),
                Position.identity(),
                Position.of(1)
        );
        assertThat(context.isIdentity()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testEquals(ICharacterRepository characterRepository) {
        Context context1 = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                Position.of(1)
        );
        Context context2 = new Context(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                Position.of(1),
                Position.of(2)
        );
        Context context3 = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                Position.of(2)
        );
        Context context4 = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                Position.of(1)
        );
        Context context5 = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                null,
                Position.of(1)
        );
        Context context6 = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                null
        );
        Context context7 = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                null
        );
        Context context8 = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                Position.of(2)
        );
        Context context9 = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                Position.of(1)
        );
        Context context10 = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                null
        );

        assertThat(context1).isEqualTo(context1);
        assertThat(context1).isNotEqualTo(null);
        assertThat(context1).isNotEqualTo("string");
        assertThat(context1).isNotEqualTo(context2);
        context2.getPrefix().add(context1.getPrefix().get(0));
        assertThat(context1).isNotEqualTo(context2);
        context2.getCore().add(context1.getCore().get(0));
        assertThat(context1).isNotEqualTo(context2);
        context2.getPostfix().add(context1.getPostfix().get(0));
        assertThat(context1).isNotEqualTo(context2);
        assertThat(context1).isNotEqualTo(context3);
        assertThat(context1).isEqualTo(context4);
        assertThat(context5).isNotEqualTo(context1);
        assertThat(context6).isNotEqualTo(context2);
        assertThat(context6).isEqualTo(context7);
        assertThat(context1).isNotEqualTo(context8);
        assertThat(context1).isEqualTo(context9);
        assertThat(context1).isNotEqualTo(context10);
        assertThat(context10).isNotEqualTo(context1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testHashCode(ICharacterRepository characterRepository) {
        Context context = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                Position.of(1)
        );

        int expected = 31 * context.getPrefix().hashCode() + context.getCore().hashCode();
        expected = 31 * expected + context.getPostfix().hashCode();
        expected = 31 * expected + context.getFrontPosition().hashCode();
        expected = 31 * expected + context.getBackPosition().hashCode();

        assertThat(context.hashCode()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testToString(ICharacterRepository characterRepository) {
        Context context = new Context(
                List.of(characterRepository.getCharacter("a")),
                List.of(characterRepository.getCharacter("b")),
                List.of(characterRepository.getCharacter("c")),
                Position.identity(),
                Position.of(1)
        );
        assertThat(context).hasToString("[[a], [b], [c], [0], [1]]");
    }

    private ICharacter s(String letter, ICharacterRepository characterRepository) {
        return characterRepository.getCharacter(letter);
    }

}
