package ase.cogniprice.integrationTest.pricingRuleIntegrationTest;

import ase.cogniprice.controller.dto.pricing.rule.PositionDto;
import ase.cogniprice.controller.dto.pricing.rule.PriceLimitDto;
import ase.cogniprice.controller.dto.pricing.rule.PriceRuleRequestDto;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.PriceLimit;
import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.type.Role;

import java.math.BigDecimal;
import java.util.Set;

public class DataProvider {

    public static final String DEFAULT_PRICE_RULE_NAME = "Test Price Rule";
    public static final PricingRule.Scope DEFAULT_PRICE_RULE_SCOPE = PricingRule.Scope.CATEGORY;
    public static final boolean DEFAULT_PRICE_RULE_IS_ACTIVE = true;

    public static PricingRule generateBasicPricingRule(String name, boolean active, ApplicationUser applicationUser) {
        PricingRule pricingRule = new PricingRule();
        pricingRule.setName(name);
        pricingRule.setActive(active);
        pricingRule.setApplicationUser(applicationUser);
        pricingRule.setPosition(new PricingRule.Position(null, null, PricingRule.Position.MatchType.EQUALS, PricingRule.Position.Reference.AVERAGE));
        pricingRule.setScope(PricingRule.Scope.PRODUCT);
        return pricingRule;
    }

    public static PricingRule generatePricingRule(String name, boolean active, ApplicationUser applicationUser, Set<Product> products) {
        PricingRule pricingRule = new PricingRule();
        pricingRule.setName(name);
        pricingRule.setActive(active);
        pricingRule.setApplicationUser(applicationUser);
        pricingRule.setPosition(new PricingRule.Position(BigDecimal.TWO, PricingRule.Position.Unit.PERCENTAGE, PricingRule.Position.MatchType.HIGHER, PricingRule.Position.Reference.AVERAGE));
        pricingRule.setScope(PricingRule.Scope.PRODUCT);
        pricingRule.setProducts(products);

        return pricingRule;
    }

    public static PricingRule generatePricingRuleForCategoryScope(String name, boolean active, ApplicationUser applicationUser, Set<ProductCategory> categories) {
        PricingRule pricingRule = new PricingRule();
        pricingRule.setName(name);
        pricingRule.setActive(active);
        pricingRule.setApplicationUser(applicationUser);
        pricingRule.setPosition(new PricingRule.Position(BigDecimal.TWO, PricingRule.Position.Unit.PERCENTAGE, PricingRule.Position.MatchType.HIGHER, PricingRule.Position.Reference.AVERAGE));
        pricingRule.setScope(DEFAULT_PRICE_RULE_SCOPE);
        pricingRule.setCategories(categories);

        return pricingRule;
    }

    public static ApplicationUser getApplicationUser() {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("anotheruser");
        user.setPassword("password");
        user.setEmail("otheruser@example.com");
        user.setFirstName("Other");
        user.setLastName("User");
        user.setRole(Role.USER);
        user.setLoginAttempts(0L);
        user.setUserLocked(false);
       return user;
    }

    public static PriceRuleRequestDto getDefaultPriceRuleRequestDto() {
        PriceRuleRequestDto priceRuleRequestDto = new PriceRuleRequestDto();
        priceRuleRequestDto.setName(DEFAULT_PRICE_RULE_NAME);
        priceRuleRequestDto.setScope(DEFAULT_PRICE_RULE_SCOPE);
        priceRuleRequestDto.setIsActive(DEFAULT_PRICE_RULE_IS_ACTIVE);

        // limits
        priceRuleRequestDto.setMinLimit(new PriceLimitDto(PriceLimit.LimitType.FIXED_AMOUNT, BigDecimal.ONE));
        priceRuleRequestDto.setMaxLimit(new PriceLimitDto(PriceLimit.LimitType.FIXED_AMOUNT, BigDecimal.TEN));

        // position
        priceRuleRequestDto.setPosition(new PositionDto(
                BigDecimal.TEN,
                PricingRule.Position.Unit.PERCENTAGE,
                PricingRule.Position.MatchType.HIGHER,
                PricingRule.Position.Reference.HIGHEST
        ));

        return priceRuleRequestDto;
    }

}
