package ase.cogniprice.integrationTest.pricingRuleIntegrationTest;

import ase.cogniprice.controller.dto.pricing.rule.PriceAdjustmentDto;
import ase.cogniprice.entity.*;
import ase.cogniprice.repository.*;
import ase.cogniprice.service.implementation.PriceRuleServiceImpl;
import ase.cogniprice.service.implementation.PricingRuleProcessorServiceImpl;
import ase.cogniprice.type.Role;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static ase.cogniprice.integrationTest.storeProductIntegrationTest.DataProvider.createProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
public class PricingRuleProcessorServiceIntegrationTest {

    @Autowired
    private PricingRuleProcessorServiceImpl pricingRuleProcessorService;

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private ProductPriceRepository productPriceRepository;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    private Product p1;
    private ProductCategory c1;
    private ApplicationUser user;
    private StoreProduct sp1, sp2;
    private PricingRule pricingRule;

    @Autowired
    private PriceRuleServiceImpl priceRuleServiceImpl;
    @Autowired
    private CompetitorRepository competitorRepository;
    @Autowired
    private StoreProductRepository storeProductRepository;

    @BeforeEach
    void setUp() {
        applicationUserRepository.deleteAll();
        productCategoryRepository.deleteAll();
        productRepository.deleteAll();
        storeProductRepository.deleteAll();
        pricingRuleRepository.deleteAll();

        user = new ApplicationUser();
        user.setUsername("testUser");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("testuser@example.com");
        user.setLoginAttempts(0L);
        user.setUserLocked(false);
        user.setRole(Role.USER);
        user = applicationUserRepository.save(user);

        c1 = new ProductCategory();
        c1.setCategory("C1");
        c1 = productCategoryRepository.save(c1);


        p1 = createProduct(c1);
        p1.setGtin("123456789");
        p1 = productRepository.save(p1);

        Competitor competitor = ase.cogniprice.integrationTest.storeProductIntegrationTest.DataProvider.generateCompetitor("comp1");
        competitor = competitorRepository.save(competitor);

        Competitor competitor2 = ase.cogniprice.integrationTest.storeProductIntegrationTest.DataProvider.generateCompetitor("comp2");
        competitor2 = competitorRepository.save(competitor2);

        sp1 = ase.cogniprice.integrationTest.storeProductIntegrationTest.DataProvider.createStoreProduct(p1, competitor);
        sp2 = ase.cogniprice.integrationTest.storeProductIntegrationTest.DataProvider.createStoreProduct(p1, competitor2);
        sp1 = storeProductRepository.save(sp1);
        sp2 = storeProductRepository.save(sp2);

        pricingRuleRepository.deleteAll();

        pricingRule = DataProvider.generatePricingRule("Test", true, user, Set.of(p1));
        pricingRule.setPosition(new PricingRule.Position(BigDecimal.TEN, PricingRule.Position.Unit.EUR, PricingRule.Position.MatchType.HIGHER, PricingRule.Position.Reference.AVERAGE)); // price rule: 10€ higher than avg
        pricingRule = pricingRuleRepository.save(pricingRule);
    }

    @AfterEach
    void cleanUp() {
        productPriceRepository.deleteAll();
        storeProductRepository.deleteAll();
        productRepository.deleteAll();
        productCategoryRepository.deleteAll();
        competitorRepository.deleteAll();
        pricingRuleRepository.deleteAll();
        applicationUserRepository.deleteAll();
    }

    @Test
    void testEvaluateAndTriggerRulesForProduct_expectSuccess() {
        ProductPrice pp1 = ase.cogniprice.integrationTest.storeProductIntegrationTest.DataProvider.generateProductPrice(sp1);
        pp1.setPrice(Money.of(5, "EUR"));
        ProductPrice pp2 = ase.cogniprice.integrationTest.storeProductIntegrationTest.DataProvider.generateProductPrice(sp1); // updated price for a later time (so the last price of product p1, c1
        pp2.setPrice(Money.of(16, "EUR"));

        ProductPrice pp3 = ase.cogniprice.integrationTest.storeProductIntegrationTest.DataProvider.generateProductPrice(sp2); // updated price for a later time (so the last price of product p1, c1
        pp3.setPrice(Money.of(20, "EUR"));
        productPriceRepository.saveAll(List.of(pp1, pp2, pp3)); // save all product prices

        // Execute the method under test
        pricingRuleProcessorService.evaluateAndTriggerRulesForProduct(p1.getId());

        // Capture the object passed to Kafka
        ArgumentCaptor<PriceAdjustmentDto> argumentCaptor = ArgumentCaptor.forClass(PriceAdjustmentDto.class);

        // Verify that KafkaTemplate's send method was called
        verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.eq("pricing.rule.adjustment"), argumentCaptor.capture());

        // Retrieve the captured value
        PriceAdjustmentDto capturedDto = argumentCaptor.getValue();

        // Last prices of product 1 are: 16 and 20. => Average = 36/2=18, with price rule 10€ higher than average => adjustedPrice = 28

        // Assertions to verify the DTO
        assertNotNull(capturedDto, "PriceAdjustmentDto should not be null");
        assertEquals(p1.getId(), capturedDto.getProductId(), "Product ID should match");
        assertEquals(BigDecimal.valueOf(28).stripTrailingZeros(), capturedDto.getAdjustedPrice().stripTrailingZeros(), "Adjusted price should match");
        assertEquals(BigDecimal.valueOf(20).stripTrailingZeros(), capturedDto.getHighestPrice().stripTrailingZeros(), "Highest should match");
        assertEquals(BigDecimal.valueOf(16).stripTrailingZeros(), capturedDto.getCheapestPrice().stripTrailingZeros(), "Cheapest should match");
        assertEquals(BigDecimal.valueOf(18).stripTrailingZeros(), capturedDto.getAveragePrice().stripTrailingZeros(), "Average should match");
        assertEquals(user.getId(), capturedDto.getUserId(), "User ID should match");
        assertNotNull(capturedDto.getAdjustmentDate(), "Adjustment date should not be null");
    }
}
