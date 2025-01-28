package ase.cogniprice.unitTest.crawlRequestGatewayTest;

import ase.cogniprice.controller.dto.crawler.CrawlJobRequest;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.exception.QueueAccessException;
import ase.cogniprice.exception.QueueDispatchException;
import ase.cogniprice.service.scheduler.TopicManagerService;
import ase.cogniprice.service.scheduler.impl.KafkaPoliteCrawlRequestGateway;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CrawRequestGatewayTest {

    private static final String HOST = "prosoundeurope.com";
    private static final String PRODUCT_URL = "prosoundeurope.com/products/1";

    @InjectMocks
    private KafkaPoliteCrawlRequestGateway crawlRequestGateway;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private TopicManagerService topicManagerService;

    @Test
    void testDispatchCrawlRequest_nullCrawJobRequest_expectIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            crawlRequestGateway.dispatchCrawlRequest(null);
        });
    }

    @Test
    void testDispatchCrawlRequest_emptyHost_expectIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            crawlRequestGateway.dispatchCrawlRequest(getCrawlJobRequest(PRODUCT_URL, ""));
        });
    }

    @Test
    void testDispatchCrawlRequest_kafkaSendThrowsException_expectQueueDispatchException() throws Exception {
        CrawlJobRequest crawlJobRequest = getCrawlJobRequest(PRODUCT_URL, HOST);

        // Mock the createTopicIfNotExists method to do nothing
        doNothing().when(topicManagerService).createTopicIfNotExists(HOST);

        // Simulate an exception when Kafka send is called
        when(kafkaTemplate.send(anyString(), any(String.class), any(CrawlJobRequest.class))).thenThrow(new RuntimeException("Kafka error", new Throwable()));

        // Assert that the QueueDispatchException is thrown
        assertThrows(QueueDispatchException.class, () -> {
            crawlRequestGateway.dispatchCrawlRequest(crawlJobRequest);
        });

        // Verify that the topic was created but Kafka send failed
        verify(topicManagerService, times(1)).createTopicIfNotExists(HOST);
        verify(kafkaTemplate, times(1)).send(HOST, crawlJobRequest.jobId().toString(), crawlJobRequest);
    }

    @Test
    void testDispatchCrawlRequest_topicManagerThrowsException_expectQueueAccessException() throws Exception {
        CrawlJobRequest crawlJobRequest = getCrawlJobRequest(PRODUCT_URL, HOST);

        // Simulate an exception when creating the topic
        doThrow(new QueueAccessException("Topic creation error")).when(topicManagerService).createTopicIfNotExists(HOST);

        // Assert that the QueueAccessException is thrown
        assertThrows(QueueAccessException.class, () -> {
            crawlRequestGateway.dispatchCrawlRequest(crawlJobRequest);
        });

        // Verify that the topic creation was attempted
        verify(topicManagerService, times(1)).createTopicIfNotExists(HOST);
    }

    CrawlJobRequest getCrawlJobRequest(String productUrl, String host) {
        return new CrawlJobRequest(
                new StoreProduct.StoreProductId(1L, 1L),
                productUrl,
                host,
                ZonedDateTime.now()
        );
    }
}
