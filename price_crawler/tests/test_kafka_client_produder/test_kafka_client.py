import json
import unittest
from unittest.mock import Mock, patch
from confluent_kafka import Consumer, KafkaException
from crawler.queue_handler.KafkaClient import KafkaClient
import logging
from jsonschema.exceptions import ValidationError

from crawler.queue_handler.MessageDataObjects import CrawlJobRequest


import unittest
from unittest.mock import patch
import json
from crawler.queue_handler.MessageDataObjects import CrawlJobRequest, StoreProductId

class TestKafkaClient(unittest.TestCase):
    @patch('crawler.queue_handler.KafkaClient.json.loads')
    @patch('crawler.queue_handler.KafkaClient.validate')
    def test_extract_crawl_job_success(self, mock_validate, mock_json_loads):
        """Test extracting a valid crawl job from a Kafka message."""
        mock_json_loads.return_value = {
            'jobId': {'productId': 123, 'competitorId': 456},
            'productUrl': 'http://example.com',
            'host': 'example',
            'dispatchedTimestamp': '2025-01-01T12:00:00Z'
        }
        mock_validate.return_value = None  # Simulate successful validation

        raw_message = json.dumps(mock_json_loads.return_value).encode('utf-8')

        # Call the method
        result = self.kafka_client._extract_crawl_job_from_value(raw_message)

        # Validate the result is of the correct type and contains the correct values
        self.assertIsInstance(result, CrawlJobRequest)
        self.assertEqual(result.job_id.product_id, 123)
        self.assertEqual(result.job_id.competitor_id, 456)
        self.assertEqual(result.product_url, 'http://example.com')
        self.assertEqual(result.host, 'example')
        self.assertEqual(result.dispatched_timestamp, '2025-01-01T12:00:00Z')



