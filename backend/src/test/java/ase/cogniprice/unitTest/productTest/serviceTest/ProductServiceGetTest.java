package ase.cogniprice.unitTest.productTest.serviceTest;

import ase.cogniprice.controller.dto.product.ProductDetailsWithPricesDto;
import ase.cogniprice.exception.GtinLookupException;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.service.implementation.ProductServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProductServiceGetTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Environment env;

    @InjectMocks
    private ProductServiceImpl productService;

    private final String gtin = "1234567890123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(env.getProperty("barcode.lookup.api")).thenReturn("mockedApiKey");
        when(env.getProperty("barcode.lookup.url")).thenReturn("https://mocked-barcode-lookup.com/%s?key=%s");

    }

    // Tests for getProductWithPriceAndImageById

    @Test
    void getProductWithPriceAndImageById_ShouldReturnProductDetails_WhenProductExists() {
        // Arrange
        Long productId = 1L;
        ProductDetailsWithPricesDto productDetailsMock = new ProductDetailsWithPricesDto();
        productDetailsMock.setProductId(productId);
        productDetailsMock.setProductName("Test Product");

        List<Object[]> priceDetails = List.of(
                new Object[]{1L, 1L, "Competitor A", Timestamp.valueOf("2024-01-01 10:00:00"), BigDecimal.valueOf(50.0), "USD", "Min Price"},
                new Object[]{1L, 2L, "Competitor B", Timestamp.valueOf("2024-01-01 11:00:00"), BigDecimal.valueOf(100.0), "USD", "Max Price"}
        );

        when(productRepository.findProductDetailsById(productId)).thenReturn(productDetailsMock);
        when(productRepository.findMinMaxPricesByProductId(productId)).thenReturn(priceDetails);

        // Act
        ProductDetailsWithPricesDto result = productService.getProductWithPriceAndImageById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals("Test Product", result.getProductName());
        assertNotNull(result.getLowestPrice());
        assertNotNull(result.getHighestPrice());
        assertEquals(BigDecimal.valueOf(50.0), result.getLowestPrice().getPrice());
        assertEquals(BigDecimal.valueOf(100.0), result.getHighestPrice().getPrice());
        verify(productRepository, times(1)).findProductDetailsById(productId);
        verify(productRepository, times(1)).findMinMaxPricesByProductId(productId);
    }

    @Test
    void getProductWithPriceAndImageById_ShouldThrowException_WhenProductNotFound() {
        // Arrange
        Long productId = 1L;
        when(productRepository.findProductDetailsById(productId)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> productService.getProductWithPriceAndImageById(productId));
        assertEquals("Product not found for ID: " + productId, exception.getMessage());
        verify(productRepository, times(1)).findProductDetailsById(productId);
        verify(productRepository, never()).findMinMaxPricesByProductId(productId);
    }

    @Test
    void getProductWithPriceAndImageById_ShouldReturnProductWithoutPrices_WhenPriceDetailsMissing() {
        // Arrange
        Long productId = 1L;
        ProductDetailsWithPricesDto productDetailsMock = new ProductDetailsWithPricesDto();
        productDetailsMock.setProductId(productId);
        productDetailsMock.setProductName("Test Product");

        when(productRepository.findProductDetailsById(productId)).thenReturn(productDetailsMock);
        when(productRepository.findMinMaxPricesByProductId(productId)).thenReturn(List.of());

        // Act
        ProductDetailsWithPricesDto result = productService.getProductWithPriceAndImageById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertNull(result.getLowestPrice());
        assertNull(result.getHighestPrice());
        verify(productRepository, times(1)).findProductDetailsById(productId);
        verify(productRepository, times(1)).findMinMaxPricesByProductId(productId);
    }

    // Tests for lookupGtin


    // Tests for lookupGtin

    @Test
    void lookupGtin_ShouldSucceed_WhenApiResponseIsValid() {
        // Arrange
        String apiKey = "mockedApiKey";
        String barcodeUrl = "https://mocked-barcode-lookup.com/%s?key=%s";
        String formattedUrl = String.format(barcodeUrl, gtin, apiKey);

        when(restTemplate.getForEntity(formattedUrl, String.class))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Act
        assertDoesNotThrow(() -> productService.lookupGtin(gtin));

        // Assert
        verify(restTemplate, times(1)).getForEntity(formattedUrl, String.class);
    }

    @Test
    void lookupGtin_ShouldThrowException_WhenApiResponseIsError() {
        // Arrange
        String apiKey = "mockedApiKey";
        String barcodeUrl = "https://mocked-barcode-lookup.com/%s?key=%s";
        String formattedUrl = String.format(barcodeUrl, gtin, apiKey);

        when(restTemplate.getForEntity(formattedUrl, String.class))
                .thenReturn(new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST));

        // Act & Assert
        GtinLookupException exception = assertThrows(GtinLookupException.class,
                () -> productService.lookupGtin(gtin));
        assertEquals("Error fetching product info from BarcodeLookup API", exception.getMessage());
        verify(restTemplate, times(1)).getForEntity(formattedUrl, String.class);
    }



}
