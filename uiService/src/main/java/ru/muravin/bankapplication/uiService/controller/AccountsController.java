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
import java.util.List;
import java.util.Objects;

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
                return accountsService.addAccount(login, currency).flatMap(response -> {
                    return reactiveUserDetailsServiceImpl.getCurrentUser().flatMap(userDto -> {
                        return accountsService.findAllAccountsByUser(userDto.getLogin()).flatMap(accountDtos -> {
                            return currenciesService.getCurrenciesList().collectList().flatMap(currencyDtos -> {
                                if (!response.equals("OK")) {
                                    attributes.put("passwordErrors", response);
                                    model.addAllAttributes(attributes);
                                    return accountsService.findAllUsers().collectList().flatMap(allUsers -> {
                                        return Mono.just(Rendering.view("main").modelAttributes(attributes)
                                                .modelAttribute("birthdate", userDto.getDateOfBirth())
                                                .modelAttribute("accounts", accountDtos)
                                                .modelAttribute("name", userDto.getLastName() + " " + userDto.getFirstName() + " " + userDto.getPatronymic())
                                                .modelAttribute("currencies", currencyDtos)
                                                .modelAttribute("users", allUsers)
                                                .build());
                                    });
                                }
                                model.addAllAttributes(attributes);
                                return Mono.just(Rendering.redirectTo("/main").build());
                            });
                        });
                    });
                });
            });
        });

    }

    @PostMapping("/user/{login}/cash")
    public Mono<Rendering> withdrawCash(ServerWebExchange exchange, @PathVariable String login) {
        Mono<CsrfToken> tokenMono = exchange.getAttribute(CsrfToken.class.getName());
        return currenciesService.getCurrenciesList().collectList().flatMap(currencyDtos -> {
            return reactiveUserDetailsServiceImpl.getCurrentUser().flatMap(userDto -> {
                return accountsService.findAllAccountsByUser(userDto.getLogin()).flatMap(accountDtos -> {
                    return tokenMono.flatMap(csrfToken -> {
                        return exchange.getFormData().flatMap(form->{
                            return accountsService.withdrawCash(
                                    userDto.getLogin(), form.getFirst("currency"), form.getFirst("value"), form.getFirst("action")
                            ).flatMap(result -> {
                                if (!result.getStatusCode().equals("OK")) {
                                    return accountsService.findAllUsers().collectList().flatMap(allUsers -> {
                                        return Mono.just(Rendering.view("main").modelAttribute("_csrf", csrfToken)
                                            .modelAttribute("cashErrors", List.of(result.getStatusMessage()))
                                            .modelAttribute("login", userDto.getLogin())
                                            .modelAttribute("currencies", currencyDtos)
                                            .modelAttribute("accounts", accountDtos)
                                            .modelAttribute("users", allUsers)
                                            .modelAttribute("birthdate", userDto.getDateOfBirth())
                                            .modelAttribute("name", userDto.getLastName() + " " + userDto.getFirstName() + " " + userDto.getPatronymic())
                                            .build());
                                    });
                                }
                                return Mono.just(Rendering.redirectTo("/main").build());
                            });
                        });
                    });
                });
            });
        });
    }
    @PostMapping("/user/{login}/transfer")
    public Mono<Rendering> transfer(ServerWebExchange exchange, @PathVariable(name = "login") String currentUserLogin) {
        Mono<CsrfToken> tokenMono = exchange.getAttribute(CsrfToken.class.getName());
        return currenciesService.getCurrenciesList().collectList().flatMap(currencyDtos -> {
            return reactiveUserDetailsServiceImpl.getCurrentUser().flatMap(userDto -> {
                return accountsService.findAllAccountsByUser(userDto.getLogin()).flatMap(accountDtos -> {
                    return tokenMono.flatMap(csrfToken -> {
                        return exchange.getFormData().flatMap(form->{
                            var login = form.getFirst("to_login");
                            return accountsService.transfer(
                                    form.getFirst("from_currency"),
                                    form.getFirst("to_currency"),
                                    userDto.getLogin(),
                                    login,
                                    form.getFirst("value")
                            ).flatMap(result -> {
                                var transferErrorsModelKey = "";
                                System.out.println("Login from: " + userDto.getLogin() + " to: " + login);
                                if (Objects.equals(login, userDto.getLogin())) {
                                    transferErrorsModelKey = "transferErrors";
                                } else {
                                    transferErrorsModelKey = "transferOtherErrors";
                                }
                                if (!result.getStatusCode().equals("OK")) {
                                    String finalTransferErrorsModelKey = transferErrorsModelKey;
                                    return accountsService.findAllUsers().collectList().flatMap(allUsers -> {
                                        return Mono.just(Rendering.view("main").modelAttribute("_csrf", csrfToken)
                                            .modelAttribute(finalTransferErrorsModelKey, List.of(result.getStatusMessage()))
                                            .modelAttribute("login", userDto.getLogin())
                                            .modelAttribute("currencies", currencyDtos)
                                            .modelAttribute("accounts", accountDtos)
                                            .modelAttribute("users", allUsers)
                                            .modelAttribute("birthdate", userDto.getDateOfBirth())
                                            .modelAttribute("name", userDto.getLastName() + " " + userDto.getFirstName() + " " + userDto.getPatronymic())
                                            .build());
                                    });
                                }
                                return Mono.just(Rendering.redirectTo("/main").build());
                            });
                        });
                    });
                });
            });
        });
    }
}
