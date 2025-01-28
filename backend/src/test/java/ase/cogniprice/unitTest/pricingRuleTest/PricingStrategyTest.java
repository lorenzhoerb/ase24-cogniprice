package ase.cogniprice.unitTest.pricingRuleTest;

import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.service.PricingStrategy;
import ase.cogniprice.service.implementation.price.strategy.EqualPricingStrategy;
import ase.cogniprice.service.implementation.price.strategy.HigherPricingStrategy;
import ase.cogniprice.service.implementation.price.strategy.LowerPricingStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PricingStrategyTest {
    private final PricingStrategy equalStrategy = new EqualPricingStrategy();
    private final PricingStrategy higherStrategy = new HigherPricingStrategy();
    private final PricingStrategy lowerStrategy = new LowerPricingStrategy();


    @Test
    void testEqualPricingStrategy() {
        BigDecimal referencePrice = BigDecimal.valueOf(100);
        BigDecimal value = BigDecimal.valueOf(20);
        PricingRule.Position.Unit unit = PricingRule.Position.Unit.EUR;

        BigDecimal result = equalStrategy.applyPricing(referencePrice, value, unit);

        assertEquals(referencePrice, result, "The equal pricing strategy should return the reference price.");
    }

    @Test
    void testHigherPricingStrategyPercentage() {
        BigDecimal referencePrice = BigDecimal.valueOf(100);
        BigDecimal value = BigDecimal.valueOf(20); // 20%
        PricingRule.Position.Unit unit = PricingRule.Position.Unit.PERCENTAGE;

        BigDecimal result = higherStrategy.applyPricing(referencePrice, value, unit);

        assertEquals(BigDecimal.valueOf(120).stripTrailingZeros(), result.stripTrailingZeros(), "The higher pricing strategy with percentage should increase the price by 20%.");
    }

    @Test
    void testHigherPricingStrategyDifferentPercentage() {
        BigDecimal referencePrice = BigDecimal.valueOf(120);
        BigDecimal value = BigDecimal.valueOf(4); // 4%
        PricingRule.Position.Unit unit = PricingRule.Position.Unit.PERCENTAGE;

        BigDecimal result = higherStrategy.applyPricing(referencePrice, value, unit);

        assertEquals(BigDecimal.valueOf(124.8).stripTrailingZeros(), result.stripTrailingZeros(), "The higher pricing strategy with percentage should increase the price by 20%.");
    }

    @Test
    void testHigherPricingStrategyEUR() {
        BigDecimal referencePrice = BigDecimal.valueOf(100);
        BigDecimal value = BigDecimal.valueOf(20); // 20 EUR
        PricingRule.Position.Unit unit = PricingRule.Position.Unit.EUR;

        BigDecimal result = higherStrategy.applyPricing(referencePrice, value, unit);

        assertEquals(BigDecimal.valueOf(120), result, "The higher pricing strategy with EUR should increase the price by 20 EUR.");
    }

    @Test
    void testLowerPricingStrategyPercentage() {
        BigDecimal referencePrice = BigDecimal.valueOf(100);
        BigDecimal value = BigDecimal.valueOf(20); // 20%
        PricingRule.Position.Unit unit = PricingRule.Position.Unit.PERCENTAGE;

        BigDecimal result = lowerStrategy.applyPricing(referencePrice, value, unit);

        assertEquals(BigDecimal.valueOf(80).stripTrailingZeros(), result.stripTrailingZeros(), "The lower pricing strategy with percentage should decrease the price by 20%.");
    }

    @Test
    void testLowerPricingStrategyEUR() {
        BigDecimal referencePrice = BigDecimal.valueOf(100);
        BigDecimal value = BigDecimal.valueOf(20); // 20 EUR
        PricingRule.Position.Unit unit = PricingRule.Position.Unit.EUR;

        BigDecimal result = lowerStrategy.applyPricing(referencePrice, value, unit);

        assertEquals(BigDecimal.valueOf(80), result, "The lower pricing strategy with EUR should increase the price by 20 EUR (same as HigherPricingStrategy in EUR case).");
    }

    @Test
    void testLowerPricingStrategyEUR_WhenNegative_ExpectZero() {
        BigDecimal referencePrice = BigDecimal.valueOf(100);
        BigDecimal value = BigDecimal.valueOf(120); // 120 EUR
        PricingRule.Position.Unit unit = PricingRule.Position.Unit.EUR;

        BigDecimal result = lowerStrategy.applyPricing(referencePrice, value, unit);

        assertEquals(BigDecimal.valueOf(0).stripTrailingZeros(), result.stripTrailingZeros(), "There should not be a negative number. Expect 0.");
    }

}
