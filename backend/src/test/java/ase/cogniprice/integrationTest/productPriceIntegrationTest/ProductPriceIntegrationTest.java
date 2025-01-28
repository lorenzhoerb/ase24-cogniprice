package ase.cogniprice.integrationTest.productPriceIntegrationTest;

import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.entity.ProductPrice;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.integrationTest.storeProductIntegrationTest.DataProvider;
import ase.cogniprice.repository.CompetitorRepository;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductPriceRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.service.CompetitorService;
import ase.cogniprice.service.ProductPriceService;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@Transactional
public class ProductPriceIntegrationTest {

    @Autowired
    private ProductPriceService productPriceService;

    @Autowired
    private ProductPriceRepository productPriceRepository;
    @Autowired
    private CompetitorRepository competitorRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CompetitorService competitorService;
    @Autowired
    private ProductCategoryRepository productCategoryRepository;
    @Autowired
    private StoreProductRepository storeProductRepository;

    private StoreProduct storeProduct;

    @BeforeEach
    void setup() {
        // Create a new user
        ApplicationUser user = DataProvider.generateApplicationUser("testUser");

        ProductCategory productCategory = DataProvider.generateProductCategory();
        productCategoryRepository.save(productCategory);

        Product product = DataProvider.createProduct(productCategory);
        productRepository.save(product);

        Competitor competitor = DataProvider.generateCompetitor("");
        competitorRepository.save(competitor);
        Competitor competitor1 = DataProvider.generateCompetitor("1");
        competitorRepository.save(competitor1);


        // Create a new StoreProduct and associate it with the user
        storeProduct = DataProvider.createStoreProduct(product, competitor);
        storeProductRepository.save(storeProduct);


    }

    @Test
    void testAddPrice_Valid_Success() {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = BigDecimal.valueOf(20);
        productPriceService.addPrice(storeProduct.getId(), Money.of(amount, "EUR"), now);
        Optional<ProductPrice> savedProductPrice = productPriceRepository.findById(new ProductPrice.ProductPriceId(storeProduct.getId(), now));

        assertAll("ProductPrice assertions",
                () -> assertTrue(savedProductPrice.isPresent(), "Product price should be saved in the repository."),
                () -> {
                    ProductPrice productPrice = savedProductPrice.get();
                    assertEquals(0, amount.compareTo(productPrice.getPrice().getNumberStripped()), "The price should match the one added.");
                    assertEquals("EUR", productPrice.getPrice().getCurrency().getCurrencyCode(), "The currency should be EUR.");
                }
        );
    }


}
