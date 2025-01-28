package ase.cogniprice.integrationTest.kafkaGatewayIntegrationTest;

import ase.cogniprice.controller.dto.crawler.CrawlJobResponse;
import ase.cogniprice.controller.dto.crawler.CrawlResponseStatus;
import ase.cogniprice.controller.dto.crawler.MoneyDto;
import ase.cogniprice.controller.dto.crawler.SimpleCrawlResponse;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.service.ProductPriceService;
import ase.cogniprice.service.scheduler.impl.SchedulerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Only works when run locally and as a single test
 */
@Disabled
@SpringBootTest
public class KafkaCrawlResponseGatewayIntegrationTest {

    @MockBean
    private SchedulerService schedulerService;

    @MockBean
    private ProductPriceService productPriceService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testKafkaListener() throws Exception {
        StoreProduct.StoreProductId expectedJobId = new StoreProduct.StoreProductId(1L, 1L);
        MoneyDto expectedMoney = new MoneyDto(new BigDecimal("12.0"), "EUR");
        ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        CrawlJobResponse crawlJobResponse = new CrawlJobResponse(
                new StoreProduct.StoreProductId(1L, 1L),
                new MoneyDto(BigDecimal.valueOf(12), "EUR"),
                120L,
                now,
                CrawlResponseStatus.SUCCESS,
                null
        );
        String jsonString = objectMapper.writeValueAsString(crawlJobResponse);


        SimpleCrawlResponse expectedResponse = new SimpleCrawlResponse(
                CrawlResponseStatus.SUCCESS,
                now
        );

        kafkaTemplate.send("crawl.response", "test", crawlJobResponse);

        Thread.sleep(20000);

        verify(schedulerService, times(1)).handleCrawlJobResponse(eq(expectedJobId), eq(expectedResponse));
    }

    @Test
    void testKafkaListener_fromCrawler() throws InterruptedException {
        Thread.sleep(20_000);

        verify(schedulerService, times(1)).handleCrawlJobResponse(any(), any());
    }

}
