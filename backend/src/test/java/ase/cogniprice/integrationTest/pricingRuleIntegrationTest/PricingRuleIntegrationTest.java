package ase.cogniprice.integrationTest.pricingRuleIntegrationTest;

import ase.cogniprice.config.JwtTestUtils;
import ase.cogniprice.controller.dto.pricing.rule.PositionDto;
import ase.cogniprice.controller.dto.pricing.rule.PriceRuleDetailsDto;
import ase.cogniprice.controller.dto.pricing.rule.PriceRuleRequestDto;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.PriceLimit;
import ase.cogniprice.entity.PricingRule;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.CompetitorRepository;
import ase.cogniprice.repository.PricingRuleRepository;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.type.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static ase.cogniprice.integrationTest.storeProductIntegrationTest.DataProvider.createProduct;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PricingRuleIntegrationTest {

    private static final String USERNAME = "testuser";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompetitorRepository competitorRepository;

    @Autowired
    private StoreProductRepository storeProductRepository;

    @Autowired
    private JwtTestUtils jwtTestUtils;

    @Autowired
    private ObjectMapper objectMapper;


    private ApplicationUser user;
    private List<PricingRule> pricingRules;
    private String token;
    private Product p2Saved;

    @BeforeEach
    void setup() {
        applicationUserRepository.deleteAll();
        productCategoryRepository.deleteAll();
        productRepository.deleteAll();
        storeProductRepository.deleteAll();
        competitorRepository.deleteAll();

        user = new ApplicationUser();
        user.setUsername(USERNAME);
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("testuser@example.com");
        user.setLoginAttempts(0L);
        user.setUserLocked(false);
        user.setRole(Role.USER);
        user = applicationUserRepository.save(user);

        token = jwtTestUtils.generateToken(USERNAME, List.of("ROLE_USER"));
        pricingRules = List.of(
                DataProvider.generateBasicPricingRule("Dog Rule 1", true, user),
                DataProvider.generateBasicPricingRule("Dog Rule 2", false, user),
                DataProvider.generateBasicPricingRule("Cat Rule 3", false, user),
                DataProvider.generateBasicPricingRule("Cat Rule 4", true, user),
                DataProvider.generateBasicPricingRule("Cat Rule 5", true, user)
        );

        ProductCategory c1 = new ProductCategory();
        c1.setCategory("C1");
        productCategoryRepository.save(c1);

        ProductCategory c2 = new ProductCategory();
        c2.setCategory("C2");
        productCategoryRepository.save(c2);

        Product p1 = createProduct(c1);
        p1.setGtin("123456789");
        productRepository.save(p1);

        Product p2 = createProduct(c2);
        p2.setGtin("123456788");
        p2Saved = productRepository.save(p2);


        pricingRuleRepository.deleteAll();
    }

    @Nested
    class GetAllPricingRulesTests {
        @Test
        void testGetAllPricingRulesForUser_Unauthenticated_ExpectUnauthorized() throws Exception {
            mockMvc.perform(get("/api/pricing-rules")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void testGetAllPricingRulesForUser_NoRules_ValidRequest() throws Exception {
            mockMvc.perform(get("/api/pricing-rules")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0))  // Expecting 0 rules if no pricing rules are present
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        void testGetAllPricingRulesForUser_NoFilter_ValidRequest() throws Exception {
            pricingRuleRepository.saveAll(pricingRules);

            mockMvc.perform(get("/api/pricing-rules")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(5))  // Expecting 5 rules
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isNotEmpty())
                    .andExpect(jsonPath("$.content.length()").value(5))  // Check array length
                    .andExpect(jsonPath("$.content[*].name").value(hasItem("Dog Rule 1")))  // Contains 'Dog Rule 1'
                    .andExpect(jsonPath("$.content[*].name").value(hasItem("Cat Rule 4")));
        }

        @Test
        void testGetAllPricingRulesForUser_ActiveFilter_ValidRequest() throws Exception {
            pricingRuleRepository.saveAll(pricingRules);

            mockMvc.perform(get("/api/pricing-rules?status=ACTIVE")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(3))  // Expecting 3 active rules
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(3))  // Check array length
                    .andExpect(jsonPath("$.content[*].name").value(hasItem("Dog Rule 1")))  // Should contain this name
                    .andExpect(jsonPath("$.content[*].name").value(hasItem("Cat Rule 4")));
        }

        @Test
        void testGetAllPricingRulesForUser_NameFilter_ValidRequest() throws Exception {
            pricingRuleRepository.saveAll(pricingRules);

            mockMvc.perform(get("/api/pricing-rules?name=cat")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(3))  // Expecting 3 cat rules
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(3))  // Check array length
                    .andExpect(jsonPath("$.content[*].name").value(hasItem("Cat Rule 3")))  // Should contain 'Cat Rule 3'
                    .andExpect(jsonPath("$.content[*].name").value(hasItem("Cat Rule 4")))  // Should contain 'Cat Rule 4'
                    .andExpect(jsonPath("$.content[*].name").value(hasItem("Cat Rule 5")));  // Should contain 'Cat Rule 5'
        }

        @Test
        void testGetAllPricingRulesForUser_NameAndActiveFilter_ValidRequest() throws Exception {
            pricingRuleRepository.saveAll(pricingRules);

            mockMvc.perform(get("/api/pricing-rules?name=cat&status=ACTIVE")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2))  // Expecting 2 active cat rules
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))  // Check array length
                    .andExpect(jsonPath("$.content[*].name").value(hasItem("Cat Rule 4")))  // Should contain 'Cat Rule 4'
                    .andExpect(jsonPath("$.content[*].name").value(hasItem("Cat Rule 5")));
        }
    }

    @Nested
    class DeletePricingRuleByIdTests {
        @Test
        void testDeletePricingRuleById_UserOwnsRule_Success() throws Exception {
            // Arrange: Save a pricing rule belonging to the test user
            PricingRule ruleToDelete = DataProvider.generateBasicPricingRule("Test Rule", true, user);
            pricingRuleRepository.save(ruleToDelete);

            // Act: Perform delete request
            mockMvc.perform(delete("/api/pricing-rules/{id}", ruleToDelete.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());  // Expect OK (200) response for successful deletion

            // Assert: Ensure the pricing rule has been deleted from the database
            boolean exists = pricingRuleRepository.existsById(ruleToDelete.getId());
            assertThat(exists).isFalse();  // Rule should no longer exist
        }

        @Test
        void testDeletePricingRuleById_UserDoesNotOwnRule_Forbidden() throws Exception {
            // Arrange: Save a pricing rule belonging to another user
            ApplicationUser anotherUser = new ApplicationUser();
            anotherUser.setUsername("anotheruser");
            anotherUser.setPassword("password");
            anotherUser.setEmail("test@email.com");
            anotherUser.setFirstName("Hans");
            anotherUser.setLastName("Peter");
            anotherUser.setRole(Role.USER);
            anotherUser.setLoginAttempts(0L);
            anotherUser.setUserLocked(false);
            anotherUser = applicationUserRepository.save(anotherUser);
            PricingRule ruleNotOwnedByUser = DataProvider.generateBasicPricingRule("Other User's Rule", true, anotherUser);
            pricingRuleRepository.save(ruleNotOwnedByUser);

            // Act: Try to delete the rule that does not belong to the authenticated user
            mockMvc.perform(delete("/api/pricing-rules/{id}", ruleNotOwnedByUser.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());  // Expect Forbidden (403) response
        }

        @Test
        void testDeletePricingRuleById_RuleNotFound_NotFound() throws Exception {
            // Act: Try to delete a rule with an ID that doesn't exist
            mockMvc.perform(delete("/api/pricing-rules/{id}", 999L)  // Non-existing rule ID
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());  // Expect Not Found (404) response
        }
    }

    @Nested
    class GetPricingRuleByIdTests {
        @Test
        void testGetPricingRuleById_ValidRequest_ProductScope_Success() throws Exception {
            List<Product> products = productRepository.findAll();
            Product p1 = products.get(0);
            Product p2 = products.get(1);
            // Arrange: Save a pricing rule belonging to the test user
            PricingRule rule = DataProvider.generatePricingRule(
                    "Test Rule",
                    true,
                    user,
                    Set.of(p1, p2)
            );
            pricingRuleRepository.save(rule);

            // Act: Perform GET request to fetch the pricing rule by ID
            mockMvc.perform(get("/api/pricing-rules/{id}", rule.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())  // Expecting OK (200) response
                    .andExpect(jsonPath("$.id").value(rule.getId()))  // Check if the correct ID is returned
                    .andExpect(jsonPath("$.name").value(rule.getName()))  // Check if the correct name is returned
                    .andExpect(jsonPath("$.active").value(rule.isActive()))  // Check if the correct status is returned
                    .andExpect(jsonPath("$.position.value").value(2))  // Check if the correct status is returned
                    .andExpect(jsonPath("$.position.unit").value("PERCENTAGE"))  // Check if the correct status is returned
                    .andExpect(jsonPath("$.position.matchType").value("HIGHER"))  // Check if the correct status is returned
                    .andExpect(jsonPath("$.position.reference").value("AVERAGE"))  // Check if the correct status is returned
                    .andExpect(jsonPath("$.scope").value("PRODUCT"))  // Check if the correct status is returned
                    .andExpect(jsonPath("$.appliedProductIds").isNotEmpty())
                    .andExpect(jsonPath("$.appliedCategories").isEmpty())
                    .andExpect(jsonPath("$.appliedProductIds[*]").value(hasItem(p1.getId().intValue())))
                    .andExpect(jsonPath("$.appliedProductIds[*]").value(hasItem(p2.getId().intValue())));
            ;
        }

        @Test
        void testGetPricingRuleById_ValidRequest_CategoryScope_Success() throws Exception {
            List<ProductCategory> products = productCategoryRepository.findAll();
            ProductCategory c1 = products.get(0);
            ProductCategory c2 = products.get(1);
            // Arrange: Save a pricing rule belonging to the test user
            PricingRule rule = DataProvider.generatePricingRuleForCategoryScope(
                    "Test Rule",
                    true,
                    user,
                    Set.of(c1, c2)
            );
            pricingRuleRepository.save(rule);

            // Act: Perform GET request to fetch the pricing rule by ID
            mockMvc.perform(get("/api/pricing-rules/{id}", rule.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())  // Expecting OK (200) response
                    .andExpect(jsonPath("$.id").value(rule.getId()))  // Check if the correct ID is returned
                    .andExpect(jsonPath("$.name").value(rule.getName()))  // Check if the correct name is returned
                    .andExpect(jsonPath("$.active").value(rule.isActive()))  // Check if the correct status is returned
                    .andExpect(jsonPath("$.position.value").value(2))  // Check if the correct status is returned
                    .andExpect(jsonPath("$.position.unit").value("PERCENTAGE"))  // Check if the correct status is returned
                    .andExpect(jsonPath("$.position.matchType").value("HIGHER"))  // Check if the correct status is returned
                    .andExpect(jsonPath("$.position.reference").value("AVERAGE"))  // Check if the correct status is returned
                    .andExpect(jsonPath("$.scope").value("CATEGORY"))  // Check if the correct status is returned
                    .andExpect(jsonPath("$.appliedProductIds").isEmpty())
                    .andExpect(jsonPath("$.appliedCategories").isNotEmpty())
                    .andExpect(jsonPath("$.appliedCategories[*]").value(hasItem(c1.getCategory())))
                    .andExpect(jsonPath("$.appliedCategories[*]").value(hasItem(c2.getCategory())));
            ;
        }


        @Test
        void testGetPricingRuleById_UserDoesNotOwnRule_Forbidden() throws Exception {
            // Arrange: Save a pricing rule owned by another user
            ApplicationUser anotherUser = new ApplicationUser();
            anotherUser.setUsername("anotheruser");
            anotherUser.setPassword("password");
            anotherUser.setEmail("otheruser@example.com");
            anotherUser.setFirstName("Other");
            anotherUser.setLastName("User");
            anotherUser.setRole(Role.USER);
            anotherUser.setLoginAttempts(0L);
            anotherUser.setUserLocked(false);
            anotherUser = applicationUserRepository.save(anotherUser);

            PricingRule rule = DataProvider.generateBasicPricingRule("Other User's Rule", true, anotherUser);
            rule = pricingRuleRepository.save(rule);

            // Act & Assert: Attempt to fetch the rule and expect 403 Forbidden
            mockMvc.perform(get("/api/pricing-rules/{id}", rule.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());  // Expect 403 Forbidden
        }

        @Test
        void testGetPricingRuleById_RuleDoesNotExist_NotFound() throws Exception {
            // Act & Assert: Attempt to fetch a non-existent rule and expect 404 Not Found
            mockMvc.perform(get("/api/pricing-rules/{id}", 999L)  // Non-existent ID
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());  // Expect 404 Not Found
        }

        @Test
        void testGetPricingRuleById_Unauthenticated_Unauthorized() throws Exception {
            // Arrange: Save a pricing rule owned by the authenticated user
            PricingRule rule = DataProvider.generateBasicPricingRule("Unauthenticated Test Rule", true, user);
            rule = pricingRuleRepository.save(rule);

            // Act & Assert: Perform GET request without authorization header
            mockMvc.perform(get("/api/pricing-rules/{id}", rule.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());  // Expect 403 Forbidden
        }
    }

    @Nested
    class CreatePriceRuleTests {

        @Test
        void testCreatePriceRule_forCategoryScope_expectCreated() throws Exception {
            PriceRuleRequestDto defaultRequestDto = DataProvider.getDefaultPriceRuleRequestDto();
            defaultRequestDto.setAppliedToIds(Set.of("C1"));

            MvcResult result = mockMvc.perform(post("/api/pricing-rules")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRequestDto))
                    )
                    .andExpect(status().isCreated())
                    .andReturn();

            PriceRuleDetailsDto resultDto = objectMapper.readValue(result.getResponse().getContentAsString(), PriceRuleDetailsDto.class);

            // Assertions grouped with assertAll
            assertAll("Price Rule Details",
                    () -> assertNotNull(resultDto),
                    () -> assertNotNull(resultDto.getId()),
                    () -> assertEquals("Test Price Rule", resultDto.getName()),
                    () -> {
                        assertNotNull(resultDto.getPosition());
                        assertAll("Position",
                                () -> assertEquals(new BigDecimal(10), resultDto.getPosition().getValue()),
                                () -> assertEquals(PricingRule.Position.Unit.PERCENTAGE, resultDto.getPosition().getUnit()),
                                () -> assertEquals(PricingRule.Position.MatchType.HIGHER, resultDto.getPosition().getMatchType()),
                                () -> assertEquals(PricingRule.Position.Reference.HIGHEST, resultDto.getPosition().getReference())
                        );
                    },
                    () -> assertEquals(PricingRule.Scope.CATEGORY, resultDto.getScope()),
                    () -> assertEquals(Set.of("C1"), resultDto.getAppliedCategories()),
                    () -> assertTrue(resultDto.getAppliedProductIds().isEmpty()),
                    () -> {
                        assertNotNull(resultDto.getMinLimit());
                        assertAll("Min Limit",
                                () -> assertEquals(PriceLimit.LimitType.FIXED_AMOUNT, resultDto.getMinLimit().getLimitType()),
                                () -> assertEquals(new BigDecimal(1), resultDto.getMinLimit().getLimitValue())
                        );
                    },
                    () -> {
                        assertNotNull(resultDto.getMaxLimit());
                        assertAll("Max Limit",
                                () -> assertEquals(PriceLimit.LimitType.FIXED_AMOUNT, resultDto.getMaxLimit().getLimitType()),
                                () -> assertEquals(new BigDecimal(10), resultDto.getMaxLimit().getLimitValue())
                        );
                    },
                    () -> assertTrue(resultDto.isActive())
            );
        }

        @Test
        void testCreatePriceRule_forProductScope_expectCreated() throws Exception {
            // Prepare
            Competitor competitor = ase.cogniprice.integrationTest.storeProductIntegrationTest.DataProvider.generateCompetitor("");
            competitorRepository.save(competitor);
            StoreProduct storeProduct = ase.cogniprice.integrationTest.storeProductIntegrationTest.DataProvider.createStoreProduct(p2Saved, competitor);
            storeProduct.setApplicationUsers(Set.of(user));
            user.setStoreProducts(Set.of(storeProduct));
            storeProductRepository.save(storeProduct);

            PriceRuleRequestDto defaultRequestDto = DataProvider.getDefaultPriceRuleRequestDto();
            defaultRequestDto.setScope(PricingRule.Scope.PRODUCT);
            defaultRequestDto.setAppliedToIds(Set.of(p2Saved.getId().toString()));

            MvcResult result = mockMvc.perform(post("/api/pricing-rules")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRequestDto))
                    )
                    .andExpect(status().isCreated())
                    .andReturn();

            PriceRuleDetailsDto resultDto = objectMapper.readValue(result.getResponse().getContentAsString(), PriceRuleDetailsDto.class);

            // Assertions grouped with assertAll
            assertAll("Price Rule Details",
                    () -> assertNotNull(resultDto),
                    () -> assertNotNull( resultDto.getId()),
                    () -> assertEquals("Test Price Rule", resultDto.getName()),
                    () -> {
                        assertNotNull(resultDto.getPosition());
                        assertAll("Position",
                                () -> assertEquals(new BigDecimal(10), resultDto.getPosition().getValue()),
                                () -> assertEquals(PricingRule.Position.Unit.PERCENTAGE, resultDto.getPosition().getUnit()),
                                () -> assertEquals(PricingRule.Position.MatchType.HIGHER, resultDto.getPosition().getMatchType()),
                                () -> assertEquals(PricingRule.Position.Reference.HIGHEST, resultDto.getPosition().getReference())
                        );
                    },
                    () -> assertEquals(PricingRule.Scope.PRODUCT, resultDto.getScope()),
                    () -> assertEquals(Set.of(p2Saved.getId()), resultDto.getAppliedProductIds()),
                    () -> assertTrue(resultDto.getAppliedCategories().isEmpty()),
                    () -> {
                        assertNotNull(resultDto.getMinLimit());
                        assertAll("Min Limit",
                                () -> assertEquals(PriceLimit.LimitType.FIXED_AMOUNT, resultDto.getMinLimit().getLimitType()),
                                () -> assertEquals(new BigDecimal(1), resultDto.getMinLimit().getLimitValue())
                        );
                    },
                    () -> {
                        assertNotNull(resultDto.getMaxLimit());
                        assertAll("Max Limit",
                                () -> assertEquals(PriceLimit.LimitType.FIXED_AMOUNT, resultDto.getMaxLimit().getLimitType()),
                                () -> assertEquals(new BigDecimal(10), resultDto.getMaxLimit().getLimitValue())
                        );
                    },
                    () -> assertTrue(resultDto.isActive())
            );
        }

        @Test
        void testCreatePriceRule_productNotStoreProductOfUser_expectConflict() throws Exception {
            PriceRuleRequestDto defaultRequestDto = DataProvider.getDefaultPriceRuleRequestDto();
            defaultRequestDto.setScope(PricingRule.Scope.PRODUCT);
            defaultRequestDto.setAppliedToIds(Set.of(p2Saved.getId().toString()));

            MvcResult result = mockMvc.perform(post("/api/pricing-rules")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRequestDto))
                    )
                    .andExpect(status().isConflict())
                    .andReturn();

            String errorMessage = result.getResponse().getContentAsString();

            assertNotNull(errorMessage);
            assertTrue(errorMessage.contains("Product with id " + p2Saved.getId() + " could not be added"));
        }

        @Test
        void testCreatePriceRule_withBlankName_expectUnprocessableEntity() throws Exception {
            PriceRuleRequestDto defaultRequestDto = DataProvider.getDefaultPriceRuleRequestDto();
            defaultRequestDto.setName("");
            defaultRequestDto.setAppliedToIds(Set.of("C1"));

            MvcResult result = mockMvc.perform(post("/api/pricing-rules")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRequestDto))
                    )
                    .andExpect(status().isUnprocessableEntity())
                    .andReturn();

            String errorMessage = result.getResponse().getContentAsString();
            assertNotNull(errorMessage);
            assertTrue(errorMessage.contains("Name"));
        }

        @Test
        void testCreatePriceRule_withNullPosition_expectUnprocessableEntity() throws Exception {
            PriceRuleRequestDto defaultRequestDto = DataProvider.getDefaultPriceRuleRequestDto();
            defaultRequestDto.setPosition(null);
            defaultRequestDto.setAppliedToIds(Set.of("C1"));

            MvcResult result = mockMvc.perform(post("/api/pricing-rules")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRequestDto))
                    )
                    .andExpect(status().isUnprocessableEntity())
                    .andReturn();

            String errorMessage = result.getResponse().getContentAsString();
            assertNotNull(errorMessage);
            assertTrue(errorMessage.contains("Position"));
        }

        @Test
        void testCreatePriceRule_invalidEqualsPosition_expectConflict() throws Exception {
            PriceRuleRequestDto defaultRequestDto = DataProvider.getDefaultPriceRuleRequestDto();
            // Invalid position Unit and Value can not be set for EQUALS Match type
            defaultRequestDto.setPosition(
                    new PositionDto(new BigDecimal(1),
                            PricingRule.Position.Unit.PERCENTAGE,
                            PricingRule.Position.MatchType.EQUALS,
                            PricingRule.Position.Reference.HIGHEST)
            );
            defaultRequestDto.setAppliedToIds(Set.of("C1"));

            MvcResult result = mockMvc.perform(post("/api/pricing-rules")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRequestDto))
                    )
                    .andExpect(status().isConflict())
                    .andReturn();

            String errorMessage = result.getResponse().getContentAsString();
            assertNotNull(errorMessage);
            assertTrue(errorMessage.contains("EQUALS"));
        }

        @Test
        void testCreatePriceRule_invalidCustomPosition_nullValue_expectConflict() throws Exception {
            PriceRuleRequestDto defaultRequestDto = DataProvider.getDefaultPriceRuleRequestDto();
            // Invalid position Unit and Value can not be set for EQUALS Match type
            defaultRequestDto.setPosition(
                    new PositionDto(null,
                            PricingRule.Position.Unit.PERCENTAGE,
                            PricingRule.Position.MatchType.HIGHER,
                            PricingRule.Position.Reference.HIGHEST)
            );
            defaultRequestDto.setAppliedToIds(Set.of("C1"));

            MvcResult result = mockMvc.perform(post("/api/pricing-rules")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRequestDto))
                    )
                    .andExpect(status().isConflict())
                    .andReturn();

            String errorMessage = result.getResponse().getContentAsString();
            assertNotNull(errorMessage);
            assertTrue(errorMessage.contains("value"));
        }

        @Test
        void testCreatePriceRule_invalidCustomPosition_nullUnit_expectConflict() throws Exception {
            PriceRuleRequestDto defaultRequestDto = DataProvider.getDefaultPriceRuleRequestDto();
            // Invalid position Unit and unit can not be set for EQUALS Match type
            defaultRequestDto.setPosition(
                    new PositionDto(new BigDecimal(1),
                            null,
                            PricingRule.Position.MatchType.HIGHER,
                            PricingRule.Position.Reference.HIGHEST)
            );
            defaultRequestDto.setAppliedToIds(Set.of("C1"));

            MvcResult result = mockMvc.perform(post("/api/pricing-rules")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRequestDto))
                    )
                    .andExpect(status().isConflict())
                    .andReturn();

            String errorMessage = result.getResponse().getContentAsString();
            assertNotNull(errorMessage);
            assertTrue(errorMessage.contains("unit"));
        }

        @Test
        void testCreatePriceRule_minLimitHigherThanMaxLimit_expectConflict() throws Exception {
            PriceRuleRequestDto defaultRequestDto = DataProvider.getDefaultPriceRuleRequestDto();
            // Invalid position Unit and unit can not be set for EQUALS Match type
            defaultRequestDto.getMinLimit().setLimitValue(new BigDecimal(10));
            defaultRequestDto.getMaxLimit().setLimitValue(new BigDecimal(1));
            defaultRequestDto.setAppliedToIds(Set.of("C1"));

            MvcResult result = mockMvc.perform(post("/api/pricing-rules")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRequestDto))
                    )
                    .andExpect(status().isConflict())
                    .andReturn();

            String errorMessage = result.getResponse().getContentAsString();
            assertNotNull(errorMessage);
            assertTrue(errorMessage.contains("Min limit must be smaller than max limit."));
        }

        @Test
        void testCreatePriceRule_withEmptyAppliedToIds_expectUnprocessable() throws Exception {
            PriceRuleRequestDto defaultRequestDto = DataProvider.getDefaultPriceRuleRequestDto();

            MvcResult result = mockMvc.perform(post("/api/pricing-rules")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRequestDto))
                    )
                    .andExpect(status().isUnprocessableEntity())
                    .andReturn();

            String errorMessage = result.getResponse().getContentAsString();
            assertNotNull(errorMessage);
            assertTrue(errorMessage.contains("The applied entity IDs must contain at least one item and no more than 100 items."));
        }
    }

    @Nested
    class UpdatePriceRuleTests {

        @Test
        void testUpdatePriceRule_expectCreated() throws Exception {
            PriceRuleRequestDto defaultRequestDto = DataProvider.getDefaultPriceRuleRequestDto();
            defaultRequestDto.setAppliedToIds(Set.of("C1"));

            MvcResult resultCreated = mockMvc.perform(post("/api/pricing-rules")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRequestDto))
                    )
                    .andExpect(status().isCreated())
                    .andReturn();

            PriceRuleDetailsDto initialDto = objectMapper.readValue(resultCreated.getResponse().getContentAsString(), PriceRuleDetailsDto.class);


            PriceRuleRequestDto toUpdateRequest = DataProvider.getDefaultPriceRuleRequestDto();
            toUpdateRequest.setAppliedToIds(Set.of("C1"));
            toUpdateRequest.setName("updated name");
            toUpdateRequest.setMaxLimit(null);
            toUpdateRequest.setMinLimit(null);
            toUpdateRequest.setIsActive(false);

            MvcResult resultUpdated = mockMvc.perform(put("/api/pricing-rules/{id}", initialDto.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(toUpdateRequest))
                    )
                    .andExpect(status().isOk())
                    .andReturn();

            PriceRuleDetailsDto resultDto = objectMapper.readValue(resultUpdated.getResponse().getContentAsString(), PriceRuleDetailsDto.class);


            // Assertions grouped with assertAll
            assertAll("Price Rule Details",
                    () -> assertNotNull(resultDto),
                    () -> assertEquals(initialDto.getId(), resultDto.getId()),
                    () -> assertEquals("updated name", resultDto.getName()),
                    () -> {
                        assertNotNull(resultDto.getPosition());
                        assertAll("Position",
                                () -> assertEquals(new BigDecimal(10), resultDto.getPosition().getValue()),
                                () -> assertEquals(PricingRule.Position.Unit.PERCENTAGE, resultDto.getPosition().getUnit()),
                                () -> assertEquals(PricingRule.Position.MatchType.HIGHER, resultDto.getPosition().getMatchType()),
                                () -> assertEquals(PricingRule.Position.Reference.HIGHEST, resultDto.getPosition().getReference())
                        );
                    },
                    () -> assertEquals(PricingRule.Scope.CATEGORY, resultDto.getScope()),
                    () -> assertEquals(Set.of("C1"), resultDto.getAppliedCategories()),
                    () -> assertTrue(resultDto.getAppliedProductIds().isEmpty()),
                    () -> assertNull(resultDto.getMinLimit()),
                    () -> assertNull(resultDto.getMaxLimit()),
                    () -> assertFalse(resultDto.isActive())
            );
        }

    }
}
