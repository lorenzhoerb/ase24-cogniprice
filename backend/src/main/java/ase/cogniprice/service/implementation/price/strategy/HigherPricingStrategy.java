package ase.cogniprice.service.implementation.price.strategy;

import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.service.PricingStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class HigherPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal applyPricing(BigDecimal referencePrice, BigDecimal value, PricingRule.Position.Unit unit) {
        return switch (unit) {
            case PERCENTAGE -> {
                // Calculate the incremented price based on percentage
                BigDecimal reductionFactor = BigDecimal.ONE.add(value.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                yield referencePrice.multiply(reductionFactor);
            }
            case EUR -> referencePrice.add(value); // reference + value
        };
    }
}
