package ru.muravin.bankapplication.notificationsService.service;
import brave.Tracer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.muravin.bankapplication.notificationsService.dto.NotificationDto;
import ru.muravin.bankapplication.notificationsService.mapper.NotificationMapper;
import ru.muravin.bankapplication.notificationsService.model.Notification;
import ru.muravin.bankapplication.notificationsService.repository.NotificationsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationsService {
    private final NotificationMapper notificationMapper;
    private final NotificationsRepository notificationsRepository;
    private final Tracer tracer;

    @Autowired
    public NotificationsService(NotificationMapper notificationMapper, NotificationsRepository notificationsRepository, Tracer tracer) {
        this.notificationMapper = notificationMapper;
        this.notificationsRepository = notificationsRepository;
        this.tracer = tracer;
    }

    public void sendNotification(NotificationDto notificationDto) {
        Notification notification = new Notification();
        notification.setSender(notificationDto.getSender());
        notification.setMessage(notificationDto.getMessage());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime localDateTime = LocalDateTime.from(formatter.parse(StringUtils.rightPad(notificationDto.getTimestamp(),23, "0")));
        notification.setCreatedAt(localDateTime);
        var span = tracer.nextSpan().name("db Saving notification").start();
        Notification saved = notificationsRepository.save(notification);
        span.finish();
    }
}
