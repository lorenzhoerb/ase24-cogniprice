package ase.cogniprice.service;

import ase.cogniprice.controller.dto.crawler.CrawlJobResponse;

/**
 * A gateway for handling product price data from a crawler.
 * This interface defines a method for processing crawl responses and updating
 * the price information of the associated store product based on the response.
 */
public interface ProductPriceHandlerGateway {

    /**
     * Processes the crawl response and updates the price of the corresponding store product.
     * If the crawl response is successful, the crawled price is saved to the store product with the matching product ID.
     *
     * @param crawlJobResponse the response from the crawl job containing price data and status information
     */
    void handleProductPrice(CrawlJobResponse crawlJobResponse);

}
