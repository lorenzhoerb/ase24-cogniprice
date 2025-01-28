package ase.cogniprice.unitTest.storeProductTest;

import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.service.implementation.StoreProductServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreProductServiceRemoveTest {

    @Mock
    private StoreProductRepository storeProductRepository;

    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @InjectMocks
    private StoreProductServiceImpl storeProductService;

    private ApplicationUser user;
    private StoreProduct storeProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize test entities
        user = new ApplicationUser();
        user.setUsername("testUser");
        user.setStoreProducts(new HashSet<>());

        storeProduct = new StoreProduct();
        storeProduct.setId(new StoreProduct.StoreProductId(1L, 1L));
    }

    @Test
    void testRemoveStoreProduct_Success() {
        // Arrange
        StoreProduct.StoreProductId storeProductId = storeProduct.getId();
        String username = "testUser";

        // Simulate store product in user's watchlist
        user.getStoreProducts().add(storeProduct);

        when(applicationUserRepository.findApplicationUserByUsername(username)).thenReturn(user);
        when(storeProductRepository.findById(storeProductId)).thenReturn(Optional.of(storeProduct));

        // Act
        storeProductService.removeStoreProduct(storeProductId, username);

        // Assert
        verify(applicationUserRepository).save(user); // Ensure user changes are persisted
        assertFalse(user.getStoreProducts().contains(storeProduct), "Store product should be removed from user");
    }

    @Test
    void testRemoveStoreProduct_UserDoesNotHaveProduct() {
        // Arrange
        StoreProduct.StoreProductId storeProductId = storeProduct.getId();
        String username = "testUser";

        when(applicationUserRepository.findApplicationUserByUsername(username)).thenReturn(user);
        when(storeProductRepository.findById(storeProductId)).thenReturn(Optional.of(storeProduct));

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            storeProductService.removeStoreProduct(storeProductId, username);
        });

        assertEquals("StoreProduct with Product ID 1 and Competitor ID 1 does not exist with user testUser.", thrown.getMessage());
    }

    @Test
    void testRemoveStoreProduct_ProductNotFoundInRepository() {
        // Arrange
        StoreProduct.StoreProductId storeProductId = storeProduct.getId();
        String username = "testUser";

        when(applicationUserRepository.findApplicationUserByUsername(username)).thenReturn(user);
        when(storeProductRepository.findById(storeProductId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            storeProductService.removeStoreProduct(storeProductId, username);
        });

        assertEquals("StoreProduct with Product ID 1 and Competitor ID 1 does not exist.", thrown.getMessage());
    }

    @Test
    void testRemoveStoreProduct_UserNotFound() {
        // Arrange
        StoreProduct.StoreProductId storeProductId = storeProduct.getId();
        String username = "nonexistentUser";

        when(storeProductRepository.findById(storeProductId)).thenReturn(Optional.of(storeProduct));
        when(applicationUserRepository.findApplicationUserByUsername(username)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            storeProductService.removeStoreProduct(storeProductId, username);
        });

        assertEquals("User with username 'nonexistentUser' not found", thrown.getMessage());
    }
}
