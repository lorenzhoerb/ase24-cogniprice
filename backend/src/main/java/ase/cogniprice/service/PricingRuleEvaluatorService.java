package ase.cogniprice.service;

import ase.cogniprice.entity.PricingRule;

import java.math.BigDecimal;

public interface PricingRuleEvaluatorService {


    /**
     * Calculates the price based on the specified pricing rule's position and limits.
     *
     * <p><b>Reference Price:</b></p>
     * The reference price is determined from the market prices of competitors for the product
     * with the given {@code productId}. It can be one of the following:
     * <ul>
     *   <li><b>AVERAGE:</b> The average market price.</li>
     *   <li><b>HIGHEST:</b> The highest market price.</li>
     *   <li><b>CHEAPEST:</b> The lowest market price.</li>
     * </ul>
     *
     * <p><b>Position Match Types:</b></p>
     * The position in the pricing rule determines how the reference price is adjusted:
     * <ul>
     *   <li><b>EQUALS:</b> The resulting price equals the reference price.</li>
     *   <li><b>HIGHER:</b> Adds a percentage or a fixed value (in EUR) to the reference price:
     *     <ul>
     *       <li><b>PERCENTAGE:</b> {@code referencePrice * (1 + value)}</li>
     *       <li><b>EUR:</b> {@code referencePrice + value}</li>
     *     </ul>
     *   </li>
     *   <li><b>LOWER:</b> Reduces the reference price by a percentage or a fixed value:
     *     <ul>
     *       <li><b>PERCENTAGE:</b> {@code referencePrice * (1 - value)}</li>
     *       <li><b>EUR:</b> {@code referencePrice - value}</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>Limit Cropping:</b></p>
     * If the pricing rule includes limits, the calculated price is cropped as follows:
     * <ul>
     *   <li><b>Min Limit:</b> Ensures the price does not fall below this value.</li>
     *   <li><b>Max Limit:</b> Ensures the price does not exceed this value.</li>
     * </ul>
     *
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li><b>10% HIGHER than [reference]:</b> {@code referencePrice * 1.1}</li>
     *   <li><b>10€ HIGHER than [reference]:</b> {@code referencePrice + 10}</li>
     *   <li><b>10% LOWER than [reference]:</b> {@code referencePrice * 0.9}</li>
     *   <li><b>10€ LOWER than [reference]:</b> {@code referencePrice - 10}</li>
     *   <li><b>EQUALS to [reference]:</b> {@code referencePrice}</li>
     * </ul>
     *
     * @param productId   the ID of the product for which the price is being calculated
     * @param pricingRule the pricing rule defining the adjustments and limits
     * @return the calculated price based on the pricing rule and market data
     */
    BigDecimal calculatePrice(Long productId, PricingRule pricingRule);
}
