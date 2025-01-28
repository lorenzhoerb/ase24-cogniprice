package ase.cogniprice.service.scheduler;

import ase.cogniprice.controller.dto.crawler.CrawlJobResponse;

/**
 * Interface for handling crawl job responses received from the crawl response event.
 * This interface processes the crawl response, updating the status of the associated
 * crawl job in the scheduling service.
 */
public interface CrawlResponseGateway {

    /**
     * Processes the crawl job response from the response queue.
     * This method updates the crawl job's status using the `handleCrawlJobResponse` method of the `ISchedulerService`.
     *
     * @param crawlJobResponse the response containing the result of a crawl job
     */
    void handleCrawlJobResponse(CrawlJobResponse crawlJobResponse);
}
