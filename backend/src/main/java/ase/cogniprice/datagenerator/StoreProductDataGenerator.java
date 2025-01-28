package ase.cogniprice.datagenerator;

import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.CompetitorRepository;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.type.CrawlState;
import jakarta.annotation.PostConstruct;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Profile({"generateData", "generateDemoData"})
@Component
@DependsOn({"productDataGenerator", "competitorDataGenerator", "applicationUserDataGenerator"})
public class StoreProductDataGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ProductRepository productRepository;
    private final CompetitorRepository competitorRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final StoreProductRepository storeProductRepository;

    @Autowired
    public StoreProductDataGenerator(ProductRepository productRepository,
                                     CompetitorRepository competitorRepository,
                                     ApplicationUserRepository applicationUserRepository,
                                     StoreProductRepository storeProductRepository) {
        this.productRepository = productRepository;
        this.competitorRepository = competitorRepository;
        this.applicationUserRepository = applicationUserRepository;
        this.storeProductRepository = storeProductRepository;
    }

    @PostConstruct
    @Transactional
    public void generateStoreProducts() {
        // Get all products, competitors, and application users from the repository
        List<Product> products = productRepository.findAll();
        List<Competitor> competitors = competitorRepository.findAll();
        ApplicationUser applicationUser = applicationUserRepository.findApplicationUserWithRelationsByUsername("regularUser");

        for (Product product : products) {
            for (Competitor competitor : competitors) {

                StoreProduct storeProduct = new StoreProduct();
                StoreProduct.StoreProductId storeProductId = new StoreProduct.StoreProductId(product.getId(), competitor.getId());
                storeProduct.setId(storeProductId);
                storeProduct.setProduct(product);
                storeProduct.setCompetitor(competitor);
                storeProduct.setProductUrl("https://www.example.com/product/" + product.getGtin()); // Example URL
                storeProduct.setInterval(Duration.ofHours(24)); // Example interval
                storeProduct.setLastCrawled(ZonedDateTime.now().minusDays(1)); // Example last crawled time
                storeProduct.setNextCrawl(ZonedDateTime.now().plusHours(24)); // Example next crawl time
                storeProduct.setCrawlState(CrawlState.PAUSED); // Default state
                storeProduct.setRetryAttempts(0L); // Example retry attempts
                storeProduct.setPauseRequested(false); // Example pause state

                // Add the StoreProduct to the ApplicationUser's storeProducts set
                if (applicationUser != null) {
                    storeProduct.setApplicationUsers(new HashSet<>(Arrays.asList(applicationUser)));
                    applicationUser.addStoreProduct(storeProduct, storeProductRepository); // Add to user's store products
                }

                // Save the StoreProduct to the database
                storeProductRepository.save(storeProduct);

                LOG.info("Created StoreProduct for Product: {} and Competitor: {}",
                    product.getName(), competitor.getName());

            }
        }
        applicationUserRepository.save(applicationUser);
    }
}
