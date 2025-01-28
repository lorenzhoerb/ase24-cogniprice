package ase.cogniprice.unitTest.productTest.serviceTest;

import ase.cogniprice.controller.dto.product.ProductCreateDto;
import ase.cogniprice.controller.mapper.ProductMapper;
import ase.cogniprice.entity.Product;
import ase.cogniprice.exception.GtinLookupException;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.service.implementation.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceCreateTest {

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

    private ProductCreateDto productCreateDto;
    private Product product;
    private final String gtin = "4044155274035";
    private  MockMultipartFile image;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(env.getProperty("barcode.lookup.api")).thenReturn("mockedApiKey");
        when(env.getProperty("barcode.lookup.url")).thenReturn("https://mocked-barcode-lookup.com/%s?key=%s");

        productCreateDto = new ProductCreateDto();
        productCreateDto.setGtin(gtin);
        product = new Product();
        product.setGtin(gtin);
        product.setId(0L);
        image = mock(MockMultipartFile.class);
    }

    @Test
    void createProduct_ShouldReturnExistingProduct_WhenProductAlreadyExists() throws GtinLookupException {
        // Given
        when(productRepository.findProductByGtin(gtin)).thenReturn(product);

        // When
        Product result = productService.createProduct(productCreateDto, image);

        // Then
        assertEquals(result, product);
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_ShouldCreateNewProduct_WhenProductDoesNotExist() throws GtinLookupException {
        // Given
        when(productRepository.findProductByGtin(gtin)).thenReturn(null);
        product.setId(null);
        when(productMapper.toEntityWithoutImage(any(ProductCreateDto.class), any(ProductCategoryRepository.class))).thenReturn(product);

        // When
        Product result = productService.createProduct(productCreateDto, image);

        // Then
        assertNull(result);
        verify(productRepository, times(2)).save(product);
    }

    void createProduct_ShouldThrowException_WhenGtinLookupFails() {
        // Arrange
        when(productRepository.findProductByGtin(gtin)).thenReturn(null);

        // Act & Assert
        assertThrows(GtinLookupException.class, () -> productService.createProduct(productCreateDto, image));
        verify(productRepository, never()).save(any());
    }




}
