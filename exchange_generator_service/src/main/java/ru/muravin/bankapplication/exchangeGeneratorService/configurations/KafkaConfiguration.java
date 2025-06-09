package ru.muravin.bankapplication.exchangeGeneratorService.configurations;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.muravin.bankapplication.exchangeGeneratorService.dto.CurrencyRateDto;

import java.util.List;
import java.util.Map;

@Configuration
public class KafkaConfiguration {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    private final KafkaProperties kafkaProperties;

    @Autowired
    public KafkaConfiguration(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public NewTopic topicExchange() {
        return TopicBuilder.name("currency-rates")
                .partitions(1)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public KafkaTemplate<String, List<CurrencyRateDto>> kafkaTemplate() {
        return new KafkaTemplate<>(
            new DefaultKafkaProducerFactory<String, List<CurrencyRateDto>>(
                kafkaProperties.buildProducerProperties(null),
                new StringSerializer(),
                new JsonSerializer<>(new TypeReference<List<CurrencyRateDto>>() {})
            )
        );
    }

    @Bean(name = "ProducerConfig")
    public Map<String, Object> producerConfig() {
        return Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers,
                // Enable safely ordered retries
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true",
                ProducerConfig.ACKS_CONFIG, "all",
                // Config number of retries
                ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 5000,
                ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 200,
                ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500
        );

    }
}
