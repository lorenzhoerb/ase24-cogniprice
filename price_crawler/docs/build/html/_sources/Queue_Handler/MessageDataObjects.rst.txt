Data Structures for a Message Object
====================================

* `StoreProductId <#crawler.queue_handler.MessageDataObjects.StoreProductId>`_ represents a composite ID combining `product_id` and `competitor_id`.
* `CrawlJobRequest <#crawler.queue_handler.MessageDataObjects.CrawlJobRequest>`_ represents a job request containing the `StoreProductId`, product URL, host, and dispatched timestamp.
* `CrawledPrice <#crawler.queue_handler.MessageDataObjects.CrawledPrice>`_ contains the crawled price and its associated currency.
* `CrawlJobResponse <#crawler.queue_handler.MessageDataObjects.CrawlJobResponse>`_ represents the response for a crawl job, including the `StoreProductId`, crawled price, processing time, status, and any error message.

.. automodule:: crawler.queue_handler.MessageDataObjects
