package ase.cogniprice.integrationTest.pricingRuleIntegrationTest;

import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.PricingRuleRepository;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.service.implementation.PriceRuleServiceImpl;
import ase.cogniprice.type.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

import static ase.cogniprice.integrationTest.storeProductIntegrationTest.DataProvider.createProduct;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PricingRuleRepositoryTest {

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    private Product p1, p2;
    private ProductCategory c1, c2;
    private ApplicationUser user;
    @Autowired
    private PriceRuleServiceImpl priceRuleServiceImpl;

    @BeforeEach
    void setUp() {
        applicationUserRepository.deleteAll();
        productCategoryRepository.deleteAll();
        productRepository.deleteAll();

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

        c2 = new ProductCategory();
        c2.setCategory("C2");
        c2 = productCategoryRepository.save(c2);

        p1 = createProduct(c1);
        p1.setGtin("123456789");
        p1 = productRepository.save(p1);

        p2 = createProduct(c2);
        p2.setGtin("123456788");
        p2 = productRepository.save(p2);

        pricingRuleRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        applicationUserRepository.deleteAll();
        productCategoryRepository.deleteAll();
        productRepository.deleteAll();
        pricingRuleRepository.deleteAll();
    }

    @Test
    void testFindPricingRulesByFilter() {
        pricingRuleRepository.findPricingRulesByFilter(1L, null, null, Pageable.ofSize(10));
    }

    @Test
    void testFindActivePricingRulesForProduct_expectCorrectRules() {
        // Prepare
        // Active category scope for p1
        PricingRule pr1 = pricingRuleRepository.save(DataProvider.generatePricingRuleForCategoryScope("Pricing Rule Active Category C1", true, user, Set.of(c1)));
        // Active category scope for p2
        PricingRule pr2 = pricingRuleRepository.save(DataProvider.generatePricingRuleForCategoryScope("Pricing Rule Active Category C2", true, user, Set.of(c2)));
        // Disabled
        PricingRule pr3 = pricingRuleRepository.save(DataProvider.generatePricingRule("Disabled 1", false, user, Set.of()));
        // Active product scope for p1, p2
        PricingRule pr4 = pricingRuleRepository.save(DataProvider.generatePricingRule("Pricing Rule Active Product P1, P2", true, user, Set.of(p1, p2)));
        // Active product scope for p1, p2
        PricingRule pr5 = pricingRuleRepository.save(DataProvider.generatePricingRule("Pricing Rule Active Product P2", true, user, Set.of(p2)));

        // Execute
        // Get pricing rules which are associated with product p1 with scope (p1 has category c1).
        // Expect: pr1, because of scope c1; pr4, because of scope p1, p2
        // Don't Expect: pr2 because of scope c2; don't expect pr3, because of disabled; don't expect pr5, because of scope p2
        List<PricingRule> result = pricingRuleRepository.findActivePricingRulesForProduct(p1.getId());
        List<Long> resultIds = result.stream().map(PricingRule::getId).toList();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertTrue(resultIds.contains(pr1.getId()), "Result should contain pr1, because of scope C1"),
                () -> assertTrue(resultIds.contains(pr4.getId()), "Result should contain pr4, because of scope p1"),
                () -> assertFalse(resultIds.contains(pr2.getId()), "Result should not contain pr2, because p1 does not have category c2"),
                () -> assertFalse(resultIds.contains(pr3.getId()), "Result should not contain pr3, because disabled"),
                () -> assertFalse(resultIds.contains(pr5.getId()), "Result should not contain pr3, because scope p2")
        );
    }

    @Test
    void testFindActivePricingRulesForProduct_StatusDisabled_ExpectNone() {
        // Prepare
        List<PricingRule> pricingRules = List.of(
                DataProvider.generatePricingRule("Disabled 1", false, user, Set.of()),
                DataProvider.generatePricingRule("Disabled 2", false, user, Set.of())
        );
        pricingRuleRepository.saveAll(pricingRules);

        // Execute
        List<PricingRule> result = pricingRuleRepository.findActivePricingRulesForProduct(p1.getId());

        // Check
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(0, result.size())
        );
    }
}
