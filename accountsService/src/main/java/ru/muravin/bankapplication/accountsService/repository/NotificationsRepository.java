package ru.muravin.bankapplication.accountsService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.muravin.bankapplication.accountsService.model.Notification;

public interface NotificationsRepository extends JpaRepository<Notification, Long> {
}
