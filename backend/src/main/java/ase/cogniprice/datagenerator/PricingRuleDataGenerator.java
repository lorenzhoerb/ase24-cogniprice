package ase.cogniprice.datagenerator;

import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.PriceLimit;
import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.PricingRuleRepository;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Profile("generateData")
@DependsOn({"applicationUserDataGenerator", "productDataGenerator"})
@Component
@Slf4j
public class PricingRuleDataGenerator {

    private final PricingRuleRepository pricingRuleRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;

    public PricingRuleDataGenerator(PricingRuleRepository pricingRuleRepository,
                                    ApplicationUserRepository applicationUserRepository,
                                    ProductCategoryRepository productCategoryRepository, ProductRepository productRepository) {
        this.pricingRuleRepository = pricingRuleRepository;
        this.applicationUserRepository = applicationUserRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productRepository = productRepository;
    }

    @PostConstruct
    @Transactional
    public void generatePricingRules() {
        ApplicationUser user = applicationUserRepository.findApplicationUserByUsername("regularUser");
        List<Product> products = productRepository.findAll();

        // Rule 1
        PricingRule pr1 = new PricingRule();
        pr1.setName("PSE - Match Equals - All Products - No Limit");
        pr1.setPosition(generateEqualsPosition(PricingRule.Position.Reference.HIGHEST));
        pr1.setScope(PricingRule.Scope.PRODUCT);
        pr1.setProducts(Set.of(products.get(0), products.get(1), products.get(2)));
        pr1.setApplicationUser(user);
        pr1.setActive(true);

        // Rule 2
        List<ProductCategory> categories = productCategoryRepository.findAll();
        PricingRule pr2 = new PricingRule();
        pr2.setName("PSE - Match Equals - All Category - No Limit");
        pr2.setPosition(generateEqualsPosition(PricingRule.Position.Reference.HIGHEST));
        pr2.setScope(PricingRule.Scope.CATEGORY);
        pr2.addCategory(categories.getFirst());
        pr2.setApplicationUser(user);
        pr2.setActive(true);

        // Rule 3 - not active
        PricingRule pr3 = new PricingRule();
        pr3.setName("PSE - Match Equals - All Category - No Limit");
        pr3.setPosition(generateEqualsPosition(PricingRule.Position.Reference.HIGHEST));
        pr3.setScope(PricingRule.Scope.CATEGORY);
        pr3.addCategory(categories.getFirst());
        pr3.setApplicationUser(user);
        pr3.setActive(false);

        // Rule 4 - 10% higher than highest - min limit 10€
        PricingRule pr4 = new PricingRule();
        pr4.setName("PSE - Match Equals - All Category - Min Limit");
        pr4.setPosition(generateCustomPosition(new BigDecimal(10), PricingRule.Position.Unit.PERCENTAGE, PricingRule.Position.MatchType.HIGHER, PricingRule.Position.Reference.HIGHEST));
        pr4.setScope(PricingRule.Scope.CATEGORY);
        pr4.addCategory(categories.getFirst());
        pr4.setApplicationUser(user);
        pr4.setActive(true);

        PriceLimit limit = generateLimit(PriceLimit.LimitType.FIXED_AMOUNT, BigDecimal.valueOf(10));
        pr4.setMinLimit(limit);

        List<PricingRule> pricingRules = List.of(pr1, pr2, pr3, pr4);
        pricingRules.forEach(pricingRule -> {
            pricingRuleRepository.save(pricingRule);
            log.info("Creating Pricing Rule: {}", pricingRule.getName());
        });
    }

    private PricingRule.Position generateEqualsPosition(PricingRule.Position.Reference reference) {
        return new PricingRule.Position(null, null, PricingRule.Position.MatchType.EQUALS, reference);
    }

    private PricingRule.Position generateCustomPosition(BigDecimal value, PricingRule.Position.Unit unit, PricingRule.Position.MatchType matchType, PricingRule.Position.Reference reference) {
        return new PricingRule.Position(value, unit, matchType, reference);
    }

    private PriceLimit generateLimit(PriceLimit.LimitType limitType, BigDecimal value) {
        PriceLimit priceLimit = new PriceLimit();
        priceLimit.setLimitType(limitType);
        priceLimit.setLimitValue(value);
        return priceLimit;
    }
}
