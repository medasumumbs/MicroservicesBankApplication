package ru.muravin.bankapplication.uiService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.uiService.service.AccountsService;
import ru.muravin.bankapplication.uiService.service.CurrenciesService;
import ru.muravin.bankapplication.uiService.service.ReactiveUserDetailsServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;

@Controller
@RequestMapping
public class AccountsController {
    private final AccountsService accountsService;
    private final ReactiveUserDetailsServiceImpl reactiveUserDetailsServiceImpl;
    private final CurrenciesService currenciesService;

    @Autowired
    public AccountsController(AccountsService accountsService, ReactiveUserDetailsServiceImpl reactiveUserDetailsServiceImpl, CurrenciesService currenciesService) {
        this.accountsService = accountsService;
        this.reactiveUserDetailsServiceImpl = reactiveUserDetailsServiceImpl;
        this.currenciesService = currenciesService;
    }

    @PostMapping("/user/{login}/accounts")
    public Mono<Rendering> changePassword(
            @PathVariable String login,
            ServerWebExchange exchange,
            Model model) {
        Mono<CsrfToken> tokenMono = exchange.getAttribute(CsrfToken.class.getName());
        var attributes = new HashMap<String,Object>();
        return tokenMono.flatMap(csrfToken -> {
            return exchange.getFormData().flatMap(data->{
                var errors = new ArrayList<>();
                var currency = data.getFirst("currency");
                attributes.put("_csrf", csrfToken);
                if ((currency==null) || (currency.isEmpty())) {
                    errors.add("Не передана валюта для регистрации счёта");
                }
                attributes.put("errors", errors);
                if (!errors.isEmpty()) {
                    model.addAllAttributes(attributes);
                    return Mono.just(Rendering.redirectTo("/main").modelAttributes(attributes).build());
                }
                return accountsService.addAccount(login, currency)
                    .flatMap(response -> {
                        return reactiveUserDetailsServiceImpl.getCurrentUser().flatMap(userDto -> {
                            return currenciesService.getCurrenciesList().collectList().flatMap(currencyDtos -> {
                                if (!response.equals("OK")) {
                                    attributes.put("passwordErrors", response);
                                    model.addAllAttributes(attributes);
                                    return Mono.just(Rendering.view("main").modelAttributes(attributes)
                                            .modelAttribute("birthdate", userDto.getDateOfBirth())
                                            .modelAttribute("name", userDto.getLastName() + " " + userDto.getFirstName() + " " + userDto.getPatronymic())
                                            .modelAttribute("currencies", currencyDtos)
                                            .build());
                                }
                                model.addAllAttributes(attributes);
                                return Mono.just(Rendering.redirectTo("/main").build());
                            });
                        });
                    });
            });
        });

    }
}
