package ase.cogniprice.integrationTest.productIntegrationTest;

import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.repository.CompetitorRepository;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.StoreProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private StoreProductRepository storeProductRepository;

    @Autowired
    private CompetitorRepository competitorRepository;

    private Long competitorId1;
    private Long competitorId2;


    @BeforeEach
    void setUp() {

        Competitor competitor1 = competitorRepository.save(DataProvider.generateCompetitor("Competitor1"));
        Competitor competitor2 = competitorRepository.save(DataProvider.generateCompetitor("Competitor2"));

        competitorId1 = competitor1.getId();
        competitorId2 = competitor2.getId();

        ProductCategory category1 = DataProvider.generateProductCategory("test1");
        ProductCategory category2 = DataProvider.generateProductCategory("test2");

        saveProductWithCompetitor(category1, "0000000000000", competitor1);
        saveProductWithCompetitor(category1, "0000003000000", competitor2);
        saveProductWithCompetitor(category2, "1230000000000", competitor1);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSearchProducts_ReturnsEmptyList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/search")
                .param("query", "UNFINDABLE")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSearchProducts_WithQueryOnName_ReturnsListOfAll() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/search")
                .param("query", "test")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content", hasSize(3)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSearchProducts_WithQueryOnGtin_ReturnsListOfTwo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/search")
                .param("query", "3")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSearchProducts_WithCompetitorId_ReturnsProductsForThatCompetitor() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/search")
                        .param("competitorId", String.valueOf(competitorId1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSearchProducts_WithCompetitorIdAndQuery_ReturnsFilteredProducts() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/search")
                        .param("competitorId", String.valueOf(competitorId1))
                        .param("query", "123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSearchProducts_WithCompetitorIdAndInvalidQuery_ReturnsEmpty() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/search")
                        .param("competitorId", String.valueOf(competitorId1))
                        .param("query", "UNFINDABLE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content", hasSize(0)));
    }


    private void saveProductWithCompetitor(ProductCategory cat, String gtin, Competitor competitor) {
        productCategoryRepository.save(cat);
        var product = productRepository.save(DataProvider.createProduct(cat, gtin));

        StoreProduct storeProduct = DataProvider.createStoreProduct(product, competitor);
        storeProductRepository.save(storeProduct);
    }

}
