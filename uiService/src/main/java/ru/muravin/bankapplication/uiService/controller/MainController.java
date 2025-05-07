package ru.muravin.bankapplication.uiService.controller;

import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Controller
@RequestMapping
public class MainController {
    @GetMapping("/main")
    public Mono<Rendering> mainPage(ServerWebExchange exchange) {
        var attributes = new HashMap<String,Object>();
        Mono<CsrfToken> tokenMono = exchange.getAttribute(CsrfToken.class.getName());
        return tokenMono.flatMap(csrfToken -> {
            attributes.put("_csrf", csrfToken);
            return Mono.just(Rendering.view("main").modelAttribute("_csrf",csrfToken).build());
        });
    }

    @GetMapping
    public Mono<Rendering> index(ServerWebExchange exchange) {
        return mainPage(exchange);
    }

}
