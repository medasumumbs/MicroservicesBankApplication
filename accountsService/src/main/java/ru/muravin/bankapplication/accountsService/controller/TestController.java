package ru.muravin.bankapplication.accountsService.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping
@RefreshScope
public class TestController {

    @Autowired
    private DiscoveryClient discoveryClient;

    // Property из Config Server, которое может быть автоматически обновлено
    @Value("${text-to-print}")
    private String textToPrint;

    // Бин Bus-properties приложения. Используем, чтобы доставать busId
    // и понимать, какой из инстансов ответил на запрос
    private final BusProperties busProps;

    public TestController(BusProperties busProps) {
        this.busProps = busProps;
    }

    @GetMapping("/texts")
    public String getText() {
        var message = busProps.getId() + ":\n" + textToPrint;
        System.out.println(message);
        return message;
    }

    @GetMapping("/discoveredServicesList")
    public Map<String,String> discoveredServicesList() {
        var response = new HashMap<String, String>();
        AtomicInteger index = new AtomicInteger();
        discoveryClient.getServices().stream().forEach(s -> {
            index.getAndIncrement();
            response.put(index.toString(), s);
        });
        return response;
    }

    @PostConstruct
    public void init() {
        System.out.println("Приложению присвоен busId : " + busProps.getId());
    }
}
