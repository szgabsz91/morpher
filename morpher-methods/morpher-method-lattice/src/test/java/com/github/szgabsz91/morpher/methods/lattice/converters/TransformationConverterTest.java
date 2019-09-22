package com.github.szgabsz91.morpher.methods.lattice.converters;

import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.attributes.Letter;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.AttributeDelta;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.ITransformation;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Removal;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Replacement;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.TransformationMessage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransformationConverterTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    TransformationConverter transformationConverter = new TransformationConverter();
                    return Arguments.of(
                            characterRepository,
                            transformationConverter
                    );
                });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithAddition(
            ICharacterRepository characterRepository,
            TransformationConverter transformationConverter) {
        Addition addition = new Addition(Set.of(Length.SHORT), characterRepository);
        TransformationMessage transformationMessage = transformationConverter.convert(addition);
        assertThat(transformationMessage.getType()).isEqualTo(Addition.class.getName());
        assertThat(transformationMessage.getChangeList()).hasSize(1);
        assertThat(transformationMessage.getChange(0)).isEqualTo(Length.class.getName() + "." + Length.SHORT);

        ITransformation result = transformationConverter.convertBack(transformationMessage);
        assertThat(result).isEqualTo(addition);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithRemoval(
            ICharacterRepository characterRepository,
            TransformationConverter transformationConverter) {
        Removal removal = new Removal(Set.of(Length.SHORT), characterRepository);
        TransformationMessage transformationMessage = transformationConverter.convert(removal);
        assertThat(transformationMessage.getType()).isEqualTo(Removal.class.getName());
        assertThat(transformationMessage.getChangeList()).hasSize(1);
        assertThat(transformationMessage.getChange(0)).isEqualTo(Length.class.getName() + "." + Length.SHORT);

        ITransformation result = transformationConverter.convertBack(transformationMessage);
        assertThat(result).isEqualTo(removal);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithReplacement(
            ICharacterRepository characterRepository,
            TransformationConverter transformationConverter) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Replacement replacement = new Replacement(
                Set.of(
                        new AttributeDelta(Length.class, Length.LONG, Length.SHORT)
                ),
                characterRepository
        );
        TransformationMessage transformationMessage = transformationConverter.convert(replacement);
        assertThat(transformationMessage.getType()).isEqualTo(Replacement.class.getName());
        assertThat(transformationMessage.getChangeList()).hasSize(1);
        assertThat(transformationMessage.getChange(0)).isEqualTo(Length.class.getName() + "." + Length.LONG + "." + Length.SHORT);

        ITransformation result = transformationConverter.convertBack(transformationMessage);
        assertThat(result).isEqualTo(replacement);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithUnknownTransformationType(
            ICharacterRepository characterRepository,
            TransformationConverter transformationConverter) {
        UnknownTransformation unknownTransformation = new UnknownTransformation();
        TransformationMessage transformationMessage = transformationConverter.convert(unknownTransformation);
        assertThat(transformationMessage.getType()).isEmpty();
        assertThat(transformationMessage.getChangeList()).isEmpty();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> transformationConverter.convertBack(transformationMessage));
        assertThat(exception.getMessage()).isEqualTo("Unknown transformation found: ");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithClassNotFoundExceptionFromAttributeCreationAtAdditionCreation(
            ICharacterRepository characterRepository,
            TransformationConverter transformationConverter) {
        TransformationMessage transformationMessage = TransformationMessage.newBuilder()
                .addChange("nonExistentType.nonExistentValue")
                .setType(Addition.class.getName())
                .build();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> transformationConverter.convertBack(transformationMessage));
        assertThat(exception.getMessage()).isEqualTo("Attribute enum could not be instantiated");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConversionWithClassNotFoundExceptionFromAttributeDeltaCreationAtReplacementCreation(
            ICharacterRepository characterRepository,
            TransformationConverter transformationConverter) {
        TransformationMessage transformationMessage = TransformationMessage.newBuilder()
                .addChange("nonExistentType.nonExistentFrom.nonExistentTo")
                .setType(Replacement.class.getName())
                .build();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> transformationConverter.convertBack(transformationMessage));
        assertThat(exception.getMessage()).isEqualTo("Attribute enum could not be instantiated");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testParse(
            ICharacterRepository characterRepository,
            TransformationConverter transformationConverter) throws IOException {
        Path file = Paths.get("build/transformation.pb");
        TransformationMessage transformationMessage = TransformationMessage.newBuilder()
                .addChange("change")
                .build();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(Files.newOutputStream(file))) {
            transformationMessage.writeTo(gzipOutputStream);
        }
        TransformationMessage result = transformationConverter.parse(file);
        assertThat(result).isEqualTo(transformationMessage);
        Files.delete(file);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConvertBackWithLetterAddition(
            ICharacterRepository characterRepository,
            TransformationConverter transformationConverter) {
        TransformationMessage transformationMessage = TransformationMessage.newBuilder()
                .setType(Addition.class.getName())
                .addChange(Letter.class.getCanonicalName() + "." + Letter.A.toString())
                .build();
        transformationConverter.setCharacterRepository(characterRepository);
        ITransformation transformation = transformationConverter.convertBack(transformationMessage);
        assertThat(transformation).isInstanceOf(Addition.class);
        Addition addition = (Addition) transformation;
        assertThat(addition.getAttributes()).hasSize(1);
        @SuppressWarnings("unchecked")
        Collection<IAttribute> attributeCollection = (Collection<IAttribute>) addition.getAttributes();
        assertThat(attributeCollection).contains(Letter.A);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConvertBackWithUnknownAttributeClass(
            ICharacterRepository characterRepository,
            TransformationConverter transformationConverter) {
        TransformationMessage transformationMessage = TransformationMessage.newBuilder()
                .setType(Addition.class.getName())
                .addChange("UnknownAttribute.UnknownValue")
                .build();
        transformationConverter.setCharacterRepository(characterRepository);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> transformationConverter.convertBack(transformationMessage));
        assertThat(exception.getMessage()).isEqualTo("Attribute enum could not be instantiated");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConvertBackWithNoFactoryMethod(
            ICharacterRepository characterRepository,
            TransformationConverter transformationConverter) {
        String attributeType = TransformationConverterTest.class.getCanonicalName() + "$" + CustomAttributeForNoSuchMethodException.class.getSimpleName() + "." + CustomAttributeForNoSuchMethodException.VALUE.toString();
        TransformationMessage transformationMessage = TransformationMessage.newBuilder()
                .setType(Addition.class.getName())
                .addChange(attributeType)
                .build();
        transformationConverter.setCharacterRepository(characterRepository);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> transformationConverter.convertBack(transformationMessage));
        assertThat(exception.getMessage()).isEqualTo(attributeType + ".factory cannot be called");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConvertBackWithPrivateFactoryMethod(
            ICharacterRepository characterRepository,
            TransformationConverter transformationConverter) {
        String attributeType = CustomAttributeForIllegalAccessException.class.getCanonicalName() + "." + CustomAttributeForIllegalAccessException.VALUE.toString();
        TransformationMessage transformationMessage = TransformationMessage.newBuilder()
                .setType(Addition.class.getName())
                .addChange(attributeType)
                .build();
        transformationConverter.setCharacterRepository(characterRepository);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> transformationConverter.convertBack(transformationMessage));
        assertThat(exception.getMessage()).isEqualTo(attributeType + ".factory cannot be called");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testConvertBackWithExceptionThrowingFactoryMethod(
            ICharacterRepository characterRepository,
            TransformationConverter transformationConverter) {
        String attributeType = CustomAttributeForInvocationTargetException.class.getCanonicalName() + "." + CustomAttributeForInvocationTargetException.VALUE.toString();
        TransformationMessage transformationMessage = TransformationMessage.newBuilder()
                .setType(Addition.class.getName())
                .addChange(attributeType)
                .build();
        transformationConverter.setCharacterRepository(characterRepository);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> transformationConverter.convertBack(transformationMessage));
        assertThat(exception.getMessage()).isEqualTo(attributeType + ".factory cannot be called");
    }

    private static class UnknownTransformation implements ITransformation {

        @Override
        public boolean isInhomogeneous() {
            return false;
        }

        @Override
        public int perform(List<ICharacter> characters, int index) {
            return 0;
        }

    }

    private static enum CustomAttributeForNoSuchMethodException implements IAttribute {

        VALUE("v");

        private final String value;

        CustomAttributeForNoSuchMethodException(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

    }

}
