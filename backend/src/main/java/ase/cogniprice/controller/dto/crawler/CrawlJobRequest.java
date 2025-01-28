package ase.cogniprice.controller.dto.crawler;

import ase.cogniprice.entity.StoreProduct;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;

public record CrawlJobRequest(
        StoreProduct.StoreProductId jobId,
        String productUrl,
        String host,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        ZonedDateTime dispatchedTimestamp
) {
}
