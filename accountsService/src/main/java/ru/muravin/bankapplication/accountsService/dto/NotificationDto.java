package ru.muravin.bankapplication.accountsService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private String message;
    private String sender;
    private LocalDateTime timestamp;
}
