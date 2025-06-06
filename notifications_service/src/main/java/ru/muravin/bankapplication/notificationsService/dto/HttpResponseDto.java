package ru.muravin.bankapplication.notificationsService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpResponseDto {
    private String statusCode;
    private String statusMessage;
    private Long notificationId;
}