class TestKafkaClient(unittest.TestCase):

    def setUp(self):
        self.mock_logger = Mock(spec=logging.Logger)
        self.mock_config = Mock()

        # Mock configuration values
        self.mock_config.get.side_effect = lambda section, key: {
            ('Kafka', 'bootstrap_servers'): 'localhost:9092',
            ('Kafka', 'kafka_topic'): 'test_topic'
        }[(section, key)]

        # Mock the validate_config_object method in KafkaClient
        with patch('crawler.queue_handler.KafkaClient.QueueHandlerHelper.validate_config_object') as mock_validate:
            mock_validate.return_value = self.mock_config
            self.kafka_client = KafkaClient(self.mock_logger, self.mock_config)

    @patch('crawler.queue_handler.KafkaClient.Consumer')
    def test_connect_success(self, mock_consumer):
        """Test successful Kafka connection."""

        # Mock the list_topics to simulate a successful response
        mock_cluster_metadata = Mock()
        mock_cluster_metadata.topics = {'test_topic': Mock()}
        mock_consumer.return_value.list_topics.return_value = mock_cluster_metadata

        # Call the connect method
        result = self.kafka_client.connect()

        # Assertions
        self.assertTrue(result)
        mock_consumer.assert_called_once_with(self.kafka_client.kafka_conf)
        mock_consumer.return_value.list_topics.assert_called_once_with(timeout=6)

    @patch('crawler.queue_handler.KafkaClient.Consumer')
    def test_connect_failure(self, mock_consumer):
        """Test Kafka connection failure."""

        # Simulate a KafkaException when listing topics
        mock_consumer.return_value.list_topics.side_effect = KafkaException("Unable to connect to Kafka")

        # Call the connect method
        result = self.kafka_client.connect()

        # Assertions
        self.assertFalse(result)
        mock_consumer.assert_called_once_with(self.kafka_client.kafka_conf)
        mock_consumer.return_value.list_topics.assert_called_once_with(timeout=6)

    def test_subscribe_success(self):
        """Test subscribing to a valid Kafka topic."""
        self.kafka_client.k_consumer = Mock()
        self.kafka_client.k_consumer.list_topics.return_value.topics = {'test_topic': None}

        result = self.kafka_client.subscribe()

        self.assertTrue(result)
        self.kafka_client.k_consumer.subscribe.assert_called_once_with([self.kafka_client.topic])
        self.mock_logger.info.assert_called_with("Successfully subscribed to topic: test_topic")

    def test_subscribe_failure(self):
        """Test subscribing to an invalid Kafka topic."""
        self.kafka_client.k_consumer = Mock()
        self.kafka_client.k_consumer.list_topics.return_value.topics = {}

        result = self.kafka_client.subscribe()

        self.assertFalse(result)
        self.kafka_client.k_consumer.subscribe.assert_not_called()
        self.mock_logger.error.assert_called_with("Kafka topic not found! (topic: test_topic)")


    @patch('crawler.queue_handler.KafkaClient.json.loads')
    @patch('crawler.queue_handler.KafkaClient.validate')
    def test_extract_crawl_job_success(self, mock_validate, mock_json_loads):
        """Test extracting a valid crawl job from a Kafka message."""
        mock_json_loads.return_value = {
            'jobId': {'productId': 123, 'competitorId': 456},
            'productUrl': 'http://example.com',
            'host': 'example',
            'dispatchedTimestamp': '2025-01-01T12:00:00Z'
        }
        mock_validate.return_value = None  # Simulate successful validation

        raw_message = json.dumps(mock_json_loads.return_value).encode('utf-8')

        # Call the method
        result = self.kafka_client._extract_crawl_job_from_value(raw_message)

        # Validate the result is of the correct type and contains the correct values
        self.assertIsInstance(result, CrawlJobRequest)
        self.assertEqual(result.job_id.product_id, 123)
        self.assertEqual(result.job_id.competitor_id, 456)
        self.assertEqual(result.product_url, 'http://example.com')
        self.assertEqual(result.host, 'example')
        self.assertEqual(result.dispatched_timestamp, '2025-01-01T12:00:00Z')

    @patch('crawler.queue_handler.KafkaClient.json.loads')
    @patch('crawler.queue_handler.KafkaClient.validate')
    def test_extract_crawl_job_invalid_json(self, mock_validate, mock_json_loads):
        """Test handling invalid JSON in a Kafka message."""
        mock_json_loads.side_effect = json.JSONDecodeError("Expecting value", "", 0)

        raw_message = b"invalid_json"

        result = self.kafka_client._extract_crawl_job_from_value(raw_message)

        self.assertIsNone(result)
        self.mock_logger.error.assert_called_with("Error decoding json from kafka: Expecting value: line 1 column 1 (char 0)")

    @patch('crawler.queue_handler.KafkaClient.json.loads')
    @patch('crawler.queue_handler.KafkaClient.validate')
    def test_extract_crawl_job_invalid_schema(self, mock_validate, mock_json_loads):
        """Test handling a message with an invalid schema."""
        mock_json_loads.return_value = {
            'jobId': {'productId': 123},  # Missing competitorId
            'productUrl': 'http://example.com',
            'host': 'example',
            'dispatchedTimestamp': '2025-01-01T12:00:00Z'
        }
        mock_validate.side_effect = ValidationError("Validation failed")

        raw_message = json.dumps(mock_json_loads.return_value).encode('utf-8')

        result = self.kafka_client._extract_crawl_job_from_value(raw_message)

        self.assertIsNone(result)
        self.mock_logger.error.assert_called_with("Could not validate the values in the json received via kafka pipeline. Error: Validation failed")

    @patch('crawler.queue_handler.KafkaClient.Consumer')
    def test_disconnect(self, mock_consumer):
        """Test disconnecting from Kafka."""
        # Mock the Kafka consumer
        mock_k_consumer = Mock()  # Create a Mock consumer
        self.kafka_client.k_consumer = mock_k_consumer

        # Call the disconnect method
        self.kafka_client._disconnect()

        # Assert that close was called before k_consumer was set to None
        mock_k_consumer.close.assert_called_once()

        # Assert that k_consumer is set to None
        self.assertIsNone(self.kafka_client.k_consumer)


if __name__ == '__main__':
    unittest.main()
