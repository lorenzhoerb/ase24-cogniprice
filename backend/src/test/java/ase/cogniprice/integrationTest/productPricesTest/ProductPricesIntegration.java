package ase.cogniprice.integrationTest.productPricesTest;

import ase.cogniprice.config.JwtTestUtils;
import ase.cogniprice.entity.*;
import ase.cogniprice.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductPricesIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompetitorRepository competitorRepository;

    @Autowired
    private StoreProductRepository storeProductRepository;

    @Autowired
    private ProductPriceRepository productPriceRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private JwtTestUtils jwtTestUtils;
    private String token;

    private Product product;
    private Competitor competitorA;
    private Competitor competitorB;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        // Generate data using DataProvider
        ProductCategory productCategory = DataProvider.generateProductCategory();
        productCategoryRepository.save(productCategory);

        // Create one product
        product = DataProvider.createProduct(productCategory);
        productRepository.save(product);

        // Create two competitors
        competitorA = DataProvider.generateCompetitor("competitor-a");
        competitorRepository.save(competitorA);

        competitorB = DataProvider.generateCompetitor("competitor-b");
        competitorRepository.save(competitorB);

        // Create store products for both competitors
        StoreProduct storeProductA = DataProvider.createStoreProduct(product, competitorA);
        storeProductRepository.save(storeProductA);

        StoreProduct storeProductB = DataProvider.createStoreProduct(product, competitorB);
        storeProductRepository.save(storeProductB);

        // Create multiple product prices
        ProductPrice priceA1 = new ProductPrice();
        priceA1.setId(new ProductPrice.ProductPriceId(storeProductA.getId(), LocalDateTime.of(2024, 12, 1, 14, 30)));
        priceA1.setPrice(Money.of(99.99, "USD"));
        priceA1.setStoreProduct(storeProductA);
        productPriceRepository.save(priceA1);

        ProductPrice priceA2 = new ProductPrice();
        priceA2.setId(new ProductPrice.ProductPriceId(storeProductA.getId(), LocalDateTime.of(2024, 12, 2, 14, 30)));
        priceA2.setPrice(Money.of(95.50, "USD"));
        priceA2.setStoreProduct(storeProductA);
        productPriceRepository.save(priceA2);

        ProductPrice priceB1 = new ProductPrice();
        priceB1.setId(new ProductPrice.ProductPriceId(storeProductB.getId(), LocalDateTime.of(2024, 12, 1, 15, 30)));
        priceB1.setPrice(Money.of(101.75, "USD"));
        priceB1.setStoreProduct(storeProductB);
        productPriceRepository.save(priceB1);

        // Create one user
        ApplicationUser existingUser = DataProvider.generateApplicationUser("testUser");
        applicationUserRepository.save(existingUser);

        token = jwtTestUtils.generateToken("testUser", List.of("ROLE_USER"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetPriceTrends_expectOk() throws Exception {
        mockMvc.perform(get("/api/productPrices/{productId}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(99.99))
                .andExpect(jsonPath("$[0].competitorName").value("Test Competitor"))
                .andExpect(jsonPath("$[0].currency").value("USD"))
                .andExpect(jsonPath("$[0].priceTime").value("2024-12-01T14:30:00"))
                .andExpect(jsonPath("$[1].price").value(101.75))
                .andExpect(jsonPath("$[1].competitorName").value("Test Competitor"))
                .andExpect(jsonPath("$[1].currency").value("USD"))
                .andExpect(jsonPath("$[1].priceTime").value("2024-12-01T15:30:00"))
                .andExpect(jsonPath("$[2].price").value(95.50))
                .andExpect(jsonPath("$[2].competitorName").value("Test Competitor"))
                .andExpect(jsonPath("$[2].currency").value("USD"))
                .andExpect(jsonPath("$[2].priceTime").value("2024-12-02T14:30:00"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetPriceTrends_noPrices_expectEmptyResponse() throws Exception {
        productPriceRepository.deleteAll();

        mockMvc.perform(get("/api/productPrices/{productId}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty()); // Expecting an empty array
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetPriceTrends_invalidProductId_expectNotFound() throws Exception {
        long invalidProductId = 9999L;

        mockMvc.perform(get("/api/productPrices/{productId}", invalidProductId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
