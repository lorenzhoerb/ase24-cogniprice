package ase.cogniprice.repository;

import ase.cogniprice.entity.PricingRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    /**
     * Filters pricing rules by the provided name and status.
     *
     * @param pageable pagination details.
     * @return a page of filtered pricing rules.
     */
    @Query("""
            SELECT pr FROM PricingRule pr
            WHERE (:name IS NULL OR :name = '' OR LOWER(pr.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:isActive IS NULL OR pr.isActive = :isActive)
            AND (:userId = pr.applicationUser.id)
            """
    )
    Page<PricingRule> findPricingRulesByFilter(Long userId, String name, Boolean isActive, Pageable pageable);

    /**
     * Finds all active pricing rules associated with the given product:
     * - If the pricing rule's scope is PRODUCT, matches the product's ID.
     * - If the pricing rule's scope is CATEGORY, matches the category of the product.
     *
     * @param productId the ID of the product
     * @return a list of applicable pricing rules
     */
    @Query("""
                SELECT pr
                FROM PricingRule pr
                LEFT JOIN pr.categories c
                LEFT JOIN pr.products p
                WHERE pr.isActive = true
                  AND (
                      (pr.scope = 'PRODUCT' AND p.id = :productId)
                      OR
                      (pr.scope = 'CATEGORY' AND c.category = (
                          SELECT p2.productCategory.category
                          FROM Product p2
                          WHERE p2.id = :productId
                      ))
                  )
            """)
    List<PricingRule> findActivePricingRulesForProduct(@Param("productId") Long productId);
}
