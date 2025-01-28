package ase.cogniprice.integrationTest.storeProductIntegrationTest;

import ase.cogniprice.config.JwtTestUtils;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.CompetitorRepository;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.StoreProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StoreProductRemoveIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private StoreProductRepository storeProductRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompetitorRepository competitorRepository;

    @Autowired
    private JwtTestUtils jwtTestUtils;
    @Autowired
    private ObjectMapper objectMapper;

    private StoreProduct storeProduct;
    private StoreProduct storeProduct1;
    private String token;

    @BeforeEach
    void setUp() {
        // Create a new user
        ApplicationUser user = DataProvider.generateApplicationUser("testUser");
        token = jwtTestUtils.generateToken("testUser", List.of("ROLE_USER"));

        ProductCategory productCategory = DataProvider.generateProductCategory();
        productCategoryRepository.save(productCategory);

        Product product = DataProvider.createProduct(productCategory);
        productRepository.save(product);

        Competitor competitor = DataProvider.generateCompetitor("");
        competitorRepository.save(competitor);
        Competitor competitor1 = DataProvider.generateCompetitor("1");
        competitorRepository.save(competitor1);


        // Create a new StoreProduct and associate it with the user
        storeProduct = DataProvider.createStoreProduct(product, competitor);
        storeProductRepository.save(storeProduct);
        storeProduct1 = DataProvider.createStoreProduct(product, competitor1);
        storeProductRepository.save(storeProduct1);

        user.getStoreProducts().add(storeProduct);
        applicationUserRepository.save(user);

    }

    @Test
    void testRemoveStoreProduct_Success() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("productId", storeProduct.getProduct().getId());
        requestBody.put("competitorId", storeProduct.getCompetitor().getId());
        // Act
        mockMvc.perform(delete("/api/storeProducts/remove", storeProduct.getId().getProductId(), storeProduct.getId().getCompetitorId())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isNoContent());

        // Verify that the store product was removed
        ApplicationUser user = applicationUserRepository.findApplicationUserByUsername("testUser");

        assertEquals(0, user.getStoreProducts().size()); // The store product should be removed
    }

    @Test
    void testRemoveStoreProduct_NotFound() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("productId", storeProduct.getProduct().getId());
        requestBody.put("competitorId", Long.MAX_VALUE);
        // Try to remove a StoreProduct that doesn't exist
        mockMvc.perform(delete("/api/storeProducts/{productId}/{competitorId}", 999L, 999L)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isNotFound());
    }

    @Test
    void testRemoveStoreProduct_NotInUserWatchlist() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("productId", storeProduct1.getProduct().getId());
        requestBody.put("competitorId", storeProduct1.getCompetitor().getId());
        // Try to remove a StoreProduct not in the user's watchlist
        mockMvc.perform(delete("/api/storeProducts/remove", storeProduct1.getId().getProductId(), storeProduct1.getId().getCompetitorId())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isNotFound());
    }
}
