package ase.cogniprice.controller.dto.pricing.rule;

import ase.cogniprice.entity.PriceLimit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceLimitDto {
    @NotNull(message = "Limit type must not be null")
    private PriceLimit.LimitType limitType;

    @NotNull(message = "Limit value must not be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Limit value must be greater than or equal to 0")
    private BigDecimal limitValue;
}
