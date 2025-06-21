package ru.muravin.bankapplication.gateway.controllers;

import brave.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.webflux.ProxyExchange;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.gateway.services.SecurityTokenService;

import java.util.List;
import java.util.Optional;

@RestController
public class GatewayController {

    Logger log = LoggerFactory.getLogger(GatewayController.class);
    @Autowired
    private DiscoveryClient discoveryClient;
    @Autowired
    private SecurityTokenService securityTokenService;

    @Value("${usingIngress:false}")
    private String usingIngress;

    @Value("${ingressUrl:unknown}")
    private String ingressUrl;
    @Autowired
    private Tracer tracer;

    public String getServiceUrl(String serviceId) {
        tracer.nextSpan().name("getServiceUrl").tag("service", serviceId).start();
        System.out.println("\n GetServiceUrl: " + serviceId);
        System.out.println("Using Ingress: " + usingIngress);
        System.out.println("Using Ingress Url: " + ingressUrl);
        if (usingIngress.equals("true")) {
            log.info("Using Ingress, url Detected: {}", ingressUrl + "/" + serviceId);
            return ingressUrl+"/"+serviceId;
        }
        log.info("Not Using Ingress");
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (instances != null && !instances.isEmpty()) {
            return instances.getFirst().getUri().toString();
        }
        return null;
    }

    private Mono<ResponseEntity<byte[]>> postWithAuthorization(ProxyExchange<byte[]> proxy, String serviceName, ServerWebExchange exchange) {
        var token = securityTokenService.getBearerToken().block();
        System.out.println("TOKEN: " + token);
        String path = proxy.path("/"+serviceName);
        System.out.println(getServiceUrl(serviceName) + path);
        var headers = exchange.getRequest().getHeaders();
        log.info("HEADERS: " + headers.toString());
        return proxy.uri(getServiceUrl(serviceName) + path)
                .headers(headers)
                .header("Authorization", "Bearer " + token)
                .sensitive()
                .post();
    }

    private Mono<ResponseEntity<byte[]>> getWithAuthorization(ProxyExchange<byte[]> proxy,
                                                              MultiValueMap<String, String> params,
                                                              String serviceName, ServerWebExchange exchange) {
        var token = securityTokenService.getBearerToken().block();
        String path = proxy.path("/" + serviceName);
        var uri = UriComponentsBuilder.fromHttpUrl(getServiceUrl(serviceName) + path).queryParams(params).build();
        System.out.println("Request URI: " + uri.toString());
        var headers = exchange.getRequest().getHeaders();
        log.info("HEADERS: " + headers.toString());
        return proxy.uri(uri.toUriString())
                .headers(headers)
                .header("Authorization", "Bearer " + token)
                .sensitive()
                .get();
    }

    @PostMapping("/notificationsService/notifications")
    public Mono<ResponseEntity<byte[]>> proxyNotificationsPost(ProxyExchange<byte[]> proxy, ServerWebExchange exchange) {
        return postWithAuthorization(proxy, "notificationsService", exchange);
    }

    @GetMapping("/notificationsService/texts")
    public Mono<ResponseEntity<byte[]>> proxy(ProxyExchange<byte[]> proxy, ServerWebExchange exchange) {
        return getWithAuthorization(proxy, null,"notificationsService", exchange);
    }

    @GetMapping("/notificationsService/discoveredServicesList")
    public Mono<ResponseEntity<byte[]>> proxyNotificationServiceList(ProxyExchange<byte[]> proxy, ServerWebExchange exchange) {
        return getWithAuthorization(proxy, null,"notificationsService", exchange);
    }
    @PostMapping("/accountsService/register")
    public Mono<ResponseEntity<byte[]>> proxyAccountsService(ProxyExchange<byte[]> proxy, ServerWebExchange exchange) {
        return postWithAuthorization(proxy,"accountsService", exchange);
    }
    @PostMapping("/accountsService/updateUserInfo")
    public Mono<ResponseEntity<byte[]>> proxyAccountsServiceUpdateUserInfo(ProxyExchange<byte[]> proxy, ServerWebExchange exchange) {
        return proxyAccountsService(proxy, exchange);
    }
    @PostMapping("/accountsService/changePassword")
    public Mono<ResponseEntity<byte[]>> proxyChangePassword(ProxyExchange<byte[]> proxy, ServerWebExchange exchange) {
        return proxyAccountsService(proxy, exchange);
    }
    @PostMapping("/accountsService/addAccount")
    public Mono<ResponseEntity<byte[]>> proxyAddAccount(ProxyExchange<byte[]> proxy, ServerWebExchange exchange) {
        return proxyAccountsService(proxy, exchange);
    }
    @PostMapping("/accountsService/cashInOrCashOut")
    public Mono<ResponseEntity<byte[]>> proxyCashInOrCashOut(ProxyExchange<byte[]> proxy, ServerWebExchange exchange) {
        return proxyAccountsService(proxy, exchange);
    }
    @PostMapping("/accountsService/transfer")
    public Mono<ResponseEntity<byte[]>> proxyAccountsTransfer(ProxyExchange<byte[]> proxy, ServerWebExchange exchange) {
        return proxyAccountsService(proxy, exchange);
    }

    @GetMapping("/accountsService/**")
    public Mono<ResponseEntity<byte[]>> proxyAccountsServiceFindByUsername(ProxyExchange<byte[]> proxy, @RequestParam(required = false) MultiValueMap<String, String> params, ServerWebExchange exchange) {
        return getWithAuthorization(proxy, params, "accountsService", exchange);
    }

    @PostMapping("/currencyExchangeService/rates")
    public Mono<ResponseEntity<byte[]>> proxyCurrencyExchange(ProxyExchange<byte[]> proxy, ServerWebExchange exchange) {
        return postWithAuthorization(proxy, "currencyExchangeService", exchange);
    }
    @GetMapping("/currencyExchangeService/rates")
    public Mono<ResponseEntity<byte[]>> proxyCurrencyExchangeGetRates(ProxyExchange<byte[]> proxy, ServerWebExchange exchange) {
        return getWithAuthorization(proxy, null,"currencyExchangeService", exchange);
    }

    @PostMapping("/cashInCashOutService/**")
    public Mono<ResponseEntity<byte[]>> proxyCashInCashOutService(ProxyExchange<byte[]> proxy, @RequestParam(required = false) MultiValueMap<String, String> params, ServerWebExchange exchange) {
        return postWithAuthorization(proxy,"cashInCashOutService", exchange);
    }

    @PostMapping("/antifraudService/**")
    public Mono<ResponseEntity<byte[]>> proxyAntifraudService(ProxyExchange<byte[]> proxy, @RequestParam(required = false) MultiValueMap<String, String> params, ServerWebExchange exchange) {
        return postWithAuthorization(proxy,"antifraudService", exchange);
    }
    @PostMapping("/transferService/transfer")
    public Mono<ResponseEntity<byte[]>> proxyTransferService(ProxyExchange<byte[]> proxy, ServerWebExchange exchange) {
        return postWithAuthorization(proxy, "transferService", exchange);
    }
}
