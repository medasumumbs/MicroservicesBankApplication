package ru.muravin.bankapplication.notificationsService.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/texts")
@RefreshScope
public class TestController {

    // Property из Config Server, которое может быть автоматически обновлено
    @Value("${text-to-print}")
    private String textToPrint;

    // Бин Bus-properties приложения. Используем, чтобы доставать busId
    // и понимать, какой из инстансов ответил на запрос
    private final BusProperties busProps;

    public TestController(BusProperties busProps) {
        this.busProps = busProps;
    }

    @GetMapping
    public String getText() {
        var message = busProps.getId() + ":\n" + textToPrint;
        System.out.println(message);
        return message;
    }

    @PostConstruct
    public void init() {
        System.out.println("Приложению присвоен busId : " + busProps.getId());
    }
}
