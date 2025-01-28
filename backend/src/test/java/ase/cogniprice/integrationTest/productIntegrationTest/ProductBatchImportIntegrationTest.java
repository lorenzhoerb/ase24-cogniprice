package ase.cogniprice.integrationTest.productIntegrationTest;


import ase.cogniprice.controller.dto.product.ProductBatchImporterDto;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductBatchImportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    private Product existingProduct;

    @BeforeEach
    void setUp() {
        existingProduct = new Product();
        existingProduct.setName("Sample Product");
        existingProduct.setGtin("1234567890123");
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategory("sampleCategory");
        productCategoryRepository.save(productCategory);
        existingProduct.setProductCategory(productCategory);
        productRepository.save(existingProduct);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testBatchCreateProducts_Success() throws Exception {
        // Prepare valid batch data
        ProductBatchImporterDto product1 = new ProductBatchImporterDto();
        product1.setName("Product 1");
        product1.setGtin("123456789012");
        product1.setProductCategory("sampleCategory");

        ProductBatchImporterDto product2 = new ProductBatchImporterDto();
        product2.setName("Product 2");
        product2.setGtin("1234567890124");
        product2.setProductCategory("sampleCategory");

        List<ProductBatchImporterDto> batch = List.of(product1, product2);

        // Perform the POST request
        mockMvc.perform(post("/api/products/batch-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch)))
                .andExpect(status().isOk());

        Product savedProduct1 = productRepository.findProductByGtin("123456789012");
        Product savedProduct2 = productRepository.findProductByGtin("1234567890124");
        assertEquals("Product 1", savedProduct1.getName());
        assertEquals("Product 2", savedProduct2.getName());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testBatchCreateProducts_InvalidData() throws Exception {
        // Prepare invalid batch data
        ProductBatchImporterDto invalidProduct = new ProductBatchImporterDto();
        invalidProduct.setName("Invalid Product");
        invalidProduct.setGtin("123"); // Invalid GTIN
        invalidProduct.setProductCategory("sampleCategory");

        List<ProductBatchImporterDto> batch = List.of(invalidProduct);

        // Perform the POST request
        mockMvc.perform(post("/api/products/batch-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch)))
                .andExpect(status().isUnprocessableEntity()); // Validation error
    }
}
