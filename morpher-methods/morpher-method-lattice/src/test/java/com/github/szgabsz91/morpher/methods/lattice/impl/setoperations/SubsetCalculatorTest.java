package com.github.szgabsz91.morpher.methods.lattice.impl.setoperations;

import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.Vowel;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.LipShape;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.EmptyNode;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.FullNode;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Context;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Position;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.AttributeDelta;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.ITransformation;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Removal;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Replacement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.szgabsz91.morpher.methods.lattice.impl.setoperations.SubsetCalculator.isSubsetOf;
import static com.github.szgabsz91.morpher.methods.lattice.impl.setoperations.SubsetCalculator.isSubsetOfPosition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SubsetCalculatorTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(Arguments::of);
    }

    @Test
    public void testConstructor() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<SubsetCalculator> constructor = SubsetCalculator.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        try {
            constructor.newInstance();
        }
        finally {
            constructor.setAccessible(false);
        }
    }

    // Node

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithNodeAndFullNode(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        boolean result = isSubsetOf(node, new FullNode());
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithNodeAndEmptyNode(ICharacterRepository characterRepository) {
        Node node = new Node(null);
        boolean result = isSubsetOf(node, new EmptyNode());
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithNodeAndNonMatchingPositions(ICharacterRepository characterRepository) {
        Rule rule1 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        null
                ),
                List.of(),
                characterRepository,
                null
        );
        Node node1 = new Node(rule1);
        Rule rule2 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.of(1),
                        null
                ),
                List.of(),
                characterRepository,
                null
        );
        Node node2 = new Node(rule2);
        boolean result = isSubsetOf(node1, node2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithNodeAndLongerPattern1ThanPattern2(ICharacterRepository characterRepository) {
        Rule rule1 = new Rule(
                new Context(
                        List.of(
                                characterRepository.getCharacter("a"),
                                characterRepository.getCharacter("b")
                        ),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        null,
                        null
                ),
                List.of(),
                characterRepository,
                null
        );
        Node node1 = new Node(rule1);
        Rule rule2 = new Rule(
                new Context(
                        List.of(
                                characterRepository.getCharacter("b")
                        ),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        null,
                        null
                ),
                List.of(),
                characterRepository,
                null
        );
        Node node2 = new Node(rule2);
        boolean result = isSubsetOf(node1, node2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithNodeAndNonMatchingPatterns(ICharacterRepository characterRepository) {
        Rule rule1 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        null,
                        null
                ),
                List.of(),
                characterRepository,
                null
        );
        Node node1 = new Node(rule1);
        Rule rule2 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("b")),
                        List.of(),
                        null,
                        null
                ),
                List.of(),
                characterRepository,
                null
        );
        Node node2 = new Node(rule2);
        boolean result = isSubsetOf(node1, node2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithNodeAndMatchingPatterns(ICharacterRepository characterRepository) {
        Rule rule1 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        null,
                        null
                ),
                List.of(),
                characterRepository,
                null
        );
        Node node1 = new Node(rule1);
        Rule rule2 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        null,
                        null
                ),
                List.of(),
                characterRepository,
                null
        );
        Node node2 = new Node(rule2);
        boolean result = isSubsetOf(node1, node2);
        assertThat(result).isTrue();
    }

    @Test
    public void testIsSubsetOfWithFullNodeAndNode() {
        Node node = new FullNode();
        boolean result = isSubsetOf(node, new Node(null));
        assertThat(result).isTrue();
    }

    @Test
    public void testIsSubsetOfWithEmptyNodeAndNode() {
        Node node = new EmptyNode();
        boolean result = isSubsetOf(node, new Node(null));
        assertThat(result).isFalse();
    }

    // Addition

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithAdditionAndNonAddition(ICharacterRepository characterRepository) {
        Addition addition = new Addition(Set.of(), characterRepository);
        boolean result = isSubsetOf(addition, new Removal(Set.of(), characterRepository));
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithAdditionAndDifferentAttributes(ICharacterRepository characterRepository) {
        Addition addition1 = new Addition(Set.of(Length.SHORT), characterRepository);
        Addition addition2 = new Addition(Set.of(Length.LONG), characterRepository);
        boolean result = isSubsetOf(addition1, addition2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithAdditionAndProperAttributes(ICharacterRepository characterRepository) {
        Addition addition1 = new Addition(Set.of(Length.SHORT), characterRepository);
        Addition addition2 = new Addition(
                Set.of(Length.SHORT, LipShape.ROUNDED),
                characterRepository
        );
        boolean result = isSubsetOf(addition1, addition2);
        assertThat(result).isTrue();
    }

    // AttributeDelta

    @Test
    public void testIsSubsetOfWithAttributeDeltaAndDifferentClasses() {
        AttributeDelta<Length> attributeDelta1 = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);
        AttributeDelta<LipShape> attributeDelta2 = new AttributeDelta<>(LipShape.class, LipShape.ROUNDED, LipShape.UNROUNDED);
        boolean result = isSubsetOf(attributeDelta1, attributeDelta2);
        assertThat(result).isFalse();
    }

    @Test
    public void testIsSubsetOfWithAttributeDeltaAndDifferentFroms() {
        AttributeDelta<Length> attributeDelta1 = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);
        AttributeDelta<Length> attributeDelta2 = new AttributeDelta<>(Length.class, Length.SHORT, Length.SHORT);
        boolean result = isSubsetOf(attributeDelta1, attributeDelta2);
        assertThat(result).isFalse();
    }

    @Test
    public void testIsSubsetOfWithAttributeDeltaAndDifferentTos() {
        AttributeDelta<Length> attributeDelta1 = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);
        AttributeDelta<Length> attributeDelta2 = new AttributeDelta<>(Length.class, Length.LONG, Length.LONG);
        boolean result = isSubsetOf(attributeDelta1, attributeDelta2);
        assertThat(result).isFalse();
    }

    @Test
    public void testIsSubsetOfWithAttributeDeltaAndSameProperties() {
        AttributeDelta<Length> attributeDelta1 = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);
        AttributeDelta<Length> attributeDelta2 = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);
        boolean result = isSubsetOf(attributeDelta1, attributeDelta2);
        assertThat(result).isTrue();
    }

    // Removal

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithRemovalAndNonRemoval(ICharacterRepository characterRepository) {
        Removal removal = new Removal(Set.of(), characterRepository);
        boolean result = isSubsetOf(removal, new Addition(Set.of(), characterRepository));
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithRemovalAndDifferentAttributes(ICharacterRepository characterRepository) {
        Removal removal1 = new Removal(Set.of(Length.SHORT), characterRepository);
        Removal removal2 = new Removal(Set.of(Length.LONG), characterRepository);
        boolean result = isSubsetOf(removal1, removal2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithRemovalAndProperAttributes(ICharacterRepository characterRepository) {
        Removal removal1 = new Removal(Set.of(Length.SHORT), characterRepository);
        Removal removal2 = new Removal(Set.of(Length.SHORT, LipShape.ROUNDED), characterRepository);
        boolean result = isSubsetOf(removal1, removal2);
        assertThat(result).isTrue();
    }

    // Replacement

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithReplacementAndNonReplacement(ICharacterRepository characterRepository) {
        Replacement replacement = new Replacement(Set.of(), characterRepository);
        boolean result = isSubsetOf(replacement, new Addition(Set.of(), characterRepository));
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithReplacementAndNonCommonAttributeDeltas(ICharacterRepository characterRepository) {
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
        boolean result = isSubsetOf(replacement1, replacement2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithReplacementAndInvalidAttributeDelta(ICharacterRepository characterRepository) {
        @SuppressWarnings("unchecked")
        Replacement replacement1 = new Replacement(
                Set.of(
                        new AttributeDelta(Length.class, Length.LONG, Length.SHORT)
                ),
                characterRepository
        );
        @SuppressWarnings("unchecked")
        Replacement replacement2 = new Replacement(
                Set.of(
                        new AttributeDelta(Length.class, Length.SHORT, Length.LONG)
                ),
                characterRepository
        );
        boolean result = isSubsetOf(replacement1, replacement2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithReplacementAndTrueResult(ICharacterRepository characterRepository) {
        @SuppressWarnings("unchecked")
        Replacement replacement1 = new Replacement(
                Set.of(
                        new AttributeDelta(Length.class, Length.LONG, Length.SHORT)
                ),
                characterRepository
        );
        @SuppressWarnings("unchecked")
        Replacement replacement2 = new Replacement(
                new HashSet(List.of(
                        new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT),
                        new AttributeDelta<>(LipShape.class, LipShape.ROUNDED, LipShape.UNROUNDED)
                )),
                characterRepository
        );
        boolean result = isSubsetOf(replacement1, replacement2);
        assertThat(result).isTrue();
    }

    @Test
    public void testIsSubsetOfWithUnknownTransformationType() {
        ITransformation transformation1 = new UnknownTransformation();
        ITransformation transformation2 = new UnknownTransformation();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> isSubsetOf(transformation1, transformation2));
        assertThat(exception).hasMessage("Unknown transformation types found: " + transformation1 + " and " + transformation2);
    }

    // Context

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithContextAndInvalidPrefixes(ICharacterRepository characterRepository) {
        Context context1 = new Context(
                List.of(Vowel.create(Length.LONG)),
                null,
                null,
                null,
                null
        );
        Context context2 = new Context(
                List.of(Vowel.create(Length.SHORT)),
                null,
                null,
                null,
                null
        );
        boolean result = isSubsetOf(context1, context2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithContextAndDifferentSizedCores(ICharacterRepository characterRepository) {
        Context context1 = new Context(
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                null,
                null,
                null
        );
        Context context2 = new Context(
                List.of(
                        Vowel.create(Length.SHORT),
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG),
                        Vowel.create(Length.SHORT)
                ),
                null,
                null,
                null
        );
        boolean result = isSubsetOf(context1, context2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithContextAndInvalidCores(ICharacterRepository characterRepository) {
        Context context1 = new Context(
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                null,
                null,
                null
        );
        Context context2 = new Context(
                List.of(
                        Vowel.create(Length.SHORT),
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG),
                        Vowel.create(Length.SHORT)
                ),
                null,
                null,
                null
        );
        boolean result = isSubsetOf(context1, context2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithContextAndInvalidPostfixes(ICharacterRepository characterRepository) {
        Context context1 = new Context(
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                null,
                null
        );
        Context context2 = new Context(
                List.of(
                        Vowel.create(Length.SHORT),
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.SHORT),
                        Vowel.create(Length.SHORT)
                ),
                null,
                null
        );
        boolean result = isSubsetOf(context1, context2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithContextAndTrueResult(ICharacterRepository characterRepository) {
        Context context1 = new Context(
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                null,
                Position.identity()
        );
        Context context2 = new Context(
                List.of(
                        Vowel.create(Length.SHORT),
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG),
                        Vowel.create(Length.SHORT)
                ),
                null,
                Position.identity()
        );
        boolean result = isSubsetOf(context1, context2);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithContextAndNonMatchingPositions(ICharacterRepository characterRepository) {
        Context context1 = new Context(
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                Position.of(1),
                null
        );
        Context context2 = new Context(
                List.of(
                        Vowel.create(Length.SHORT),
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG),
                        Vowel.create(Length.SHORT)
                ),
                Position.identity(),
                null
        );
        boolean result = isSubsetOf(context1, context2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithContextAndLongerThisPrefixThanOtherPrefix(ICharacterRepository characterRepository) {
        Context context1 = new Context(
                List.of(
                        Vowel.create(Length.SHORT),
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                Position.of(1),
                null
        );
        Context context2 = new Context(
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG),
                        Vowel.create(Length.SHORT)
                ),
                Position.of(1),
                null
        );
        boolean result = isSubsetOf(context1, context2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithContextAndLongerThisPostfixThanOtherPostfix(ICharacterRepository characterRepository) {
        Context context1 = new Context(
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG),
                        Vowel.create(Length.SHORT)
                ),
                Position.of(1),
                null
        );
        Context context2 = new Context(
                List.of(
                        Vowel.create(Length.SHORT),
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                Position.of(1),
                null
        );
        boolean result = isSubsetOf(context1, context2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfWithContextAndNonMatchingCores(ICharacterRepository characterRepository) {
        Context context1 = new Context(
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                null,
                Position.identity()
        );
        Context context2 = new Context(
                List.of(
                        Vowel.create(Length.SHORT),
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.SHORT)
                ),
                List.of(
                        Vowel.create(Length.LONG),
                        Vowel.create(Length.SHORT)
                ),
                null,
                Position.identity()
        );
        boolean result = isSubsetOf(context1, context2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfPositionWithContextAndNonNullThisFrontPositionAndNullOtherFrontPosition(ICharacterRepository characterRepository) {
        Context context1 = new Context(null, null, null, Position.identity(), Position.identity());
        Context context2 = new Context(null, null, null, null, Position.identity());
        boolean result = isSubsetOfPosition(context1, context2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfPositionWithContextAndFrontPositionsReturningTrue(ICharacterRepository characterRepository) {
        Context context1 = new Context(null, null, null, Position.identity(), Position.identity());
        Context context2 = new Context(null, null, null, Position.identity(), Position.identity());
        boolean result = isSubsetOfPosition(context1, context2);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfPositionWithContextAndFrontPositionsReturningFalse(ICharacterRepository characterRepository) {
        Context context1 = new Context(null, null, null, Position.identity(), Position.of(1));
        Context context2 = new Context(null, null, null, Position.identity(), Position.identity());
        boolean result = isSubsetOfPosition(context1, context2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfPositionWithContextAndNullFrontPositions(ICharacterRepository characterRepository) {
        Context context1 = new Context(null, null, null, null, Position.identity());
        Context context2 = new Context(null, null, null, null, Position.identity());
        boolean result = isSubsetOfPosition(context1, context2);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfPositionWithContextAndNonNullThisBackPositionAndNullOtherBackPosition(ICharacterRepository characterRepository) {
        Context context1 = new Context(null, null, null, Position.identity(), Position.identity());
        Context context2 = new Context(null, null, null, Position.identity(), null);
        boolean result = isSubsetOfPosition(context1, context2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfPositionWithContextAndBackPositionsReturningTrue(ICharacterRepository characterRepository) {
        Context context1 = new Context(null, null, null, Position.identity(), Position.identity());
        Context context2 = new Context(null, null, null, Position.identity(), Position.identity());
        boolean result = isSubsetOfPosition(context1, context2);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfPositionWithContextAndBackPositionsReturningFalse(ICharacterRepository characterRepository) {
        Context context1 = new Context(null, null, null, Position.identity(), Position.identity());
        Context context2 = new Context(null, null, null, Position.identity(), Position.of(1));
        boolean result = isSubsetOfPosition(context1, context2);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIsSubsetOfPositionWithContextAndNullBackPositions(ICharacterRepository characterRepository) {
        Context thisContext = new Context(null, null, null, Position.identity(), null);
        Context otherContext = new Context(null, null, null, Position.identity(), null);
        boolean result = isSubsetOfPosition(thisContext, otherContext);
        assertThat(result).isTrue();
    }

    // Position

    @Test
    public void testIsSubsetOfWithPositionAndNonEqualPositions() {
        Position position1 = Position.of(4);
        Position position2 = Position.of(5);
        boolean result = isSubsetOf(position1, position2);
        assertThat(result).isFalse();
    }

    @Test
    public void testIsSubsetOfWithPositionAndEqualPositions() {
        Position position1 = Position.of(5);
        Position position2 = Position.of(5);
        boolean result = isSubsetOf(position1, position2);
        assertThat(result).isTrue();
    }

    // Character

    @Test
    public void testCharacterIntersectionWithUnknownCharacterType() {
        ICharacter character1 = new UnknownCharacter();
        ICharacter character2 = new UnknownCharacter();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> isSubsetOf(character1, character2));
        assertThat(exception).hasMessage("Uncovered character types found: " + character1 + " and " + character2);
    }

}
