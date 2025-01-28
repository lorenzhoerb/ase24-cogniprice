package ase.cogniprice.unitTest.storeProductTest;

import ase.cogniprice.controller.dto.store.product.StoreProductCreateDto;
import ase.cogniprice.controller.mapper.StoreProductMapper;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.exception.StoreProductAlreadyExistsException;
import ase.cogniprice.exception.UrlNotReachableException;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.CompetitorRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.service.implementation.StoreProductServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class StoreProductServiceTest {

    @Mock
    private StoreProductRepository storeProductRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CompetitorRepository competitorRepository;

    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @Mock
    private StoreProductMapper storeProductMapper; // Mock the mapper

    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private StoreProductServiceImpl storeProductService;

    private StoreProductCreateDto storeProductCreateDto;
    private Long productId = 1L;
    private Long competitorId = 2L;

    private final String username = "testUser";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        storeProductCreateDto = new StoreProductCreateDto();
        storeProductCreateDto.setProductId(productId);
        storeProductCreateDto.setCompetitorId(competitorId);
        storeProductCreateDto.setProductUrl("https://example.com/product");
    }

    @Test
    void testCreateStoreProduct_whenProductAndCompetitorExist() {
        // Given: The StoreProduct does not already exist, and both Product and Competitor are found in their repositories
        StoreProduct storeProduct = new StoreProduct();
        Competitor competitor = new Competitor();
        competitor.setUrl("https://www.test.com");
        storeProduct.setCompetitor(competitor);
        storeProduct.setProductUrl("https://www.test.com/122343");
        ApplicationUser applicationUser = mock(ApplicationUser.class);

        // Mock the behavior of the repositories
        when(storeProductRepository.existsById(any(StoreProduct.StoreProductId.class))).thenReturn(false);
        when(applicationUserRepository.findApplicationUserByUsername(any(String.class))).thenReturn(applicationUser);
        when(restTemplate.exchange(
            eq("https://www.test.com/122343"),
            eq(HttpMethod.HEAD),
            isNull(),
            eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Mock the mapper's behavior
        when(storeProductMapper.toEntity(any(StoreProductCreateDto.class), eq(productRepository), eq(competitorRepository)))
            .thenReturn(storeProduct);

        // When: The createStoreProduct method is called
        storeProductService.createStoreProduct(storeProductCreateDto, username);

        // Then: The StoreProduct should be saved to the repository
        verify(storeProductRepository, times(1)).save(any(StoreProduct.class));
    }

    @Test
    void testCreateStoreProduct_whenStoreProductAlreadyExists() {
        // Given: The StoreProduct already exists in the repository
        when(storeProductRepository.existsById(any(StoreProduct.StoreProductId.class))).thenReturn(true);
        when(restTemplate.exchange(
            eq("https://www.test.com/122343"),
            eq(HttpMethod.HEAD),
            isNull(),
            eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When: The createStoreProduct method is called
        // Then: It should throw a StoreProductAlreadyExistsException
        StoreProductAlreadyExistsException exception = assertThrows(
            StoreProductAlreadyExistsException.class,
            () -> storeProductService.createStoreProduct(storeProductCreateDto, username)
        );
        assertEquals("StoreProduct with Product ID 1 and Competitor ID 2 already exists.", exception.getMessage());
    }

    @Test
    void testCreateStoreProduct_whenProductNotFound() {
        // Given: The Product with the given ID is not found in the repository
        when(storeProductRepository.existsById(any(StoreProduct.StoreProductId.class))).thenReturn(false);
        // Mock the mapper's behavior
        when(storeProductMapper.toEntity(any(StoreProductCreateDto.class), eq(productRepository), eq(competitorRepository)))
            .thenThrow(EntityNotFoundException.class);
        when(restTemplate.exchange(
            eq("https://www.test.com/122343"),
            eq(HttpMethod.HEAD),
            isNull(),
            eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When: The createStoreProduct method is called
        // Then: It should throw an EntityNotFoundException for Product
        assertThrows(
            EntityNotFoundException.class,
            () -> storeProductService.createStoreProduct(storeProductCreateDto, username)
        );
    }

    @Test
    void testCreateStoreProduct_whenCompetitorNotFound() {
        // Given: The Competitor with the given ID is not found in the repository
        when(storeProductRepository.existsById(any(StoreProduct.StoreProductId.class))).thenReturn(false);
        // Mock the mapper's behavior
        when(storeProductMapper.toEntity(any(StoreProductCreateDto.class), eq(productRepository), eq(competitorRepository)))
            .thenThrow(EntityNotFoundException.class);

        when(restTemplate.exchange(
            eq("https://www.test.com/122343"),
            eq(HttpMethod.HEAD),
            isNull(),
            eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When: The createStoreProduct method is called
        // Then: It should throw an EntityNotFoundException for Competitor
        assertThrows(
            EntityNotFoundException.class,
            () -> storeProductService.createStoreProduct(storeProductCreateDto, username)
        );
    }

    //@Test // no longer needed due to unreliability of http request via code
    void testCreateStoreProduct_whenUrlNotReachable() {
        StoreProduct storeProduct = new StoreProduct();
        Competitor competitor = new Competitor();
        competitor.setUrl("https://www.test.com");
        storeProduct.setCompetitor(competitor);
        storeProduct.setProductUrl("https://www.test.com/122343");
        ApplicationUser applicationUser = mock(ApplicationUser.class);

        // Mock the behavior of the repositories
        when(storeProductRepository.existsById(any(StoreProduct.StoreProductId.class))).thenReturn(false);
        when(applicationUserRepository.findApplicationUserByUsername(any(String.class))).thenReturn(applicationUser);
        when(storeProductMapper.toEntity(any(StoreProductCreateDto.class), eq(productRepository), eq(competitorRepository)))
            .thenReturn(storeProduct);
        when(restTemplate.exchange(
            eq("https://www.test.com/122343"),
            eq(HttpMethod.HEAD),
            isNull(),
            eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When: The createStoreProduct method is called
        // Then: It should throw an UrlNotReachableException

        assertThrows(
            UrlNotReachableException.class,
            () -> storeProductService.createStoreProduct(storeProductCreateDto, username)
        );
    }
}
