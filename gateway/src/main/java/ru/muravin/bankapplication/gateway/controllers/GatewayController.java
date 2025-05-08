package ru.muravin.bankapplication.gateway.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.webflux.ProxyExchange;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
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

    @PostMapping("/accountsService/register")
    public Mono<ResponseEntity<byte[]>> proxyAccountsService(ProxyExchange<byte[]> proxy) {
        String path = proxy.path("/accountsService");
        System.out.println(getServiceUrl("accountsService") + path);
        return proxy.uri(getServiceUrl("accountsService") + path).post();
    }

    @PostMapping("/currencyExchangeService/rates")
    public Mono<ResponseEntity<byte[]>> proxyCurrencyExchange(ProxyExchange<byte[]> proxy) {
        String path = proxy.path("/currencyExchangeService");
        System.out.println(getServiceUrl("currencyExchangeService") + path);
        return proxy.uri(getServiceUrl("currencyExchangeService") + path).post();
    }
    @GetMapping("/currencyExchangeService/rates")
    public Mono<ResponseEntity<byte[]>> proxyCurrencyExchangeGetRates(ProxyExchange<byte[]> proxy) {
        String path = proxy.path("/currencyExchangeService");
        System.out.println(getServiceUrl("currencyExchangeService") + path);
        return proxy.uri(getServiceUrl("currencyExchangeService") + path).get();
    }

    @GetMapping("/accountsService/**")
    public Mono<ResponseEntity<byte[]>> proxyAccountsServiceFindByUsername(ProxyExchange<byte[]> proxy, @RequestParam(required = false) MultiValueMap<String, String> params) {
        String path = proxy.path("/accountsService");
        System.out.println(getServiceUrl("accountsService") + path + params.getFirst("username"));
        var uri = UriComponentsBuilder.fromHttpUrl(getServiceUrl("accountsService") + path).queryParams(params).build();
        return proxy.uri(uri.toUriString()).get();
    }
}
