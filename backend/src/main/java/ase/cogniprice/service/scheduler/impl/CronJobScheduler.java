package ase.cogniprice.service.scheduler.impl;

import ase.cogniprice.service.scheduler.SchedulerService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * CronJobScheduler is responsible for periodically dispatching crawl jobs based on a configurable cron expression.
 * It is designed to ensure that scheduled jobs are executed regularly in a controlled manner.
 *
 * <p>The cron expression can be customized through the configuration property {@code scheduler.scheduling.cronExpression}.
 * If no value is provided, a default cron expression ("0 0/1 * * * ?") is used, which triggers the jobs every minute.
 * Additionally, the maximum number of crawl jobs dispatched in a single execution can be configured via the
 * {@code scheduling.scheduler.crawlLimit} property, with a default value of 100.
 *
 * <p>This service is only activated in the profiles where the cron expression is explicitly enabled, typically for
 * testing or production environments, excluding CI or test-specific configurations.
 *
 * <p>The scheduler runs at fixed intervals as defined by the cron expression, triggering the crawl job dispatching
 * operation at each interval.
 */
@Slf4j
@Service
@Profile("(cron | !test) & ( cron | !ci)")
// This component should only get initiated for tests that are explicitly declare the cron profile and for the default profile
public class CronJobScheduler {

    private static final String DEFAULT_CRON_EXPRESSION = "0 0/1 * * * ?"; // Default is every full minute
    private static final String SCHEDULING_VALUE_EXPRESSION = "${scheduler.scheduling.cronExpression:" + DEFAULT_CRON_EXPRESSION + "}";

    @Value("${scheduling.scheduler.crawlLimit:100}") // Default limit is 100
    private Integer crawlLimit;

    @Value(SCHEDULING_VALUE_EXPRESSION)
    private String scheduling;

    private final SchedulerService schedulerService;

    public CronJobScheduler(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostConstruct
    public void init() {
        log.info("CronJobScheduler initialized with cron schedule: '{}' and crawl limit: {}", scheduling, crawlLimit);
    }

    @Scheduled(cron = SCHEDULING_VALUE_EXPRESSION)
    private void dispatchScheduledJobs() {
        log.info("Dispatching scheduled crawl jobs (crawl limit: {})", crawlLimit);
        schedulerService.dispatchScheduledCrawlJobs(crawlLimit);
    }

}
