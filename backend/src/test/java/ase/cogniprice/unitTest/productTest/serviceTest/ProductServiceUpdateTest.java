package ase.cogniprice.unitTest.productTest.serviceTest;

import ase.cogniprice.controller.dto.product.ProductUpdateDto;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.controller.mapper.ProductMapper;
import ase.cogniprice.service.implementation.ProductServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ProductServiceUpdateTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private ProductMapper productMapper;
    @Mock
    private Environment env;
    @InjectMocks
    private ProductServiceImpl productService;

    private ProductUpdateDto productUpdateDto;
    private Product existingProduct;
    private ProductCategory productCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setting up test data
        productUpdateDto = new ProductUpdateDto();
        productUpdateDto.setId(1L);
        productUpdateDto.setProductCategoryId("Books");

        existingProduct = new Product();
        existingProduct.setId(1L);

        productCategory = new ProductCategory();
        productCategory.setCategory("Books");
    }

    @Test
    void testUpdateProduct_Success() {
        // GIVEN: Mocking the behavior
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(existingProduct));

        // WHEN: Calling the updateProduct method
        productService.updateProduct(productUpdateDto);

        // THEN: Verifying the updateEntity and save method calls
        verify(productMapper, times(1)).updateEntity(any(Product.class), any(ProductUpdateDto.class), any(ProductCategoryRepository.class));  // Verify updateEntity was called
        verify(productRepository, times(1)).save(existingProduct);  // Verify save was called
    }

    @Test
    void testUpdateProduct_ProductNotFound() {
        // GIVEN: Mocking the behavior
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        // WHEN & THEN: Calling the updateProduct method and asserting the exception
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            productService.updateProduct(productUpdateDto);
        });

        assertEquals("Product with ID 1 not found.", exception.getMessage());
    }

    @Test
    void testUpdateProduct_ProductCategoryNotFound() {
        // GIVEN: Mocking the behavior
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(existingProduct));
        doThrow(IllegalArgumentException.class).when(productMapper).updateEntity(any(Product.class), any(ProductUpdateDto.class), any(ProductCategoryRepository.class));

        // WHEN & THEN: Calling the updateProduct method and asserting the exception
        assertThrows(IllegalArgumentException.class, () -> {
            productService.updateProduct(productUpdateDto);
        });
    }
}
