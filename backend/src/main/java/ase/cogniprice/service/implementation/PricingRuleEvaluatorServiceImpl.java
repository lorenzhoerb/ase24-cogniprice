package ase.cogniprice.service.implementation;

import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.repository.ProductPriceRepository;
import ase.cogniprice.service.PricingRuleEvaluatorService;
import ase.cogniprice.service.PricingStrategy;
import ase.cogniprice.service.implementation.price.strategy.EqualPricingStrategy;
import ase.cogniprice.service.implementation.price.strategy.HigherPricingStrategy;
import ase.cogniprice.service.implementation.price.strategy.LowerPricingStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PricingRuleEvaluatorServiceImpl implements PricingRuleEvaluatorService {

    private final Map<PricingRule.Position.MatchType, PricingStrategy> pricingStrategies = new EnumMap<>(PricingRule.Position.MatchType.class);
    private final ProductPriceRepository productPriceRepository;

    @PostConstruct
    public void postConstructor() {
        pricingStrategies.put(PricingRule.Position.MatchType.EQUALS, new EqualPricingStrategy());
        pricingStrategies.put(PricingRule.Position.MatchType.HIGHER, new HigherPricingStrategy());
        pricingStrategies.put(PricingRule.Position.MatchType.LOWER, new LowerPricingStrategy());
        log.debug("Pricing strategies initialized: {}", pricingStrategies);
    }

    @Override
    public BigDecimal calculatePrice(Long productId, PricingRule pricingRule) {
        log.trace("Calculating price for productId: {}, pricingRule: {}", productId, pricingRule);
        PricingRule.Position position = pricingRule.getPosition();
        BigDecimal referencePrice = getReferencePriceOrThrows(productId, position.getReference());

        PricingStrategy strategy = pricingStrategies.get(position.getMatchType());

        // Get the position
        BigDecimal positionPrice = strategy.applyPricing(referencePrice, position.getValue(), position.getUnit());
        log.debug("Calculated position price (before cropping): {}", positionPrice);

        // crop the price if min and max limits exist
        BigDecimal finalPrice = cropPrice(positionPrice, pricingRule);
        log.debug("Calculated price (after cropping): {}", finalPrice);

        return finalPrice;
    }

    /**
     * Adjusts the price to respect the minimum and maximum limits if configured.
     *
     * @param price       The calculated price before applying limits.
     * @param pricingRule The pricing rule that may define the limits.
     * @return The adjusted price respecting the limits.
     */
    private BigDecimal cropPrice(BigDecimal price, PricingRule pricingRule) {
        BigDecimal minLimit = pricingRule.getMinLimit() != null ? pricingRule.getMinLimit().getLimitValue() : null;
        BigDecimal maxLimit = pricingRule.getMaxLimit() != null ? pricingRule.getMaxLimit().getLimitValue() : null;
        log.debug("Applying cropping logic. Min limit: {}, Max limit: {}", minLimit, maxLimit);

        if (minLimit != null) {
            price = price.max(minLimit); // Ensure price is not below min limit
        }
        if (maxLimit != null) {
            price = price.min(maxLimit); // Ensure price is not above max limit
        }
        return price;
    }


    private Optional<BigDecimal> getReferencePrice(Long productId, PricingRule.Position.Reference reference) {
        log.trace("Fetching reference price for reference type: {}", reference);
        return switch (reference) {
            case HIGHEST -> productPriceRepository.findMaxPriceByProductId(productId);
            case AVERAGE -> productPriceRepository.findAvgPriceByProductId(productId);
            case CHEAPEST -> productPriceRepository.findMinPriceByProductId(productId);
        };
    }

    private BigDecimal getReferencePriceOrThrows(Long productId, PricingRule.Position.Reference reference) {
        log.debug("get reference price or throws ({}, {})", productId, reference);
        return getReferencePrice(productId, reference)
                .orElseThrow(() -> {
                    log.error("Could not get price for product {}. This should not happen.", productId);
                    return new RuntimeException("Could not get reference price");
                });
    }
}
