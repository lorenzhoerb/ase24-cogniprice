package ase.cogniprice.service;

import ase.cogniprice.entity.PricingRule;


/**
 * Handles the evaluation and triggering of pricing rules for products.
 * Responsible for processing active rules and initiating price adjustment events.
 */
public interface PricingRuleProcessorService {

    /**
     * Evaluates and triggers pricing rules associated with a specific product.
     *
     * <p>A product is associated with a {@link ase.cogniprice.entity.PricingRule} if:
     * 1. The rule's scope is `PRODUCT`, and the product ID matches the rule's configuration.
     * 2. The rule's scope is `CATEGORY`, and the product's category matches the rule's configuration.
     *
     * <p>For each applicable rule, the `processPricingRule` method is invoked to evaluate and trigger adjustments.
     *
     * @param productId the unique identifier of the product to process pricing rules for.
     */
    void evaluateAndTriggerRulesForProduct(Long productId);

    /**
     * Processes a specific pricing rule and suggests a price adjustment.
     *
     * <p>The method evaluates the rule based on its configuration and constraints, then triggers an adjustment event.
     *
     * @param productId   the product that triggered the evaluation
     * @param pricingRule the pricing rule to evaluate and trigger.
     */
    void processPricingRule(Long productId, PricingRule pricingRule);
}
