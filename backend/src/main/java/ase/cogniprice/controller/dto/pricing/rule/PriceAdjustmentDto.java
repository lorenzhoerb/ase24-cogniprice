package ase.cogniprice.controller.dto.pricing.rule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceAdjustmentDto {
    private Long productId; // ID of the product being adjusted
    private Long pricingRuleId; // ID of the pricing rule that triggered the adjustment
    private Long userId; // ID of the user who owns the pricing rule
    private BigDecimal adjustedPrice; // Price calculated based on the pricing rule
    private BigDecimal averagePrice; // Average price in the market
    private BigDecimal cheapestPrice; // Cheapest price found in the market
    private BigDecimal highestPrice; // Highest price found in the market
    private ZonedDateTime adjustmentDate; // Date and time of the adjustment
}

