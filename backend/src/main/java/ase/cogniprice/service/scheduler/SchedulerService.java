package ase.cogniprice.service.scheduler;

import ase.cogniprice.controller.dto.crawler.SimpleCrawlResponse;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.exception.NotFoundException;

/**
 * Service interface for scheduling, dispatching and managing URL price crawling jobs.
 * This service executes a regular cron job to find and dispatch URLs
 * that are scheduled for price crawling.
 *
 * @author Lorenz
 */
public interface SchedulerService {

    /**
     * Attempts to start a crawl job by setting its state to `SCHEDULED` if it is currently `PAUSED`.
     *
     * <p>If the job is already in the `SCHEDULED` or `IN_PROGRESS` state, this method does nothing
     * and returns `false`.
     *
     * <p>When successfully restarted, the job’s state is set to `SCHEDULED`, and a new `next_crawl`
     * time is assigned.
     *
     * @param jobId the ID of the job to be restarted
     *
     * @return `true` if the job was successfully restarted from `PAUSED`, `false` if the job was
     *     already `SCHEDULED` or `IN_PROGRESS` or if it could not be restarted.
     *
     * @throws IllegalArgumentException if `jobId` is null.
     * @throws NotFoundException        if the job with `jobId` could not be found
     */
    boolean startJobById(StoreProduct.StoreProductId jobId);

    /**
     * Pauses a crawl job by setting its state to `PAUSED`.
     *
     * <p>If the job's current state is `SCHEDULED`, it will be immediately paused.
     * If the job is currently `IN_PROGRESS`, it will wait for the crawl response
     * to be processed, after which its state will be set to `PAUSED`.
     *
     * <p>This method ensures that a crawl job does not continue processing once
     * it has been paused. It returns `true` if the job's state was successfully changed
     * to `PAUSED`, and `false` if the job was already in the `PAUSED` state or if
     * the pause operation could not be performed.
     *
     * @param jobId the ID of the crawl job to be paused
     * @return `true` if the job was successfully paused, `false` if the job
     *     was already in the `PAUSED` state or if the pause operation
     *     could not be applied.
     * @throws IllegalArgumentException if `jobId` is null.
     * @throws NotFoundException        if the job with `jobId` could not be found
     */
    boolean pauseJobById(StoreProduct.StoreProductId jobId);

    /**
     * Dispatches scheduled crawl jobs to a worker queue based on their schedule and current state.
     *
     * <p>This method identifies all crawl jobs that meet the following criteria:
     * <ul>
     *   <li>They are marked as `SCHEDULED`.</li>
     *   <li>Their `next_crawl` time is due (i.e., `next_crawl` time is less than or equal to the current time).</li>
     * </ul>
     *
     * <p>The method processes these jobs in order of their `next_crawl` times, up to an optional maximum number of jobs specified by `limit`.
     * If no limit is provided (or if the limit is `null`), all eligible jobs are dispatched.
     *
     * <p>For each job dispatched:
     * <ul>
     *   <li>The job's state is updated to `IN_PROGRESS`, indicating that it is currently being processed.</li>
     *   <li>If dispatching fails (e.g., due to an unavailable worker queue), the job's `retry_attempts` counter is incremented and the next_crawl is updated.</li>
     *   <li>If the job's `retry_attempts` exceed a predefined maximum, the job's state is changed to `PAUSED` to prevent further dispatch attempts until manually restarted.</li>
     * </ul>
     *
     * <p>Edge cases handled include:
     * <ul>
     *   <li>If `limit` is set to a negative value, an {@code IllegalArgumentException} is thrown.</li>
     *   <li>If the job is already in the `IN_PROGRESS` or `PAUSED` state, it is skipped in the dispatch queue.</li>
     * </ul>
     *
     * @param limit the maximum number of jobs to dispatch in one call. If `limit` is null or omitted, all eligible jobs are dispatched.
     * @throws IllegalArgumentException if `limit` is less than 0.
     */
    void dispatchScheduledCrawlJobs(Integer limit);

    /**
     * Handles the response from a crawl job.
     *
     * <p>If the response is successful:
     * <ul>
     *     <li>Updates the job's `last_crawl` date to reflect the crawled date.</li>
     *     <li>Updates the job's `next_crawl` date to reflect the next scheduled time.</li>
     *     <li>Sets the job state to `SCHEDULED`.</li>
     * </ul>
     *
     * <p>If the response indicates a failure:
     * <ul>
     *     <li>Increments the job's `retryAttempts` counter.</li>
     *     <li>If the retry limit is not yet reached, reschedules the job.</li>
     *     <li>If the retry limit is reached, sets the job state to `PAUSED`.</li>
     * </ul>
     *
     * @param jobId the unique identifier of the crawl job
     * @param crawlResponse the crawl response of the crawl job response (status SUCCESS or FAILURE and crawled timestamp)
     *
     * @throws IllegalArgumentException if `jobId` or `status` is null or if the crawledDate is in the future
     * @throws NotFoundException        if the job with `jobId` could not be found
     */
    void handleCrawlJobResponse(StoreProduct.StoreProductId jobId, SimpleCrawlResponse crawlResponse);
}
