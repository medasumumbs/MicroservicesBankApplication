package ru.muravin.bankapplication.uiService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityWebFilterChain(ServerHttpSecurity http) {
        return http.authorizeExchange(exchanges ->
                        exchanges
                                .anyExchange().permitAll())
                .httpBasic(basic -> {})
                .csrf(csrf -> csrf.disable()) // Отключаем CSRF только в тестах
                .build();
    }
}
