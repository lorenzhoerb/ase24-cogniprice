package ase.cogniprice.unitTest.ProductCategoryTest;

import ase.cogniprice.controller.dto.product.category.ProductCategoryDto;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.service.ProductCategoryService;
import ase.cogniprice.service.implementation.ProductCategoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ProductCategoryTest {
    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @BeforeEach
    void setUp() {
        productCategoryRepository.deleteAll(); // Clean up before each test
        productCategoryService = new ProductCategoryImpl(productCategoryRepository);
    }

    @Nested
    class GetAllProductCategoriesTests {
        @Test
        void testGetAllProductCategories_ShouldReturnAllCategories() {
            // Arrange
            ProductCategory category1 = new ProductCategory();
            category1.setCategory("Electronics");
            ProductCategory category2 = new ProductCategory();
            category2.setCategory("Home Appliances");

            productCategoryRepository.save(category1);
            productCategoryRepository.save(category2);

            // Act
            List<ProductCategoryDto> result = productCategoryService.getAllProductCategories();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Electronics", result.get(0).getCategory());
            assertEquals("Home Appliances", result.get(1).getCategory());
        }

        @Test
        void testGetAllProductCategories_ShouldReturnEmptyListWhenNoCategoriesExist() {
            // Act
            List<ProductCategoryDto> result = productCategoryService.getAllProductCategories();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetProductCategoriesByNameTests {
        @Test
        void testGetProductCategoriesByName_ShouldReturnMatchingCategories() {
            // Arrange
            ProductCategory category1 = new ProductCategory();
            category1.setCategory("Electronics");
            ProductCategory category2 = new ProductCategory();
            category2.setCategory("Electronic Gadgets");
            ProductCategory category3 = new ProductCategory();
            category3.setCategory("Home Appliances");

            productCategoryRepository.save(category1);
            productCategoryRepository.save(category2);
            productCategoryRepository.save(category3);

            // Act
            List<ProductCategoryDto> result = productCategoryService.getProductCategoriesByName("Electronic");

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(dto -> dto.getCategory().equals("Electronics")));
            assertTrue(result.stream().anyMatch(dto -> dto.getCategory().equals("Electronic Gadgets")));
        }

        @Test
        void testGetProductCategoriesByName_ShouldReturnEmptyListWhenNoMatch() {
            // Arrange
            ProductCategory category = new ProductCategory();
            category.setCategory("Home Appliances");
            productCategoryRepository.save(category);

            // Act
            List<ProductCategoryDto> result = productCategoryService.getProductCategoriesByName("NonExistingCategory");

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void testGetProductCategoriesByName_ShouldBeCaseInsensitive() {
            // Arrange
            ProductCategory category = new ProductCategory();
            category.setCategory("Electronics");
            productCategoryRepository.save(category);

            // Act
            List<ProductCategoryDto> result = productCategoryService.getProductCategoriesByName("electronics");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Electronics", result.get(0).getCategory());
        }
    }
}
