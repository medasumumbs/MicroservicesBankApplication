package ru.muravin.bankapplication.notificationsService.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private String message;
    private String sender;
    private LocalDateTime timestamp;
}
