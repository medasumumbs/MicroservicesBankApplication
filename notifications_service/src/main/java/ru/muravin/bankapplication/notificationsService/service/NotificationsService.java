package ru.muravin.bankapplication.notificationsService.service;

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

    @Autowired
    public NotificationsService(NotificationMapper notificationMapper, NotificationsRepository notificationsRepository) {
        this.notificationMapper = notificationMapper;
        this.notificationsRepository = notificationsRepository;
    }

    public void sendNotification(NotificationDto notificationDto) {
        Notification notification = new Notification();
        notification.setSender(notificationDto.getSender());
        notification.setMessage(notificationDto.getMessage());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime localDateTime = LocalDateTime.from(formatter.parse(notificationDto.getTimestamp()));
        notification.setCreatedAt(localDateTime);
        Notification saved = notificationsRepository.save(notification);
    }
}
