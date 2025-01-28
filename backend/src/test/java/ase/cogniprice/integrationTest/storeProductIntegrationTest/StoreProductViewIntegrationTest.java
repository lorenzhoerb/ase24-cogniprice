package ase.cogniprice.integrationTest.storeProductIntegrationTest;

import ase.cogniprice.config.JwtTestUtils;
import ase.cogniprice.controller.dto.product.ProductDetailsWithPricesDto;
import ase.cogniprice.controller.dto.store.product.StoreProductWithPriceDto;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.entity.ProductPrice;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.CompetitorRepository;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductPriceRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.StoreProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Ensures database rollback after each test
class StoreProductViewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTestUtils jwtTestUtils;

    @Autowired
    private StoreProductRepository storeProductRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private CompetitorRepository competitorRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    ProductPriceRepository productPriceRepository;

    private final String path = "/api/storeProducts";

    @Test
    void testGetStoreProductsWithPriceAndImage_success() throws Exception {
        // Arrange
        String username = "testuser";
        List<String> roles = List.of("ROLE_USER");
        String jwtToken = jwtTestUtils.generateToken(username, roles);

        // Populate test data in the database
        populateTestData(username);

        // Act & Assert
        mockMvc.perform(get(path + "/view")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].productId").exists())
            .andExpect(jsonPath("$.content[0].highestPrice").exists());
    }

    @Test
    void testGetStoreProductsWithPriceAndImage_unauthorized() throws Exception {
        // Arrange
        String username = "testuser";
        List<String> roles = List.of(); // User does not have ROLE_USER
        String jwtToken = jwtTestUtils.generateToken(username, roles);

        // Act & Assert
        mockMvc.perform(get(path + "/view")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testRepositoryQuery() {
        // Arrange
        String username = "testuser";

        // Populate test data in the database
        populateTestData(username);

        // Act
        List<ProductDetailsWithPricesDto> result = storeProductRepository.findStoreProductsWithImageByUser(username, "", "", PageRequest.of(0, 10)).getContent();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getProductId()).isNotNull();
    }

    private void populateTestData(String username) {
        // Generate test data using the DataProvider
        ApplicationUser user = DataProvider.generateApplicationUser(username);

        ProductCategory category = DataProvider.generateProductCategory();
        productCategoryRepository.save(category);

        Product product = DataProvider.createProduct(category);
        Competitor competitor = DataProvider.generateCompetitor("test");
        productRepository.save(product);
        competitorRepository.save(competitor);

        StoreProduct storeProduct = DataProvider.createStoreProduct(product, competitor);
        storeProduct.setApplicationUsers(Collections.singleton(user));
        user.setStoreProducts(Collections.singleton(storeProduct));

        applicationUserRepository.save(user);
        storeProductRepository.save(storeProduct);

        ProductPrice productPrice = DataProvider.generateProductPrice(storeProduct);
        productPriceRepository.save(productPrice);

    }
}
