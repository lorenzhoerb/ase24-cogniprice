package ase.cogniprice.unitTest.productTest.serviceTest;

import ase.cogniprice.controller.dto.competitor.CompetitorDetailsDto;
import ase.cogniprice.controller.dto.product.ProductBatchImporterDto;
import ase.cogniprice.controller.dto.store.product.StoreProductCreateDto;
import ase.cogniprice.controller.mapper.ProductMapper;
import ase.cogniprice.entity.Product;
import ase.cogniprice.exception.GtinLookupException;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.service.CompetitorService;
import ase.cogniprice.service.StoreProductService;
import ase.cogniprice.service.implementation.ProductServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProductServiceCreateBatchTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CompetitorService competitorService;

    @Mock
    private StoreProductService storeProductService;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductBatchImporterDto validProductDto;
    private ProductBatchImporterDto invalidCompetitorDto;
    private Product existingProduct;
    private Product newProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        validProductDto = new ProductBatchImporterDto();
        validProductDto.setName("Valid Product");
        validProductDto.setGtin("4044155274035");
        validProductDto.setProductCategory("Books");
        validProductDto.setCompetitor("Valid");
        validProductDto.setProductUrl("http://valid.com/valid-product");

        invalidCompetitorDto = new ProductBatchImporterDto();
        invalidCompetitorDto.setName("Invalid Competitor Product");
        invalidCompetitorDto.setGtin("4044155274036");
        invalidCompetitorDto.setProductCategory("Books");
        invalidCompetitorDto.setCompetitor("Invalid");
        invalidCompetitorDto.setProductUrl("http://invalid.com/invalid-product");

        existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setGtin("4044155274035");

        newProduct = new Product();
        newProduct.setId(2L);
        newProduct.setGtin("4044155274036");
    }

    @Test
    void createBatchProducts_ShouldSkipExistingProducts() throws GtinLookupException {
        // Given
        when(productRepository.findProductByGtin("4044155274035")).thenReturn(existingProduct);

        // When
        productService.createBatchProducts(List.of(validProductDto), "test_user");

        // Then
        verify(productRepository, never()).save(any());
        verify(storeProductService, never()).createStoreProduct(any(), eq("test_user"));
    }

    @Test
    void createBatchProducts_ShouldCreateProductAndStoreProduct_WhenValidData() throws GtinLookupException {
        // Given
        when(productRepository.findProductByGtin("4044155274035")).thenReturn(null);
        when(productMapper.toEntityWithoutImage(eq(validProductDto), any())).thenReturn(newProduct);

        // Mock save behavior
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            savedProduct.setId(2L); // Simulate database ID assignment
            return savedProduct;
        });

        when(competitorService.getCompetitorsByName("Valid"))
                .thenReturn(List.of(new CompetitorDetailsDto(1L, "Valid", "http://valid.com")));

        // When
        productService.createBatchProducts(List.of(validProductDto), "test_user");

        // Then
        verify(productRepository, times(1)).save(newProduct);
        verify(storeProductService, times(1)).createStoreProduct(any(StoreProductCreateDto.class), eq("test_user"));
    }

    @Test
    void createBatchProducts_ShouldThrowException_WhenCompetitorNotFound() {
        // Given
        when(productRepository.findProductByGtin("4044155274036")).thenReturn(null);
        when(productMapper.toEntityWithoutImage(eq(invalidCompetitorDto), any())).thenReturn(newProduct);
        when(competitorService.getCompetitorsByName("Nonexistent Competitor"))
                .thenReturn(List.of());

        // When & Then
        assertThrows(EntityNotFoundException.class, () ->
                productService.createBatchProducts(List.of(invalidCompetitorDto), "test_user")
        );

        verify(productRepository, times(1)).save(newProduct);
        verify(storeProductService, never()).createStoreProduct(any(), eq("test_user"));
    }

    @Test
    void createBatchProducts_ShouldSkipStoreProductCreation_WhenCompetitorOrUrlMissing() throws GtinLookupException {
        // Given
        ProductBatchImporterDto incompleteDto = new ProductBatchImporterDto();
        incompleteDto.setName("Incomplete Product");
        incompleteDto.setGtin("4044155274037");
        incompleteDto.setProductCategory("Books");
        // Competitor and ProductUrl are null

        when(productRepository.findProductByGtin("4044155274037")).thenReturn(null);
        when(productMapper.toEntityWithoutImage(eq(incompleteDto), any())).thenReturn(newProduct);

        // When
        productService.createBatchProducts(List.of(incompleteDto), "test_user");

        // Then
        verify(productRepository, times(1)).save(newProduct);
        verify(storeProductService, never()).createStoreProduct(any(), eq("test_user"));
    }
}
