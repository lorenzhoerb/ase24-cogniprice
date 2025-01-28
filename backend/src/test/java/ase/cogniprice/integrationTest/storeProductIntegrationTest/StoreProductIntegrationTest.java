package ase.cogniprice.integrationTest.storeProductIntegrationTest;

import ase.cogniprice.config.JwtTestUtils;
import ase.cogniprice.controller.dto.store.product.StoreProductCreateDto;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.CompetitorRepository;
import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.service.StoreProductService;
import ase.cogniprice.type.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class StoreProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @LocalServerPort
    private int port;

    @Autowired
    private StoreProductService storeProductService;

    @Autowired
    private StoreProductRepository storeProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompetitorRepository competitorRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;
    @Autowired
    private JwtTestUtils jwtTestUtils;
    private String token;


    private StoreProductCreateDto storeProductCreateDto;
    private StoreProductCreateDto storeProductCreateDto2;
    private Product product;
    private Competitor competitor;
    private Competitor competitor2;

    private String url;

    StoreProduct.StoreProductId storeProductId;

    @BeforeEach
    void setUp() {
        // Set up the data to be used in the test
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategory("sampleCategory");
        productCategoryRepository.save(productCategory);

        product = new Product();
        product.setName("Test Product");
        product.setGtin("1234567890123");
        product.setProductCategory(productCategory);
        productRepository.save(product);

        competitor = new Competitor();
        competitor.setName("Test Competitor");
        competitor.setUrl("https://competitor.com");
        competitorRepository.save(competitor);

        competitor2 = new Competitor();
        competitor2.setName("Test Competitor2");
        competitor2.setUrl("https://competitor2.com");
        competitorRepository.save(competitor2);

        storeProductCreateDto = new StoreProductCreateDto();
        storeProductCreateDto.setProductId(product.getId());  // Use saved product ID
        storeProductCreateDto.setCompetitorId(competitor.getId()); // Use saved competitor ID
        storeProductCreateDto.setProductUrl("https://competitor.com/product");
        url = "https://competitor.com/product";

        storeProductId = new StoreProduct.StoreProductId(product.getId(), competitor.getId());

        storeProductCreateDto2 = new StoreProductCreateDto();
        storeProductCreateDto2.setProductId(product.getId());  // Use saved product ID
        storeProductCreateDto2.setCompetitorId(competitor2.getId()); // Use saved competitor ID
        storeProductCreateDto2.setProductUrl("https://competitor2.com/product");

        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setUsername("testUser");
        existingUser.setEmail("existinguser@example.com");
        existingUser.setPassword("password");
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setLoginAttempts(0L);
        existingUser.setUserLocked(false);
        existingUser.setRole(Role.USER);
        existingUser.setStoreProducts(new HashSet<>());
        applicationUserRepository.save(existingUser);
        token = jwtTestUtils.generateToken("testUser", List.of("ROLE_USER"));


        mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(requestTo(url))
            .andExpect(method(HttpMethod.HEAD))
            .andRespond(withSuccess());
        mockServer.expect(requestTo("https://competitor2.com/product"))
            .andExpect(method(HttpMethod.HEAD))
            .andRespond(withSuccess());
    }

    @Test
    void testCreateStoreProduct_whenValidRequest_thenReturnsCreatedStatus() throws Exception {
        // Given: Valid StoreProductCreateDto

        // When: The POST request is made to create a new store product
        mockMvc.perform(post("/api/storeProducts/create")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(storeProductCreateDto)))
            .andExpect(status().isCreated()); // Then: It should return 201 Created

        assertTrue(storeProductRepository.existsById(storeProductId));
        assertEquals(1, applicationUserRepository.findApplicationUserByUsername("testUser").getStoreProducts().size());
    }

    @Test
    void testCreateStoreProduct_whenTwoValidRequests_thenReturnsCreatedStatusAndTwoObjectsInSet() throws Exception {
        // Given: Valid StoreProductCreateDto
        // When: The POST request is made to create a new store product
        mockMvc.perform(post("/api/storeProducts/create")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(storeProductCreateDto)))
            .andExpect(status().isCreated()); // Then: It should return 201 Created


        mockMvc.perform(post("/api/storeProducts/create")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(storeProductCreateDto2)))
            .andExpect(status().isCreated()); // Then: It should return 201 Created

        assertTrue(storeProductRepository.existsById(storeProductId));
        assertEquals(2, applicationUserRepository.findApplicationUserByUsername("testUser").getStoreProducts().size());
    }

    @Test
    void testCreateStoreProduct_whenProductAlreadyExists_thenReturnsConflictStatus() throws Exception {
        // Given: Product already exists
        storeProductService.createStoreProduct(storeProductCreateDto, "testUser"); // Persist the storeProduct before to ensure no conflict

        // When: The POST request is made with the same data
        mockMvc.perform(post("/api/storeProducts/create")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(storeProductCreateDto)))
            .andExpect(status().isConflict()); // Then: It should return 409 Conflict
    }

    @Test
    void testCreateStoreProduct_whenUrlDoesNotMatch_thenReturnsConflictStatus() throws Exception {
        // Given: Product already exists
        competitor.setUrl("https://will-not-match.com");
        competitorRepository.save(competitor);

        // When: The POST request is made with the same data
        mockMvc.perform(post("/api/storeProducts/create")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(storeProductCreateDto)))
            .andExpect(status().isConflict()); // Then: It should return 409 Conflict
    }

    void testCreateStoreProduct_whenUrlIsNotReachable_thenReturnsBadRequestStatus() throws Exception {
        // Given: Url not reachable
        mockServer.expect(requestTo(url))
            .andExpect(method(HttpMethod.HEAD))
            .andRespond(withBadRequest());

        // When: The POST request is made with the same data
        mockMvc.perform(post("/api/storeProducts/create")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(storeProductCreateDto)))
            .andExpect(status().isConflict()); // Then: It should return 409 Conflict
    }

    @Test
    void testCreateStoreProduct_whenInvalidRequest_thenReturnsUnprocessableEntityStatus() throws Exception {
        // Given: An invalid DTO (e.g., missing required fields)
        StoreProductCreateDto invalidDto = new StoreProductCreateDto(); // Not setting any fields

        // When: The POST request is made with invalid data
        mockMvc.perform(post("/api/storeProducts/create")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(invalidDto)))
            .andExpect(status().isUnprocessableEntity()); // Then: It should return 400 Bad Request
    }

    @Test
    void testCreateStoreProduct_whenProductNotFound_thenReturnsNotFoundStatus() throws Exception {
        // Given: A product ID that does not exist
        storeProductCreateDto.setProductId(9999L); // Assuming this product does not exist

        // When: The POST request is made with an invalid product
        mockMvc.perform(post("/api/storeProducts/create")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(storeProductCreateDto)))
            .andExpect(status().isNotFound()); // Then: It should return 404 Not Found
    }

    @Test
    void testCreateStoreProduct_whenCompetitorNotFound_thenReturnsNotFoundStatus() throws Exception {
        // Given: A competitor ID that does not exist
        storeProductCreateDto.setCompetitorId(9999L); // Assuming this competitor does not exist

        // When: The POST request is made with an invalid competitor
        mockMvc.perform(post("/api/storeProducts/create")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(storeProductCreateDto)))
            .andExpect(status().isNotFound()); // Then: It should return 404 Not Found
    }
}
