package ase.cogniprice.controller.dto.pricing.rule;

import ase.cogniprice.entity.PricingRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimplePricingRuleDto {
    private Long id;
    private String name;
    private boolean active;
    private PricingRule.Position.MatchType positionMatchType;
    private PricingRule.Position.Reference positionReference;
    private BigDecimal positionValue;
    private PricingRule.Position.Unit positionUnit;
    private PricingRule.Scope scope;
    private boolean hasMaxLimit;
    private boolean hasMinLimit;
}
