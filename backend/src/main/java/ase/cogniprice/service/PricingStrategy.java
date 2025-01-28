package ase.cogniprice.service;

import ase.cogniprice.entity.PricingRule;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal applyPricing(BigDecimal referencePrice, BigDecimal value, PricingRule.Position.Unit unit);
}
