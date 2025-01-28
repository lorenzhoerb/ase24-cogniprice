package ase.cogniprice.unitTest.productPriceHandlerTest;

import ase.cogniprice.controller.dto.crawler.CrawlJobResponse;
import ase.cogniprice.controller.dto.crawler.CrawlResponseStatus;
import ase.cogniprice.controller.dto.crawler.MoneyDto;
import ase.cogniprice.entity.StoreProduct;
import ase.cogniprice.service.ProductPriceService;
import ase.cogniprice.service.implementation.KafkaProductPriceHandlerGateway;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class ProductPriceHandlerTest {

    @Mock
    private ProductPriceService productPriceService;

    @InjectMocks
    private KafkaProductPriceHandlerGateway kafkaProductPriceHandlerGateway;

    private CrawlJobResponse validCrawlJobResponse;

    @BeforeEach
    void setUp() {
        // Setting up a valid CrawlJobResponse
        validCrawlJobResponse = new CrawlJobResponse(
                new StoreProduct.StoreProductId(1L, 1L), // mock jobId
                new MoneyDto(BigDecimal.valueOf(20), "EUR"), // valid price and currency
                200L,
                ZonedDateTime.now(), // completedAt
                CrawlResponseStatus.SUCCESS, // status
                null // no error message
        );
    }

    @Test
    void testHandleProductPrice_ValidResponse() {
        // Execute the method with valid data
        kafkaProductPriceHandlerGateway.handleProductPrice(validCrawlJobResponse);

        // Capture the arguments passed to the productPriceService.addPrice method
        ArgumentCaptor<Money> moneyCaptor = ArgumentCaptor.forClass(Money.class);
        ArgumentCaptor<StoreProduct.StoreProductId> productIdCaptor = ArgumentCaptor.forClass(StoreProduct.StoreProductId.class);

        // Verify that addPrice was called with the correct arguments
        verify(productPriceService).addPrice(productIdCaptor.capture(), moneyCaptor.capture(), any());

        // Check that the correct price was passed
        assertEquals(0, BigDecimal.valueOf(20).compareTo(moneyCaptor.getValue().getNumberStripped()));
        assertEquals("EUR", moneyCaptor.getValue().getCurrency().getCurrencyCode());
    }


    @Test
    void testHandleProductPrice_InvalidResponse_NoPrice() {
        // Create an invalid response with null price
        CrawlJobResponse invalidCrawlJobResponse = new CrawlJobResponse(
                new StoreProduct.StoreProductId(1L, 1L),
                null, // null price
                200L,
                ZonedDateTime.now(),
                CrawlResponseStatus.SUCCESS,
                null
        );

        // Execute the method with invalid data
        kafkaProductPriceHandlerGateway.handleProductPrice(invalidCrawlJobResponse);

        // Verify that addPrice was never called due to invalid response
        verify(productPriceService, never()).addPrice(any(), any(), any());
    }

    @Test
    void testHandleProductPrice_CrawlFailure() {
        // Create a CrawlJobResponse with FAILURE status
        CrawlJobResponse failureCrawlJobResponse = new CrawlJobResponse(
                new StoreProduct.StoreProductId(1L, 1L),
                new MoneyDto(BigDecimal.valueOf(20), "EUR"),
                200L,
                ZonedDateTime.now(),
                CrawlResponseStatus.FAILURE, // FAILURE status
                "Crawl failed"
        );

        // Execute the method with FAILURE status
        kafkaProductPriceHandlerGateway.handleProductPrice(failureCrawlJobResponse);

        // Verify that addPrice was never called because of FAILURE status
        verify(productPriceService, never()).addPrice(any(), any(), any());
    }

    @Test
    void testHandleProductPrice_MissingCurrency() {
        // Create an invalid response with null currency
        CrawlJobResponse invalidCrawlJobResponse = new CrawlJobResponse(
                new StoreProduct.StoreProductId(1L, 1L),
                new MoneyDto(BigDecimal.valueOf(20), null), // null currency
                200L,
                ZonedDateTime.now(),
                CrawlResponseStatus.SUCCESS,
                null
        );

        // Execute the method with missing currency
        kafkaProductPriceHandlerGateway.handleProductPrice(invalidCrawlJobResponse);

        // Verify that addPrice was never called due to missing currency
        verify(productPriceService, never()).addPrice(any(), any(), any());
    }

    @Test
    void testHandleProductPrice_NullCrawlJobResponse() {
        // Execute the method with a null response
        kafkaProductPriceHandlerGateway.handleProductPrice(null);

        // Verify that addPrice was never called
        verify(productPriceService, never()).addPrice(any(), any(), any());
    }

    @Test
    void testHandleProductPrice_MissingPrice() {
        // Create an invalid response with null price in MoneyDto
        CrawlJobResponse invalidCrawlJobResponse = new CrawlJobResponse(
                new StoreProduct.StoreProductId(1L, 1L),
                new MoneyDto(null, "EUR"), // null price
                200L,
                ZonedDateTime.now(),
                CrawlResponseStatus.SUCCESS,
                null
        );

        // Execute the method with missing price
        kafkaProductPriceHandlerGateway.handleProductPrice(invalidCrawlJobResponse);

        // Verify that addPrice was never called due to missing price
        verify(productPriceService, never()).addPrice(any(), any(), any());
    }

    @Test
    void testHandleProductPrice_InvalidCurrencySymbol() {
        // Create an invalid response with an unknown or invalid currency symbol
        CrawlJobResponse invalidCrawlJobResponse = new CrawlJobResponse(
                new StoreProduct.StoreProductId(1L, 1L),
                new MoneyDto(BigDecimal.valueOf(20), "INVALID"), // Invalid currency symbol
                200L,
                ZonedDateTime.now(),
                CrawlResponseStatus.SUCCESS,
                null
        );

        // Execute the method with invalid currency
        kafkaProductPriceHandlerGateway.handleProductPrice(invalidCrawlJobResponse);

        // Verify that addPrice was never called due to invalid currency
        verify(productPriceService, never()).addPrice(any(), any(), any());
    }


}
