package ase.cogniprice.service.scheduler;

import ase.cogniprice.controller.dto.crawler.CrawlJobRequest;
import ase.cogniprice.exception.QueueAccessException;
import ase.cogniprice.exception.QueueDispatchException;

/**
 * Interface for dispatching price crawl job requests to a specified host worker queue.
 *
 * @author Lorenz
 */
public interface CrawlRequestGateway {

    /**
     * Dispatches a crawl job request to the specified host worker queue and manages the host heap if
     * the job’s host-specific queue is empty, the host’s timeout status is refreshed, and the job’s `next_crawl` time is set to the current time.
     *
     * <p>This method retrieves the queue specified in the `crawlJobRequest` host attribute
     * and routes the job to that queue. If the specified queue does not already exist, it
     * will be created. Successful dispatch marks the request for processing by the
     * corresponding worker service.
     *
     * <p><b>Note:</b> The `host` attribute in `crawlJobRequest` is mandatory and must
     * contain a valid target host. If this attribute is missing or invalid, the method
     * will throw an exception.
     *
     * @param crawlJobRequest the crawl job request containing details of the job and
     *                        the target host queue
     * @throws IllegalArgumentException if `crawlJobRequest` or its `host` attribute is
     *                                  null or invalid
     * @throws QueueAccessException     if there is an issue accessing or creating the host
     *                                  queue (e.g., network or configuration issues)
     * @throws QueueDispatchException   if the dispatching process fails (e.g., due to
     *                                  queue capacity limits or unexpected runtime errors)
     */
    void dispatchCrawlRequest(CrawlJobRequest crawlJobRequest) throws QueueDispatchException, QueueAccessException;
}
