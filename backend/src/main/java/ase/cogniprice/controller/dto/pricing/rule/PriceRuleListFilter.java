package ase.cogniprice.controller.dto.pricing.rule;

public record PriceRuleListFilter(
        String name,
        PricingRuleStatus status
) {
}
