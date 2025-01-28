package ase.cogniprice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
@Profile("ci")
public class KafkaMockConfig {
    @Bean
    public KafkaTemplate<String, Object> mockKafkaTemplate() {
        // Return a no-op KafkaTemplate
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(Map.of()));
    }

    @Bean
    public KafkaListenerContainerFactory<?> mockKafkaListenerContainerFactory() {
        return new ConcurrentKafkaListenerContainerFactory() {
            // No-op implementation or mock if needed
        };
    }

    @Bean
    public ConsumerFactory<String, Object> mockConsumerFactory() {
        // Return a no-op or mock ConsumerFactory
        return new DefaultKafkaConsumerFactory<>(Map.of());
    }

    @Bean
    public ProducerFactory<String, Object> mockProducerFactory() {
        // Return a no-op or mock ProducerFactory
        return new DefaultKafkaProducerFactory<>(Map.of());
    }
}
