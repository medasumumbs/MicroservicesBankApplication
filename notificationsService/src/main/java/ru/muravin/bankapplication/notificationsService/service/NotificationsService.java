package ru.muravin.bankapplication.notificationsService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.muravin.bankapplication.notificationsService.dto.NotificationDto;
import ru.muravin.bankapplication.notificationsService.mapper.NotificationMapper;
import ru.muravin.bankapplication.notificationsService.model.Notification;
import ru.muravin.bankapplication.notificationsService.repository.NotificationsRepository;

@Service
public class NotificationsService {
    private final NotificationMapper notificationMapper;
    private final NotificationsRepository notificationsRepository;

    @Autowired
    public NotificationsService(NotificationMapper notificationMapper, NotificationsRepository notificationsRepository) {
        this.notificationMapper = notificationMapper;
        this.notificationsRepository = notificationsRepository;
    }

    public Long sendNotification(NotificationDto notificationDto) {
        Notification notification = notificationMapper.toEntity(notificationDto);
        Notification saved = notificationsRepository.save(notification);
        return saved.getId();
    }
}
