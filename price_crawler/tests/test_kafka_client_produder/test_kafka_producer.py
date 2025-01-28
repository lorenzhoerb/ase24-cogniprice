import unittest
from unittest.mock import Mock, patch, MagicMock
from datetime import datetime, timezone
import time
import json

from confluent_kafka import Producer

from crawler.queue_handler.MessageDataObjects import StoreProductId, CrawledPrice, CrawlJobResponse
from crawler.utils import ConfigReader
from crawler.queue_handler.KafkaProducer import KafkaProducer


class TestKafkaProducer(unittest.TestCase):

    def setUp(self):
        # Mock logger
        self.mock_logger = Mock()

        # Mock ConfigReader
        self.mock_config = Mock()
        self.mock_config.get.side_effect = lambda section, key: {
            ("Kafka", "kafka_response_topic"): "test_topic",
            ("Kafka", "bootstrap_servers"): "localhost:9092"
        }.get((section, key))

        # Instantiate KafkaProducer with mocked dependencies
        self.kafka_producer = KafkaProducer(self.mock_logger, self.mock_config)
        self.kafka_producer.producer = Mock()  # Mock Kafka Producer

    def test_serialize_key(self):
        """Test _serialize_key method."""
        job_id = StoreProductId(product_id="123", competitor_id="456")
        result = self.kafka_producer._serialize_key(job_id)
        self.assertEqual(result, "123:456")
        self.mock_logger.error.assert_not_called()

    def test_object_to_json(self):
        """Test _object_to_json method."""
        job_id = StoreProductId(product_id="123", competitor_id="456")
        crawled_price = CrawledPrice(price=10.99, currency="USD")
        response = CrawlJobResponse(
            job_id=job_id,
            crawledPrice=crawled_price,
            processTime=1500,
            crawledTimestamp="2023-12-12T12:00:00Z",
            status="SUCCESS",
            errorMessage=None
        )
        json_result = self.kafka_producer._object_to_json(response)
        expected_result = json.dumps({
            "jobId": {
                "productId": "123",
                "competitorId": "456"
            },
            "crawledPrice": {
                "price": 10.99,
                "currency": "USD"
            },
            "processTime": 1500,
            "crawledTimestamp": "2023-12-12T12:00:00Z",
            "status": "SUCCESS",
            "errorMessage": None
        })
        self.assertEqual(json_result, expected_result)

    def test_serialize_timestamp(self):
        """Test _serialize_timestamp method."""
        timestamp = time.time()
        iso_format = datetime.fromtimestamp(timestamp, timezone.utc).replace(microsecond=0).isoformat()
        result = self.kafka_producer._serialize_timestamp(timestamp)
        self.assertEqual(result, iso_format)

        # Test with None
        result = self.kafka_producer._serialize_timestamp(None)
        self.assertIsNone(result)

    @patch("crawler.queue_handler.KafkaProducer.Producer")
    def test_send_message(self, mock_producer_class):
        """Test _send_message method."""
        mock_producer = mock_producer_class.return_value  # Mock the producer instance
        mock_producer.produce = Mock()  # Mock the produce method
        mock_producer.flush = Mock()  # Mock the flush method

        # Mock input data
        job_id = StoreProductId(product_id="123", competitor_id="456")
        crawled_price = CrawledPrice(price=10.99, currency="USD")
        message = CrawlJobResponse(
            job_id=job_id,
            crawledPrice=crawled_price,
            processTime=1500,
            crawledTimestamp="2023-12-12T12:00:00Z",
            status="SUCCESS",
            errorMessage=None
        )

        # Create an instance of KafkaProducer
        logger_mock = Mock()
        config_mock = Mock()
        config_mock.get = Mock(side_effect=lambda section, key: {
            ("Kafka", "kafka_response_topic"): "test_topic",
            ("Kafka", "bootstrap_servers"): "localhost:9092"
        }[(section, key)])

        kafka_producer = KafkaProducer(logger=logger_mock, config=config_mock)

        # Call the method under test
        kafka_producer._send_message(job_id, message)

        # Assert produce was called with the correct parameters
        mock_producer.produce.assert_called_once_with(
            "test_topic",
            key="123:456",
            value=json.dumps({
                "jobId": {"productId": "123", "competitorId": "456"},
                "crawledPrice": {"price": 10.99, "currency": "USD"},
                "processTime": 1500,
                "crawledTimestamp": "2023-12-12T12:00:00Z",
                "status": "SUCCESS",
                "errorMessage": None
            }).encode("utf-8"),
            headers=[('__TypeId__', 'ase.cogniprice.controller.dto.crawler.CrawlJobResponse')]
        )

        # Assert flush was called
        mock_producer.flush.assert_called_once()


    def test_send_success(self):
        """Test send_success method."""
        job_id = StoreProductId(product_id="123", competitor_id="456")
        self.kafka_producer._send_message = Mock()

        self.kafka_producer.send_success(job_id, 10.99, "USD", 1500)
        self.kafka_producer._send_message.assert_called_once()

    def test_send_failure(self):
        """Test send_failure method."""
        job_id = StoreProductId(product_id="123", competitor_id="456")
        self.kafka_producer._send_message = Mock()

        self.kafka_producer.send_failure(job_id, 1500, "Error occurred")
        self.kafka_producer._send_message.assert_called_once()

    def test_create_producer(self):
        """Test _create_producer method."""
        producer = self.kafka_producer._create_producer(self.mock_config)
        self.assertIsInstance(producer, Producer)

    def test_logging_on_errors(self):
        """Test logging of errors in _send_message."""
        job_id = StoreProductId(product_id="123", competitor_id="456")
        crawled_price = CrawledPrice(price=10.99, currency="USD")
        message = CrawlJobResponse(
            job_id=job_id,
            crawledPrice=crawled_price,
            processTime=1500,
            crawledTimestamp="2023-12-12T12:00:00Z",
            status="SUCCESS",
            errorMessage=None
        )

        # Simulate an exception during the produce call
        self.kafka_producer.producer.produce = Mock(side_effect=Exception("Kafka error"))

        with self.assertRaises(Exception):
            self.kafka_producer._send_message(job_id, message)

        # Assert the error message is logged correctly
        self.mock_logger.error.assert_any_call("Failed to send message to Kafka: Kafka error")


if __name__ == "__main__":
    unittest.main()
