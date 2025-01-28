package ase.cogniprice.service.implementation;

import ase.cogniprice.controller.dto.pricing.rule.PriceAdjustmentDto;
import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.repository.PricingRuleRepository;
import ase.cogniprice.repository.ProductPriceRepository;
import ase.cogniprice.service.PricingRuleEvaluatorService;
import ase.cogniprice.service.PricingRuleProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PricingRuleProcessorServiceImpl implements PricingRuleProcessorService {

    private final PricingRuleRepository pricingRuleRepository;
    private final PricingRuleEvaluatorService pricingRuleEvaluatorService;
    private final ProductPriceRepository productPriceRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Inject the Kafka topic name with a default value
    @Value("${kafka.topic.pricingRuleAdjustment:pricing.rule.adjustment}")
    private String kafkaTopic;

    @Override
    public void evaluateAndTriggerRulesForProduct(Long productId) {
        log.trace("Evaluating rules for product {}", productId);
        pricingRuleRepository.findActivePricingRulesForProduct(productId)
                .forEach(pricingRule -> processPricingRule(productId, pricingRule));
    }

    @Override
    public void processPricingRule(Long productId, PricingRule pricingRule) {
        log.trace("Processing rule {}", pricingRule);
        // 1. Get pricing information
        // calculate adjusted price
        BigDecimal adjustedPrice = pricingRuleEvaluatorService.calculatePrice(productId, pricingRule);

        // get price statistics
        BigDecimal highestPrice = productPriceRepository.findMaxPriceByProductId(productId).orElse(null);
        BigDecimal averagePrice = productPriceRepository.findAvgPriceByProductId(productId).orElse(null);
        BigDecimal cheapestPrice = productPriceRepository.findMinPriceByProductId(productId).orElse(null);

        // 2. map adjusted
        PriceAdjustmentDto priceAdjustmentDto = PriceAdjustmentDto.builder()
                .pricingRuleId(pricingRule.getId())
                .productId(productId)
                .userId(pricingRule.getApplicationUser().getId())
                .adjustedPrice(adjustedPrice)
                .adjustmentDate(ZonedDateTime.now())
                .highestPrice(highestPrice)
                .averagePrice(averagePrice)
                .cheapestPrice(cheapestPrice)
                .build();


        // 3. send price rule trigger event to kafka
        kafkaTemplate.send(kafkaTopic, priceAdjustmentDto);
        log.debug("Triggered price rule {} for product {}: {}", pricingRule.getId(), productId, adjustedPrice);
    }
}
