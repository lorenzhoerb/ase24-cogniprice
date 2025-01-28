Client Message Queue Interface
==============================
.. _my-reference-label:

The `IMessageQueueClient <#crawler.queue_handler.IMessageQueueClient.IMessageQueueClient>`_ class is an abstract base class that defines the essential methods for interacting with a message queue or data source, such as a Kafka broker. It provides the following key methods, which must be implemented by any subclass:

    * `connect() <#crawler.queue_handler.IMessageQueueClient.IMessageQueueClient.connect>`_: Establishes a connection to the message source and returns a boolean indicating success or failure.
    * `disconnect() <#crawler.queue_handler.IMessageQueueClient.IMessageQueueClient.disconnect>`_: Closes the connection to the message source and releases any associated resources.
    * `test_connection() <#crawler.queue_handler.IMessageQueueClient.IMessageQueueClient.test_connection>`_: Tests the connection to the message source and returns a boolean indicating whether the connection is functional.
    * `get_next_job() <#crawler.queue_handler.IMessageQueueClient.IMessageQueueClient.get_next_job>`_: Retrieves the next job (e.g., a CrawlJobRequest object) from the message source to be processed.

This class serves as a blueprint for creating concrete implementations that interact with different types of message queues or brokers.

.. automodule:: crawler.queue_handler.IMessageQueueClient
   :members:
   :undoc-members:
   :show-inheritance: