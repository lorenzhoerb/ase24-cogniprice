package ase.cogniprice.controller.dto.crawler;

import java.time.ZonedDateTime;

public record SimpleCrawlResponse(
       CrawlResponseStatus status,
        ZonedDateTime crawledDate
){
}
