package ase.cogniprice.unitTest.pricingRuleTest;

import ase.cogniprice.controller.dto.pricing.rule.PriceRuleListFilter;
import ase.cogniprice.controller.dto.pricing.rule.PricingRuleStatus;
import ase.cogniprice.controller.dto.pricing.rule.SimplePricingRuleDto;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.PricingRuleRepository;
import ase.cogniprice.service.implementation.PriceRuleServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class PriceRuleServiceTest {
    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @Mock
    private PricingRuleRepository pricingRuleRepository;

    @InjectMocks
    private PriceRuleServiceImpl priceRuleServiceImpl;

    private ApplicationUser mockUser;
    private PricingRule mockPricingRule;
    private PriceRuleListFilter mockFilter;
    private Pageable mockPageable;

    @BeforeEach
    void setUp() {
        // Mock user
        mockUser = new ApplicationUser();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");

        // Mock pricing rule
        mockPricingRule = new PricingRule();
        mockPricingRule.setId(1L);
        mockPricingRule.setName("Test Rule");
        mockPricingRule.setActive(true);
        mockPricingRule.setScope(PricingRule.Scope.PRODUCT);

        PricingRule.Position position = new PricingRule.Position();
        position.setValue(new BigDecimal("10.00"));
        position.setUnit(PricingRule.Position.Unit.EUR);
        position.setMatchType(PricingRule.Position.MatchType.HIGHER);
        position.setReference(PricingRule.Position.Reference.CHEAPEST);
        mockPricingRule.setPosition(position);

        // Mock filter and pageable
        mockFilter = new PriceRuleListFilter("Test Rule", PricingRuleStatus.ACTIVE);
        mockPageable = Pageable.unpaged();
    }

    @Nested
    class GetPriceRulesTests {

        @Test
        void testGetPriceRules_UserNotFound() {
            // Mock the repository to return null for the user
            when(applicationUserRepository.findApplicationUserByUsername("testUser")).thenReturn(null);

            // Test if the EntityNotFoundException is thrown
            Exception exception = assertThrows(EntityNotFoundException.class, () -> {
                priceRuleServiceImpl.getPriceRules("testUser", mockFilter, mockPageable);
            });

            assertEquals("User with username 'testUser' not found.", exception.getMessage());
        }

        @Test
        void testGetPriceRules_ReturnsPriceRules() {
            // Mock the user repository
            when(applicationUserRepository.findApplicationUserByUsername("testUser")).thenReturn(mockUser);

            // Mock the pricing rule repository
            Page<PricingRule> pricingRulePage = new PageImpl<>(Collections.singletonList(mockPricingRule));
            when(pricingRuleRepository.findPricingRulesByFilter(1L, "Test Rule", true, mockPageable)).thenReturn(pricingRulePage);

            // Call the service method
            Page<SimplePricingRuleDto> result = priceRuleServiceImpl.getPriceRules("testUser", mockFilter, mockPageable);

            // Verify the result
            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(1, result.getTotalElements()),
                    () -> assertEquals("Test Rule", result.getContent().get(0).getName()),
                    () -> assertTrue(result.getContent().get(0).isActive())
            );
        }

        @Test
        void testGetPriceRules_NoFilter_ExpectAllRulesFromUser() {
            // Mock the user repository
            when(applicationUserRepository.findApplicationUserByUsername("testUser")).thenReturn(mockUser);

            // Mock the pricing rule repository with no filter
            Page<PricingRule> pricingRulePage = new PageImpl<>(Collections.singletonList(mockPricingRule));
            when(pricingRuleRepository.findPricingRulesByFilter(1L, "", null, mockPageable)).thenReturn(pricingRulePage);

            // Test with no filter
            Page<SimplePricingRuleDto> result = priceRuleServiceImpl.getPriceRules("testUser", null, mockPageable);

            // Verify that the result is correct using assertAll
            assertAll("Price rule assertions",
                    () -> assertNotNull(result),
                    () -> assertEquals(1, result.getTotalElements()),
                    () -> assertEquals("Test Rule", result.getContent().getFirst().getName()),
                    () -> assertTrue(result.getContent().getFirst().isActive()) // Default status should be inactive
            );
        }
    }
}
