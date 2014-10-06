package com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.atomicrule;

import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.astra.impl.rules.AtomicRule;
import com.github.szgabsz91.morpher.methods.astra.impl.rules.RuleGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static com.github.szgabsz91.morpher.methods.astra.impl.testutils.RuleGroupFactory.createRuleGroup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GlobalSmoothAtomicRuleFitnessCalculatorTest {

    private IAtomicRuleFitnessCalculator atomicRuleFitnessCalculator;

    @BeforeEach
    public void setUp() {
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        int averageSimilarityExponent = 2;
        double exponentialFactor = 3.0;
        this.atomicRuleFitnessCalculator = new GlobalSmoothAtomicRuleFitnessCalculator(wordConverter, characterRepository, averageSimilarityExponent, exponentialFactor);
    }

    @Test
    public void testConstructorWithZeroExponentialFactor() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new GlobalSmoothAtomicRuleFitnessCalculator(null, null, 0, 0.0));
        assertThat(exception).hasMessage("The exponentialFactor property must be positive");
    }

    @Test
    public void testCalculate() {
        String inputWord = "aNc";
        RuleGroup ruleGroup = createRuleGroup("aN", RuleGroup::straight,
                new AtomicRule("", "a", "á", "N", 2, 1)
        );
        Collection<RuleGroup> ruleGroups = Set.of(
                createRuleGroup("aN", RuleGroup::straight,
                        new AtomicRule("", "a", "á", "N", 2, 1),
                        new AtomicRule("a", "", "á", "N", 3, 1)
                ),
                createRuleGroup("eN", RuleGroup::straight,
                        new AtomicRule("", "e", "á", "N", 4, 1)
                )
        );
        AtomicRule atomicRule = ruleGroup.getAtomicRules().iterator().next();
        double result = this.atomicRuleFitnessCalculator.calculate(inputWord, ruleGroups, ruleGroup, atomicRule);
        assertThat(result).isCloseTo(0.8288609603224693, offset(0.0000005));
    }

    @Test
    public void testInvalidateCache() {
        RuleGroup ruleGroup1 = createRuleGroup("ma", RuleGroup::straight,
                new AtomicRule("m", "a", "á", "", 2, 1)
        );
        RuleGroup ruleGroup2 = createRuleGroup("ma", RuleGroup::straight,
                new AtomicRule("m", "a", "á", "", 2, 1),
                new AtomicRule("", "ma", "á", "", 3, 1)
        );

        double result1 = this.atomicRuleFitnessCalculator.calculate("alma", Set.of(ruleGroup1), ruleGroup1, ruleGroup1.getAtomicRules().iterator().next());
        assertThat(result1).isCloseTo(0.975106465816068, offset(0.0000005));

        this.atomicRuleFitnessCalculator.invalidateCache();

        double result2 = this.atomicRuleFitnessCalculator.calculate("alma", Set.of(ruleGroup2), ruleGroup2, ruleGroup2.getAtomicRules().iterator().next());
        assertThat(result2).isCloseTo(0.849402894043899, offset(0.0000005));
    }

}
