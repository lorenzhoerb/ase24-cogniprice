from crawler.queue_handler.MessageDataObjects import StoreProductId, CrawledPrice, CrawlJobResponse
from crawler.queue_handler.IMessageQueueProducer import IMessageQueueProducer
from crawler.utils import ConfigReader
from confluent_kafka import Producer

import logging
import json
import time
from typing import Optional
from datetime import datetime, timezone


class KafkaProducer(IMessageQueueProducer):
    def __init__(self, logger: logging.Logger, config: ConfigReader):
        """Initialize KafkaProducer with logger and configuration."""
        self.logger = logger
        self.topic = config.get("Kafka", 'kafka_response_topic')
        self.producer = self._create_producer(config)
        self.logger.debug(f"KafkaProducer initialized with topic: {self.topic}")


    def connect(self) -> bool:
        pass

    def disconnect(self) -> None:
        pass

    def test_connection(self) -> bool:
        pass


    def send_success(self, job_id: StoreProductId, amount: float, currency: str, process_time: int) -> None:
        """Send a success message to the Kafka topic."""
        price: CrawledPrice = CrawledPrice(
            price=amount,
            currency=currency
        )
        crawled_timestamp: float = time.time()
        status: str = "SUCCESS"
        error_message: Optional[str] = None

        # create the message
        message: CrawlJobResponse = self._create_message(job_id, price, process_time, crawled_timestamp, status, error_message)

        try:
            self._send_message(job_id, message)
        except Exception as e:
            self.logger.error("Could not send message to Kafka: " + str(e))

        self.logger.info(f"Success message sent for job_id: {job_id.product_id}")


    def send_failure(self, job_id: StoreProductId, process_time: int, error_msg: str) -> None:
        """Send a failure message to the Kafka topic."""
        crawled_price = CrawledPrice(
            price=None,
            currency=None
        )
        crawled_timestamp = time.time()
        status: str = "FAILURE"
        error_message: str = error_msg

        message: CrawlJobResponse = self._create_message(job_id, crawled_price, process_time, crawled_timestamp, status,
                                                         error_message)

        try:
            self._send_message(job_id, message)
        except Exception as e:
            self.logger.error("Could not send message to Kafka: " + str(e))

        self.logger.info(f"Failure message sent for job_id: {job_id.product_id}")

    def _create_message(self, job_id: StoreProductId, crawled_price: Optional[CrawledPrice],
                        process_time: int,
                        crawled_timestamp: Optional[float],
                        status: str,
                        error_msg: Optional[str]
                        ) -> CrawlJobResponse:
        """Create the message to send to Kafka."""

        ret_response_obj: CrawlJobResponse = CrawlJobResponse(
            job_id=job_id,
            crawledPrice=crawled_price,
            processTime=process_time,
            crawledTimestamp=self._serialize_timestamp(crawled_timestamp),
            status=status,
            errorMessage=error_msg
        )
        return ret_response_obj


    def _serialize_timestamp(self, timestamp: Optional[float]) -> Optional[str]:
        """Convert a timestamp into ISO 8601 format."""
        # if no crawled timestamp has been provided -> set is to None
        if not timestamp:
            return None

        # try to convert the provided time into ISO 8601 format
        try:
            timestamp_str = datetime.fromtimestamp(timestamp, timezone.utc).replace(microsecond=0).isoformat()
            self.logger.debug(f"Serialized timestamp: {timestamp_str}")
        except Exception as e:
            self.logger.error(f"Failed to convert timestamp to ISO 8601 format: {str(e)}")
            return None

        return timestamp_str


    def _send_message(self, job_id: StoreProductId, message_value: CrawlJobResponse):
        """Send the message to the Kafka topic."""
        try:
            message_key = self._serialize_key(job_id)
            self.logger.debug(f"Sending message with key: {message_key}")
        except Exception as e:
            self.logger.error("Failed to serialize message.")
            raise

        # convert CrawlJobResponse object to json
        try:
            json_message: json = self._object_to_json(message_value)
        except Exception as e:
            # pass exception
            raise e

        try:
            self.producer.produce(
                self.topic,
                key=message_key,
                value=json_message.encode("utf-8"),  # The message value as a JSON string
                headers=[('__TypeId__', 'ase.cogniprice.controller.dto.crawler.CrawlJobResponse')]
            )
            self.producer.flush()
        except Exception as e:
            self.logger.error(f"Failed to send message to Kafka: {str(e)}")
            raise e

        self.logger.debug("Message sent and producer flushed.")

    def _object_to_json(self, message: CrawlJobResponse) -> json:
        try:
            ret: json = json.dumps({
            "jobId": {
                "productId": message.job_id.product_id,
                "competitorId": message.job_id.competitor_id
            },
            "crawledPrice": {
                "price": message.crawledPrice.price,
                "currency": message.crawledPrice.currency
            },
            "processTime": message.processTime,
            "crawledTimestamp": message.crawledTimestamp,
            "status": message.status,
            "errorMessage": message.errorMessage
            })
        except Exception as e:
            self.logger.error("Failed to convert CrawlJobResponse to json. Error: " + str(e))
            raise e

        return ret

    def _serialize_key(self, job_id: StoreProductId) -> str:
        """Serialize the message key."""
        try:
            return f"{job_id.product_id}:{job_id.competitor_id}"
        except Exception as e:
            self.logger.error("Failed to serialize StoreProductId for message. Error: " + str(e))
            raise e


    def _create_producer(self, config: ConfigReader) -> Producer:
        """Create and return a Kafka producer instance."""
        kafka_conf = {
            "bootstrap.servers": config.get("Kafka", 'bootstrap_servers'),
            'client.id': 'pycrawler'
        }
        self.logger.debug("Kafka producer created with configuration.")
        return Producer(kafka_conf)
