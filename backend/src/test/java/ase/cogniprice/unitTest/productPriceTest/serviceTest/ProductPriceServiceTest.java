package ase.cogniprice.unitTest.productPriceTest.serviceTest;

import ase.cogniprice.controller.dto.store.product.PriceDto;
import ase.cogniprice.repository.ProductPriceRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.service.implementation.ProductPriceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductPriceServiceTest {

    @Mock
    private ProductPriceRepository productPriceRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreProductRepository storeProductRepository;

    private ProductPriceServiceImpl productPriceService;

    private List<PriceDto> priceDtoList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productPriceService = new ProductPriceServiceImpl(storeProductRepository, productPriceRepository, productRepository);

        priceDtoList = new ArrayList<>();
        priceDtoList.add(new PriceDto(1L, new BigDecimal("99.99"), "Competitor A", "USD", LocalDateTime.of(2024, 12, 1, 14, 30)));
        priceDtoList.add(new PriceDto(1L, new BigDecimal("95.50"), "Competitor A", "USD", LocalDateTime.of(2024, 12, 2, 14, 30)));
        priceDtoList.add(new PriceDto(1L, new BigDecimal("101.75"), "Competitor B", "USD", LocalDateTime.of(2024, 12, 1, 14, 30)));
    }

    @Test
    void getProductPricesByProductIt_ShouldReturnPriceTrends() {
        Long productId = 1L;

        when(productRepository.existsById(productId)).thenReturn(true);
        when(productPriceRepository.findProductPricesByProductId(productId)).thenReturn(priceDtoList);

        List<PriceDto> result = productPriceService.getProductPricesByProductIt(productId);

        assertEquals(priceDtoList, result);
        verify(productPriceRepository, times(1)).findProductPricesByProductId(productId);
    }

    @Test
    void getProductPricesByProductIt_ShouldReturnEmptyList_WhenNoPricesFound() {
        Long productId = 1L;

        when(productRepository.existsById(productId)).thenReturn(true);
        when(productPriceRepository.findProductPricesByProductId(productId)).thenReturn(new ArrayList<>());

        List<PriceDto> result = productPriceService.getProductPricesByProductIt(productId);

        assertTrue(result.isEmpty()); // Verify the result is an empty list
        verify(productRepository, times(1)).existsById(productId);
        verify(productPriceRepository, times(1)).findProductPricesByProductId(productId);
    }

    @Test
    void getProductPricesByProductIt_ShouldThrowException_WhenProductDoesNotExist() {
        Long productId = 2L;

        when(productRepository.existsById(productId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> productPriceService.getProductPricesByProductIt(productId)
        );

        assertEquals("Product with ID " + productId + " does not exist.", exception.getMessage());
        verify(productRepository, times(1)).existsById(productId);
        verifyNoInteractions(productPriceRepository); // Ensure price repository is not called
    }
}
