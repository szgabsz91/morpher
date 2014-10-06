package com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator;

import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.SoundProductionPlace;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant.SoundProductionWay;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Addition;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.AttributeDelta;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.ITransformation;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Removal;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.Replacement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultCostCalculatorTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    ICostCalculator costCalculator = new DefaultCostCalculator();
                    return Arguments.of(
                            characterRepository,
                            costCalculator
                    );
                });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testCostOfAddition(ICharacterRepository characterRepository, ICostCalculator costCalculator) {
        ITransformation transformation = new Addition(
                Set.of(
                        SoundProductionPlace.BILABIAL,
                        SoundProductionWay.FRICATIVE
                ),
                characterRepository
        );
        int result = costCalculator.calculateCost(transformation);
        assertThat(result).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testCostOfRemoval(ICharacterRepository characterRepository, ICostCalculator costCalculator) {
        ITransformation transformation = new Removal(
                Set.of(
                        SoundProductionPlace.BILABIAL,
                        SoundProductionWay.FRICATIVE
                ),
                characterRepository
        );
        int result = costCalculator.calculateCost(transformation);
        assertThat(result).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testCostOfVariantReplacement(ICharacterRepository characterRepository, ICostCalculator costCalculator) {
        @SuppressWarnings("unchecked")
        Set<AttributeDelta<? super IAttribute>> attributeDeltas = Set.of(
                new AttributeDelta(SoundProductionPlace.class, SoundProductionPlace.BILABIAL, SoundProductionPlace.DENTAL_ALVEOLAR),
                new AttributeDelta(SoundProductionWay.class, SoundProductionWay.FRICATIVE, SoundProductionWay.LATERAL_APPROXIMATIVE)
        );
        ITransformation transformation = new Replacement(attributeDeltas, characterRepository);
        int result = costCalculator.calculateCost(transformation);
        assertThat(result).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testCostOfInvariantReplacement(ICharacterRepository characterRepository, ICostCalculator costCalculator) {
        ITransformation transformation = new Replacement(Set.of(), characterRepository);
        int result = costCalculator.calculateCost(transformation);
        assertThat(result).isEqualTo(0);
    }

}
