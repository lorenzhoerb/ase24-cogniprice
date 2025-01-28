from abc import ABC, abstractmethod
from crawler.queue_handler.MessageDataObjects import CrawlJobRequest

import logging
from crawler.utils.ConfigReader import ConfigReader


class IMessageQueueClient(ABC):

    @abstractmethod
    def __init__(self, logger: logging.Logger, config: ConfigReader) -> None:
        """
        Abstract method to initialize an instance with a logger and configuration reader.

        :param logger: A logger instance used for logging messages in the class.
        :type logger: logging.Logger
        :param config: A configuration reader instance used to read configuration settings.
        :type config: ConfigReader
        """
        pass

    @abstractmethod
    def connect(self) -> bool:
        """
        Abstract method to establish a connection to the data source (e.g., Kafka broker).
        This method should be implemented by subclasses to initiate the connection and return
        the status of the connection attempt.

        :return: True if the connection is successfully established, False otherwise.
        :rtype: bool
        """
        pass

    @abstractmethod
    def disconnect(self) -> None:
        """
        Abstract method to disconnect from the data source (e.g., Kafka broker).
        This method should be implemented by subclasses to properly handle the disconnection process
        and release any resources associated with the connection.

        :return: None
        :rtype: None
        """
        pass

    @abstractmethod
    def test_connection(self) -> bool:
        """
        Abstract method to test the connection to the data source (e.g., Kafka broker).
        This method should be implemented by subclasses to attempt a connection test and
        return whether the connection is successful or not.

        :return: True if the connection is successful, False otherwise.
        :rtype: bool
        """
        pass

    @abstractmethod
    def get_next_job(self) -> CrawlJobRequest:
        """
        Abstract method to retrieve the next crawl job from the message source.
        This method should be implemented by subclasses to fetch and return a `CrawlJobRequest` object.
        If no job is available or an error occurs, the implementation should handle those cases appropriately.

        :return: A `CrawlJobRequest` object representing the next job to be processed.
        :rtype: CrawlJobRequest
        """
        pass