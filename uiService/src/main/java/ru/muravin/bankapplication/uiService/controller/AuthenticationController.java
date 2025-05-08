package ru.muravin.bankapplication.uiService.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.uiService.dto.UserDto;
import ru.muravin.bankapplication.uiService.service.ReactiveUserDetailsServiceImpl;

import java.sql.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping
public class AuthenticationController {
    private final ReactiveUserDetailsServiceImpl reactiveUserDetailsServiceImpl;

    public AuthenticationController(ReactiveUserDetailsServiceImpl reactiveUserDetailsServiceImpl) {
        this.reactiveUserDetailsServiceImpl = reactiveUserDetailsServiceImpl;
    }

    @GetMapping("/signup")
    public Mono<Rendering> signUpPage(ServerWebExchange exchange) {
        var attributes = new HashMap<String,Object>();
        Mono<CsrfToken> tokenMono = exchange.getAttribute(CsrfToken.class.getName());
        return tokenMono.flatMap(csrfToken -> {
            attributes.put("_csrf", csrfToken);
            return Mono.just(Rendering.view("signup").modelAttribute("_csrf",csrfToken).build());
        });
    }

    @PostMapping("/signup")
    public Mono<Rendering> registerUser(/*@RequestParam String login,
                                        @RequestParam String password,
                                        @RequestParam(name = "confirm_password") String confirmPassword,
                                        @RequestParam(name = "name") String fioDelimitedBySpace,
                                        @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date dateOfBirth,*/
                                        ServerWebExchange exchange,
                                        Model model) {
        Mono<CsrfToken> tokenMono = exchange.getAttribute(CsrfToken.class.getName());
        var attributes = new HashMap<String,Object>();
        return tokenMono.flatMap(csrfToken -> {
            return exchange.getFormData().flatMap(data->{
                var errors = new ArrayList<>();
                var confirmPassword = data.getFirst("confirm_password");
                var password = data.getFirst("password");
                var login = data.getFirst("login");
                var fioDelimetedBySpace = data.getFirst("name");
                var dateOfBirthString = data.getFirst("dateOfBirth");
                if (login == null || login.isEmpty()) {
                    errors.add("Логин не заполнен");
                }
                if (confirmPassword == null || confirmPassword.isEmpty()) {
                    errors.add("Не введено подтверждение пароля");
                }
                if (password == null || password.isEmpty()) {
                    errors.add("Не введен пароль");
                }
                if (fioDelimetedBySpace == null || fioDelimetedBySpace.isEmpty()) {
                    errors.add("Отсутствует ФИО");
                } else {
                    if (fioDelimetedBySpace.split(" ").length < 2) {
                        errors.add("ФИО должно содержать как минимум фамилию и имя");
                    }
                }
                Date dateOfBirth = null;
                try {
                    dateOfBirth = new SimpleDateFormat("yyyy-MM-dd").parse(dateOfBirthString);
                } catch (ParseException e) {
                    errors.add("Использован некорректный формат даты. Корректный формат - yyyy-MM-dd");
                }
                if (dateOfBirth != null) {
                    if (Period.between(
                            LocalDate.ofInstant(dateOfBirth.toInstant(), ZoneId.systemDefault()), LocalDate.now()).get(ChronoUnit.YEARS) < 18) {
                        errors.add("Пользователь не может быть несовершеннолетним");
                    }
                }
                attributes.put("_csrf", csrfToken);
                if ((confirmPassword!=null) && (password!=null) && (!confirmPassword.equals(password))) {
                    errors.add("Пароли не совпадают");
                }
                attributes.put("errors", errors);
                if (!errors.isEmpty()) {
                    model.addAllAttributes(attributes);
                    return Mono.just(Rendering.view("signup").modelAttributes(attributes).build());
                }
                return reactiveUserDetailsServiceImpl.registerUser(
                        UserDto.builder()
                                .dateOfBirth(dateOfBirthString)
                                .login(login)
                                .password(password)
                                .lastName(fioDelimetedBySpace.split(" ")[0])
                                .firstName(fioDelimetedBySpace.split(" ")[1])
                                .patronymic(fioDelimetedBySpace.split(" ")[2])
                                .build()
                ).flatMap(response -> {
                    if (!response.equals("OK")) {
                        attributes.put("errors", response);
                        model.addAllAttributes(attributes);
                        return Mono.just(Rendering.view("signup").modelAttributes(attributes).build());
                    }
                    model.addAllAttributes(attributes);
                    return Mono.just(Rendering.redirectTo("/main?registeredNewUser=true").build());
                });

            });
        });

    }

    @PostMapping("/user/{login}/editPassword")
    public Mono<Rendering> changePassword(
            @PathVariable String login,
            ServerWebExchange exchange,
            Model model) {
        Mono<CsrfToken> tokenMono = exchange.getAttribute(CsrfToken.class.getName());
        var attributes = new HashMap<String,Object>();
        return tokenMono.flatMap(csrfToken -> {
            return exchange.getFormData().flatMap(data->{
                var errors = new ArrayList<>();
                var confirmPassword = data.getFirst("confirm_password");
                var password = data.getFirst("password");
                attributes.put("_csrf", csrfToken);
                if ((confirmPassword!=null) && (password!=null) && (!confirmPassword.equals(password))) {
                    errors.add("Пароли не совпадают");
                }
                attributes.put("errors", errors);
                if (!errors.isEmpty()) {
                    model.addAllAttributes(attributes);
                    return Mono.just(Rendering.redirectTo("/main").modelAttributes(attributes).build());
                }
                return reactiveUserDetailsServiceImpl.updatePassword(login, password)
                        .flatMap(response -> {
                    if (!response.equals("OK")) {
                        attributes.put("passwordErrors", response);
                        model.addAllAttributes(attributes);
                        return Mono.just(Rendering.view("main").modelAttributes(attributes).build());
                    }
                    model.addAllAttributes(attributes);
                    return Mono.just(Rendering.redirectTo("/main?changedPassword=true").build());
                });

            });
        });

    }
}
