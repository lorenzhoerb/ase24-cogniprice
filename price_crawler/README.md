# CogniPrice Web Crawler

CogniPrice is a web crawler designed for price extraction from various online platforms. 
The crawler is designed to be scalable and flexible, supporting Kafka for message queuing.

## Quick Start
1. Install requirements
```shell
pip install -r requirements.txt
```

2. Run the crawler
```shell
python main.py
```

## Requirements

- Python 3.10
- Kafka (Local or Remote)

The dependencies can be installed using requirements.txt:

```shell
pip install -r requirements.txt
```

## Config.ini Properties

| Section       | Property                | Description                                                            | Example                          |
|---------------|-------------------------|------------------------------------------------------------------------|----------------------------------|
| **General**   | `crawler_name`           | The name of the crawler.                                                | `CogniPrice`                     |
|               | `robots_crawl_timeout`   | Timeout for crawling `robots.txt` in seconds.                           | `7`                              |
| **Kafka**     | `bootstrap_servers`      | Kafka broker addresses.                                                | `127.0.0.1:9092`                 |
|               | `kafka_topic`            | The Kafka topic to subscribe to for URL crawling jobs.                  | `urls`                           |
|               | `kafka_response_topic`   | The Kafka topic to send crawl results to.                               | `crawl.response`                 |
| **Crawler**   | `robots_txt_cache_time`  | Cache time for `robots.txt` in seconds.                                 | `600`                            |
| **Selenium**  | `page_load_timeout`      | Timeout for page load in milliseconds.                                  | `15000` (15 seconds)             |

