package ase.cogniprice.service.implementation.price.strategy;

import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.service.PricingStrategy;

import java.math.BigDecimal;

public class EqualPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal applyPricing(BigDecimal referencePrice, BigDecimal value, PricingRule.Position.Unit unit) {
        return referencePrice;
    }
}
