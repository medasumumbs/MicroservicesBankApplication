package ru.muravin.bankapplication.notificationsService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import ru.muravin.bankapplication.notificationsService.dto.NotificationDto;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Service
public class NotificationsConsumer {
    private final NotificationsService notificationsService;

    @Autowired
    public NotificationsConsumer(NotificationsService notificationsService) {
        this.notificationsService = notificationsService;
    }


    @RetryableTopic(backoff = @Backoff(500), timeout = "4000")
    @KafkaListener(topics = "notifications", groupId = "notifications-grp")
    public void consume(NotificationDto notificationDto, Acknowledgment ack) {
        log.info("NotificationsConsumer called: {}", notificationDto);
        ack.acknowledge();
        log.info("NotificationsConsumer acknowledged");
        notificationsService.sendNotification(notificationDto);
        log.info("NotificationsConsumer saved rates");
    }
}
