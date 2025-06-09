package ru.muravin.bankapplication.notificationsService.integrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import ru.muravin.bankapplication.exchangeGeneratorService.ExchangeGeneratorServiceApplication;
import ru.muravin.bankapplication.exchangeGeneratorService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.exchangeGeneratorService.service.RatesGenerationService;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ExchangeGeneratorServiceApplication.class,properties = "spring.kafka.producer.bootstrap-servers=localhost:9092")
@EmbeddedKafka(
        topics = {"currency-rates"},
        partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://:9092", "port=9092" }
)
public class KafkaIntegrationTest {

    @Autowired
    private RatesGenerationService ratesGenerationService;

    @Autowired
    private ConsumerFactory<String, List<CurrencyRateDto>> consumerFactory;

    @Test
    void testSendMessageAndConsumeIt() throws InterruptedException {
        String topic = "currency-rates";
        String groupId = "test-group";

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<CurrencyRateDto>> received = new AtomicReference<>();

        // Создаем контейнер с указанием group.id
        ContainerProperties containerProps = new ContainerProperties(topic);
        KafkaMessageListenerContainer<String, List<CurrencyRateDto>> container =
                new KafkaMessageListenerContainer<>(consumerFactory, containerProps);

        container.setupMessageListener((MessageListener<String, List<CurrencyRateDto>>) record -> {
            received.set(record.value());
            latch.countDown();
        });

        // Устанавливаем group.id перед запуском
        container.getContainerProperties().setGroupId(groupId);

        container.start();

        try {
            // Запуск генерации и отправки сообщения
            ratesGenerationService.generateAndSendRates();

            boolean messageReceived = latch.await(10, TimeUnit.SECONDS);
            assertTrue(messageReceived);
            assertNotNull(received.get());
            assertFalse(received.get().isEmpty());
        } finally {
            container.stop();
        }
    }
}