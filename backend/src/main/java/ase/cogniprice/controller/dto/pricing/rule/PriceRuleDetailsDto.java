package ase.cogniprice.controller.dto.pricing.rule;

import ase.cogniprice.entity.PriceLimit;
import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceRuleDetailsDto {
    private Long id;
    private String name;
    private PricingRule.Position position;
    private PricingRule.Scope scope;
    private Set<String> appliedCategories = new HashSet<>(); // Category names
    private Set<Long> appliedProductIds = new HashSet<>();   // Product IDs
    private PriceLimit minLimit;
    private PriceLimit maxLimit;
    private boolean isActive;

    public static PriceRuleDetailsDto fromPricingRule(PricingRule pricingRule) {
        // Extract category names and product IDs
        Set<String> appliedCategories = new HashSet<>();
        Set<Long> appliedProductIds = new HashSet<>();

        if (pricingRule.getScope() == PricingRule.Scope.CATEGORY) {
            appliedCategories = pricingRule.getCategories().stream()
                    .map(ProductCategory::getCategory) // Extract category names
                    .collect(Collectors.toSet());
        } else if (pricingRule.getScope() == PricingRule.Scope.PRODUCT) {
            appliedProductIds = pricingRule.getProducts().stream()
                    .map(Product::getId) // Extract product IDs
                    .collect(Collectors.toSet());
        }

        // Build and return the DTO
        return new PriceRuleDetailsDto(
                pricingRule.getId(),
                pricingRule.getName(),
                pricingRule.getPosition(),
                pricingRule.getScope(),
                appliedCategories,
                appliedProductIds,
                pricingRule.getMinLimit(),
                pricingRule.getMaxLimit(),
                pricingRule.isActive()
        );
    }
}
