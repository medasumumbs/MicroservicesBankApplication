package ru.muravin.bankapplication.cashInCashOutService.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.muravin.bankapplication.cashInCashOutService.dto.NotificationDto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class NotificationsServiceClient {
    private final ApplicationContext applicationContext;
    private final MeterRegistry meterRegistry;

    @Setter
    private KafkaTemplate<String, NotificationDto> kafkaTemplate;

    @Value("${kafkaTopic:notifications}")
    private String kafkaTopic;

    @Value("${kafkaSecondsTimeout:10}")
    private Integer kafkaSecondsTimeout;

    @Autowired
    public NotificationsServiceClient(ApplicationContext applicationContext, KafkaTemplate<String, NotificationDto> kafkaTemplate, MeterRegistry meterRegistry) {
        this.applicationContext = applicationContext;
        this.kafkaTemplate = kafkaTemplate;
        this.meterRegistry = meterRegistry;
    }

    public void sendNotification(String notificationText, String login) {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setMessage(notificationText);
        notificationDto.setTimestamp(new Timestamp(System.currentTimeMillis()).toString());
        notificationDto.setSender(applicationContext.getApplicationName());
        log.info("Sending notification: {}", notificationDto);
        var guid = UUID.randomUUID().toString();
        Message<NotificationDto> message = MessageBuilder
                .withPayload(notificationDto)  // Объект в качестве значения
                .setHeader(KafkaHeaders.TOPIC, kafkaTopic)  // Топик
                .setHeader(KafkaHeaders.KEY, guid)
                .setHeader("idempotencyKey", guid)  // Произвольный заголовок
                .build();
        try {
            kafkaTemplate.send(message).get(kafkaSecondsTimeout, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            meterRegistry.counter("failedToSendNotification", "login", login).increment();
            log.error("{}:{}", e.getMessage(), e.getCause().getMessage());
            return;
        }
        log.info("Notification sent: {}", notificationDto);
    }
}