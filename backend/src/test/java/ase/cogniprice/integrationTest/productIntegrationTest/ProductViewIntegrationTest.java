package ase.cogniprice.integrationTest.productIntegrationTest;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductViewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private CompetitorRepository competitorRepository;

    @Autowired
    private StoreProductRepository storeProductRepository;

    @Autowired
    private JwtTestUtils jwtTestUtils;

    private ApplicationUser applicationUser;
    private Product product;
    @BeforeEach
    void setUp() {
        applicationUser = DataProvider.generateApplicationUser("testUser");

        ProductCategory productCategory = DataProvider.generateProductCategory();
        productCategoryRepository.save(productCategory);
        product = DataProvider.createProduct(productCategory, "1");
        productRepository.save(product);
        Competitor competitor = DataProvider.generateCompetitor("");
        competitorRepository.save(competitor);
        StoreProduct storeProduct = DataProvider.createStoreProduct(product, competitor);
        storeProductRepository.save(storeProduct);

        applicationUser.addStoreProduct(storeProduct, storeProductRepository);
        applicationUserRepository.save(applicationUser);
    }

    @Test
    void testViewProductsForUser_ReturnsEmptyList() throws Exception {
        String token = jwtTestUtils.generateToken("testUserEmpty", List.of("ROLE_USER"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/storeProducts/view")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").isEmpty());  // Check if "content" is empty (for paginated response)
    }

    @Test
    void testViewProductsForUser_WithOneProduct_ReturnsListWithOneElement() throws Exception {
        String token = jwtTestUtils.generateToken("testUser", List.of("ROLE_USER"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/storeProducts/view")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content", hasSize(1)))  // Check if the "content" array has 1 element
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].productId").exists())  // Check if the first product has an id field
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].productName").value("Test Product"))  // Check the product name
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].category").value("sampleCategory"));  // Check the category
    }
}
