package ru.muravin.bankapplication.notificationsService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.muravin.bankapplication.notificationsService.dto.HttpResponseDto;
import ru.muravin.bankapplication.notificationsService.dto.NotificationDto;
import ru.muravin.bankapplication.notificationsService.service.NotificationsService;

@RestController
@RequestMapping("/notifications")
public class NotificationsController {
    private final NotificationsService notificationsService;

    @Autowired
    public NotificationsController(NotificationsService notificationsService) {
        this.notificationsService = notificationsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HttpResponseDto createNotification(
            @RequestBody NotificationDto notification) {
        Long savedId = notificationsService.sendNotification(notification);
        return new HttpResponseDto("OK","Notification created", savedId);
    }

    @ExceptionHandler
    public HttpResponseDto handleException(Exception ex) {
        ex.printStackTrace();
        return new HttpResponseDto("ERROR",ex.getLocalizedMessage(),null);
    }
}
