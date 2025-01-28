package ase.cogniprice.unitTest.schedulerTest;

import ase.cogniprice.entity.*;
import ase.cogniprice.type.CrawlState;
import ase.cogniprice.type.Role;

import java.time.Duration;
import java.time.ZonedDateTime;

public class TestDataProvider {
    public static ApplicationUser createUser(String username, Role role) {
        ApplicationUser user = new ApplicationUser();
        user.setUsername(username);
        user.setPassword("password");
        user.setFirstName("User");
        user.setLastName("Test");
        user.setEmail(username + "@email.com");
        user.setLoginAttempts(0L);
        user.setUserLocked(false);
        user.setRole(role);
        return user;
    }

    public static Competitor createCompetitor(String name, String url) {
        Competitor competitor = new Competitor();
        competitor.setName(name);
        competitor.setUrl(url);
        return competitor;
    }

    public static ProductCategory createProductCategory(String category) {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategory(category);
        return productCategory;
    }

    public static Product createProduct(String name, ProductCategory category) {
        Product product = new Product();
        product.setName(name);
        product.setProductCategory(category);
        return product;
    }

    public static StoreProduct createStoreProduct(CrawlState state, ZonedDateTime nextCrawl, long retryAttempts, Competitor competitor, Product product) {
        StoreProduct storeProduct = new StoreProduct();
        StoreProduct.StoreProductId id = new StoreProduct.StoreProductId();
        id.setCompetitorId(competitor.getId());
        id.setProductId(product.getId());
        storeProduct.setId(id);
        storeProduct.setCrawlState(state);
        storeProduct.setLastCrawled(ZonedDateTime.now());
        storeProduct.setRetryAttempts(retryAttempts);
        storeProduct.setPauseRequested(false);
        storeProduct.setCompetitor(competitor);
        storeProduct.setInterval(Duration.ofDays(1));
        storeProduct.setNextCrawl(nextCrawl);
        storeProduct.setProduct(product);
        storeProduct.setProductUrl("http://competitor.com/p1");
        return storeProduct;
    }
}
