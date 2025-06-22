package ru.muravin.bankapplication.transferService.kafkaIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.muravin.bankapplication.transferService.configuration.OAuth2SecurityConfig;
import ru.muravin.bankapplication.transferService.dto.NotificationDto;
import ru.muravin.bankapplication.transferService.service.NotificationsServiceClient;
import ru.muravin.bankapplication.transferService.TransferServiceApplication;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = TransferServiceApplication.class,
        properties = {
                "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
                "spring.kafka.producer.bootstrap-servers=localhost:9092",
                "spring.kafka.consumer.bootstrap-servers=localhost:9092",
                "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
                "spring.kafka.consumer.properties.spring.json.trusted.packages=*"
        })
@EmbeddedKafka(
        topics = {"notifications"},
        partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://:9092", "port=9092" }
)
public class KafkaIntegrationTest {

    @Autowired
    KafkaTemplate<String, NotificationDto> kafkaTemplate;
    @MockitoBean
    private OAuth2SecurityConfig oAuth2SecurityConfig;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Autowired
    private NotificationsServiceClient notificationsServiceClient;

    @Autowired
    private ConsumerFactory<String, NotificationDto> consumerFactory;

    @Test
    void testSendMessageAndConsumeIt() throws InterruptedException {
        String topic = "notifications";
        String groupId = "test-group";

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<NotificationDto> received = new AtomicReference<>();

        // Создаем контейнер с указанием group.id
        ContainerProperties containerProps = new ContainerProperties(topic);
        KafkaMessageListenerContainer<String, NotificationDto> container =
                new KafkaMessageListenerContainer<>(consumerFactory, containerProps);

        container.setupMessageListener((MessageListener<String, NotificationDto>) record -> {
            received.set(record.value());
            latch.countDown();
        });

        // Устанавливаем group.id перед запуском
        container.getContainerProperties().setGroupId(groupId);
        container.start();

        while (true) {
            AtomicBoolean doBreak = new AtomicBoolean(false);
            container.getAssignedPartitions().forEach(partition -> {
                if (partition.topic().equals("notifications")) {
                    doBreak.set(true);
                }
            });
            if (doBreak.get()) break;
        }
        try {
            // Запуск генерации и отправки сообщения
            notificationsServiceClient.sendNotification("123", "123");

            boolean messageReceived = latch.await(15, TimeUnit.SECONDS);
            assertTrue(messageReceived);
            NotificationDto result = received.get();
            assertNotNull(result);
            System.out.println(result);

        } finally {
            container.stop();
        }
    }
}