package ase.cogniprice.service.scheduler;

import ase.cogniprice.exception.QueueAccessException;

/**
 * Service interface for managing message topics or queues within a distributed system.
 * Provides functionality to ensure that specific topics or queues are created if they do not already exist.
 * This is particularly useful for systems that dynamically manage communication channels for message processing.
 *
 * @author Lorenz
 */
public interface TopicManagerService {

    /**
     * Ensures that a message topic or queue exists within the system.
     *
     * <p>This method checks whether the specified topic or queue exists in the messaging
     * infrastructure. If it does not exist, it will be created. The implementation should
     * handle cases where topic or queue creation fails due to network issues, lack of permissions,
     * or other runtime problems.
     * </p>
     *
     * <p><b>Note:</b> The topic or queue name must adhere to the naming conventions of the
     * underlying messaging system. Invalid names may result in runtime errors.
     * </p>
     *
     * @param topicName the name of the message topic or queue to check and create if absent.
     *                  Must not be null or empty.
     * @throws IllegalArgumentException if {@code topicName} is null or empty.
     * @throws QueueAccessException     if the topic or queue cannot be accessed or created due to
     *                                  network, configuration, or other issues.
     */
    void createTopicIfNotExists(String topicName) throws QueueAccessException;
}
