package ru.muravin.bankapplication.notificationsService.service;

import lombok.Setter;
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
    @Setter
    private NotificationsService notificationsService;

    @Autowired
    public NotificationsConsumer(NotificationsService notificationsService) {
        this.notificationsService = notificationsService;
    }


    @RetryableTopic(backoff = @Backoff(500), timeout = "4000")
    @KafkaListener(topics = "notifications", groupId = "notifications-grp")
    public void consume(LinkedHashMap<String, String> linkedHashMap, Acknowledgment ack) {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTimestamp(linkedHashMap.get("timestamp"));
        notificationDto.setMessage(linkedHashMap.get("message"));
        notificationDto.setSender(linkedHashMap.get("sender"));
        log.info("NotificationsConsumer called: {}", notificationDto);
        ack.acknowledge();
        log.info("NotificationsConsumer acknowledged");
        notificationsService.sendNotification(notificationDto);
        log.info("NotificationsConsumer saved rates");
    }
}
