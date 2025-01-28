package ase.cogniprice.integrationTest.productIntegrationTest;

import ase.cogniprice.controller.dto.product.ProductCreateDto;
import ase.cogniprice.controller.dto.product.ProductUpdateDto;
import ase.cogniprice.controller.mapper.ProductMapper;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ProductMapper productMapper;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private ProductService productService;

    private Product existingProduct;

    @BeforeEach
    void setUp() {
        // Prepare data for testing the update endpoint
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
    void testCreateProduct() throws Exception {
        // Prepare request data
        ProductCreateDto createDto = new ProductCreateDto();
        createDto.setName("New Product");
        createDto.setGtin("1234567890124");
        createDto.setProductCategoryId("sampleCategory");

        // Create a MockMultipartFile for the DTO (converted to JSON)
        MockMultipartFile jsonPart = new MockMultipartFile(
            "productCreateDto",                           // The name of the part in the request
            "",                                           // Empty file name
            "application/json",                           // Content type for JSON
            objectMapper.writeValueAsBytes(createDto)     // The content of the JSON (converted DTO)
        );

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("{}", HttpStatus.CREATED));

        // Perform the POST request to create the product
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/products/create")
                .file(jsonPart))       // Add the product DTO as JSON                .contentType(MediaType.MULTIPART_FORM_DATA)) // Set the content type for file upload
            .andExpect(status().isCreated());  // Check if the status is 201 Created
            //.andExpect(content().string("Product created successfully."));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateProductWithImage() throws Exception {
        // Prepare request data
        byte[] pngContent = Files.readAllBytes(Paths.get("src/test/resources/not-found.png"));

        // Create a MockMultipartFile from the real PNG
        MockMultipartFile image = new MockMultipartFile(
            "image",               // The field name for the file
            "not-found.png",       // The original file name
            "image/png",           // The content type
            pngContent             // The file content as byte array
        );

        ProductCreateDto createDto = new ProductCreateDto();
        createDto.setName("New Product");
        createDto.setGtin("1234567890124");
        createDto.setProductCategoryId("sampleCategory");

        // Create a MockMultipartFile for the DTO (converted to JSON)
        MockMultipartFile jsonPart = new MockMultipartFile(
            "productCreateDto",                           // The name of the part in the request
            "",                                           // Empty file name
            "application/json",                           // Content type for JSON
            objectMapper.writeValueAsBytes(createDto)     // The content of the JSON (converted DTO)
        );

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("{}", HttpStatus.CREATED));

        // Perform the POST request with multipart form data
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/products/create")
                .file(image)                          // Add the empty image file
                .file(jsonPart)                            // Add the product DTO as JSON
                .contentType(MediaType.MULTIPART_FORM_DATA)) // Set the content type for file upload
            .andExpect(status().isCreated());  // Check if the status is 201 Created
            //.andExpect(content().string("Product created successfully."));  // Verify success message
        Product savedProduct = productRepository.findProductByGtin(createDto.getGtin());
        assertEquals("not-found.png", savedProduct.getProductImage().getFileName());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateProductWithExistingGtin() throws Exception {
        // Prepare request data with an existing GTIN
        ProductCreateDto createDto = new ProductCreateDto();
        createDto.setName("New Product");
        createDto.setGtin(existingProduct.getGtin());  // Using the existing GTIN
        createDto.setProductCategoryId("sampleCategory");

        // Create a MockMultipartFile for the DTO (converted to JSON)
        MockMultipartFile jsonPart = new MockMultipartFile(
            "productCreateDto",                           // The name of the part in the request
            "",                                           // Empty file name
            "application/json",                           // Content type for JSON
            objectMapper.writeValueAsBytes(createDto)     // The content of the JSON (converted DTO)
        );

        // Perform the POST request
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/products/create")
                .file(jsonPart))                            // Add the product DTO as JSON
            .andExpect(status().isOk());  // Check if the status is 200 ok
            //.andExpect(content().string("Product already exists with GTIN: " + existingProduct.getGtin()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateProduct() throws Exception {
        // Prepare request data for updating the product
        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setId(existingProduct.getId());
        updateDto.setName("Updated Product Name");
        updateDto.setGtin("1234567890125");

        // Create a MockMultipartFile for the DTO (converted to JSON)
        MockMultipartFile jsonPart = new MockMultipartFile(
            "productUpdateDto",                           // The name of the part in the request
            "",                                           // Empty file name
            "application/json",                           // Content type for JSON
            objectMapper.writeValueAsBytes(updateDto)     // The content of the JSON (converted DTO)
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/products/{id}", existingProduct.getId())
                .file(jsonPart)
                .with(request -> {
                    request.setMethod("PUT");  // Explicitly set the method to PUT
                    return request;
                }))
            .andExpect(status().isOk())  // Expecting a 200 OK status
            .andExpect(jsonPath("$.name").value("Updated Product Name")) // Check if the name is updated
            .andExpect(jsonPath("$.gtin").value("1234567890125")); // Check if the GTIN is updated
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateProductWithInvalidId() throws Exception {
        // Prepare request data with an invalid ID
        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setId(existingProduct.getId() + 1); // Invalid ID
        updateDto.setName("Updated Product Name");
        updateDto.setGtin("1234567890125");

        // Create a MockMultipartFile for the DTO (converted to JSON)
        MockMultipartFile jsonPart = new MockMultipartFile(
            "productUpdateDto",                           // The name of the part in the request
            "",                                           // Empty file name
            "application/json",                           // Content type for JSON
            objectMapper.writeValueAsBytes(updateDto)     // The content of the JSON (converted DTO)
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/products/{id}", Long.MAX_VALUE)
            .file(jsonPart)
            .with(request -> {
                request.setMethod("PUT");  // Explicitly set the method to PUT
                return request;
            })).andExpect(status().isBadRequest()); // Expecting a 400 Bad Request due to ID mismatch
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateProductNotFound() throws Exception {
        // Prepare request data for a non-existent product ID
        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setId(Long.MAX_VALUE); // Invalid ID
        updateDto.setName("Non-existent Product");
        updateDto.setGtin("9876543210987");

        // Create a MockMultipartFile for the DTO (converted to JSON)
        MockMultipartFile jsonPart = new MockMultipartFile(
            "productUpdateDto",                           // The name of the part in the request
            "",                                           // Empty file name
            "application/json",                           // Content type for JSON
            objectMapper.writeValueAsBytes(updateDto)     // The content of the JSON (converted DTO)
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/products/{id}", Long.MAX_VALUE)
                .file(jsonPart)
                .with(request -> {
                    request.setMethod("PUT");  // Explicitly set the method to PUT
                    return request;
                }))
            .andExpect(status().isNotFound()); // Expecting a 404 Not Found for the non-existent product
    }

}
