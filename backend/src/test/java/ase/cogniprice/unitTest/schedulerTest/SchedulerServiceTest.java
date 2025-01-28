package ase.cogniprice.unitTest.schedulerTest;

import ase.cogniprice.config.properties.SchedulerConfigProperties;
import ase.cogniprice.controller.dto.crawler.CrawlJobRequest;
import ase.cogniprice.controller.dto.crawler.CrawlResponseStatus;
import ase.cogniprice.controller.dto.crawler.SimpleCrawlResponse;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.entity.Product;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.exception.QueueDispatchException;
import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.service.scheduler.CrawlRequestGateway;
import ase.cogniprice.service.scheduler.impl.SchedulerService;
import ase.cogniprice.type.CrawlState;
import jakarta.validation.constraints.Max;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
public class SchedulerServiceTest {

    @InjectMocks
    private SchedulerService schedulerService;
    @Mock
    private StoreProductRepository storeProductRepository;
    @Mock
    private SchedulerConfigProperties schedulerConfigProperties;
    @Mock
    private CrawlRequestGateway crawlRequestGateway;

    @Nested
    class StartJobTests {
        @Test
        void testStartJobById_NullJobId_ThrowsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> {
                schedulerService.startJobById(null);
            });
        }

        @Test
        void testStartJobById_JobNotFound_ThrowsNotFoundException() {
            StoreProduct.StoreProductId nonExistentJobId = new StoreProduct.StoreProductId();
            nonExistentJobId.setCompetitorId(Long.MAX_VALUE);
            nonExistentJobId.setProductId(Long.MAX_VALUE);

            when(storeProductRepository.findById(nonExistentJobId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> {
                schedulerService.startJobById(nonExistentJobId);
            });

            verify(storeProductRepository).findById(nonExistentJobId);
        }

        @Test
        void testStartJobById_JobPaused_SchedulesJobSuccessfully() {
            StoreProduct.StoreProductId jobId = new StoreProduct.StoreProductId();
            jobId.setCompetitorId(1L);
            jobId.setProductId(1L);
            ZonedDateTime now = ZonedDateTime.now();

            StoreProduct sp = new StoreProduct();
            sp.setLastCrawled(now.minusMinutes(5L));
            sp.setCrawlState(CrawlState.PAUSED);
            sp.setInterval(Duration.ofDays(1));

            when(storeProductRepository.findById(jobId)).thenReturn(Optional.of(sp));
            when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(sp);

            boolean result = schedulerService.startJobById(jobId);

            assertAll(
                () -> assertTrue(result),
                () -> assertEquals(CrawlState.SCHEDULED, sp.getCrawlState()),
                () -> assertNotNull(sp.getNextCrawl()),
                () -> assertTrue(sp.getNextCrawl().isAfter(now))
            );

            verify(storeProductRepository).findById(jobId);
            verify(storeProductRepository).save(sp);
        }

        @Test
        void testStartJobById_JobScheduled_NothingShouldHappen() {
            StoreProduct.StoreProductId jobId = new StoreProduct.StoreProductId();
            jobId.setCompetitorId(1L);
            jobId.setProductId(2L);

            StoreProduct sp = new StoreProduct();
            sp.setCrawlState(CrawlState.SCHEDULED);

            when(storeProductRepository.findById(jobId)).thenReturn(Optional.of(sp));
            when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(sp);

            boolean result = schedulerService.startJobById(jobId);

            assertAll(
                () -> assertFalse(result),
                () -> assertEquals(CrawlState.SCHEDULED, sp.getCrawlState())
            );

            verify(storeProductRepository).findById(jobId);
            verify(storeProductRepository, times(0)).save(sp);
        }

        @Test
        void testStartJobById_JobInProgress_NothingShouldHappen() {
            StoreProduct.StoreProductId jobId = new StoreProduct.StoreProductId();
            jobId.setCompetitorId(1L);
            jobId.setProductId(3L);

            StoreProduct sp = new StoreProduct();
            sp.setCrawlState(CrawlState.IN_PROGRESS);

            when(storeProductRepository.findById(jobId)).thenReturn(Optional.of(sp));
            when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(sp);

            boolean result = schedulerService.startJobById(jobId);

            assertAll(
                () -> assertFalse(result),
                () -> assertEquals(CrawlState.IN_PROGRESS, sp.getCrawlState())
            );

            verify(storeProductRepository).findById(jobId);
            verify(storeProductRepository, times(0)).save(sp);
        }
    }

    @Nested
    class PauseJobTests {
        @Test
        void testPauseJobById_NullJobId_ThrowsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> {
                schedulerService.pauseJobById(null);
            });
        }

        @Test
        void testPauseJobById_JobNotFound_ThrowsNotFoundException() {
            StoreProduct.StoreProductId nonExistentJobId = new StoreProduct.StoreProductId();
            nonExistentJobId.setCompetitorId(2L);
            nonExistentJobId.setProductId(4L);

            when(storeProductRepository.findById(nonExistentJobId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> {
                schedulerService.pauseJobById(nonExistentJobId);
            });

            verify(storeProductRepository).findById(nonExistentJobId);
        }

        @Test
        void testPauseJobById_JobScheduled_PausesJobSuccessfully() {
            StoreProduct.StoreProductId jobId = new StoreProduct.StoreProductId();
            jobId.setCompetitorId(1L);
            jobId.setProductId(1L);
            StoreProduct sp = new StoreProduct();
            sp.setCrawlState(CrawlState.SCHEDULED);

            // Mock fetch store product / job
            when(storeProductRepository.findById(jobId)).thenReturn(Optional.of(sp));
            // Simulate saving the entity / the result of save is not important in the implementation
            when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(sp);

            boolean result = schedulerService.pauseJobById(jobId);
            assertAll(
                () -> assertTrue(result),
                () -> assertEquals(CrawlState.PAUSED, sp.getCrawlState())
            );

            verify(storeProductRepository).findById(jobId);
            verify(storeProductRepository).save(sp);
        }

        @Test
        void testPauseJobById_JobInProgress_PauseRequestedAndStillInProgress() {
            StoreProduct.StoreProductId jobId = new StoreProduct.StoreProductId();
            jobId.setCompetitorId(2L);
            jobId.setProductId(2L);
            StoreProduct sp = new StoreProduct();
            sp.setCrawlState(CrawlState.IN_PROGRESS);

            // Mock fetch store product / job
            when(storeProductRepository.findById(jobId)).thenReturn(Optional.of(sp));
            // Simulate saving the entity / the result of save is not important in the implementation
            when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(sp);

            boolean result = schedulerService.pauseJobById(jobId);
            assertAll(
                // job state didn't change
                () -> assertFalse(result),
                () -> assertEquals(CrawlState.IN_PROGRESS, sp.getCrawlState()),
                // pause requested
                () -> assertTrue(sp.getPauseRequested())
            );

            verify(storeProductRepository).findById(jobId);
            verify(storeProductRepository).save(sp);
        }

        @Test
        void testPauseJobById_JobPaused_NothingShouldHappen() {
            StoreProduct.StoreProductId jobId = new StoreProduct.StoreProductId();
            jobId.setCompetitorId(3L);
            jobId.setProductId(3L);
            StoreProduct sp = new StoreProduct();
            sp.setCrawlState(CrawlState.PAUSED);

            // Mock fetch store product / job
            when(storeProductRepository.findById(jobId)).thenReturn(Optional.of(sp));
            // Simulate saving the entity / the result of save is not important in the implementation
            when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(sp);

            boolean result = schedulerService.pauseJobById(jobId);
            assertAll(
                // job state didn't change
                () -> assertFalse(result),
                () -> assertEquals(CrawlState.PAUSED, sp.getCrawlState())
            );

            verify(storeProductRepository).findById(jobId);
            verify(storeProductRepository, times(0)).save(sp);
        }
    }

    @Nested
    class HandleCrawlJobResponseTests {

        @Test
        void testHandleCrawlJobResponse_NullJobId_ThrowsIllegalArgumentException() {
            SimpleCrawlResponse someCrawlResponse = new SimpleCrawlResponse(
                null,
                null
            );
            assertThrows(IllegalArgumentException.class, () -> {
                schedulerService.handleCrawlJobResponse(null, someCrawlResponse);
            });
        }

        @Test
        void testHandleCrawlJobResponse_NullCrawlResponse_ThrowsIllegalArgumentException() {
            StoreProduct.StoreProductId someJobId = new StoreProduct.StoreProductId();
            someJobId.setCompetitorId(4L);
            someJobId.setProductId(4L);
            when(storeProductRepository.findById(someJobId)).thenReturn(Optional.of(new StoreProduct()));
            assertThrows(IllegalArgumentException.class, () -> {
                schedulerService.handleCrawlJobResponse(someJobId, null);
            });
        }

        @Test
        void testHandleCrawlJobResponse_FutureCrawlDate_ThrowsIllegalArgumentException() {
            StoreProduct.StoreProductId someJobId = new StoreProduct.StoreProductId();
            someJobId.setCompetitorId(5L);
            someJobId.setProductId(5L);
            when(storeProductRepository.findById(someJobId)).thenReturn(Optional.of(new StoreProduct()));
            SimpleCrawlResponse crawlResponse = new SimpleCrawlResponse(
                CrawlResponseStatus.SUCCESS, // some state
                ZonedDateTime.now().plusMinutes(1) // future crawled date
            );
            assertThrows(IllegalArgumentException.class, () -> {
                schedulerService.handleCrawlJobResponse(someJobId, crawlResponse);
            });
        }

        @Test
        void testHandleCrawlJobResponse_JobNotFound_ThrowsNotFoundException() {
            StoreProduct.StoreProductId nonExistentJobId = new StoreProduct.StoreProductId();
            nonExistentJobId.setCompetitorId(999L);
            nonExistentJobId.setProductId(999L);

            when(storeProductRepository.findById(nonExistentJobId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> {
                schedulerService.handleCrawlJobResponse(nonExistentJobId, null);
            });

            // verify that findById was called
            verify(storeProductRepository).findById(nonExistentJobId);
        }

        @Test
        void testHandleCrawlJobResponse_Success_SchedulesNextCrawlJob() {
            // Mock scheduler max retry limit config
            when(schedulerConfigProperties.getMaxRetryAttempts()).thenReturn(3);

            ZonedDateTime now = ZonedDateTime.now();
            StoreProduct.StoreProductId someJobId = new StoreProduct.StoreProductId();
            someJobId.setCompetitorId(5L); // Set the competitor ID
            someJobId.setProductId(5L); // Set the product ID
            StoreProduct sp = new StoreProduct();
            sp.setCrawlState(CrawlState.IN_PROGRESS);
            sp.setLastCrawled(now.minusMinutes(15L));
            sp.setInterval(Duration.ofDays(1));
            sp.setRetryAttempts(2L);
            sp.setPauseRequested(false);

            SimpleCrawlResponse crawlResponse = new SimpleCrawlResponse(CrawlResponseStatus.SUCCESS, now);

            // Mock getting product by StoreProductId
            when(storeProductRepository.findById(someJobId)).thenReturn(Optional.of(sp));
            // Mock saving product
            when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(sp);

            schedulerService.handleCrawlJobResponse(someJobId, crawlResponse);

            assertAll(
                // Assert the job is scheduled with a future next crawl date
                () -> assertEquals(CrawlState.SCHEDULED, sp.getCrawlState()),
                () -> assertTrue(sp.getNextCrawl().isAfter(now)),
                () -> assertEquals(crawlResponse.crawledDate(), sp.getLastCrawled()),
                // Assert that retry attempts are reset
                () -> assertEquals(0, sp.getRetryAttempts())
            );

            verify(storeProductRepository).findById(someJobId);
            verify(storeProductRepository).save(sp);
        }

        @Test
        void testHandleCrawlJobResponse_FailureRetryLimitNotReached_SchedulesNextCrawlJob() {
            // Mock scheduler max retry limit config
            when(schedulerConfigProperties.getMaxRetryAttempts()).thenReturn(3);

            ZonedDateTime now = ZonedDateTime.now();
            StoreProduct.StoreProductId someJobId = new StoreProduct.StoreProductId();
            someJobId.setCompetitorId(6L); // Set the competitor ID
            someJobId.setProductId(6L); // Set the product ID
            StoreProduct sp = new StoreProduct();
            sp.setCrawlState(CrawlState.IN_PROGRESS);
            sp.setLastCrawled(now.minusMinutes(15L));
            sp.setInterval(Duration.ofDays(1));
            sp.setRetryAttempts(0L);
            sp.setPauseRequested(false);

            SimpleCrawlResponse crawlResponse = new SimpleCrawlResponse(CrawlResponseStatus.FAILURE, now.minusMinutes(5L));

            // Mock getting product by StoreProductId
            when(storeProductRepository.findById(someJobId)).thenReturn(Optional.of(sp));
            // Mock saving product
            when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(sp);

            schedulerService.handleCrawlJobResponse(someJobId, crawlResponse);

            assertAll(
                // Assert the job is scheduled with a future next crawl date
                () -> assertEquals(CrawlState.SCHEDULED, sp.getCrawlState()),
                () -> assertTrue(sp.getNextCrawl().isAfter(now)),
                // last crawled didn't update
                () -> assertEquals(sp.getLastCrawled(), now.minusMinutes(15L)),
                // reset attempt is increased by one
                () -> assertEquals(1L, sp.getRetryAttempts())
            );

            verify(storeProductRepository).findById(someJobId);
            verify(storeProductRepository).save(sp);
        }

        @Test
        void testHandleCrawlJobResponse_FailureRetryLimitReached_PausesJobSuccessfully() {
            // Mock config of the value of maxRetryAttempts
            when(schedulerConfigProperties.getMaxRetryAttempts()).thenReturn(3);

            ZonedDateTime now = ZonedDateTime.now();
            StoreProduct.StoreProductId someJobId = new StoreProduct.StoreProductId();
            someJobId.setCompetitorId(7L); // Set the competitor ID
            someJobId.setProductId(7L); // Set the product ID
            StoreProduct sp = new StoreProduct();
            sp.setCrawlState(CrawlState.IN_PROGRESS);
            sp.setLastCrawled(now.minusMinutes(15L));
            sp.setInterval(Duration.ofDays(1));
            sp.setRetryAttempts(3L);
            sp.setPauseRequested(false);

            SimpleCrawlResponse crawlResponse = new SimpleCrawlResponse(CrawlResponseStatus.FAILURE, now.minusMinutes(5L));

            // Mock store repository
            when(storeProductRepository.findById(someJobId)).thenReturn(Optional.of(sp));
            when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(sp);

            schedulerService.handleCrawlJobResponse(someJobId, crawlResponse);

            assertAll(
                // Assert the job is scheduled with a future next crawl date
                () -> assertEquals(CrawlState.PAUSED, sp.getCrawlState()),
                // last crawled didn't update
                () -> assertEquals(now.minusMinutes(15L), sp.getLastCrawled()),
                // Reset retry attempts when put in paused state
                () -> assertEquals(0L, sp.getRetryAttempts())
            );

            verify(storeProductRepository).findById(someJobId);
            verify(storeProductRepository).save(sp);
        }

        @Test
        void testHandleCrawlJobResponse_SuccessAndPauseRequested_PausesJobSuccessfully() {
            // Mock config of the value of maxRetryAttempts
            when(schedulerConfigProperties.getMaxRetryAttempts()).thenReturn(3);

            ZonedDateTime now = ZonedDateTime.now();
            StoreProduct.StoreProductId someJobId = new StoreProduct.StoreProductId();
            someJobId.setCompetitorId(8L); // Set the competitor ID
            someJobId.setProductId(8L); // Set the product ID
            StoreProduct sp = new StoreProduct();
            sp.setCrawlState(CrawlState.IN_PROGRESS);
            sp.setLastCrawled(now.minusMinutes(15L));
            sp.setInterval(Duration.ofDays(1));
            sp.setRetryAttempts(0L);
            sp.setPauseRequested(true);

            SimpleCrawlResponse crawlResponse = new SimpleCrawlResponse(CrawlResponseStatus.SUCCESS, now.minusMinutes(5L));

            // Mock store repository
            when(storeProductRepository.findById(someJobId)).thenReturn(Optional.of(sp));
            when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(sp);

            schedulerService.handleCrawlJobResponse(someJobId, crawlResponse);

            assertAll(
                // Assert the job is scheduled with a future next crawl date
                () -> assertEquals(CrawlState.PAUSED, sp.getCrawlState()),
                // updated last crawled
                () -> assertEquals(crawlResponse.crawledDate(), sp.getLastCrawled()),
                // Reset retry attempts when put in paused state
                () -> assertEquals(0L, sp.getRetryAttempts()),
                () -> assertFalse(sp.getPauseRequested())
            );

            verify(storeProductRepository).findById(someJobId);
            verify(storeProductRepository).save(sp);
        }

        @Test
        void testHandleCrawlJobResponse_FailureAndPauseRequested_PausesJobSuccessfully() {
            // Mock config of the value of maxRetryAttempts
            when(schedulerConfigProperties.getMaxRetryAttempts()).thenReturn(3);

            ZonedDateTime now = ZonedDateTime.now();
            StoreProduct.StoreProductId someJobId = new StoreProduct.StoreProductId();
            someJobId.setCompetitorId(9L); // Set the competitor ID
            someJobId.setProductId(9L); // Set the product ID
            StoreProduct sp = new StoreProduct();
            sp.setCrawlState(CrawlState.IN_PROGRESS);
            sp.setLastCrawled(now.minusMinutes(15L));
            sp.setInterval(Duration.ofDays(1));
            sp.setRetryAttempts(0L);
            sp.setPauseRequested(true);

            SimpleCrawlResponse crawlResponse = new SimpleCrawlResponse(CrawlResponseStatus.FAILURE, now.minusMinutes(5L));

            // Mock store repository
            when(storeProductRepository.findById(someJobId)).thenReturn(Optional.of(sp));
            when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(sp);

            schedulerService.handleCrawlJobResponse(someJobId, crawlResponse);

            assertAll(
                // Assert the job is scheduled with a future next crawl date
                () -> assertEquals(CrawlState.PAUSED, sp.getCrawlState()),
                // last crawled didn't update
                () -> assertEquals(now.minusMinutes(15L), sp.getLastCrawled()),
                // Reset retry attempts when put in paused state
                () -> assertEquals(0L, sp.getRetryAttempts()),
                () -> assertFalse(sp.getPauseRequested())
            );
        }
    }

    @Nested
    class DispatchScheduledCrawlJobsTests {

        @Test
        void testDispatchScheduledCrawlJobs_InvalidLimit_throwsIllegalArgumentException() {
            final Integer jobDispatchLimit = -1;
            assertThrows(IllegalArgumentException.class, () -> {
                schedulerService.dispatchScheduledCrawlJobs(jobDispatchLimit);
            });
        }

        @Test
        void testDispatchScheduledCrawlJobs_NoLimit_DispatchesAllEligibleJobs() throws Exception {
            List<StoreProduct> storeProducts = getMockStoreProducts();
            when(storeProductRepository.findScheduledJobs()).thenReturn(storeProducts);

            schedulerService.dispatchScheduledCrawlJobs(null);

            assertAll(
                () -> assertEquals(CrawlState.IN_PROGRESS, storeProducts.get(0).getCrawlState()),
                () -> assertEquals(CrawlState.IN_PROGRESS, storeProducts.get(1).getCrawlState())
            );

            verify(storeProductRepository).findScheduledJobs();
            verify(storeProductRepository).save(storeProducts.get(0));
            verify(storeProductRepository).save(storeProducts.get(1));
            verify(crawlRequestGateway, times(2)).dispatchCrawlRequest(any(CrawlJobRequest.class));
        }

        @Test
        void testDispatchScheduledCrawlJobs_WithLimit_DispatchesLimitedEligibleJobs() throws Exception {
            int limit = 1;
            List<StoreProduct> storeProducts = getMockStoreProducts();
            when(storeProductRepository.findScheduledJobs(limit)).thenReturn(storeProducts.subList(0, limit));

            schedulerService.dispatchScheduledCrawlJobs(limit);

            assertAll(
                () -> assertEquals(CrawlState.IN_PROGRESS, storeProducts.get(0).getCrawlState()),
                () -> assertNotEquals(CrawlState.IN_PROGRESS, storeProducts.get(1).getCrawlState())
            );

            verify(storeProductRepository).findScheduledJobs(limit);
            verify(storeProductRepository).save(storeProducts.get(0));
            verify(crawlRequestGateway, times(1)).dispatchCrawlRequest(any(CrawlJobRequest.class));
        }

        @Test
        void testDispatchScheduledCrawlJobs_DispatchFailure_ReschedulesAndRetryAttempt() throws Exception {
            List<StoreProduct> storeProducts = getMockStoreProducts();

            when(schedulerConfigProperties.getMaxRetryAttempts()).thenReturn(3);
            doThrow(new QueueDispatchException("Dispatch failed"))
                .when(crawlRequestGateway).dispatchCrawlRequest(any(CrawlJobRequest.class));
            when(storeProductRepository.findScheduledJobs()).thenReturn(storeProducts);

            schedulerService.dispatchScheduledCrawlJobs(null);

            assertAll(
                () -> assertEquals(CrawlState.SCHEDULED, storeProducts.get(0).getCrawlState()),
                () -> assertEquals(CrawlState.SCHEDULED, storeProducts.get(1).getCrawlState()),
                // Increases retry attempt
                () -> assertEquals(1L, storeProducts.get(0).getRetryAttempts()),
                () -> assertEquals(1L, storeProducts.get(1).getRetryAttempts())
            );

            verify(storeProductRepository).findScheduledJobs();
            verify(crawlRequestGateway, times(2)).dispatchCrawlRequest(any(CrawlJobRequest.class));
        }

        List<StoreProduct> getMockStoreProducts() {
            Product product = new Product();
            product.setId(1L);
            Competitor competitor = new Competitor();
            competitor.setId(1L);
            competitor.setUrl("host.com");

            StoreProduct sp1 = new StoreProduct();
            StoreProduct.StoreProductId sp1Id = new StoreProduct.StoreProductId();
            sp1Id.setCompetitorId(1L);
            sp1Id.setProductId(1L);
            sp1.setId(sp1Id);
            sp1.setNextCrawl(ZonedDateTime.now().minusMinutes(1L));
            sp1.setCrawlState(CrawlState.SCHEDULED);
            sp1.setProduct(product);
            sp1.setProductUrl("https://host.com/p1");
            sp1.setCompetitor(competitor);
            sp1.setRetryAttempts(0L);
            sp1.setLastCrawled(ZonedDateTime.now().minusMinutes(1L));
            sp1.setInterval(Duration.ofDays(1));

            StoreProduct sp2 = new StoreProduct();
            StoreProduct.StoreProductId sp2Id = new StoreProduct.StoreProductId();
            sp2Id.setCompetitorId(1L);
            sp2Id.setProductId(2L);
            sp2.setId(sp2Id);
            sp2.setNextCrawl(ZonedDateTime.now().minusMinutes(2L));
            sp2.setCrawlState(CrawlState.SCHEDULED);
            sp2.setProduct(product);
            sp2.setProductUrl("https://host.com/p2");
            sp2.setCompetitor(competitor);
            sp2.setRetryAttempts(0L);
            sp2.setLastCrawled(ZonedDateTime.now().minusMinutes(1L));
            sp2.setInterval(Duration.ofDays(1));

            return List.of(sp2, sp1);
        }
    }

}
