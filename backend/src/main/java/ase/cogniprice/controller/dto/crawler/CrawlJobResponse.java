package ase.cogniprice.controller.dto.crawler;


import ase.cogniprice.entity.StoreProduct;

import java.time.ZonedDateTime;

public record CrawlJobResponse(
        StoreProduct.StoreProductId jobId,
        MoneyDto crawledPrice,
        Long processTime, // in milliseconds
        ZonedDateTime crawledTimestamp,
        CrawlResponseStatus status,
        String errorMessage
) {
}
