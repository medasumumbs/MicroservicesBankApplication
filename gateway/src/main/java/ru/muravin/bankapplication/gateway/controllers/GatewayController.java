package ru.muravin.bankapplication.gateway.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.webflux.ProxyExchange;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class GatewayController {

    @Autowired
    private DiscoveryClient discoveryClient;

    public String getServiceUrl(String serviceId) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (instances != null && !instances.isEmpty()) {
            return instances.getFirst().getUri().toString();
        }
        return null;
    }

    @PostMapping("/notificationsService/notifications")
    public Mono<ResponseEntity<byte[]>> proxyNotificationsPost(ProxyExchange<byte[]> proxy) {
        String path = proxy.path("/notificationsService");
        System.out.println(getServiceUrl("notificationsService") + path);
        return proxy.uri(getServiceUrl("notificationsService") + path).post();
    }

    @GetMapping("/notificationsService/texts")
    public Mono<ResponseEntity<byte[]>> proxy(ProxyExchange<byte[]> proxy) {
        String path = proxy.path("/notificationsService");
        System.out.println(getServiceUrl("notificationsService") + path);
        return proxy.uri(getServiceUrl("notificationsService") + path).get();
    }

    @GetMapping("/notificationsService/discoveredServicesList")
    public Mono<ResponseEntity<byte[]>> proxyNotificationServiceList(ProxyExchange<byte[]> proxy) {
        String path = proxy.path("/notificationsService");
        System.out.println(getServiceUrl("notificationsService") + path);
        return proxy.uri(getServiceUrl("notificationsService") + path).get();
    }
}
