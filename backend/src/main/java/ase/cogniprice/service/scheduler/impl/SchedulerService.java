package ase.cogniprice.service.scheduler.impl;

import ase.cogniprice.config.properties.SchedulerConfigProperties;
import ase.cogniprice.controller.dto.crawler.CrawlJobRequest;
import ase.cogniprice.controller.dto.crawler.SimpleCrawlResponse;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.exception.QueueAccessException;
import ase.cogniprice.exception.QueueDispatchException;
import ase.cogniprice.repository.StoreProductRepository;
import ase.cogniprice.service.scheduler.CrawlRequestGateway;
import ase.cogniprice.type.CrawlState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class SchedulerService implements ase.cogniprice.service.scheduler.SchedulerService {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);
    private final StoreProductRepository storeProductRepository;
    private final SchedulerConfigProperties schedulerConfigProperties;
    private final CrawlRequestGateway crawlRequestGateway;

    public SchedulerService(StoreProductRepository storeProductRepository,
                            SchedulerConfigProperties schedulerConfigProperties,
                            CrawlRequestGateway crawlRequestGateway) {
        this.storeProductRepository = storeProductRepository;
        this.schedulerConfigProperties = schedulerConfigProperties;
        this.crawlRequestGateway = crawlRequestGateway;
    }

    @Override
    public boolean startJobById(StoreProduct.StoreProductId jobId) {
        LOG.trace("startJobById({})", jobId);
        StoreProduct storeProduct = getAndValidateJobById(jobId);

        switch (storeProduct.getCrawlState()) {
            case SCHEDULED:
            case IN_PROGRESS:
                return false;

            case PAUSED:
                storeProduct.setCrawlState(CrawlState.SCHEDULED);
                storeProduct.updateNextCrawl();
                storeProductRepository.save(storeProduct);
                return true;

            default:
                throw new IllegalStateException("Unexpected crawl state: " + storeProduct.getCrawlState());
        }
    }

    @Override
    public boolean pauseJobById(StoreProduct.StoreProductId jobId) {
        LOG.trace("pauseJobById({})", jobId);
        StoreProduct storeProduct = getAndValidateJobById(jobId);

        switch (storeProduct.getCrawlState()) {
            case PAUSED:
                return false;

            case SCHEDULED:
                storeProduct.setCrawlState(CrawlState.PAUSED);
                storeProductRepository.save(storeProduct);
                return true;

            case IN_PROGRESS:
                storeProduct.setPauseRequested(true);
                storeProductRepository.save(storeProduct);
                return false;

            default:
                throw new IllegalStateException("Unexpected crawl state: " + storeProduct.getCrawlState());
        }
    }

    @Override
    @Transactional
    public void dispatchScheduledCrawlJobs(Integer limit) {
        LOG.trace("dispatchScheduledCrawlJobs({})", limit);
        validateLimit(limit);

        // Retrieve the scheduled jobs
        List<StoreProduct> storeProducts = fetchScheduledJobs(limit);

        // Dispatch each job
        storeProducts.forEach(this::dispatchJob);
    }

    private List<StoreProduct> fetchScheduledJobs(Integer limit) {
        return (limit == null)
                ? storeProductRepository.findScheduledJobs()
                : storeProductRepository.findScheduledJobs(limit);
    }

    private void dispatchJob(StoreProduct storeProduct) {
        try {
            CrawlJobRequest crawlJobRequest = toCrawlJobRequest(storeProduct);
            crawlRequestGateway.dispatchCrawlRequest(crawlJobRequest);
            storeProduct.setCrawlState(CrawlState.IN_PROGRESS);
            storeProductRepository.save(storeProduct);
        } catch (QueueDispatchException | QueueAccessException e) {
            LOG.warn("Dispatch for store product {} failed", storeProduct.getId(), e);
            handleJobFailure(storeProduct);
        }
    }

    private CrawlJobRequest toCrawlJobRequest(StoreProduct storeProduct) {
        return new CrawlJobRequest(
                storeProduct.getId(),
                storeProduct.getProductUrl(),
                storeProduct.getCompetitor().getUrl(),
                ZonedDateTime.now()
        );
    }

    private static void validateLimit(Integer limit) {
        if (limit != null && limit < 0) {
            throw new IllegalArgumentException("Limit must be greater than or equal to 0");
        }
    }

    @Override
    public void handleCrawlJobResponse(StoreProduct.StoreProductId jobId, SimpleCrawlResponse crawlResponse) {
        LOG.trace("handleCrawlJobResponse({}, {})", jobId, crawlResponse);
        StoreProduct storeProduct = getAndValidateJobById(jobId);
        validateCrawlResponse(crawlResponse);

        switch (crawlResponse.status()) {
            case SUCCESS:
                handleSuccessfulCrawlResponse(crawlResponse, storeProduct);
                break;

            case FAILURE:
                handleJobFailure(storeProduct);
                break;

            default:
                throw new IllegalArgumentException("Unsupported crawl response status: " + crawlResponse.status());
        }

        if (storeProduct.getPauseRequested()) {
            handlePauseRequest(storeProduct);
        }

        storeProductRepository.save(storeProduct);
    }

    private void validateCrawlResponse(SimpleCrawlResponse crawlResponse) {
        LOG.trace("validateCrawlResponse({})", crawlResponse);
        if (crawlResponse == null) {
            LOG.error("Crawl Response is null");
            throw new IllegalArgumentException("Invalid must not be null");
        }

        if (crawlResponse.crawledDate() != null) {
            ZonedDateTime now = ZonedDateTime.now();
            if (crawlResponse.crawledDate().isAfter(now)) {
                throw new IllegalArgumentException("Crawled date must be in the past");
            }
        }
    }

    private StoreProduct getAndValidateJobById(StoreProduct.StoreProductId jobId) {
        LOG.trace("getAndValidateJobById({})", jobId);
        if (jobId == null) {
            throw new IllegalArgumentException("jobId cannot be null");
        }
        return storeProductRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Store product not found"));
    }

    private static void handlePauseRequest(StoreProduct storeProduct) {
        LOG.trace("handlePauseRequest({})", storeProduct);
        storeProduct.setPauseRequested(false);
        storeProduct.setRetryAttempts(0L);
        storeProduct.setCrawlState(CrawlState.PAUSED);
    }

    private static void handleSuccessfulCrawlResponse(SimpleCrawlResponse crawlResponse, StoreProduct storeProduct) {
        LOG.trace("handleSuccessResponse({}, {})", crawlResponse, storeProduct);
        storeProduct.setLastCrawled(crawlResponse.crawledDate());
        storeProduct.updateNextCrawl();
        storeProduct.setCrawlState(CrawlState.SCHEDULED);
        storeProduct.setRetryAttempts(0L);
    }

    private void handleJobFailure(StoreProduct storeProduct) {
        LOG.trace("handleJobFailure({})", storeProduct);
        storeProduct.setRetryAttempts(storeProduct.getRetryAttempts() + 1); // increase retry attempts
        if (hasExceededRetryLimit(storeProduct)) {
            // Max retry limit exceeded
            storeProduct.setCrawlState(CrawlState.PAUSED);
            storeProduct.setRetryAttempts(0L);
        } else {
            storeProduct.setCrawlState(CrawlState.SCHEDULED);
            storeProduct.updateNextCrawl();
        }
    }

    private boolean hasExceededRetryLimit(StoreProduct storeProduct) {
        Integer maxRetries = schedulerConfigProperties.getMaxRetryAttempts();
        return maxRetries != null && storeProduct.getRetryAttempts() > maxRetries;
    }
}
