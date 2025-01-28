from abc import ABC, abstractmethod
from crawler.queue_handler.MessageDataObjects import CrawlJobRequest, StoreProductId


class IMessageQueueProducer(ABC):

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
        Abstract method to test the connection to the data source.
        This method should be implemented by subclasses to check if the connection to the
        data source is still active and return whether it is successful or not.

        :return: True if the connection is active, False otherwise.
        :rtype: bool
        """
        pass

    @abstractmethod
    def send_failure(self, job_id: StoreProductId, process_time: int, error_msg: str) -> None:
        """
        Abstract method to send failure details for a crawl job.
        This method should be implemented by subclasses to send information regarding a failed job.

        :param job_id: The job ID associated with the failed crawl.
        :type job_id: StoreProductId
        :param process_time: The time it took to process the job.
        :type process_time: int
        :param error_msg: The error message describing the failure.
        :type error_msg: str
        :return: None
        :rtype: None
        """
        pass

    @abstractmethod
    def send_success(self, job_id: StoreProductId, amount: float, currency: str, process_time: int) -> None:
        """
        Abstract method to send success details for a crawl job.
        This method should be implemented by subclasses to send information about a successfully processed job.

        :param job_id: The job ID associated with the successfully processed crawl.
        :type job_id: StoreProductId
        :param amount: The crawled amount.
        :type amount: float
        :param currency: The currency associated with the crawled amount.
        :type currency: str
        :param process_time: The time it took to process the job.
        :type process_time: int
        :return: None
        :rtype: None
        """
        pass