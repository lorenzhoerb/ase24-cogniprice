package ase.cogniprice.unitTest.productPriceTest;

import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.exception.InvalidProductPriceException;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.repository.ProductPriceRepository;
import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.service.ProductPriceService;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class ProductPriceServiceTest {

    @Autowired
    private ProductPriceService productPriceService;

    @MockBean
    private ProductPriceRepository productPriceRepository;

    @MockBean
    private StoreProductRepository storeProductRepository;

    private StoreProduct.StoreProductId createStoreProductId() {
        return new StoreProduct.StoreProductId(1L, 1L);
    }

    private Money createMoney(BigDecimal amount, String currency) {
        return Money.of(amount, currency);
    }

    private StoreProduct createStoreProduct(StoreProduct.StoreProductId storeProductId) {
        StoreProduct storeProduct = new StoreProduct();
        storeProduct.setId(storeProductId);
        return storeProduct;
    }

    @Test
    void testAddPrice_ValidInput_Success() {
        // Arrange
        StoreProduct.StoreProductId storeProductId = createStoreProductId();
        Money price = createMoney(BigDecimal.valueOf(10), "USD");
        LocalDateTime timestamp = LocalDateTime.now();
        StoreProduct storeProduct = createStoreProduct(storeProductId);

        when(storeProductRepository.findById(storeProductId)).thenReturn(Optional.of(storeProduct));

        // Act & Assert
        assertDoesNotThrow(() -> productPriceService.addPrice(storeProductId, price, timestamp));
        verify(productPriceRepository).save(any());
    }

    @Test
    void testAddPrice_StoreProductNotFound_ThrowsNotFoundException() {
        // Arrange
        StoreProduct.StoreProductId storeProductId = createStoreProductId();
        Money price = createMoney(BigDecimal.valueOf(10), "USD");
        LocalDateTime timestamp = LocalDateTime.now();

        when(storeProductRepository.findById(storeProductId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productPriceService.addPrice(storeProductId, price, timestamp));
        assertEquals("StoreProduct not found for ID: " + storeProductId, exception.getMessage());
    }

    @Test
    void testAddPrice_InvalidPrice_ThrowsInvalidProductPriceException() {
        // Arrange
        StoreProduct.StoreProductId storeProductId = createStoreProductId();
        Money price = createMoney(BigDecimal.valueOf(-10), "USD");
        LocalDateTime timestamp = LocalDateTime.now();

        // Act & Assert
        InvalidProductPriceException exception = assertThrows(InvalidProductPriceException.class,
                () -> productPriceService.addPrice(storeProductId, price, timestamp));
        assertEquals("Price must be greater than or equal to 0.", exception.getMessage());
    }

    @Test
    void testAddPrice_FutureTimestamp_ThrowsInvalidProductPriceException() {
        // Arrange
        StoreProduct.StoreProductId storeProductId = createStoreProductId();
        Money price = createMoney(BigDecimal.valueOf(10), "USD");
        LocalDateTime timestamp = LocalDateTime.now().plusDays(1);

        when(storeProductRepository.findById(storeProductId)).thenReturn(Optional.of(new StoreProduct()));

        // Act & Assert
        InvalidProductPriceException exception = assertThrows(InvalidProductPriceException.class,
                () -> productPriceService.addPrice(storeProductId, price, timestamp));
        assertEquals("Timestamp cannot be in the future.", exception.getMessage());
    }

    @Test
    void testAddPrice_NullStoreProductId_ThrowsIllegalArgumentException() {
        // Arrange
        Money price = createMoney(BigDecimal.valueOf(10), "USD");
        LocalDateTime timestamp = LocalDateTime.now();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productPriceService.addPrice(null, price, timestamp));
        assertEquals("StoreProductId must not be null.", exception.getMessage());
    }

    @Test
    void testAddPrice_NullPrice_ThrowsIllegalArgumentException() {
        // Arrange
        StoreProduct.StoreProductId storeProductId = createStoreProductId();
        LocalDateTime timestamp = LocalDateTime.now();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productPriceService.addPrice(storeProductId, null, timestamp));
        assertEquals("Price must not be null.", exception.getMessage());
    }

    @Test
    void testAddPrice_NullTimestamp_ThrowsIllegalArgumentException() {
        // Arrange
        StoreProduct.StoreProductId storeProductId = createStoreProductId();
        Money price = createMoney(BigDecimal.valueOf(10), "USD");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productPriceService.addPrice(storeProductId, price, null));
        assertEquals("Timestamp must not be null.", exception.getMessage());
    }
}
