package ase.cogniprice.service.implementation;

import ase.cogniprice.controller.dto.crawler.CrawlJobResponse;
import ase.cogniprice.controller.dto.crawler.CrawlResponseStatus;
import ase.cogniprice.controller.dto.crawler.MoneyDto;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.service.PricingRuleProcessorService;
import ase.cogniprice.service.ProductPriceHandlerGateway;
import ase.cogniprice.service.ProductPriceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProductPriceHandlerGateway implements ProductPriceHandlerGateway {

    private static final String DEFAULT_GROUP_ID = "product-price-handler";
    private static final String GROUP_ID_VALUE_EXPRESSION = "${scheduler.crawl.response.price.groupId:" + DEFAULT_GROUP_ID + "}";

    private static final String DEFAULT_TOPIC = "crawl.response";
    private static final String TOPIC_VALUE_EXPRESSION = "${scheduler.crawl.response.topic:" + DEFAULT_TOPIC + "}";

    @Value(GROUP_ID_VALUE_EXPRESSION)
    private String groupId;
    @Value(TOPIC_VALUE_EXPRESSION)
    private String topic;

    private final ProductPriceService productPriceService;
    private final PricingRuleProcessorService  pricingRuleProcessorService;

    @PostConstruct
    public void init() {
        log.info("KafkaProductPriceHandlerGateway initialized with listener settings: [groupId='{}', topic='{}']",
                groupId, topic);
    }

    @KafkaListener(
            id = "productPriceHandler",
            groupId = GROUP_ID_VALUE_EXPRESSION,
            topics = TOPIC_VALUE_EXPRESSION
    )
    @Override
    public void handleProductPrice(CrawlJobResponse crawlJobResponse) {
        log.info("Received CrawlJobResponse: {}", crawlJobResponse);
        if (!isValid(crawlJobResponse)) {
            return;
        }

        StoreProduct.StoreProductId jobId = crawlJobResponse.jobId();
        MoneyDto crawlPriceDto = crawlJobResponse.crawledPrice();

        try {
            Money crawlPrice = Money.of(crawlPriceDto.price(), crawlPriceDto.currency());
            productPriceService.addPrice(jobId, crawlPrice, crawlJobResponse.crawledTimestamp().toLocalDateTime());
            // Trigger price rule check
            pricingRuleProcessorService.evaluateAndTriggerRulesForProduct(jobId.getProductId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private boolean isValid(CrawlJobResponse crawlJobResponse) {
        if (crawlJobResponse == null) {
            log.warn("CrawlJobResponse is null. Ignoring.");
            return false;
        }
        if (crawlJobResponse.crawledPrice() == null) {
            log.warn("CrawledPrice is null. Ignoring.");
            return false;
        }
        if (crawlJobResponse.crawledPrice().price() == null) {
            log.warn("Price is null. Ignoring.");
            return false;
        }
        if (crawlJobResponse.crawledPrice().currency() == null) {
            log.warn("Currency is null. Ignoring.");
            return false;
        }
        if (crawlJobResponse.crawledTimestamp() == null) {
            log.warn("Completed timestamp is null. Ignoring.");
            return false;
        }

        if (crawlJobResponse.status() == CrawlResponseStatus.FAILURE) {
            log.debug("Crawl response status is FAILURE. Ignoring.");
            return false;
        }

        return true;
    }
}
