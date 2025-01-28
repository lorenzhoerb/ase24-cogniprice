package ase.cogniprice.unitTest.schedulerTest;

import ase.cogniprice.controller.dto.crawler.CrawlJobRequest;
import ase.cogniprice.controller.dto.crawler.CrawlResponseStatus;
import ase.cogniprice.controller.dto.crawler.SimpleCrawlResponse;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.ProductCategory;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.exception.QueueAccessException;
import ase.cogniprice.exception.QueueDispatchException;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.repository.CompetitorRepository;
import ase.cogniprice.repository.ProductCategoryRepository;
import ase.cogniprice.repository.ProductRepository;
import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.service.scheduler.CrawlRequestGateway;
import ase.cogniprice.service.scheduler.impl.SchedulerService;
import ase.cogniprice.type.CrawlState;
import ase.cogniprice.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

import static ase.cogniprice.unitTest.schedulerTest.TestDataProvider.createCompetitor;
import static ase.cogniprice.unitTest.schedulerTest.TestDataProvider.createProduct;
import static ase.cogniprice.unitTest.schedulerTest.TestDataProvider.createProductCategory;
import static ase.cogniprice.unitTest.schedulerTest.TestDataProvider.createStoreProduct;
import static ase.cogniprice.unitTest.schedulerTest.TestDataProvider.createUser;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@Disabled
public class SchedulerServiceRepositoryIntegrationTest {

    @Autowired
    private SchedulerService schedulerService;
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
    @MockBean
    private CrawlRequestGateway crawlRequestGateway;

    private Competitor competitor;
    private Product p1, p2, p3, p4, p5;

    @BeforeEach
    void init() {
        applicationUserRepository.deleteAll();
        competitorRepository.deleteAll();
        productCategoryRepository.deleteAll();
        productRepository.deleteAll();
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

    @Test
    void testDispatchScheduledCrawlJobs_Schedules() throws QueueAccessException, QueueDispatchException {
        doNothing().when(crawlRequestGateway).dispatchCrawlRequest(any(CrawlJobRequest.class));

        ZonedDateTime now = ZonedDateTime.now();
        StoreProduct sp = createStoreProduct(CrawlState.IN_PROGRESS, now.minusMinutes(1), 0, competitor, p1); // not to schedule
        StoreProduct sp2 = createStoreProduct(CrawlState.SCHEDULED, now.minusMinutes(1), 0, competitor, p2); // schedule ready
        StoreProduct sp3 = createStoreProduct(CrawlState.SCHEDULED, now.minusMinutes(5), 0, competitor, p3); // schedule ready before sp2
        StoreProduct sp4 = createStoreProduct(CrawlState.SCHEDULED, now.plusMinutes(5), 0, competitor, p4); // scheduled but in the future
        StoreProduct sp5 = createStoreProduct(CrawlState.PAUSED, ZonedDateTime.now().minusMinutes(5), 0, competitor, p5); // not to schedule

        var savedSp = storeProductRepository.save(sp);
        var savedSp4 = storeProductRepository.save(sp4);
        var savedSp5 = storeProductRepository.save(sp5);
        var savedSp3 = storeProductRepository.save(sp3);
        var savedSp2 = storeProductRepository.save(sp2); //only job that should be scheduled

        schedulerService.dispatchScheduledCrawlJobs(1);

        var resultSp = storeProductRepository.findById(savedSp.getId()).get();
        var resultSp4 = storeProductRepository.findById(savedSp4.getId()).get();
        var resultSp5 = storeProductRepository.findById(savedSp5.getId()).get();
        var resultSp3 = storeProductRepository.findById(savedSp3.getId()).get();
        var resultSp2 = storeProductRepository.findById(savedSp2.getId()).get();

        // Assert
        // Verify state remains unchanged for all except sp3
        assertEquals(CrawlState.IN_PROGRESS, resultSp.getCrawlState(), "sp should remain IN_PROGRESS");
        assertEquals(CrawlState.SCHEDULED, resultSp2.getCrawlState(), "sp2 should remain SCHEDULED");
        assertEquals(CrawlState.SCHEDULED, resultSp4.getCrawlState(), "sp4 should remain SCHEDULED");
        assertEquals(CrawlState.PAUSED, resultSp5.getCrawlState(), "sp5 should remain PAUSED");
        // Verify sp3 is scheduled (state should now be IN_PROGRESS)
        assertEquals(CrawlState.IN_PROGRESS, resultSp3.getCrawlState(), "sp3 should be IN_PROGRESS");

        verify(crawlRequestGateway, times(1)).dispatchCrawlRequest(any(CrawlJobRequest.class));
    }

    @Test
    @Rollback
    void testHandleCrawlJobResponse_SuccessResponse_Reschedules() {
        StoreProduct sp = createStoreProduct(CrawlState.IN_PROGRESS, ZonedDateTime.now().minusMinutes(1), 0, competitor, p1);

        ZonedDateTime now = ZonedDateTime.now();
        var savedSP = storeProductRepository.save(sp);

        ZonedDateTime crawledDate = now.minusMinutes(1);
        schedulerService.handleCrawlJobResponse(savedSP.getId(), new SimpleCrawlResponse(CrawlResponseStatus.SUCCESS, crawledDate));
        StoreProduct out = storeProductRepository.findById(savedSP.getId()).get();
        assertAll(
                () -> assertEquals(CrawlState.SCHEDULED, out.getCrawlState()),
                () -> assertTrue(out.getNextCrawl().isAfter(now)),
                () -> assertEquals(0, out.getRetryAttempts())
        );
    }
}
