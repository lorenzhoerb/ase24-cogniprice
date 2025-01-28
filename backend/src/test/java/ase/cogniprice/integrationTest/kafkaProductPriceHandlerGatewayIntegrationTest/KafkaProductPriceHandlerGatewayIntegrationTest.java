package ase.cogniprice.integrationTest.kafkaProductPriceHandlerGatewayIntegrationTest;

import ase.cogniprice.controller.dto.crawler.CrawlJobResponse;
import ase.cogniprice.controller.dto.crawler.CrawlResponseStatus;
import ase.cogniprice.controller.dto.crawler.MoneyDto;
import ase.cogniprice.controller.dto.crawler.SimpleCrawlResponse;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.service.ProductPriceService;
import ase.cogniprice.service.scheduler.SchedulerService;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Only works when run locally and as a single test
 */
@Disabled
@SpringBootTest
public class KafkaProductPriceHandlerGatewayIntegrationTest {
    @MockBean
    private ProductPriceService productPriceService;

    @MockBean
    private SchedulerService schedulerService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void testHandleProductPriceListener() throws InterruptedException {
        StoreProduct.StoreProductId expectedJobId = new StoreProduct.StoreProductId(1L, 1L);
        MoneyDto money = new MoneyDto(new BigDecimal("12.0"), "EUR");
        Money expectedMoney = Money.of(money.price(), money.currency());
        ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        CrawlJobResponse crawlJobResponse = new CrawlJobResponse(
                expectedJobId,
                money,
                200L,
                now,
                CrawlResponseStatus.SUCCESS,
                null
        );

        kafkaTemplate.send("crawl.response", "1:1", crawlJobResponse);

        Thread.sleep(10_000);

        verify(productPriceService, times(1)).addPrice(eq(expectedJobId), eq(expectedMoney), eq(now.toLocalDateTime()));

    }
}
