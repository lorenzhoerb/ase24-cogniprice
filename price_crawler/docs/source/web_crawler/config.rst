Web Crawler Configuration
=========================

.. _config_label:

| This page outlines the configuration settings required for setting up and operating the web crawler. It is divided into multiple sections, each focusing on a specific aspect of the crawler's functionality. Each setting includes its type, whether it is required, and examples to guide implementation.
| This documentation serves as a comprehensive reference for configuring the web crawler to suit specific needs.

.. _config-file_label:

The configuration file for the web crawler is located at: **24ws-ase-pr-qse-02/price_crawler/config.ini**

=========
[General]
=========

.. confval:: crawler_name

    :type: string
    :required: yes
    :example: CogniPrice

    The useragent name of the web crawler which is sent along with each request the crawler makes.

.. confval:: robots_crawl_timeout

    :type: integer
    :required: yes
    :example: 7

    Specifies the time limit, in seconds, for how long the connection should wait before giving up when trying to access the robots.txt file for a website.


=======
[Kafka]
=======
This configuration section is only required when working with the provided Kafka Client and Kafka Producer implementations.

.. confval:: bootstrap_servers

    :type: string
    :required: yes
    :example: 127.0.0.1:9092

    Specifies the address (IP and port) of the Kafka broker that the client connects to, to discover the Kafka cluster.

.. confval:: kafka_topic

    :type: string
    :required: yes
    :example: urls

    Specifies the Kafka topic name where messages (urls which should be crawled) will be consumed.

.. confval:: kafka_response_topic

    :type: string
    :required: yes
    :example: crawl.response

    Specifies the Kafka topic name where the crawl job results will be published.

=========
[Crawler]
=========

.. confval:: robots_txt_cache_time

    :type: integer
    :required: yes
    :example: 600

    Specifies time in seconds for how long a cached entry for the robots.txt allowance/disallowance should be valid.

.. confval:: gemini_api_key

    :type: string
    :required: yes
    :example: Ouza87bb2MeRrwbb-NAEdIDugoihr_nyaabP9Yw

    Specifies the api key for the gemini ai.

==========
[Selenium]
==========

.. confval:: page_load_timeout

    :type: integer
    :required: yes
    :example: 15000

    Specifies the time limit, in milliseconds, for how long the connection should wait before giving up when trying to download the html page of a website.