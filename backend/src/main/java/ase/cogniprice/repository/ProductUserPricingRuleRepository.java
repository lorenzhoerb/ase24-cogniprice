package ase.cogniprice.repository;

import ase.cogniprice.entity.ProductUserPricingRule;
import ase.cogniprice.entity.ProductUserPricingRule.ProductUserPricingRuleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUserPricingRuleRepository extends JpaRepository<ProductUserPricingRule, ProductUserPricingRuleId> {
    // Additional custom query methods can be added here if needed
}
