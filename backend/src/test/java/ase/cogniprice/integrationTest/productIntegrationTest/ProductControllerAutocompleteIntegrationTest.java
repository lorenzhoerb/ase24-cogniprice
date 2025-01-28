package ase.cogniprice.integrationTest.productIntegrationTest;

import ase.cogniprice.config.JwtTestUtils;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerAutocompleteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @BeforeEach
    public void setUp() {
        // Populate the database with test data
        ProductCategory category = new ProductCategory();
        category.setCategory("Books");
        productCategoryRepository.save(category);

        Product product = new Product();
        product.setName("test1");
        product.setGtin("1234567890123");
        product.setProductCategory(category);
        productRepository.save(product);
        product = new Product();
        product.setName("test2");
        product.setGtin("1111111111111");
        product.setProductCategory(category);
        productRepository.save(product);
        product = new Product();
        product.setName("neverQueried");
        product.setGtin("1111111112111");
        product.setProductCategory(category);
        productRepository.save(product);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSearchProductsWithNoResults() throws Exception {
        mockMvc.perform(get("/api/products/autocomplete")
                .param("q", "NonExistentProduct"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSearchProductsWithResults() throws Exception {
        mockMvc.perform(get("/api/products/autocomplete")
                .param("q", "test"))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[*].name", hasItem("test1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[*].name", hasItem("test2")));
    }




}
