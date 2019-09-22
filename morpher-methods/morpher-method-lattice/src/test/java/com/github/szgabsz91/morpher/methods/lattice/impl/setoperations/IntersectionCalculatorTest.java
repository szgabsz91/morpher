package com.github.szgabsz91.morpher.methods.lattice.impl.setoperations;

import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.Consonant;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.EndSound;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.StartSound;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.Vowel;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.Discriminator;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.SoundProductionPlace;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.SoundProductionWay;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.UvulaPosition;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.Voice;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.LipShape;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.VerticalTonguePosition;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
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
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.IWordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.WordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
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

import static com.github.szgabsz91.morpher.methods.lattice.impl.setoperations.IntersectionCalculator.intersect;
import static com.github.szgabsz91.morpher.methods.lattice.impl.setoperations.SubsetCalculator.isSubsetOf;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IntersectionCalculatorTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(Arguments::of);
    }

    @Test
    public void testConstructor() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<IntersectionCalculator> constructor = IntersectionCalculator.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        try {
            constructor.newInstance();
        }
        finally {
            constructor.setAccessible(false);
        }
    }

    // Nodes

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithNodeAndNode(ICharacterRepository characterRepository) throws IntersectionException {
        Rule rule1 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.of(1)
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
                        Position.identity(),
                        Position.of(1)
                ),
                List.of(),
                characterRepository,
                null
        );
        Node node2 = new Node(rule2);
        Node intersection = intersect(node1, node2, characterRepository, null);
        Rule ruleIntersection = intersect(rule1, rule2, characterRepository, null);
        assertThat(intersection.getRule()).isEqualTo(ruleIntersection);
        assertThat(intersection.getParents()).isEmpty();
        assertThat(intersection.getChildren()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithInhomogeneousNode1AndInhomogeneousNode2(ICharacterRepository characterRepository) throws IntersectionException {
        Rule rule = new Rule(
                new Context(
                        List.of(),
                        List.of(),
                        List.of(characterRepository.getCharacter("#")),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        Node node1 = new Node(rule, true);
        Node node2 = new Node(rule, true);
        Node intersection = intersect(node1, node2, characterRepository, null);
        assertThat(intersection.getRule()).isEqualTo(rule);
        assertThat(intersection.isInhomogeneous()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithInhomogeneousNode1AndHomogeneousNode2(ICharacterRepository characterRepository) throws IntersectionException {
        Rule rule = new Rule(
                new Context(
                        List.of(),
                        List.of(),
                        List.of(characterRepository.getCharacter("#")),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        Node node1 = new Node(rule, true);
        Node node2 = new Node(rule, false);
        Node intersection = intersect(node1, node2, characterRepository, null);
        assertThat(intersection.getRule()).isEqualTo(rule);
        assertThat(intersection.isInhomogeneous()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithHomogeneousNode1AndInhomogeneousNode2(ICharacterRepository characterRepository) throws IntersectionException {
        Rule rule = new Rule(
                new Context(
                        List.of(),
                        List.of(),
                        List.of(characterRepository.getCharacter("#")),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        Node node1 = new Node(rule, false);
        Node node2 = new Node(rule, true);
        Node intersection = intersect(node1, node2, characterRepository, null);
        assertThat(intersection.getRule()).isEqualTo(rule);
        assertThat(intersection.isInhomogeneous()).isTrue();
    }

    @Test
    public void testIntersectWithHomogeneousNodeBecomingInhomogeneousBasedOnNode1WithHungarianAttributedCharacterRepository() throws IntersectionException {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        ICharacter a = characterRepository.getCharacter("a");
        ICharacter e = characterRepository.getCharacter("e");
        Node node1 = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(),
                                List.of(a),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                ),
                false
        );
        Node node2 = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(),
                                List.of(e),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                ),
                false
        );
        Node intersection = intersect(node1, node2, characterRepository, null);
        assertThat(intersection.getRule()).isNotNull();
        assertThat(intersection.getRule().getContext()).isNotNull();
        assertThat(intersection.getRule().getContext().getPrefix()).isEmpty();
        assertThat(intersection.getRule().getContext().getCore()).isEmpty();
        assertThat(intersection.getRule().getContext().getPostfix()).hasSize(1);
        assertThat(intersection.getRule().getContext().getPostfix()).containsSequence(intersect(a, e));
        assertThat(intersection.isInhomogeneous()).isTrue();
    }

    @Test
    public void testIntersectWithHomogeneousNodeBecomingInhomogeneousBasedOnNode1WithHungarianSimpleCharacterRepository() {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        ICharacter a = characterRepository.getCharacter("a");
        ICharacter e = characterRepository.getCharacter("e");
        Node node1 = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(),
                                List.of(a),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                ),
                false
        );
        Node node2 = new Node(
                new Rule(
                        new Context(
                                List.of(),
                                List.of(),
                                List.of(e),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                ),
                false
        );
        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(node1, node2, characterRepository, null));
        assertThat(exception.getMessage()).isEqualTo("All 3 parts of the context became empty!");
    }

    @Test
    public void testIntersectAndIsSubsetOfWithChangingMiddleRegression1() throws IntersectionException {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        Node node1 = new Node(
                new Rule(
                        new Context(
                                List.of(
                                        StartSound.get(),
                                        characterRepository.getCharacter("f"),
                                        characterRepository.getCharacter("ü"),
                                        characterRepository.getCharacter("v"),
                                        characterRepository.getCharacter("e"),
                                        characterRepository.getCharacter("z"),
                                        characterRepository.getCharacter("é"),
                                        characterRepository.getCharacter("s")
                                ),
                                List.of(),
                                List.of(
                                        characterRepository.getCharacter("k"),
                                        characterRepository.getCharacter("g"),
                                        characterRepository.getCharacter("v"),
                                        characterRepository.getCharacter("n"),
                                        EndSound.get()
                                ),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )
        );
        Node node2 = new Node(
                new Rule(
                        new Context(
                                List.of(
                                        StartSound.get(),
                                        characterRepository.getCharacter("m"),
                                        characterRepository.getCharacter("e"),
                                        characterRepository.getCharacter("g"),
                                        characterRepository.getCharacter("r"),
                                        characterRepository.getCharacter("o"),
                                        characterRepository.getCharacter("h"),
                                        characterRepository.getCharacter("a"),
                                        characterRepository.getCharacter("n"),
                                        characterRepository.getCharacter("á"),
                                        characterRepository.getCharacter("s")
                                ),
                                List.of(),
                                List.of(
                                        characterRepository.getCharacter("m"),
                                        characterRepository.getCharacter("g"),
                                        characterRepository.getCharacter("b"),
                                        characterRepository.getCharacter("u"),
                                        EndSound.get()
                                ),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                )
        );
        Node expectedIntersection = new Node(
                new Rule(
                        new Context(
                                List.of(
                                        Consonant.create(UvulaPosition.ORAL, Discriminator.NORMAL),
                                        Vowel.create(Length.SHORT, LipShape.ROUNDED),
                                        Consonant.create(SoundProductionWay.FRICATIVE, UvulaPosition.ORAL,
                                                Discriminator.NORMAL),
                                        Vowel.create(VerticalTonguePosition.SEMI_OPEN, Length.SHORT),
                                        Consonant.create(Voice.VOICED, SoundProductionPlace.DENTAL_ALVEOLAR,
                                                Discriminator.NORMAL),
                                        Vowel.create(Length.LONG, VerticalTonguePosition.SEMI_OPEN),
                                        characterRepository.getCharacter("s")
                                ),
                                List.of(),
                                List.of(
                                        Consonant.create(SoundProductionWay.PLOSIVE, Discriminator.NORMAL),
                                        characterRepository.getCharacter("g"),
                                        Consonant.create(Voice.VOICED, UvulaPosition.ORAL, Discriminator.NORMAL)
                                ),
                                Position.identity(),
                                Position.identity()
                        ),
                        List.of(
                                new Addition(
                                        new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                        characterRepository
                                )
                        ),
                        characterRepository,
                        null
                ),
                true
        );
        Node intersection = intersect(node1, node2, characterRepository, null);
        assertThat(intersection).isEqualTo(expectedIntersection);
        assertThat(isSubsetOf(intersection, node1)).isTrue();
        assertThat(isSubsetOf(intersection, node2)).isTrue();
    }

    @Test
    public void testIntersectWithFullNodeAndNode() throws IntersectionException {
        Node node = new FullNode();
        Node otherNode = new Node(null);
        Node result = intersect(node, otherNode, null, null);
        assertThat(result).isSameAs(otherNode);
    }

    @Test
    public void testIntersectWithEmptyNodeAndNode() throws IntersectionException {
        Node node = new EmptyNode();
        Node otherNode = new Node(null);
        Node result = intersect(node, otherNode, null, null);
        assertThat(result).isEqualTo(node);
    }

    // Addition

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithAdditionAndNonAddition(ICharacterRepository characterRepository) {
        Addition addition = new Addition(Set.of(), characterRepository);
        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(addition, new Removal(Set.of(), characterRepository), characterRepository));
        assertThat(exception.getMessage()).isEqualTo("The two transformations have different types");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithAdditionAndDifferentAttributes(ICharacterRepository characterRepository) {
        Addition addition1 = new Addition(Set.of(Length.SHORT), characterRepository);
        Addition addition2 = new Addition(Set.of(Length.LONG), characterRepository);
        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(addition1, addition2, characterRepository));
        assertThat(exception.getMessage()).isEqualTo("The two additions' intersection wouldn't be performable!");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithAdditionAndSameAttributes(ICharacterRepository characterRepository) throws IntersectionException {
        Addition addition1 = new Addition(Set.of(Length.SHORT), characterRepository);
        Addition addition2 = new Addition(Set.of(Length.SHORT), characterRepository);
        Addition intersection = (Addition) intersect(addition1, addition2, characterRepository);
        assertThat(intersection).isEqualTo(addition1);
    }

    // AttributeDelta

    @Test
    public void testIntersectWithAttributeDeltasAndDifferentClasses() {
        @SuppressWarnings("rawtypes")
        AttributeDelta attributeDelta1 = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);
        @SuppressWarnings("rawtypes")
        AttributeDelta attributeDelta2 = new AttributeDelta<>(LipShape.class, LipShape.ROUNDED, LipShape.UNROUNDED);
        @SuppressWarnings("unchecked")
        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(attributeDelta1, attributeDelta2));
        assertThat(exception.getMessage()).isEqualTo("The class of the two attribute deltas are not the same");
    }

    @Test
    public void testIntersectWithAttributeDeltasAndDifferentFroms() {
        @SuppressWarnings("rawtypes")
        AttributeDelta attributeDelta1 = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);
        @SuppressWarnings("rawtypes")
        AttributeDelta attributeDelta2 = new AttributeDelta<>(Length.class, Length.SHORT, Length.SHORT);
        @SuppressWarnings("unchecked")
        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(attributeDelta1, attributeDelta2));
        assertThat(exception.getMessage()).isEqualTo("The from property of the two attribute deltas are not the same");
    }

    @Test
    public void testIntersectWithAttributeDeltasAndDifferentTos() {
        @SuppressWarnings("rawtypes")
        AttributeDelta attributeDelta1 = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);
        @SuppressWarnings("rawtypes")
        AttributeDelta attributeDelta2 = new AttributeDelta<>(Length.class, Length.LONG, Length.LONG);
        @SuppressWarnings("unchecked")
        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(attributeDelta1, attributeDelta2));
        assertThat(exception.getMessage()).isEqualTo("The to property of the two attribute deltas are not the same");
    }

    @Test
    public void testIntersectWithAttributeDeltasAndSameProperties() throws IntersectionException {
        @SuppressWarnings("rawtypes")
        AttributeDelta attributeDelta1 = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);
        @SuppressWarnings("rawtypes")
        AttributeDelta attributeDelta2 = new AttributeDelta<>(Length.class, Length.LONG, Length.SHORT);
        @SuppressWarnings("unchecked")
        AttributeDelta<? extends IAttribute> intersection = intersect(attributeDelta1, attributeDelta2);
        assertThat(intersection).isEqualTo(attributeDelta1);
    }

    // Removal

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithRemovalAndNonRemoval(ICharacterRepository characterRepository) {
        Removal removal = new Removal(Set.of(), characterRepository);
        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(removal, new Addition(Set.of(), characterRepository), characterRepository));
        assertThat(exception.getMessage()).isEqualTo("The two transformations have different types");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithRemovalAndDifferentAttributes(ICharacterRepository characterRepository) {
        Removal removal1 = new Removal(Set.of(Length.SHORT), characterRepository);
        Removal removal2 = new Removal(Set.of(Length.LONG), characterRepository);
        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(removal1, removal2, characterRepository));
        assertThat(exception.getMessage()).isEqualTo("The two removals' intersection wouldn't be performable!");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithRemovalAndSameAttributes(ICharacterRepository characterRepository) throws IntersectionException {
        Removal removal1 = new Removal(Set.of(Length.SHORT), characterRepository);
        Removal removal2 = new Removal(Set.of(Length.SHORT), characterRepository);
        Removal intersection = (Removal) intersect(removal1, removal2, characterRepository);
        assertThat(intersection).isSameAs(removal1);
    }

    // Replacement

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithReplacementAndNonReplacement(ICharacterRepository characterRepository) {
        Replacement replacement = new Replacement(Set.of(), characterRepository);
        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(replacement, new Addition(Set.of(), characterRepository), characterRepository));
        assertThat(exception.getMessage()).isEqualTo("The two transformations have different types");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithReplacementAndReplacement(ICharacterRepository characterRepository) throws IntersectionException {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Replacement replacement1 = new Replacement(
                Set.of(
                        new AttributeDelta(Length.class, Length.LONG, Length.SHORT)
                ),
                characterRepository
        );
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Replacement replacement2 = new Replacement(
                Set.of(
                        new AttributeDelta(Length.class, Length.LONG, Length.SHORT)
                ),
                characterRepository
        );
        Replacement intersection = (Replacement) intersect(replacement1, replacement2, characterRepository);
        assertThat(intersection.getAttributeDeltas()).isEqualTo(replacement1.getAttributeDeltas());
    }

    @Test
    public void testIntersectWithUnknownTransformationType() {
        ITransformation transformation1 = new UnknownTransformation();
        ITransformation transformation2 = new UnknownTransformation();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> intersect(transformation1, transformation2, null));
        assertThat(exception).hasMessage("Unknown transformation types found: " + transformation1 + " and " + transformation2);
    }

    // Context

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndDroppingLetters(ICharacterRepository characterRepository) throws IntersectionException {
        Context context1 = new Context(
                List.of(
                        Consonant.create(),
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG),
                        Consonant.create()
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
        Context intersection = intersect(context1, context2);

        assertThat(intersection.getPrefix()).hasSize(1);
        assertThat(intersection.getPrefix()).containsSequence(Vowel.create(Length.LONG));
        assertThat(intersection.getCore()).isEqualTo(context1.getCore());
        assertThat(intersection.getPostfix()).hasSize(1);
        assertThat(intersection.getPostfix()).containsSequence(Vowel.create(Length.LONG));
        assertThat(intersection.getFrontPosition()).isNull();
        assertThat(intersection.getBackPosition().getIndex()).isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndContextAndLongerThisPrefixAndPostfixThanOtherPrefixAndPostfix(ICharacterRepository characterRepository) throws IntersectionException {
        Context context1 = new Context(
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
                        Vowel.create(Length.LONG)
                ),
                Position.of(1),
                null
        );
        Context intersection = intersect(context1, context2);

        assertThat(intersection.getPrefix()).isEqualTo(context2.getPrefix());
        assertThat(intersection.getCore()).isEqualTo(context2.getCore());
        assertThat(intersection.getPostfix()).isEqualTo(context2.getPostfix());
        assertThat(intersection.getFrontPosition()).isEqualTo(context2.getFrontPosition());
        assertThat(intersection.getBackPosition()).isNull();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndDifferentSizedCores(ICharacterRepository characterRepository) {
        Context context1 = new Context(
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(),
                        Vowel.create()
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
                        Vowel.create()
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                Position.of(1),
                null
        );
        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(context1, context2));
        assertThat(exception.getMessage()).isEqualTo("Full intersection must be executed on two lists with same size");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndDifferentCharacterInCores(ICharacterRepository characterRepository) {
        Context context1 = new Context(
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create()
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
                        Consonant.create()
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                Position.of(1),
                null
        );
        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(context1, context2));
        assertThat(exception.getMessage()).isEqualTo("The two characters have different types");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndBothNullFrontPositions(ICharacterRepository characterRepository) throws IntersectionException {
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
                Position.of(1)
        );
        Context context2 = new Context(
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
                Position.of(1)
        );
        Context intersection = intersect(context1, context2);

        assertThat(intersection.getPrefix()).isEqualTo(context1.getPrefix());
        assertThat(intersection.getCore()).isEqualTo(context1.getCore());
        assertThat(intersection.getPostfix()).isEqualTo(context1.getPostfix());
        assertThat(intersection.getFrontPosition()).isNull();
        assertThat(intersection.getBackPosition()).isEqualTo(context1.getBackPosition());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndNullThisFrontPositionAndNonNullOtherFrontPosition(ICharacterRepository characterRepository) throws IntersectionException {
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
                Position.of(1)
        );
        Context context2 = new Context(
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                Position.identity(),
                Position.of(1)
        );
        Context intersection = intersect(context1, context2);

        assertThat(intersection.getPrefix()).isEqualTo(context1.getPrefix());
        assertThat(intersection.getCore()).isEqualTo(context1.getCore());
        assertThat(intersection.getPostfix()).isEqualTo(context1.getPostfix());
        assertThat(intersection.getFrontPosition()).isNull();
        assertThat(intersection.getBackPosition()).isEqualTo(context1.getBackPosition());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndNonNullThisFrontPositionAndNullOtherFrontPosition(ICharacterRepository characterRepository) throws IntersectionException {
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
                Position.identity(),
                Position.of(1)
        );
        Context context2 = new Context(
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
                Position.of(1)
        );
        Context intersection = intersect(context1, context2);

        assertThat(intersection.getPrefix()).isEqualTo(context1.getPrefix());
        assertThat(intersection.getCore()).isEqualTo(context1.getCore());
        assertThat(intersection.getPostfix()).isEqualTo(context1.getPostfix());
        assertThat(intersection.getFrontPosition()).isNull();
        assertThat(intersection.getBackPosition()).isEqualTo(context1.getBackPosition());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndDifferentFrontPositions(ICharacterRepository characterRepository) throws IntersectionException {
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
                Position.of(1)
        );
        Context context2 = new Context(
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                Position.identity(),
                Position.of(1)
        );
        Context intersection = intersect(context1, context2);

        assertThat(intersection.getPrefix()).isEqualTo(context1.getPrefix());
        assertThat(intersection.getCore()).isEqualTo(context1.getCore());
        assertThat(intersection.getPostfix()).isEqualTo(context1.getPostfix());
        assertThat(intersection.getFrontPosition()).isNull();
        assertThat(intersection.getBackPosition()).isEqualTo(context1.getBackPosition());
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndBothNullBackPositions(ICharacterRepository characterRepository) throws IntersectionException {
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
                Position.identity(),
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
                        Vowel.create(Length.LONG)
                ),
                Position.identity(),
                null
        );
        Context intersection = intersect(context1, context2);

        assertThat(intersection.getPrefix()).isEqualTo(context1.getPrefix());
        assertThat(intersection.getCore()).isEqualTo(context1.getCore());
        assertThat(intersection.getPostfix()).isEqualTo(context1.getPostfix());
        assertThat(intersection.getFrontPosition()).isEqualTo(context1.getFrontPosition());
        assertThat(intersection.getBackPosition()).isNull();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndNullThisBackPositionAndNonNullOtherBackPosition(ICharacterRepository characterRepository) throws IntersectionException {
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
                Position.identity(),
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
                        Vowel.create(Length.LONG)
                ),
                Position.identity(),
                Position.of(2)
        );
        Context intersection = intersect(context1, context2);

        assertThat(intersection.getPrefix()).isEqualTo(context1.getPrefix());
        assertThat(intersection.getCore()).isEqualTo(context1.getCore());
        assertThat(intersection.getPostfix()).isEqualTo(context1.getPostfix());
        assertThat(intersection.getFrontPosition()).isEqualTo(context1.getFrontPosition());
        assertThat(intersection.getBackPosition()).isNull();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndNonNullThisBackPositionAndNullOtherBackPosition(ICharacterRepository characterRepository) throws IntersectionException {
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
                Position.identity(),
                Position.identity()
        );
        Context context2 = new Context(
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                Position.identity(),
                null
        );
        Context intersection = intersect(context1, context2);

        assertThat(intersection.getPrefix()).isEqualTo(context1.getPrefix());
        assertThat(intersection.getCore()).isEqualTo(context1.getCore());
        assertThat(intersection.getPostfix()).isEqualTo(context1.getPostfix());
        assertThat(intersection.getFrontPosition()).isEqualTo(context1.getFrontPosition());
        assertThat(intersection.getBackPosition()).isNull();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndDifferentBackPositions(ICharacterRepository characterRepository) throws IntersectionException {
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
                Position.identity(),
                Position.of(1)
        );
        Context context2 = new Context(
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                List.of(
                        Vowel.create(Length.LONG)
                ),
                Position.identity(),
                Position.of(2)
        );
        Context intersection = intersect(context1, context2);

        assertThat(intersection.getPrefix()).isEqualTo(context1.getPrefix());
        assertThat(intersection.getCore()).isEqualTo(context1.getCore());
        assertThat(intersection.getPostfix()).isEqualTo(context1.getPostfix());
        assertThat(intersection.getFrontPosition()).isEqualTo(context1.getFrontPosition());
        assertThat(intersection.getBackPosition()).isNull();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndBothPositionsResultingNull(ICharacterRepository characterRepository) {
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
                Position.identity(),
                Position.of(1)
        );
        Context context2 = new Context(
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
                Position.of(2)
        );
        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(context1, context2));
        assertThat(exception.getMessage()).isEqualTo("Both position intersections became null");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndEmptyContext(ICharacterRepository characterRepository) {
        Context context1 = new Context(
                List.of(),
                List.of(),
                List.of(),
                Position.identity(),
                Position.identity()
        );
        Context context2 = new Context(
                List.of(),
                List.of(),
                List.of(),
                Position.identity(),
                Position.identity()
        );
        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(context1, context2));
        assertThat(exception.getMessage()).isEqualTo("All 3 parts of the context became empty!");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndEmptyPrefix(ICharacterRepository characterRepository) throws IntersectionException {
        Context context1 = new Context(
                List.of(),
                List.of(Vowel.create()),
                List.of(Vowel.create()),
                Position.identity(),
                Position.identity()
        );
        Context context2 = new Context(
                List.of(),
                List.of(Vowel.create()),
                List.of(Vowel.create()),
                Position.identity(),
                Position.identity()
        );
        Context intersection = intersect(context1, context2);
        assertThat(intersection).isEqualTo(context1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndEmptyCore(ICharacterRepository characterRepository) throws IntersectionException {
        Context context1 = new Context(
                List.of(Vowel.create()),
                List.of(),
                List.of(Vowel.create()),
                Position.identity(),
                Position.identity()
        );
        Context context2 = new Context(
                List.of(Vowel.create()),
                List.of(),
                List.of(Vowel.create()),
                Position.identity(),
                Position.identity()
        );
        Context intersection = intersect(context1, context2);
        assertThat(intersection).isEqualTo(context1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndEmptyPostfix(ICharacterRepository characterRepository) throws IntersectionException {
        Context context1 = new Context(
                List.of(Vowel.create()),
                List.of(Vowel.create()),
                List.of(),
                Position.identity(),
                Position.identity()
        );
        Context context2 = new Context(
                List.of(Vowel.create()),
                List.of(Vowel.create()),
                List.of(),
                Position.identity(),
                Position.identity()
        );
        Context intersection = intersect(context1, context2);
        assertThat(intersection).isEqualTo(context1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testIntersectWithContextAndEmptyPrefixAndCore(ICharacterRepository characterRepository) throws IntersectionException {
        Context context1 = new Context(
                List.of(),
                List.of(),
                List.of(Vowel.create()),
                Position.identity(),
                Position.identity()
        );
        Context context2 = new Context(
                List.of(),
                List.of(),
                List.of(Vowel.create()),
                Position.identity(),
                Position.identity()
        );
        Context intersection = intersect(context1, context2);
        assertThat(intersection).isEqualTo(context1);
    }

    // Position

    @Test
    public void testIntersectWithPositionAndNonEqualPositions() throws IntersectionException {
        Position position1 = Position.of(4);
        Position position2 = Position.of(5);
        Position intersection = intersect(position1, position2);
        assertThat(intersection).isNull();
    }

    @Test
    public void testIntersectWithPositionAndEqualPositions() throws IntersectionException {
        Position position1 = Position.of(5);
        Position position2 = Position.of(5);
        Position intersection = intersect(position1, position2);
        assertThat(intersection).isSameAs(position1);
    }

    // Rule

    public static Stream<Arguments> ruleParameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    IWordConverter wordConverter = new DoubleConsonantWordConverter();
                    ICostCalculator costCalculator = new AttributeBasedCostCalculator();
                    IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                            .characterRepository(characterRepository)
                            .wordConverter(wordConverter)
                            .costCalculator(costCalculator)
                            .maximalContextSize(3)
                            .build();
                    return Arguments.of(
                            characterRepository,
                            wordConverter,
                            wordPairProcessor
                    );
                });
    }

    @ParameterizedTest
    @MethodSource("ruleParameters")
    public void testIntersectWithDifferentlySizedTransformationLists(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule rule1 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("a").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        Rule rule2 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("a").getAttributes()),
                                characterRepository
                        ),
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("b").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(rule1, rule2, characterRepository, null));
        assertThat(exception.getMessage()).isEqualTo("The size of transformation lists does not equal in the two rules");
    }

    @ParameterizedTest
    @MethodSource("ruleParameters")
    public void testIntersectWithTheSecondTransformationIntersectionBecomingInhomogeneous(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) throws IntersectionException {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Rule rule1 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Replacement(
                                characterRepository.getCharacter("a"), characterRepository.getCharacter("á"),
                                characterRepository
                        ),
                        new Replacement(
                                Set.of(new AttributeDelta(Length.class, Length.SHORT, Length.LONG)),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Rule rule2 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Replacement(
                                characterRepository.getCharacter("a"), characterRepository.getCharacter("á"),
                                characterRepository
                        ),
                        new Replacement(
                                Set.of(new AttributeDelta(LipShape.class, LipShape.ROUNDED, LipShape.UNROUNDED)),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        Rule expected = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        );
        Rule intersection = intersect(rule1, rule2, characterRepository, null);
        assertThat(intersection).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("ruleParameters")
    public void testIntersectionWithNullThisTransformationsAndNonNullOtherTransformations(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) throws IntersectionException {
        Rule rule1 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        );
        @SuppressWarnings("unchecked")
        Rule rule2 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(),
                characterRepository,
                null
        );
        Rule expected = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        );
        Rule intersection = intersect(rule1, rule2, characterRepository, null);
        assertThat(intersection).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("ruleParameters")
    public void testIntersectionWithNonNullThisTransformationsAndNullOtherTransformations(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) throws IntersectionException {
        Rule rule1 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(),
                characterRepository,
                null
        );
        @SuppressWarnings("unchecked")
        Rule rule2 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        );
        Rule expected = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        );
        Rule intersection = intersect(rule1, rule2, characterRepository, null);
        assertThat(intersection).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("ruleParameters")
    public void testIntersectionWithNullThisTransformationsAndNullOtherTransformations(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) throws IntersectionException {
        Rule rule1 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        );
        @SuppressWarnings("unchecked")
        Rule rule2 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        );
        Rule expected = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                null,
                characterRepository,
                null
        );
        Rule intersection = intersect(rule1, rule2, characterRepository, null);
        assertThat(intersection).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("ruleParameters")
    public void testRuleIntersectionAndSubset(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) throws IntersectionException {
        WordPair wordPair1 = WordPair.of("alma", "almát");
        WordPair wordPair2 = WordPair.of("malma", "malmát");

        Rule rule1 = wordPairProcessor.induceRules(wordPair1).getRules().get(0);
        Rule rule2 = wordPairProcessor.induceRules(wordPair2).getRules().get(0);

        // Test if m is excluded from the prefix of the rule generated from malma-malmát
        assertThat(s(rule2.getContext().getPrefix(), characterRepository)).isEqualTo("alm");

        Rule intersection = intersect(rule1, rule2, characterRepository, wordConverter);

        // Test intersection structure
        assertThat(s(intersection.getContext().getPrefix(), characterRepository)).isEqualTo("alm");
        assertThat(s(intersection.getContext().getCore(), characterRepository)).isEqualTo("a");
        assertThat(s(intersection.getContext().getPostfix(), characterRepository)).isEqualTo("#");
        assertThat(intersection.getContext().getFrontPosition().getIndex()).isEqualTo(0);
        assertThat(intersection.getContext().getBackPosition().getIndex()).isEqualTo(0);
        assertThat(intersection.getTransformations()).hasSize(2);
        assertThat(intersection.getTransformations()).containsSequence(
                new Replacement(
                        characterRepository.getCharacter("a"),
                        characterRepository.getCharacter("á"),
                        characterRepository
                ),
                new Addition(
                        new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                        characterRepository
                )
        );

        // Test the intersection rule
        assertThat(intersection.transform(wordPair1.getLeftWord())).isEqualTo(wordPair1.getRightWord());
        assertThat(intersection.transform(wordPair2.getLeftWord())).isEqualTo(wordPair2.getRightWord());

        // Test subset
        assertThat(rule2).isEqualTo(rule1);
        assertThat(intersection).isEqualTo(rule1);
        assertThat(intersection).isEqualTo(rule2);
    }

    @ParameterizedTest
    @MethodSource("ruleParameters")
    public void testIntersection(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) throws IntersectionException {
        Rule rule1 = new Rule(
                new Context(
                        List.of(
                                characterRepository.getCharacter("m"),
                                characterRepository.getCharacter("u"),
                                characterRepository.getCharacter("s")
                        ),
                        List.of(),
                        List.of(
                                EndSound.get()
                        ),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        Rule rule2 = new Rule(
                new Context(
                        List.of(
                                characterRepository.getCharacter("g"),
                                characterRepository.getCharacter("á"),
                                characterRepository.getCharacter("s")
                        ),
                        List.of(),
                        List.of(
                                EndSound.get()
                        ),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        Rule intersection = intersect(rule1, rule2, characterRepository, wordConverter);
        assertThat(intersection).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("ruleParameters")
    public void testTransformationIntersectionWithInvalidAddition(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule rule1 = new Rule(
                new Context(
                        List.of(
                                characterRepository.getCharacter("a"),
                                characterRepository.getCharacter("b"),
                                characterRepository.getCharacter("c")
                        ),
                        List.of(),
                        List.of(
                                EndSound.get()
                        ),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("a").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        Rule rule2 = new Rule(
                new Context(
                        List.of(
                                characterRepository.getCharacter("a"),
                                characterRepository.getCharacter("b"),
                                characterRepository.getCharacter("c")
                        ),
                        List.of(),
                        List.of(
                                EndSound.get()
                        ),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("e").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );

        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(rule1, rule2, characterRepository, wordConverter));
        assertThat(exception.getMessage()).isEqualTo("The two additions' intersection wouldn't be performable!");
    }

    @ParameterizedTest
    @MethodSource("ruleParameters")
    public void testTransformationIntersectionWithInvalidRemoval(
            ICharacterRepository characterRepository,
            IWordConverter wordConverter,
            IWordPairProcessor wordPairProcessor) {
        Rule rule1 = new Rule(
                new Context(
                        List.of(
                                characterRepository.getCharacter("a"),
                                characterRepository.getCharacter("b"),
                                characterRepository.getCharacter("c")
                        ),
                        List.of(),
                        List.of(
                                EndSound.get()
                        ),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Removal(
                                new HashSet<>(characterRepository.getCharacter("a").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        Rule rule2 = new Rule(
                new Context(
                        List.of(
                                characterRepository.getCharacter("a"),
                                characterRepository.getCharacter("b"),
                                characterRepository.getCharacter("c")
                        ),
                        List.of(),
                        List.of(
                                EndSound.get()
                        ),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Removal(
                                new HashSet<>(characterRepository.getCharacter("e").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );

        IntersectionException exception = assertThrows(IntersectionException.class, () -> intersect(rule1, rule2, characterRepository, wordConverter));
        assertThat(exception.getMessage()).isEqualTo("The two removals' intersection wouldn't be performable!");
    }

    @Test
    public void testAA_tAndEE_tCommonTransformationEquality() throws IntersectionException {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        Rule rule1 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Replacement(
                                characterRepository.getCharacter("a"),
                                characterRepository.getCharacter("á"),
                                characterRepository
                        ),
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        List<ITransformation> rule1Transformations = rule1.getTransformations();
        Rule rule2 = new Rule(
                new Context(
                        List.of(),
                        List.of(characterRepository.getCharacter("a")),
                        List.of(),
                        Position.identity(),
                        Position.identity()
                ),
                List.of(
                        new Replacement(
                                characterRepository.getCharacter("e"),
                                characterRepository.getCharacter("é"),
                                characterRepository
                        ),
                        new Addition(
                                new HashSet<>(characterRepository.getCharacter("t").getAttributes()),
                                characterRepository
                        )
                ),
                characterRepository,
                null
        );
        List<ITransformation> rule2Transformations = rule2.getTransformations();
        Rule intersection = intersect(rule1, rule2, characterRepository, null);
        List<ITransformation> intersectionTransformations = intersection.getTransformations();
        assertThat(rule2Transformations).isEqualTo(rule1Transformations);
        assertThat(intersectionTransformations).isEqualTo(rule1Transformations);
        assertThat(rule2Transformations).isEqualTo(intersectionTransformations);
    }

    // Characters

    @Test
    public void testCharacterIntersectionWithUnknownCharacterType() {
        ICharacter character1 = new UnknownCharacter();
        ICharacter character2 = new UnknownCharacter();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> intersect(character1, character2));
        assertThat(exception).hasMessage("Unknown character types found: " + character1 + " and " + character2);
    }

    @Test
    public void testCharacterIntersectionWithDisjointAttributeClasses() throws IntersectionException {
        ICharacter character1 = Consonant.create(SoundProductionPlace.BILABIAL);
        ICharacter character2 = Consonant.create(SoundProductionWay.PLOSIVE);
        ICharacter intersection = intersect(character1, character2);
        assertThat(intersection).isInstanceOf(Consonant.class);
        assertThat(intersection.getAttributes()).isEmpty();
    }

    private String s(List<ICharacter> characters, ICharacterRepository characterRepository) {
        return characters
                .stream()
                .map(characterRepository::getLetter)
                .collect(joining());
    }

}
