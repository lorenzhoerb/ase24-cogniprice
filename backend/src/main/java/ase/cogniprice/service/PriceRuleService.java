package ase.cogniprice.service;

import ase.cogniprice.controller.dto.pricing.rule.PriceRuleDetailsDto;
import ase.cogniprice.controller.dto.pricing.rule.PriceRuleListFilter;
import ase.cogniprice.controller.dto.pricing.rule.PriceRuleRequestDto;
import ase.cogniprice.controller.dto.pricing.rule.SimplePricingRuleDto;
import ase.cogniprice.exception.ConflictException;
import ase.cogniprice.exception.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PriceRuleService {

    /**
     * Retrieves the price rules for a specific user, filtered by optional criteria.
     * The filter supports searching by rule status (e.g., Active or Inactive)
     * and by rule name with a case-insensitive match.
     *
     * @param username the username of the user whose price rules are to be fetched
     * @param filter   the filter containing status and/or name criteria for price rules
     * @param pageable the pagination information for the query
     * @return a paginated list of price rules matching the specified criteria
     */
    Page<SimplePricingRuleDto> getPriceRules(String username, PriceRuleListFilter filter, Pageable pageable);

    /**
     * Deletes a specific pricing rule by its ID, if the authenticated user is the owner of the rule.
     *
     * <p>If the user does not own the pricing rule, a {@link ForbiddenException} will be thrown.
     *
     * @param username    the username of the authenticated user attempting to delete the pricing rule.
     * @param priceRuleId the ID of the pricing rule to delete.
     * @throws ForbiddenException      if the user does not own the pricing rule and thus cannot delete it.
     * @throws EntityNotFoundException if no pricing rule with the specified ID is found.
     */
    void deletePriceRuleById(String username, Long priceRuleId);

    /**
     * Creates a new price rule for the authenticated user.
     *
     * <p>This method allows the authenticated user to create a pricing rule, specifying details such as
     * name, scope, applied entities, and position. The rule is created with the specified configuration.
     * Validation checks are applied to ensure that the rule is logically valid (e.g., ensuring valid product IDs
     * or category names for the applied scope and checking for conflicts in configuration combinations).
     *
     * @param username         the username of the authenticated user attempting to create the price rule.
     * @param priceRuleRequest the valid {@link PriceRuleRequestDto} containing the details for the price rule.
     *                         This includes information such as rule name, scope, applied entities (products/categories),
     *                         position, and price limits.
     * @return a {@link PriceRuleDetailsDto} representing the details of the newly created price rule.
     *     This includes the rule's unique ID and other relevant details after creation.
     * @throws ConflictException if there are conflicts with the provided price rule configuration.
     *                           This could be due to:
     *                           <ul>
     *                             <li>The applied entity (product ID or category) not being valid or accessible by the user.</li>
     *                             <li>Invalid combinations in the configuration, e.g., applying a unit to a "equals" match type.</li>
     *                             <li>If min and max limit are set. The min limit must be lower then the max value</li>
     *                           </ul>
     */
    PriceRuleDetailsDto createPriceRule(String username, PriceRuleRequestDto priceRuleRequest);

    /**
     * Update the price rule for the authenticated user.
     *
     * <p>This method allows the authenticated user to create a pricing rule, specifying details such as
     * name, scope, applied entities, and position. The rule is created with the specified configuration.
     * Validation checks are applied to ensure that the rule is logically valid (e.g., ensuring valid product IDs
     * or category names for the applied scope and checking for conflicts in configuration combinations).
     *
     * @param username         the username of the authenticated user attempting to create the price rule.
     * @param priceRuleId      the id of the price rule to update
     * @param priceRuleRequest the valid {@link PriceRuleRequestDto} containing the details for the price rule.
     *                         This includes information such as rule name, scope, applied entities (products/categories),
     *                         position, and price limits.
     * @return a {@link PriceRuleDetailsDto} representing the details of the newly created price rule.
     *     This includes the rule's unique ID and other relevant details after creation.
     * @throws ConflictException if there are conflicts with the provided price rule configuration.
     *                           This could be due to:
     *                           <ul>
     *                             <li>The applied entity (product ID or category) not being valid or accessible by the user.</li>
     *                             <li>Invalid combinations in the configuration, e.g., applying a unit to a "equals" match type.</li>
     *                             <li>If min and max limit are set. The min limit must be lower then the max value</li>
     *                           </ul>
     * @throws EntityNotFoundException if the user does not have a price rule with this id
     */
    PriceRuleDetailsDto updatePriceRule(String username, Long priceRuleId, PriceRuleRequestDto priceRuleRequest);

    /**
     * Retrieves a specific pricing rule by its ID for a given user.
     *
     * <p>This method ensures that the authenticated user has access to the requested
     * pricing rule. If the rule does not exist or the user does not own the rule,
     * appropriate exceptions are thrown.
     *
     * @param username    the username of the authenticated user attempting to fetch the pricing rule.
     * @param priceRuleId the ID of the pricing rule to retrieve.
     * @return a {@link PriceRuleDetailsDto} containing details of the pricing rule.
     * @throws ForbiddenException      if the user does not own the pricing rule or lacks access rights.
     * @throws EntityNotFoundException if no pricing rule with the specified ID is found.
     */
    PriceRuleDetailsDto getPriceRuleById(String username, Long priceRuleId);
}
