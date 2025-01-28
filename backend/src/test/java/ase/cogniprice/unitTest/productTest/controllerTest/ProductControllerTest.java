package ase.cogniprice.unitTest.productTest.controllerTest;

import ase.cogniprice.controller.dto.product.ProductBatchImporterDto;
import ase.cogniprice.controller.dto.product.ProductCreateDto;
import ase.cogniprice.entity.Product;
import ase.cogniprice.exception.GtinLookupException;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductCreateDto validProductDto;
    private ProductCreateDto invalidProductDto;
    private Product existingProduct;
    private Product newProduct;

    private List<ProductBatchImporterDto> validBatchDtos;
    private List<ProductBatchImporterDto> invalidBatchDtos;

    @BeforeEach
    public void setUp() {
        String username = "test_user";

        // Set up valid DTO
        validProductDto = new ProductCreateDto();
        validProductDto.setName("Sample Product");
        validProductDto.setGtin("4044155274035");
        validProductDto.setProductCategoryId("Books");

        // Set up invalid DTO (invalid GTIN)
        invalidProductDto = new ProductCreateDto();
        invalidProductDto.setName("Sample Product");
        invalidProductDto.setGtin("123"); // Too short
        invalidProductDto.setProductCategoryId("Books");

        // Set up products
        existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setGtin("4044155274035");

        newProduct = new Product();
        newProduct.setId(null);
        newProduct.setGtin("4044155274035");


        // Set up valid batch DTO
        ProductBatchImporterDto validProduct = new ProductBatchImporterDto();
        validProduct.setName("Valid Product");
        validProduct.setGtin("4044155274035");
        validProduct.setProductCategory("Books");

        validBatchDtos = List.of(validProduct);

        // Set up invalid batch DTO
        ProductBatchImporterDto invalidProduct = new ProductBatchImporterDto();
        invalidProduct.setName("Invalid Product");
        invalidProduct.setGtin("123"); // Too short GTIN
        invalidProduct.setProductCategory("Books");

        invalidBatchDtos = List.of(invalidProduct);
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProduct_ShouldReturnOK_WhenProductAlreadyExists() throws Exception {
        when(productService.createProduct(eq(validProductDto), any())).thenReturn(existingProduct);
        when(productService.getProductByGtin(eq(validProductDto.getGtin()))).thenReturn(existingProduct);
        // Create a MockMultipartFile for the DTO (converted to JSON)
        MockMultipartFile jsonPart = new MockMultipartFile(
            "productCreateDto",                           // The name of the part in the request
            "",                                           // Empty file name
            "application/json",                           // Content type for JSON
            objectMapper.writeValueAsBytes(validProductDto)     // The content of the JSON (converted DTO)
        );

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/products/create")
                .file(jsonPart))                            // Add the product DTO as JSON
            .andExpect(status().isOk())
            //.andExpect(content().string("Product already exists with GTIN: 4044155274035"))
                .andExpect(content().string("1"))
            .andReturn();

        //assertEquals("Product already exists with GTIN: 4044155274035", result.getResponse().getContentAsString());
        assertEquals("1", result.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProduct_ShouldReturnCreated_WhenProductIsNew() throws Exception {
        when(productService.createProduct(eq(validProductDto), any())).thenReturn(existingProduct);
        when(productService.getProductByGtin(eq(validProductDto.getGtin()))).thenReturn(null);

        MockMultipartFile jsonPart = new MockMultipartFile(
            "productCreateDto",                           // The name of the part in the request
            "",                                           // Empty file name
            "application/json",                           // Content type for JSON
            objectMapper.writeValueAsBytes(validProductDto)     // The content of the JSON (converted DTO)
        );

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/products/create")
                .file(jsonPart))                            // Add the product DTO as JSON
            .andExpect(status().isCreated())
            .andExpect(content().string("1"))
            .andReturn();

        assertEquals("1", result.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProduct_ShouldReturnBadRequest_WhenGtinLookupFails() throws Exception {
        when(productService.createProduct(eq(validProductDto), any()))
            .thenThrow(new GtinLookupException("Invalid GTIN"));

        MockMultipartFile jsonPart = new MockMultipartFile(
            "productCreateDto",                           // The name of the part in the request
            "",                                           // Empty file name
            "application/json",                           // Content type for JSON
            objectMapper.writeValueAsBytes(validProductDto)     // The content of the JSON (converted DTO)
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/products/create")
                .file(jsonPart))                            // Add the product DTO as JSON
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error with GTIN lookup: Invalid GTIN"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProduct_ShouldReturn422_WhenDtoValidationFails() throws Exception {
        // Invalid GTIN format
        MockMultipartFile jsonPart = new MockMultipartFile(
            "productCreateDto",                           // The name of the part in the request
            "",                                           // Empty file name
            "application/json",                           // Content type for JSON
            objectMapper.writeValueAsBytes(invalidProductDto)     // The content of the JSON (converted DTO)
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/products/create")
                .file(jsonPart))                            // Add the product DTO as JSON
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().string("{Validation errors=[gtin GTIN must be between 8 and 13 digits]}"));
    }

    @Test
    void createProduct_ShouldReturnForbidden_WhenUserNotAuthenticated() throws Exception {
        // No user authentication, should fail

        MockMultipartFile jsonPart = new MockMultipartFile(
            "productCreateDto",                           // The name of the part in the request
            "",                                           // Empty file name
            "application/json",                           // Content type for JSON
            objectMapper.writeValueAsBytes(validProductDto)     // The content of the JSON (converted DTO)
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/products/create")
                .file(jsonPart))                            // Add the product DTO as JSON
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createBatchProducts_ShouldReturnOK_WhenAllProductsValid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products/batch-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBatchDtos)))
                .andExpect(status().isOk())
                .andExpect(content().string("All Products created successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createBatchProducts_ShouldReturn422_WhenDtoValidationFails() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products/batch-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBatchDtos)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json("""
                {
                    "status": 422,
                    "error": "GTIN must be between 8 and 13 digits"
                }
            """));
    }
}
