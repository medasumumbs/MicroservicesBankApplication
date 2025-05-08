package ru.muravin.bankapplication.uiService.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.uiService.service.CurrenciesService;
import ru.muravin.bankapplication.uiService.service.ReactiveUserDetailsServiceImpl;

import java.util.HashMap;

@Controller
@RequestMapping
public class MainController {
    private final ReactiveUserDetailsServiceImpl reactiveUserDetailsServiceImpl;
    private final CurrenciesService currenciesService;

    public MainController(ReactiveUserDetailsServiceImpl reactiveUserDetailsServiceImpl, CurrenciesService currenciesService) {
        this.reactiveUserDetailsServiceImpl = reactiveUserDetailsServiceImpl;
        this.currenciesService = currenciesService;
    }

    @GetMapping("/main")
    public Mono<Rendering> mainPage(ServerWebExchange exchange) {
        Mono<CsrfToken> tokenMono = exchange.getAttribute(CsrfToken.class.getName());
        return currenciesService.getCurrenciesList().collectList().flatMap(currencyDtos -> {
            return reactiveUserDetailsServiceImpl.getCurrentUser().flatMap(userDto -> {
                return tokenMono.flatMap(csrfToken -> {
                    return Mono.just(Rendering.view("main").modelAttribute("_csrf",csrfToken)
                            .modelAttribute("changedPassword", exchange.getRequest().getQueryParams().getFirst("changedPassword"))
                            .modelAttribute("login", userDto.getLogin())
                            .modelAttribute("currencies", currencyDtos)
                            .modelAttribute("birthdate", userDto.getDateOfBirth())
                            .modelAttribute("name", userDto.getLastName() + " " + userDto.getFirstName() + " " + userDto.getPatronymic())
                            .build());
                });
            });
        });
    }

    @GetMapping
    public Mono<Rendering> index(ServerWebExchange exchange) {
        return mainPage(exchange);
    }

}
