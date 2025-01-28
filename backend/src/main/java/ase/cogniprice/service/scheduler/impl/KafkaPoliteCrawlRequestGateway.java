package ase.cogniprice.service.scheduler.impl;

import ase.cogniprice.controller.dto.crawler.CrawlJobRequest;
import ase.cogniprice.exception.QueueAccessException;
import ase.cogniprice.exception.QueueDispatchException;
import ase.cogniprice.service.scheduler.TopicManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class KafkaPoliteCrawlRequestGateway implements ase.cogniprice.service.scheduler.CrawlRequestGateway {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TopicManagerService topicManagerService;

    public KafkaPoliteCrawlRequestGateway(KafkaTemplate<String, Object> kafkaTemplate,
                                          TopicManagerService topicManagerService) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicManagerService = topicManagerService;
    }

    @Override
    public void dispatchCrawlRequest(CrawlJobRequest crawlJobRequest) throws QueueDispatchException, QueueAccessException {
        log.trace("dispatchCrawlRequest({})", crawlJobRequest);
        if (crawlJobRequest == null || crawlJobRequest.host() == null || crawlJobRequest.host().isEmpty()) {
            throw new IllegalArgumentException("CrawlJobRequest or its host attribute cannot be null/empty.");
        }

        String topicName = crawlJobRequest.host();

        // Ensure the topic exists
        topicManagerService.createTopicIfNotExists(topicName);

        // Dispatch the job to the Kafka topic
        try {
            kafkaTemplate.send(topicName, crawlJobRequest.jobId().toString(), crawlJobRequest).get(); // Ensure the send is successful
        } catch (Exception e) {
            log.error("Failed to dispatch crawl request for host {}", topicName, e);
            throw new QueueDispatchException("Failed to dispatch crawl request to host: " + topicName, e);
        }
    }
}
