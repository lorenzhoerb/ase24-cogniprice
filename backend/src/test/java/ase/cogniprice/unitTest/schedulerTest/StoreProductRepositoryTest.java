package ase.cogniprice.unitTest.schedulerTest;

import ase.cogniprice.entity.*;
import ase.cogniprice.repository.*;
import ase.cogniprice.type.CrawlState;
import ase.cogniprice.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

import static ase.cogniprice.unitTest.schedulerTest.TestDataProvider.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class StoreProductRepositoryTest {

    @Autowired
    private StoreProductRepository storeProductRepository;
    @Autowired
    private ApplicationUserRepository applicationUserRepository;
    @Autowired
    private CompetitorRepository competitorRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    private Competitor competitor;
    private Product p1, p2, p3, p4, p5;


    @BeforeEach
    void init() {
        applicationUserRepository.deleteAll();
        competitorRepository.deleteAll();
        productRepository.deleteAll();
        productCategoryRepository.deleteAll();
        storeProductRepository.deleteAll();
        // Initialize shared test data
        ApplicationUser user = createUser("regularUser", Role.USER);
        competitor = createCompetitor("Competitor", "competitor.com");
        ProductCategory productCategory = createProductCategory("test");
        p1 = createProduct("Product 1", productCategory);
        p2 = createProduct("Product 2", productCategory);
        p3 = createProduct("Product 3", productCategory);
        p4 = createProduct("Product 4", productCategory);
        p5 = createProduct("Product 5", productCategory);


        applicationUserRepository.save(user);
        competitorRepository.save(competitor);
        productCategoryRepository.save(productCategory);
        productRepository.saveAll(List.of(p1, p2, p3, p4, p5));
    }


    //@Test
    @Rollback
    void testFindScheduledJobs() {
        ZonedDateTime now = ZonedDateTime.now();
        StoreProduct sp = createStoreProduct(CrawlState.IN_PROGRESS, now.minusMinutes(1), 0, competitor, p1); // not to schedule
        StoreProduct sp2 = createStoreProduct(CrawlState.SCHEDULED, now.minusMinutes(1), 0, competitor, p2); // schedule ready
        StoreProduct sp3 = createStoreProduct(CrawlState.SCHEDULED, now.minusMinutes(5), 0, competitor, p3); // schedule ready before sp2
        StoreProduct sp4 = createStoreProduct(CrawlState.SCHEDULED, now.plusMinutes(5), 0, competitor, p4); // scheduled but in the future
        StoreProduct sp5 = createStoreProduct(CrawlState.PAUSED, ZonedDateTime.now().minusMinutes(5), 0, competitor, p5); // not to schedule
        storeProductRepository.save(sp);
        storeProductRepository.save(sp4);
        storeProductRepository.save(sp5);
        var savedSp2 = storeProductRepository.save(sp2);
        var savedSp3 = storeProductRepository.save(sp3);

        List<StoreProduct> scheduledJobs = storeProductRepository.findScheduledJobs();

        assertEquals(2, scheduledJobs.size());
        assertEquals(savedSp3.getId(), scheduledJobs.getFirst().getId(), "Job with earliest next_crawl should be first in list");
        assertEquals(savedSp2.getId(), scheduledJobs.get(1).getId(), "Job Sp2 should be scheduled ad second position");

    }

    @Test
    @Rollback
    void testFindScheduledJobsWithLimit() {
        ZonedDateTime now = ZonedDateTime.now();
        StoreProduct sp = createStoreProduct(CrawlState.IN_PROGRESS, now.minusMinutes(1), 0, competitor, p1); // not to schedule
        StoreProduct sp2 = createStoreProduct(CrawlState.SCHEDULED, now.minusMinutes(1), 0, competitor, p1); // schedule ready
        StoreProduct sp3 = createStoreProduct(CrawlState.SCHEDULED, now.minusMinutes(5), 0, competitor, p1); // schedule ready before sp2
        StoreProduct sp4 = createStoreProduct(CrawlState.SCHEDULED, now.plusMinutes(5), 0, competitor, p1); // scheduled but in the future
        StoreProduct sp5 = createStoreProduct(CrawlState.PAUSED, ZonedDateTime.now().minusMinutes(5), 0, competitor, p1); // not to schedule
        storeProductRepository.save(sp);
        storeProductRepository.save(sp2);
        storeProductRepository.save(sp4);
        storeProductRepository.save(sp5);

        var savedSp3 = storeProductRepository.save(sp3);

        List<StoreProduct> scheduledJobs = storeProductRepository.findScheduledJobs(1);
        assertAll(
                () -> assertEquals(1, scheduledJobs.size()),
                () -> assertEquals(savedSp3.getId(), scheduledJobs.getFirst().getId(), "Job with earliest next_crawl should be first in list")
        );
    }
}
