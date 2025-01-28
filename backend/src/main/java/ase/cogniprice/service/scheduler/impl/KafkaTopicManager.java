package ase.cogniprice.service.scheduler.impl;

import ase.cogniprice.exception.QueueAccessException;
import ase.cogniprice.service.scheduler.TopicManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class KafkaTopicManager implements TopicManagerService {
    private final KafkaAdmin kafkaAdmin;

    public KafkaTopicManager(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    @Override
    public void createTopicIfNotExists(String topicName) throws QueueAccessException {
        log.trace("Creating topic {}", topicName);
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            var topics = adminClient.listTopics().names().get();
            if (!topics.contains(topicName)) {
                adminClient.createTopics(Collections.singleton(new NewTopic(topicName, 1, (short) 1)));
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("failed to create or access Kafka topic {}", topicName, e);
            throw new QueueAccessException("Failed to create or access Kafka topic: " + topicName, e);
        }
    }
}
