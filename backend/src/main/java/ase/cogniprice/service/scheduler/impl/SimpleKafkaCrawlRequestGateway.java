package ase.cogniprice.service.scheduler.impl;

import ase.cogniprice.controller.dto.crawler.CrawlJobRequest;
import ase.cogniprice.exception.QueueAccessException;
import ase.cogniprice.exception.QueueDispatchException;
import ase.cogniprice.service.scheduler.CrawlRequestGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * A simple Kafka-based implementation of {@link CrawlRequestGateway} for dispatching crawl job requests.
 *
 * <p>This implementation routes all crawl job requests to a single Kafka topic defined by the
 * configuration property {@code scheduler.crawl.request.topic}. It does not handle politeness
 * considerations (e.g., rate-limiting or host-specific restrictions), leaving such concerns to downstream consumers.</p>
 *
 * <h2>Dispatch Details</h2>
 * <p>Crawl requests are published to Kafka with the following structure:</p>
 * <ul>
 *   <li>Topic: Configured via the {@code scheduler.crawl.request.topic} property.</li>
 *   <li>Key: {@code jobId} of the crawl request.</li>
 *   <li>Value: The serialized {@link CrawlJobRequest} object.</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <p>To customize the Kafka topic name, set the following property in your application configuration:</p>
 * <pre>{@code
 * scheduler.crawl.request.topic=your_custom_topic_name
 * }</pre>
 *
 * @see CrawlRequestGateway
 */
@Slf4j
@Service
@Primary
public class SimpleKafkaCrawlRequestGateway implements CrawlRequestGateway {
    @Value("${scheduler.crawl.request.topic:urls}")
    private String crawlRequestTopic;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SimpleKafkaCrawlRequestGateway(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void dispatchCrawlRequest(CrawlJobRequest crawlJobRequest) throws QueueDispatchException, QueueAccessException {
        log.trace("dispatchCrawlRequest({})", crawlJobRequest);
        try {
            kafkaTemplate.send(crawlRequestTopic, crawlJobRequest.jobId().toString(), crawlJobRequest);
        } catch (Exception e) {
            log.error("Failed to dispatch crawl request for host {}", crawlRequestTopic, e);
            throw new QueueDispatchException("Failed to dispatch crawl request to host: " + crawlRequestTopic, e);
        }
    }
}
