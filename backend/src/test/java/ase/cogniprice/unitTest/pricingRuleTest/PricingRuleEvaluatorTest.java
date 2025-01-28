package ase.cogniprice.unitTest.pricingRuleTest;

import ase.cogniprice.entity.PriceLimit;
import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.repository.ProductPriceRepository;
import ase.cogniprice.service.implementation.PricingRuleEvaluatorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
public class PricingRuleEvaluatorTest {
    @Mock
    private ProductPriceRepository productPriceRepository;

    @InjectMocks
    private PricingRuleEvaluatorServiceImpl pricingRuleEvaluatorService;

    @BeforeEach
    void setUp() {
        pricingRuleEvaluatorService.postConstructor(); // Manually trigger the @PostConstruct logic
    }

    @Test
    void testEqualsPosition_Euro_WithoutLimits() {
        Long productId = 1L;
        BigDecimal referencePrice = BigDecimal.valueOf(100);

        PricingRule pricingRule = new PricingRule();
        pricingRule.setPosition(new PricingRule.Position(null, PricingRule.Position.Unit.EUR, PricingRule.Position.MatchType.EQUALS, PricingRule.Position.Reference.AVERAGE));

        when(productPriceRepository.findAvgPriceByProductId(productId)).thenReturn(Optional.of(referencePrice));

        BigDecimal calculatedPrice = pricingRuleEvaluatorService.calculatePrice(productId, pricingRule);
        assertEquals(referencePrice, calculatedPrice, "The calculated price should equal the reference price.");
    }

    @Test
    void testHigherPercentagePosition_Percentage_WithoutLimits() {
        Long productId = 1L;
        BigDecimal referencePrice = BigDecimal.valueOf(40);

        PricingRule pricingRule = new PricingRule();
        pricingRule.setPosition(new PricingRule.Position(BigDecimal.TEN, PricingRule.Position.Unit.PERCENTAGE, PricingRule.Position.MatchType.HIGHER, PricingRule.Position.Reference.HIGHEST));

        when(productPriceRepository.findMaxPriceByProductId(productId)).thenReturn(Optional.of(referencePrice));

        BigDecimal calculatedPrice = pricingRuleEvaluatorService.calculatePrice(productId, pricingRule);
        assertEquals(BigDecimal.valueOf(44).stripTrailingZeros(), calculatedPrice.stripTrailingZeros(), "The price should increase by 10%."); // 40*1,1=44
    }


    @Test
    void testLowerPercentagePosition_Percentage_WithoutLimits() {
        Long productId = 1L;
        BigDecimal referencePrice = BigDecimal.valueOf(40);

        PricingRule pricingRule = new PricingRule();
        pricingRule.setPosition(new PricingRule.Position(BigDecimal.TEN, PricingRule.Position.Unit.PERCENTAGE, PricingRule.Position.MatchType.LOWER, PricingRule.Position.Reference.CHEAPEST));

        when(productPriceRepository.findMinPriceByProductId(productId)).thenReturn(Optional.of(referencePrice));

        BigDecimal calculatedPrice = pricingRuleEvaluatorService.calculatePrice(productId, pricingRule);
        assertEquals(BigDecimal.valueOf(36).stripTrailingZeros(), calculatedPrice.stripTrailingZeros(), "The price should decrease by 10%."); // 40*0,9=36
    }

    @Test
    void testHigherPercentagePosition_Percentage_OverMaxLimits() {
        Long productId = 1L;
        BigDecimal referencePrice = BigDecimal.valueOf(40);

        PricingRule pricingRule = new PricingRule();
        pricingRule.setPosition(new PricingRule.Position(BigDecimal.TEN, PricingRule.Position.Unit.PERCENTAGE, PricingRule.Position.MatchType.HIGHER, PricingRule.Position.Reference.HIGHEST));
        // max limit = 42€
        pricingRule.setMaxLimit(new PriceLimit(1L, PriceLimit.LimitType.FIXED_AMOUNT, BigDecimal.valueOf(42)));

        when(productPriceRepository.findMaxPriceByProductId(productId)).thenReturn(Optional.of(referencePrice));

        // expect: 40*1,1=44 -> crop to 42
        BigDecimal calculatedPrice = pricingRuleEvaluatorService.calculatePrice(productId, pricingRule);
        assertEquals(BigDecimal.valueOf(42).stripTrailingZeros(), calculatedPrice.stripTrailingZeros(), "The price should increase by 10%. and be cropped at 42");
    }

    @Test
    void testLowerPercentagePosition_Percentage_OverMinLimits() {
        Long productId = 1L;
        BigDecimal referencePrice = BigDecimal.valueOf(40);

        PricingRule pricingRule = new PricingRule();
        pricingRule.setPosition(new PricingRule.Position(BigDecimal.TEN, PricingRule.Position.Unit.PERCENTAGE, PricingRule.Position.MatchType.LOWER, PricingRule.Position.Reference.CHEAPEST));
        // min limit = 39€
        pricingRule.setMinLimit(new PriceLimit(1L, PriceLimit.LimitType.FIXED_AMOUNT, BigDecimal.valueOf(39)));


        when(productPriceRepository.findMinPriceByProductId(productId)).thenReturn(Optional.of(referencePrice));

        // expect: 40*0,9=36 -> crop to 39
        BigDecimal calculatedPrice = pricingRuleEvaluatorService.calculatePrice(productId, pricingRule);
        assertEquals(BigDecimal.valueOf(39).stripTrailingZeros(), calculatedPrice.stripTrailingZeros(), "The price should decrease by 10%."); // 40*0,9=36
    }

    @Test
    void testLowerPercentagePosition_Percentage_BetweenMinMaxLimits_ShouldNotCrop() {
        Long productId = 1L;
        BigDecimal referencePrice = BigDecimal.valueOf(40);

        PricingRule pricingRule = new PricingRule();
        pricingRule.setPosition(new PricingRule.Position(BigDecimal.TEN, PricingRule.Position.Unit.PERCENTAGE, PricingRule.Position.MatchType.LOWER, PricingRule.Position.Reference.CHEAPEST));

        // min limit = 20
        pricingRule.setMinLimit(new PriceLimit(1L, PriceLimit.LimitType.FIXED_AMOUNT, BigDecimal.valueOf(20)));
        // max limit = 20
        pricingRule.setMaxLimit(new PriceLimit(1L, PriceLimit.LimitType.FIXED_AMOUNT, BigDecimal.valueOf(50)));


        when(productPriceRepository.findMinPriceByProductId(productId)).thenReturn(Optional.of(referencePrice));

        // expect: 40*0,9=36 -> don' crop -> 36€
        BigDecimal calculatedPrice = pricingRuleEvaluatorService.calculatePrice(productId, pricingRule);
        assertEquals(BigDecimal.valueOf(36).stripTrailingZeros(), calculatedPrice.stripTrailingZeros(), "The price should decrease by 10%."); // 40*0,9=36
    }

}
