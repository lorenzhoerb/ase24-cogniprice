from dataclasses import dataclass
from typing import Optional

@dataclass
class StoreProductId:
    """
    Represents the ID of a store product, combining productId and competitorId.
    """
    product_id: int
    competitor_id: int


@dataclass
class CrawlJobRequest:
    """
    Represents a job request received from the message queue.
    """
    job_id: StoreProductId  # Composite ID combining product and competitor IDs
    product_url: str        # The URL of the product to crawl
    host: str               # The host of the product URL
    dispatched_timestamp: str  # Timestamp of when the job was dispatched

@dataclass
class CrawledPrice:
    """
    Represents a crawled price object
    """
    price: Optional[float]
    currency: Optional[str]

@dataclass
class CrawlJobResponse:
    """
    Represents the response for a crawl job.
    """
    job_id: StoreProductId  # Composite ID combining product and competitor IDs
    crawledPrice: Optional[CrawledPrice] # Crawled price; in case of an error None is returned
    processTime: int # Time it took to crawl the price; in case of an error None is returned
    crawledTimestamp: Optional[str] # Timestamp when the product price was crawled; in case of an error None is returned
    status: str # Status message of the crawl process
    errorMessage: Optional[str] # In case of an error: The error message why the crawl was unsuccessful; None: if not error occurred