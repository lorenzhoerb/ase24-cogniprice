Producer Message Queue Interface
================================
.. _my-reference-label2:

The `IMessageQueueProducer <#crawler.queue_handler.IMessageQueueProducer.IMessageQueueProducer>`_ class is an abstract base class that defines the essential methods for producing messages to a message queue, such as a Kafka broker. It provides the following key methods, which must be implemented by any subclass:

  * `connect() <#crawler.queue_handler.IMessageQueueProducer.IMessageQueueProducer.connect>`_: Establishes a connection to the message source and returns a boolean indicating whether the connection was successfully established.
  * `disconnect() <#crawler.queue_handler.IMessageQueueProducer.IMessageQueueProducer.disconnect>`_: Closes the connection to the message source and releases any associated resources.
  * `test_connection() <#crawler.queue_handler.IMessageQueueProducer.IMessageQueueProducer.test_connection>`_: Tests the connection to the message source and returns a boolean indicating whether the connection is still active.
  * `send_failure(job_id, process_time, error_msg) <#crawler.queue_handler.IMessageQueueProducer.IMessageQueueProducer.send_failure>`_: Sends failure details for a job, including the job ID, processing time, and error message.
  * `send_success(job_id, amount, currency, process_time) <#crawler.queue_handler.IMessageQueueProducer.IMessageQueueProducer.send_success>`_: Sends success details for a job, including the job ID, crawled amount, currency, and processing time.

This class serves as a blueprint for creating concrete implementations that handle sending job status messages to different message queues or brokers.

.. automodule:: crawler.queue_handler.IMessageQueueProducer
