CogniPrice - Web Crawler Documentation
======================================

This project is a **scalable, modular web crawling application designed for extracting and processing price data from various websites**. It provides functionalities for efficient queue handling, robust data fetching, and website-specific parsing, all while adhering to robots.txt rules. The project leverages Kafka (but not limited to) for message queue management, making it well-suited for high-throughput data pipelines.

The documentation structure is organized into distinct modules:

- **Guides**: Lists some guides for how to setup the crawler, add a custom website parser or add a message queue handler.
- **Configuration**: Specifies which configurations can be set for the web crawler.
- **Implemented Website Parsers**: Shows the implemented website parsers for which the crawler can extract product prices.
- **Implemented Message Queue Handlers**: Show the implemented message queue handlers from which the crawler gets crawl jobs and stores crawl job results.
- **Web Crawler Architecture**: Provides an overview of the web crawler architecture.
- **Web Crawler**: Defines basic web crawler functionalities, such as identifying which website is being crawled or to fetch the html page from a website.
- **Web Crawler Utils**: Configuration management, logging, and robots.txt compliance.
- **Parsers**: Defines an interface for parsing the content of websites and implements it for some common Websites like Shopify and WooCommerce.
- **Message Queue Handler**: Defines an interface for the communication with message queues and implements it for Kafka-based message queue operations.


The crawler can be configured via a `config.ini` file and uses a `requirements.txt` file for managing dependencies. Logs are maintained in the `logs` directory, and the main entry point is `main.py`.


.. toctree::
    :caption: Table of Contents
    :maxdepth: 2

    Home <self>
    Guides <howto>
    Configuration <web_crawler/config>
    Implemented Website Parsers <Parsers/impl/impl_parsers>
    Implemented Message Queue Handlers <Queue_Handler/impl/impl_msg_queues>
    crawler_architecture
    web_crawler
    utils
    parsers
    queue_handler