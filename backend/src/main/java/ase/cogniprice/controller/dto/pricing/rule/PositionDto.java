package ase.cogniprice.controller.dto.pricing.rule;

import ase.cogniprice.entity.PricingRule;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionDto {
    private BigDecimal value;

    private PricingRule.Position.Unit unit;

    @NotNull(message = "Match type must not be null")
    private PricingRule.Position.MatchType matchType;

    @NotNull(message = "Reference must not be null")
    private PricingRule.Position.Reference reference;
}
