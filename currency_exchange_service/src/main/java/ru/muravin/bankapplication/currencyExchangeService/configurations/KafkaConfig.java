package ru.muravin.bankapplication.currencyExchangeService.configurations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.muravin.bankapplication.currencyExchangeService.dto.CurrencyRateDto;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
@Configuration
public class KafkaConfig {
    @Bean
    public ConsumerFactory<String, List<CurrencyRateDto>> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "myapp.kafka.ru:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put("spring.json.trusted.packages", "ru.muravin.bankapplication");
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new CustomDeserializer()
        );

    }

    static class CustomDeserializer extends JsonDeserializer<List<CurrencyRateDto>> {

        @Override
        public List<CurrencyRateDto> deserialize(String topic, Headers headers, byte[] data) {
            return deserialize(topic, data);
        }

        @Override
        public List<CurrencyRateDto> deserialize(String topic, byte[] data) {
            if (data == null) {
                return null;
            }
            try {
                return objectMapper.readValue(data, new TypeReference<List<CurrencyRateDto>>() {
                });
            } catch (IOException e) {
                throw new SerializationException("Can't deserialize data [" + Arrays.toString(data) +
                        "] from topic [" + topic + "]", e);
            }
        }
    }

}
