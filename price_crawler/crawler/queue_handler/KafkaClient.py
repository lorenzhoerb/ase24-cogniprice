from crawler.queue_handler.IMessageQueueClient import IMessageQueueClient, CrawlJobRequest
from crawler.queue_handler.MessageDataObjects import StoreProductId
from crawler.utils.ConfigReader import ConfigReader
from crawler.queue_handler.QueueHandlerHelper import QueueHandlerHelper

from confluent_kafka import Consumer
from jsonschema import validate, exceptions

import logging
import json
from typing import Optional

class KafkaClient(IMessageQueueClient):

    def __init__(self, logger: logging.Logger, config: ConfigReader) -> None:
        """
        Initializes the Kafka consumer and configures Kafka connection settings, as well as JSON schema validation
        for incoming messages. The Kafka configuration is derived from the provided configuration reader.

        :param logger: The logger instance used for logging errors and events.
        :type logger: logging.Logger
        :param config: The configuration reader used to retrieve Kafka-related settings.
        :type config: ConfigReader
        """
        self.logger = QueueHandlerHelper.validate_logging_object(logger)
        self.config = QueueHandlerHelper.validate_config_object(config)
        self.bootstrap_servers = self.config.get('Kafka', 'bootstrap_servers')
        self.topic = self.config.get('Kafka', 'kafka_topic')

        self.k_consumer: Consumer = None

        self.kafka_conf = {
            'bootstrap.servers': self.bootstrap_servers,
            'group.id': 'web_crawler',
            'session.timeout.ms': 6000,
            'auto.offset.reset': 'earliest'
        }

        self.json_schema = {
            "type": "object",
            "properties": {
                "jobId": {
                    "type": "object",
                    "properties": {
                        "productId": {
                            "type": "integer"
                        },
                        "competitorId": {
                            "type": "integer"
                        }
                    },
                    "required": ["productId", "competitorId"],
                    "additionalProperties": False
                },
                "productUrl": {
                    "type": "string"
                },
                "host": {
                    "type": "string"
                    },
                "dispatchedTimestamp": {
                    "type": "string"
                }
            },
            "required": ["jobId", "productUrl", "host", "dispatchedTimestamp"],
            "additionalProperties": False
        }

    def __del__(self) -> None:
        """
        Destructor method that is called when an instance of the class is about to be destroyed.
        It ensures that any necessary cleanup actions, such as disconnecting from external resources,
        are performed before the object is removed from memory.

        :return: None
        :rtype: None
        """
        self._disconnect()


    def connect(self) -> bool:
        """
        Establishes a connection to the Kafka broker using the provided configuration settings.
        It initializes the Kafka consumer and tests the connection to ensure it is successful.

        :return: True if the connection to Kafka was successful, False otherwise.
        :rtype: bool
        """
        # noinspection PyArgumentList
        self.k_consumer = Consumer(self.kafka_conf)

        # test the kafka connection and return status
        return self._test_connection()

    def subscribe(self) -> bool:
        """
        Subscribes to the specified Kafka topic. If the topic exists on the Kafka broker, the method will
        successfully subscribe to it. If the topic is not found or an error occurs during subscription,
        an error is logged.

        :return: True if the subscription was successful, False otherwise.
        :rtype: bool
        """
        # even if a topic does not exist and i subscribe to it, there is no error
        try:
            # list all topics on kafka broker
            for topic in self.k_consumer.list_topics(timeout=3).topics:
                if topic == self.topic:
                    # subscribe to topic
                    self.logger.info("Successfully subscribed to topic: " + str(self.topic))
                    self.k_consumer.subscribe([self.topic])
                    return True
        except Exception as e:
            self.logger.error("Error during subscribing to topic. Error: " + str(e))

        self.logger.error("Kafka topic not found! (topic: " + str(self.topic) + ")")
        return False

    def disconnect(self) -> None:
        """
        Disconnects from the Kafka broker by invoking the internal `_disconnect` method.
        This ensures that any resources related to the Kafka consumer are properly released.

        :return: None
        :rtype: None
        """
        self._disconnect()

    def test_connection(self) -> bool:
        """
        Tests the connection to the Kafka broker by invoking the internal `_test_connection` method.
        This method checks whether the connection to Kafka is successful.

        :return: True if the connection to Kafka is successful, False otherwise.
        :rtype: bool
        """
        return self._test_connection()

    def _test_connection(self) -> bool:
        """
        Tests the connection to the Kafka broker by attempting to list the topics.
        If the connection is successful, a debug message is logged. If the connection fails,
        a warning is logged.

        :return: True if the connection to Kafka broker is successful, False otherwise.
        :rtype: bool
        """
        self.logger.debug("Testing Kafka broker connection")
        try:
            self.k_consumer.list_topics(timeout=6)
            self.logger.debug("Connection to Kafka broker succeeded.")
            return True
        except Exception as e:
            self.logger.warning("Connection to Kafka broker failed.")
            return False

    def get_next_job(self) -> CrawlJobRequest:
        """
        Polls Kafka for the next message, processes it, and returns a `CrawlJobRequest` object.
        The method loops indefinitely until a valid message is received, parsed, and returned.
        If an invalid or unprocessable message is encountered, it logs a warning and continues polling.

        :return: A `CrawlJobRequest` object extracted from the next valid Kafka message.
        :rtype: CrawlJobRequest
        :raises RuntimeError: If an error occurs while polling the Kafka consumer.
        """
        # loop as long as a new message can be consumed
        while True:
            try:
                self.logger.info("Polling the next message from kafka.")
                # returns None on timeout
                msg = self.k_consumer.poll(timeout=30.0)
            # RuntimeError if called on a closed consumer
            except Exception as e:
                msg = None
                self.logger.critical("Error polling the next message from kafka: " + str(e))

            # check if message from kafka has data
            if msg is not None:
                crawl_job_object = self._extract_crawl_job_from_value(msg.value())
                # check if message from kafka is a compliant json
                if crawl_job_object is not None:
                    return crawl_job_object
                self.logger.warning("Could not parse the kafka message correctly. Message: " + str(msg.value()))

    def _extract_crawl_job_from_value(self, value: bytes) -> Optional[CrawlJobRequest]:
        """
        Extracts and validates a crawl job from a Kafka message. The method attempts to decode the message,
        validate its JSON schema, and create a `CrawlJobRequest` object from the valid message.
        If any step fails, an error is logged and None is returned.

        :param value: The raw byte message received from Kafka.
        :type value: bytes
        :return: A `CrawlJobRequest` object if the message is valid and can be parsed, or None if an error occurs.
        :rtype: Optional[CrawlJobRequest]
        """
        self.logger.debug("Extracting json from kafka message.")

        try:
            json_data = json.loads(value.decode('utf-8'))
        except Exception as e:
            self.logger.error("Error decoding json from kafka: " + str(e))
            # no valid message from kafka
            return None

        # VALIDATE JSON
        try:
            validate(instance=json_data, schema=self.json_schema)
        except exceptions.SchemaError as e:
            self.logger.error("Json schema received via kafka pipeline is not correct. Error: " + str(e))
            return None
        except exceptions.ValidationError as e:
            self.logger.error("Could not validate the values in the json received via kafka pipeline. Error: " + str(e))
            return None
        except Exception as e:
            self.logger.error("General Exception during validating the json from the kafka pipline. Error: " + str(e))
            return None

        # CREATE OBJECT FROM JSON
        try:
            ret_crawl_obj =  CrawlJobRequest(
                job_id=StoreProductId(
                    product_id=json_data['jobId']['productId'],
                    competitor_id=json_data['jobId']['competitorId']
                ),
                product_url=json_data['productUrl'],
                host=json_data['host'],
                dispatched_timestamp=json_data['dispatchedTimestamp']
            )
        except Exception as e:
            self.logger.error("General Exception during creating the CrawlJobRequest object from the received"
                              " kafka message. Error: " + str(e))
            return None

        return ret_crawl_obj


    def _disconnect(self) -> None:
        """
        Closes the connection to the Kafka broker by closing the Kafka consumer.
        This method does not perform a connection test before closing, avoiding potential delays
        in case of no active connection. It logs the closure of the connection.

        :return: None
        :rtype: None
        """
        # would also be possible to test connection before closing, however this would result in
        #  a 6 seconds delay when there is no connection
        if self.k_consumer is not None:  # Ensure the consumer exists
            try:
                self.k_consumer.close()  # Attempt to close the connection
                self.k_consumer = None  # Set it to None after closing
                self.logger.debug("Closed the Kafka broker connection.")
            except Exception as e:
                self.logger.error(f"Failed to close the Kafka consumer: {e}")
        else:
            self.logger.warning("Kafka consumer is already None; no active connection to close.")


