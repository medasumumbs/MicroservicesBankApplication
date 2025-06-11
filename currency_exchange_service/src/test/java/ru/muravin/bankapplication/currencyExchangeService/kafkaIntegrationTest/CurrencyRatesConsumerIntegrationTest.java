package ru.muravin.bankapplication.currencyExchangeService.kafkaIntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.muravin.bankapplication.currencyExchangeService.CurrencyExchangeServiceApplication;
import ru.muravin.bankapplication.currencyExchangeService.configurations.OAuth2SecurityConfig;
import ru.muravin.bankapplication.currencyExchangeService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.currencyExchangeService.service.CurrencyRatesConsumer;
import ru.muravin.bankapplication.currencyExchangeService.service.CurrencyRatesService;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.liquibase.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
},
        classes = {CurrencyExchangeServiceApplication.class, CurrencyRatesConsumer.class,})
@TestPropertySource(locations = "classpath:application.yml")
@EmbeddedKafka(partitions = 1, topics = { "currency-rates" }, brokerProperties = {
        "key.serializer=org.apache.kafka.common.serialization.StringSerializer",
        "value.serializer=org.springframework.kafka.support.serializer.JsonSerializer"
})
public class CurrencyRatesConsumerIntegrationTest {

    @MockitoBean
    private OAuth2SecurityConfig oAuth2SecurityConfig;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;
    @Autowired
    private CurrencyRatesService currencyRatesService;

    private KafkaMessageListenerContainer<String, Object> container;

    private MockConsumer mockConsumer;

    @Autowired
    CurrencyRatesConsumer currencyRatesConsumer;
    @Autowired
    ApplicationContext applicationContext;


    @BeforeEach
    void setUp() {
        // 1. Создаем ConsumerFactory с минимальными настройками
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "currency-rates-grp");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.springframework.kafka.support.serializer.JsonDeserializer");

        ConsumerFactory<String, Object> consumerFactory =
                new DefaultKafkaConsumerFactory<>(props);


        // 2. Создаем реальный Kafka Consumer
        Deserializer<String> keyDeserializer = new StringDeserializer();
        Deserializer<Map> valueDeserializer = new JsonDeserializer();

        mockConsumer = new MockConsumer<>(InMemoryOffsetBacking, keyDeserializer, valueDeserializer);

        // 3. Настраиваем контейнер
        ContainerProperties containerProps = new ContainerProperties("currency-rates");
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProps);
        container.setupMessageListener((MessageListener<String, Object>) record -> {
            List<LinkedHashMap> list = Collections.singletonList((LinkedHashMap) record.value());
            Acknowledgment ack = mock(Acknowledgment.class);
            currencyRatesConsumer.consume(list, ack);
        });

        container.start();
        ContainerTestUtils.waitForAssignment(container, 1); // Ждем партиционное назначение
    }

    @AfterEach
    void tearDown() {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    void testConsume_ShouldProcessAndSaveCurrencyRates() {
        // Given
        LinkedHashMap<String, Object> message = new LinkedHashMap<>();
        message.put("buyRate", 1.12);
        message.put("sellRate", 1.15);
        message.put("currencyCode", "USD");

        ConsumerRecord<String, Object> record = new ConsumerRecord<>("currency-rates", 0, 0L, null, message);

        // When
        mockConsumer.addRecord(record);
        mockConsumer.poll(1000);

        // Then
        verify(currencyRatesService, timeout(1000).times(1)).saveRates(anyList());
    }
    class NoOpOffsetBackingStore implements BackingStore {
        @Override
        public void start() {}

        @Override
        public void stop() {}

        @Override
        public void commit(Map<TopicPartition, Long> offsets) {}

        @Override
        public Map<TopicPartition, Long> read() {
            return new HashMap<>();
        }
    }
}
