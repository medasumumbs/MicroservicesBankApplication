package ru.muravin.bankapplication.notificationsService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.muravin.bankapplication.notificationsService.model.Notification;

public interface NotificationsRepository extends JpaRepository<Notification, Long> {
}
