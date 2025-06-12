package ru.muravin.bankapplication.accountsService.kafkaIntegrationTest;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.muravin.bankapplication.accountsService.AccountsServiceApplication;
import ru.muravin.bankapplication.accountsService.configuration.OAuth2SecurityConfig;
import ru.muravin.bankapplication.accountsService.dto.NotificationDto;
import ru.muravin.bankapplication.accountsService.service.NotificationsServiceClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = AccountsServiceApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=update",
                "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
                "spring.liquibase.enabled=false",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
                "spring.kafka.producer.bootstrap-servers=localhost:9092",
                "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
                "spring.kafka.consumer.properties.spring.json.trusted.packages=*"
        }
)
@EmbeddedKafka(
        topics = {"notifications"},
        partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://:9092", "port=9092" }
)
@DirtiesContext
public class NotificationsServiceClientTest {
    @MockitoBean
    private OAuth2SecurityConfig oAuth2SecurityConfig;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;
    @Autowired
    private NotificationsServiceClient notificationsServiceClient;

    @Autowired
    private ConsumerFactory<String, NotificationDto> consumerFactory;

    public static EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private DefaultKafkaConsumerFactory<?, ?> kafkaConsumerFactory;

    @BeforeEach
    public void setup() {
        embeddedKafkaBroker = new EmbeddedKafkaKraftBroker(1,1,"notifications");
        embeddedKafkaBroker.kafkaPorts(9092); // указываем порт
        embeddedKafkaBroker.afterPropertiesSet();   // инициализация
    }
    @Test
    void testSendNotificationAndConsumeIt() throws InterruptedException {
        String topic = "notifications";
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<NotificationDto> received = new AtomicReference<>();


        // Используем уже внедрённый EmbeddedKafkaBroker из @EmbeddedKafka
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "notification-group",
                "true",
                embeddedKafkaBroker
        );

        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NotificationDto.class);

        ConsumerFactory<String, NotificationDto> cf =
                new DefaultKafkaConsumerFactory<>(consumerProps,
                        new StringDeserializer(),
                        new JsonDeserializer<>(NotificationDto.class));

        ContainerProperties containerProps = new ContainerProperties(topic);
        containerProps.setGroupId("notification-group");

        KafkaMessageListenerContainer<String, NotificationDto> container =
                new KafkaMessageListenerContainer<>(cf, containerProps);

        container.setupMessageListener((MessageListener<String, NotificationDto>) record -> {
            System.out.println("Received: " + record.value());
            received.set(record.value());
            latch.countDown();
        });

        container.start();

        try {
            // Отправляем уведомление
            String message = "Test notification message";
            String result = notificationsServiceClient.sendNotification(message);
            assertThat(result).isEqualTo("OK");

            // Ожидаем получения сообщения
            boolean messageReceived = latch.await(10, TimeUnit.SECONDS);
            assertTrue(messageReceived);
            NotificationDto dto = received.get();

            // Проверяем поля
            assertNotNull(dto);
            assertEquals(message, dto.getMessage());
            assertNotNull(dto.getTimestamp());
            assertTrue(dto.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
            assertEquals("test-application", dto.getSender()); // из мока ApplicationContext
        } finally {
            container.stop();
        }
    }
    @Test
    void testManualConsumer() throws InterruptedException {
        String topic = "notifications";

        consumer.subscribe(List.of(topic));

        CountDownLatch latch = new CountDownLatch(1);
        Thread consumerThread = new Thread(() -> {
            try {
                while (true) {
                    var records = consumer.poll(Duration.ofSeconds(1));
                    for (var record : records) {
                        System.out.println("Manual consumer got message: " + record.value());
                        latch.countDown();
                        return;
                    }
                }
            } finally {
                consumer.close();
            }
        });

        consumerThread.start();

        // Отправляем уведомление
        String message = "Test notification message";
        String result = notificationsServiceClient.sendNotification(message);
        assertThat(result).isEqualTo("OK");

        boolean messageReceived = latch.await(10, TimeUnit.SECONDS);
        assertTrue(messageReceived);
    }
}