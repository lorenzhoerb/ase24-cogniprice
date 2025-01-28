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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import scala.App;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StoreProductRemoveAllIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private StoreProductRepository storeProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private CompetitorRepository competitorRepository;

    @Autowired
    private JwtTestUtils jwtTestUtils;

    private ApplicationUser applicationUser;
    private StoreProduct storeProduct;

    private String token;
    private Product product;

    @BeforeEach
    void setUp() {
        // Set up test data
        applicationUser = DataProvider.generateApplicationUser("testUser");
        applicationUserRepository.save(applicationUser);

        token = jwtTestUtils.generateToken("testUser", List.of("ROLE_USER"));


        ProductCategory productCategory = DataProvider.generateProductCategory();
        this.productCategoryRepository.save(productCategory);

        product = DataProvider.createProduct(productCategory);
        this.productRepository.save(product);

        Competitor competitor = DataProvider.generateCompetitor("");
        this.competitorRepository.save(competitor);

        storeProduct = DataProvider.createStoreProduct(product,competitor);
        storeProductRepository.save(storeProduct);

        applicationUser.addStoreProduct(storeProduct, storeProductRepository);

        applicationUserRepository.save(applicationUser);
    }

    @Test
    void testRemoveAllStoreProduct() throws Exception {
        // Assert product is added
        assertTrue(applicationUser.getStoreProducts().contains(storeProduct));

        // Send DELETE request to remove the product
        mockMvc.perform(delete("/api/storeProducts/removeAll")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(String.valueOf(product.getId())))
            .andExpect(status().isNoContent());

        applicationUserRepository.flush(); // This will synchronize the persistence context

        // Verify that the product is removed from the user's watchlist
        ApplicationUser fetchedApplicationUser = applicationUserRepository.findApplicationUserByUsername("testUser");
        assertFalse(fetchedApplicationUser.getStoreProducts().contains(storeProduct));
    }

}