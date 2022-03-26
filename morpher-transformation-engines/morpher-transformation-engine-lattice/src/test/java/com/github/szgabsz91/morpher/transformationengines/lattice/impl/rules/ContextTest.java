package com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules;

import com.github.szgabsz91.morpher.transformationengines.api.characters.ICharacter;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.transformationengines.api.characters.repositories.ICharacterRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
