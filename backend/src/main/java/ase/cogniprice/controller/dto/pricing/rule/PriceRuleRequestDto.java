package ase.cogniprice.controller.dto.pricing.rule;

import ase.cogniprice.entity.PricingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceRuleRequestDto {
    @NotBlank(message = "Name must not be blank")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters long")
    private String name;

    @NotNull(message = "Is active must not be null")
    private Boolean isActive;

    @NotNull(message = "Position must not be null")
    @Valid
    private PositionDto position;

    @NotNull(message = "Scope must not be null")
    private PricingRule.Scope scope;

    @NotNull(message = "Applied to scope entities must not be null")
    @Size(min = 1, max = 100, message = "The applied entity IDs must contain at least one item and no more than 100 items.")
    private Set<String> appliedToIds = new HashSet<>();

    @Valid
    private PriceLimitDto minLimit;

    @Valid
    private PriceLimitDto maxLimit;
}
