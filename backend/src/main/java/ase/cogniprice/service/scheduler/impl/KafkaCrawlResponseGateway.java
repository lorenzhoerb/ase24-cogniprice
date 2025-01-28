package ase.cogniprice.service.scheduler.impl;

import ase.cogniprice.controller.dto.crawler.CrawlJobResponse;
import ase.cogniprice.controller.dto.crawler.CrawlResponseStatus;
import ase.cogniprice.controller.dto.crawler.SimpleCrawlResponse;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.service.scheduler.CrawlResponseGateway;
import ase.cogniprice.service.scheduler.SchedulerService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Profile("!ci")
@Slf4j
@Service
public class KafkaCrawlResponseGateway implements CrawlResponseGateway {

    private static final String DEFAULT_GROUP_ID = "scheduler-response-handler";
    private static final String GROUP_ID_VALUE_EXPRESSION = "${scheduler.crawl.response.queue.groupId:" + DEFAULT_GROUP_ID + "}";

    private static final String DEFAULT_TOPIC = "crawl.response";
    private static final String TOPIC_VALUE_EXPRESSION = "${scheduler.crawl.response.topic:" + DEFAULT_TOPIC + "}";

    @Value(GROUP_ID_VALUE_EXPRESSION)
    private String groupId;
    @Value(TOPIC_VALUE_EXPRESSION)
    private String topic;

    private final ase.cogniprice.service.scheduler.SchedulerService schedulerService;

    public KafkaCrawlResponseGateway(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostConstruct
    public void init() {
        log.info("KafkaCrawlResponseGateway initialized with listener settings: [groupId='{}', topic='{}']",
                groupId, topic);
    }

    @KafkaListener(
            id = "crawlResponseListener",
            groupId = GROUP_ID_VALUE_EXPRESSION,
            topics = TOPIC_VALUE_EXPRESSION
    )
    @Override
    public void handleCrawlJobResponse(CrawlJobResponse crawlJobResponse) {
        if (crawlJobResponse == null) {
            log.warn("Received null CrawlJobResponse. Ignoring.");
            return;
        }

        log.info("Received CrawlJobResponse: {}", crawlJobResponse);

        StoreProduct.StoreProductId jobId = crawlJobResponse.jobId();
        SimpleCrawlResponse simpleCrawlResponse = new SimpleCrawlResponse(CrawlResponseStatus.SUCCESS, crawlJobResponse.crawledTimestamp());

        try {
            schedulerService.handleCrawlJobResponse(jobId, simpleCrawlResponse);
            log.trace("CrawlJobResponse processed successfully for jobId: {}", jobId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments provided for CrawlJobResponse: {}. Error: {}", crawlJobResponse, e.getMessage(), e);
        } catch (NotFoundException e) {
            log.error("Job not found for jobId: {} in CrawlJobResponse: {}. Error: {}", jobId, crawlJobResponse, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error processing CrawlJobResponse: {}", crawlJobResponse, e);
        }
    }
}
