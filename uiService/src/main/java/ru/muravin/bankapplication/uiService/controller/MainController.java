package ru.muravin.bankapplication.uiService.controller;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.uiService.dto.UserDto;
import ru.muravin.bankapplication.uiService.service.CurrenciesService;
import ru.muravin.bankapplication.uiService.service.ReactiveUserDetailsServiceImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
                            .modelAttribute("userIsUpdated", exchange.getRequest().getQueryParams().getFirst("userIsUpdated"))
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

    @PostMapping("/user/{login}/changeInfo")
    public Mono<Rendering> changeUserInfo(ServerWebExchange exchange,
                                          @PathVariable String login,
                                          Model model) {
        Mono<CsrfToken> tokenMono = exchange.getAttribute(CsrfToken.class.getName());
        return tokenMono.flatMap(csrfToken -> {
            return reactiveUserDetailsServiceImpl.getCurrentUser().flatMap(userDto -> {
                return exchange.getFormData().flatMap(data -> {
                    var name = data.getFirst("name");
                    var birthDate = data.getFirst("birthDate");
                    var errors = new ArrayList<>();
                    if (name == null) {
                        errors.add("Не переданы новые фамилия, имя и отчество");
                        model.addAttribute("userAccountsErrors", errors);
                        return Mono.just(Rendering.view("main").modelAttribute("_csrf", csrfToken)
                                .modelAttribute("birthdate", userDto.getDateOfBirth())
                                .modelAttribute("name", userDto.getLastName() + " " + userDto.getFirstName() + " " + userDto.getPatronymic())
                                .build());
                    }
                    if (birthDate == null) {
                        errors.add("Не передана новая дата рождения");
                        model.addAttribute("userAccountsErrors", errors);
                        return Mono.just(Rendering.view("main").modelAttribute("_csrf", csrfToken)
                                .modelAttribute("birthdate", userDto.getDateOfBirth())
                                .modelAttribute("name", userDto.getLastName() + " " + userDto.getFirstName() + " " + userDto.getPatronymic())
                                .build());
                    }

                    if (name.split(" ").length < 2) {
                        errors.add("ФИО должно содержать как минимум фамилию и имя");
                        model.addAttribute("userAccountsErrors", errors);
                        return Mono.just(Rendering.view("main")
                                .modelAttribute("birthdate", userDto.getDateOfBirth())
                                .modelAttribute("name", userDto.getLastName() + " " + userDto.getFirstName() + " " + userDto.getPatronymic())
                                .modelAttribute("_csrf", csrfToken).build());
                    }

                    Date dateOfBirth = null;
                    try {
                        dateOfBirth = new SimpleDateFormat("yyyy-MM-dd").parse(birthDate);
                    } catch (ParseException e) {
                        errors.add("Использован некорректный формат даты. Корректный формат - yyyy-MM-dd");
                        model.addAttribute("userAccountsErrors", errors);
                        return Mono.just(Rendering.view("main")
                                .modelAttribute("birthdate", userDto.getDateOfBirth())
                                .modelAttribute("name", userDto.getLastName() + " " + userDto.getFirstName() + " " + userDto.getPatronymic())
                                .modelAttribute("_csrf", csrfToken).build());
                    }
                    if (dateOfBirth != null) {
                        if (Period.between(
                                LocalDate.ofInstant(dateOfBirth.toInstant(), ZoneId.systemDefault()), LocalDate.now()).get(ChronoUnit.YEARS) < 18) {
                            errors.add("Пользователь не может быть несовершеннолетним");
                            model.addAttribute("userAccountsErrors", errors);
                            return Mono.just(Rendering.view("main")
                                    .modelAttribute("birthdate", userDto.getDateOfBirth())
                                    .modelAttribute("name", userDto.getLastName() + " " + userDto.getFirstName() + " " + userDto.getPatronymic())
                                    .modelAttribute("_csrf", csrfToken).build());
                        }
                    }
                    Date finalDateOfBirth = dateOfBirth;
                    return reactiveUserDetailsServiceImpl.updateUserInfo(login, birthDate, name).flatMap(result -> {
                        if (!result.equals("OK")) {
                            errors.add(result);
                            model.addAttribute("userAccountsErrors", errors);
                            return Mono.just(Rendering.view("main")
                                    .modelAttribute("birthdate", userDto.getDateOfBirth())
                                    .modelAttribute("name", userDto.getLastName() + " " + userDto.getFirstName() + " " + userDto.getPatronymic())
                                    .modelAttribute("_csrf", csrfToken).build());
                        }
                        return ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {
                            var user = (UserDto) securityContext.getAuthentication().getPrincipal();
                            user.setDateOfBirth(new SimpleDateFormat("yyyy-MM-dd").format(finalDateOfBirth));
                            user.setFirstName(name.split(" ")[1]);
                            user.setLastName(name.split(" ")[0]);
                            if (name.split(" ").length > 2) user.setPatronymic(name.split(" ")[2]);
                            return Mono.just(Rendering.redirectTo("/main?userIsUpdated=true").build());
                        });
                    });
                });
            });
        });
    }

}
