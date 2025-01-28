package ase.cogniprice.integrationTest.storeProductIntegrationTest;

import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.entity.ProductPrice;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.type.CrawlState;
import ase.cogniprice.type.Role;
import org.javamoney.moneta.Money;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;

public class DataProvider {

    public static ApplicationUser generateApplicationUser(String username) {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setLoginAttempts(0L);
        user.setPassword("password123");
        user.setRole(Role.USER);
        user.setUsername(username);
        user.setStoreProducts(new HashSet<>());
        return user;
    }

    public static ProductCategory generateProductCategory() {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategory("sampleCategory");
        return productCategory;
    }

    public static Competitor generateCompetitor(String urlAppendex) {
        Competitor competitor = new Competitor();
        competitor.setName("Test Competitor");
        competitor.setUrl("https://competitor.com/" + urlAppendex);
        return competitor;
    }

    public static Product createProduct(ProductCategory productCategory) {
        Product product = new Product();
        product.setName("Test Product");
        product.setGtin("1234567890123");
        product.setStoreProducts(new HashSet<>()); // Set empty list for storeProducts
        product.setWebhooks(Collections.emptySet()); // Set empty set for webhooks
        product.setProductUserPricingRules(new HashSet<>()); // Set empty list for productUserPricingRules
        product.setProductCategory(productCategory); // Set product category
        return product;
    }

    public static StoreProduct createStoreProduct(Product product, Competitor competitor) {
        StoreProduct storeProduct = new StoreProduct();
        StoreProduct.StoreProductId id = new StoreProduct.StoreProductId();
        id.setProductId(product.getId());
        id.setCompetitorId(competitor.getId());
        storeProduct.setId(id);
        storeProduct.setProductUrl("https://www.example.com/product");
        storeProduct.setProduct(product);
        storeProduct.setCompetitor(competitor);
        storeProduct.setInterval(Duration.ofHours(24)); // Default interval
        storeProduct.setLastCrawled(ZonedDateTime.now().minusDays(1)); // Last crawled 1 day ago
        storeProduct.setNextCrawl(ZonedDateTime.now().plusHours(1)); // Next crawl in 1 hour
        storeProduct.setCrawlState(CrawlState.IN_PROGRESS); // Default crawl state
        storeProduct.setRetryAttempts(0L); // Default retry attempts
        storeProduct.setPauseRequested(false); // Default value for pauseRequested
        storeProduct.setApplicationUsers(new HashSet<>()); // Set empty set for applicationUsers
        return storeProduct;
    }

    public static ProductPrice generateProductPrice(StoreProduct storeProduct) {
        ProductPrice productPrice = new ProductPrice();
        ProductPrice.ProductPriceId productPriceId = new ProductPrice.ProductPriceId(storeProduct.getId(), LocalDateTime.now());
        productPrice.setId(productPriceId);
        productPrice.setStoreProduct(storeProduct);
        productPrice.setPrice(Money.of(10, "EUR"));
        return productPrice;
    }
}
