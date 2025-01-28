package ase.cogniprice.unitTest.storeProductTest;

import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.exception.StoreProductAlreadyExistsException;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.service.implementation.StoreProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.persistence.EntityNotFoundException;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreProductServiceAddTest {

    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @Mock
    private StoreProductRepository storeProductRepository;

    @InjectMocks
    private StoreProductServiceImpl storeProductService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addStoreProduct_ShouldAddProductSuccessfully() {
        // Arrange
        StoreProduct.StoreProductId storeProductId = new StoreProduct.StoreProductId(1L, 1L);
        String username = "testUser";

        ApplicationUser mockUser = mock(ApplicationUser.class);
        StoreProduct mockProduct = mock(StoreProduct.class);


        when(applicationUserRepository.findApplicationUserByUsername(username)).thenReturn(mockUser);
        when(storeProductRepository.findById(storeProductId)).thenReturn(java.util.Optional.of(mockProduct));
        when(mockUser.addStoreProduct(mockProduct, storeProductRepository)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> storeProductService.addStoreProduct(storeProductId, username));

        // Assert
        verify(applicationUserRepository, times(1)).save(mockUser);
    }


    @Test
    void addStoreProduct_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        StoreProduct.StoreProductId storeProductId = new StoreProduct.StoreProductId(1L, 1L);
        String username = "nonExistentUser";

        when(applicationUserRepository.findApplicationUserByUsername(username)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
            storeProductService.addStoreProduct(storeProductId, username)
        );

        assertEquals("User with username 'nonExistentUser' not found", exception.getMessage());
        verify(applicationUserRepository, never()).save(any());
    }


    @Test
    void addStoreProduct_ShouldThrowException_WhenProductAlreadyExists() {
        // Arrange
        StoreProduct.StoreProductId storeProductId = new StoreProduct.StoreProductId(1L, 1L);
        String username = "testUser";

        StoreProduct mockProduct = new StoreProduct();
        mockProduct.setId(storeProductId);
        ApplicationUser mockUser = new ApplicationUser();
        mockUser.setUsername("testUser");

        Set<StoreProduct> userSet = new HashSet<>();
        userSet.add(mockProduct);
        mockUser.setStoreProducts(userSet);

        when(applicationUserRepository.findApplicationUserByUsername(username)).thenReturn(mockUser);
        when(storeProductRepository.findById(storeProductId)).thenReturn(java.util.Optional.of(mockProduct));


        // Act & Assert
        StoreProductAlreadyExistsException exception = assertThrows(StoreProductAlreadyExistsException.class, () ->
            storeProductService.addStoreProduct(storeProductId, username)
        );

        assertTrue(exception.getMessage().contains("does already exist with user"));
        verify(applicationUserRepository, never()).save(any());
    }

}
