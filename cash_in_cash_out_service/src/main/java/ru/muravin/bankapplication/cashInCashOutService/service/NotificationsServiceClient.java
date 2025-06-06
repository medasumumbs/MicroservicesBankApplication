package ru.muravin.bankapplication.cashInCashOutService.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.muravin.bankapplication.cashInCashOutService.dto.NotificationDto;

import java.time.LocalDateTime;

@Service
public class NotificationsServiceClient {
    private final ApplicationContext applicationContext;
    private final RestTemplate restTemplate;
    @Value("${gatewayHost:gateway}")
    private String gatewayHost;

    public NotificationsServiceClient(ApplicationContext applicationContext, RestTemplate restTemplate) {
        this.applicationContext = applicationContext;
        this.restTemplate = restTemplate;
    }

    public void sendNotification(String notificationText) {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setMessage(notificationText);
        notificationDto.setTimestamp(LocalDateTime.now());
        notificationDto.setSender(applicationContext.getApplicationName());
        var response = restTemplate.postForObject(
                "http://"+gatewayHost+"/notificationsService/notifications",notificationDto,String.class
        );
        System.out.println("Notifications Service Response: " + response);
    }
}
