package com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.atomicrule;

import com.github.szgabsz91.morpher.methods.astra.impl.rules.AtomicRule;
import com.github.szgabsz91.morpher.methods.astra.impl.rules.RuleGroup;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.szgabsz91.morpher.methods.astra.impl.testutils.RuleGroupFactory.createRuleGroup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

public class DefaultAtomicRuleFitnessCalculatorTest {

    private IAtomicRuleFitnessCalculator atomicRuleFitnessCalculator;

    @BeforeEach
    public void setUp() {
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        int averageSimilarityExponent = 2;
        this.atomicRuleFitnessCalculator = new DefaultAtomicRuleFitnessCalculator(wordConverter, characterRepository, averageSimilarityExponent);
    }

    @Test
    public void testCalculate() {
        String inputWord = "aNc";
        RuleGroup ruleGroup = createRuleGroup("aN", RuleGroup::straight,
                new AtomicRule("", "a", "á", "N", 2)
        );
        AtomicRule atomicRule = ruleGroup.getAtomicRules().iterator().next();
        double result = this.atomicRuleFitnessCalculator.calculate(inputWord, null, ruleGroup, atomicRule);
        assertThat(result).isCloseTo(0.6666666666666666, offset(0.0000005));
    }

    @Test
    public void testInvalidateCache() {
        this.atomicRuleFitnessCalculator.invalidateCache();
    }

}
