package ru.muravin.bankapplication.gateway.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.webflux.ProxyExchange;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.gateway.services.SecurityTokenService;

import java.util.List;

@RestController
public class GatewayController {

    @Autowired
    private DiscoveryClient discoveryClient;
    @Autowired
    private SecurityTokenService securityTokenService;

    public String getServiceUrl(String serviceId) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (instances != null && !instances.isEmpty()) {
            return instances.getFirst().getUri().toString();
        }
        return null;
    }

    private Mono<ResponseEntity<byte[]>> postWithAuthorization(ProxyExchange<byte[]> proxy, String serviceName) {
        var token = securityTokenService.getBearerToken().block();
        System.out.println("TOKEN: " + token);
        String path = proxy.path("/"+serviceName);
        System.out.println(getServiceUrl(serviceName) + path);
        return proxy.uri(getServiceUrl(serviceName) + path)
                .header("Authorization", "Bearer " + token)
                .sensitive()
                .post();
    }

    private Mono<ResponseEntity<byte[]>> getWithAuthorization(ProxyExchange<byte[]> proxy,
                                                              MultiValueMap<String, String> params,
                                                              String serviceName) {
        var token = securityTokenService.getBearerToken().block();
        String path = proxy.path("/" + serviceName);
        var uri = UriComponentsBuilder.fromHttpUrl(getServiceUrl(serviceName) + path).queryParams(params).build();
        System.out.println("Request URI: " + uri.toString());
        return proxy.uri(uri.toUriString())
                .header("Authorization", "Bearer " + token)
                .sensitive()
                .get();
    }

    @PostMapping("/notificationsService/notifications")
    public Mono<ResponseEntity<byte[]>> proxyNotificationsPost(ProxyExchange<byte[]> proxy) {
        return postWithAuthorization(proxy, "notificationsService");
    }

    @GetMapping("/notificationsService/texts")
    public Mono<ResponseEntity<byte[]>> proxy(ProxyExchange<byte[]> proxy) {
        return getWithAuthorization(proxy, null,"notificationsService");
    }

    @GetMapping("/notificationsService/discoveredServicesList")
    public Mono<ResponseEntity<byte[]>> proxyNotificationServiceList(ProxyExchange<byte[]> proxy) {
        return getWithAuthorization(proxy, null,"notificationsService");
    }
    @PostMapping("/accountsService/register")
    public Mono<ResponseEntity<byte[]>> proxyAccountsService(ProxyExchange<byte[]> proxy) {
        return postWithAuthorization(proxy,"accountsService");
    }
    @PostMapping("/accountsService/updateUserInfo")
    public Mono<ResponseEntity<byte[]>> proxyAccountsServiceUpdateUserInfo(ProxyExchange<byte[]> proxy) {
        return proxyAccountsService(proxy);
    }
    @PostMapping("/accountsService/changePassword")
    public Mono<ResponseEntity<byte[]>> proxyChangePassword(ProxyExchange<byte[]> proxy) {
        return proxyAccountsService(proxy);
    }
    @PostMapping("/accountsService/addAccount")
    public Mono<ResponseEntity<byte[]>> proxyAddAccount(ProxyExchange<byte[]> proxy) {
        return proxyAccountsService(proxy);
    }
    @PostMapping("/accountsService/cashInOrCashOut")
    public Mono<ResponseEntity<byte[]>> proxyCashInOrCashOut(ProxyExchange<byte[]> proxy) {
        return proxyAccountsService(proxy);
    }
    @PostMapping("/accountsService/transfer")
    public Mono<ResponseEntity<byte[]>> proxyAccountsTransfer(ProxyExchange<byte[]> proxy) {
        return proxyAccountsService(proxy);
    }

    @GetMapping("/accountsService/**")
    public Mono<ResponseEntity<byte[]>> proxyAccountsServiceFindByUsername(ProxyExchange<byte[]> proxy, @RequestParam(required = false) MultiValueMap<String, String> params) {
        return getWithAuthorization(proxy, params, "accountsService");
    }

    @PostMapping("/currencyExchangeService/rates")
    public Mono<ResponseEntity<byte[]>> proxyCurrencyExchange(ProxyExchange<byte[]> proxy) {
        return postWithAuthorization(proxy, "currencyExchangeService");
    }
    @GetMapping("/currencyExchangeService/rates")
    public Mono<ResponseEntity<byte[]>> proxyCurrencyExchangeGetRates(ProxyExchange<byte[]> proxy) {
        return getWithAuthorization(proxy, null,"currencyExchangeService");
    }

    @PostMapping("/cashInCashOutService/**")
    public Mono<ResponseEntity<byte[]>> proxyCashInCashOutService(ProxyExchange<byte[]> proxy, @RequestParam(required = false) MultiValueMap<String, String> params) {
        return postWithAuthorization(proxy,"cashInCashOutService");
    }

    @PostMapping("/antifraudService/**")
    public Mono<ResponseEntity<byte[]>> proxyAntifraudService(ProxyExchange<byte[]> proxy, @RequestParam(required = false) MultiValueMap<String, String> params) {
        return postWithAuthorization(proxy,"antifraudService");
    }
    @PostMapping("/transferService/transfer")
    public Mono<ResponseEntity<byte[]>> proxyTransferService(ProxyExchange<byte[]> proxy) {
        return postWithAuthorization(proxy, "transferService");
    }
}
