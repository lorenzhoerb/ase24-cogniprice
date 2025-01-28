import csv
import random
import time
from uuid import uuid4
from datetime import datetime
import logging
import json
from crawler.queue_handler import KafkaProducer
from crawler.queue_handler.MessageDataObjects import StoreProductId
from utils import ConfigReader
from shopify_link_extraction import extract_links_for_query

def inject_urls_to_kafka_from_csv(csv_path, max_results=10, extract_collections=False):
    # Set up logging and Kafka producer
    logger = logging.getLogger("test_logger")
    logger.setLevel(logging.INFO)
    logging.basicConfig(level=logging.INFO)  # Simple console logging setup

    config = ConfigReader("config.dev.ini")  # Ensure this file is correctly set up
    producer = KafkaProducer(logger, config)

    # Kafka topic
    topic = config.get("Kafka", "kafka_topic")  # Ensure the topic is specified in config.ini

    try:
        with open(csv_path, mode="r") as csvfile:
            reader = csv.reader(csvfile, delimiter=';')
            next(reader)  # Skip the header row

            for row in reader:
                keyword, allowed_deviation, job_id_start = row
                logger.info(f"Extracting links for keyword: {keyword}")

                # Query using the keyword
                query = f'site:myshopify.com "{keyword}"'
                extracted_urls = list(extract_links_for_query(query, max_results, extract_collections))  # Convert set to list for shuffling
                random.shuffle(extracted_urls)

                # Generate and send crawl job requests
                job_id_prefix = job_id_start  # Ensure the job ID prefix is zero-padded
                job_id_counter = 0  # Start the counter for this keyword

                for url in extracted_urls:
                    try:
                        # Generate job ID
                        job_id = StoreProductId(
                            product_id=int(f"{job_id_prefix}{job_id_counter}"),  # Concatenate prefix with counter
                            competitor_id=int(uuid4().int >> 64)  # Random competitor ID
                        )

                        # Create a job message
                        crawl_job_request = {
                            "jobId": {
                                "productId": job_id.product_id,
                                "competitorId": job_id.competitor_id,
                            },
                            "productUrl": url,
                            "host": url.split("/")[2],  # Extract host from URL
                            "dispatchedTimestamp": datetime.utcnow().isoformat(),
                        }

                        # Serialize and send the message
                        producer.producer.produce(
                            topic=topic,
                            key=str(job_id.product_id),  # Optional: Use product ID as key
                            value=json.dumps(crawl_job_request).encode("utf-8")
                        )
                        producer.producer.flush()  # Ensure the message is sent
                        logger.info(f"Injected job for URL: {url} with Job ID: {job_id.product_id} into Kafka topic: {topic}")

                        # Delay before the next injection
                        time.sleep(7)

                        # Increment job ID
                        job_id_counter += 1

                    except Exception as e:
                        logger.error(f"Failed to inject job for URL: {url}. Error: {str(e)}")

    except Exception as e:
        logger.error(f"Error processing CSV file: {e}")

if __name__ == "__main__":
    inject_urls_to_kafka_from_csv("general_product_keywords.csv", 10, True)
