package ru.muravin.bankapplication.notificationsService.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class OAuth2SecurityConfig {


        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
                    .authorizeHttpRequests(authorizeRequests ->
                            authorizeRequests
                                    .anyRequest().authenticated() // All other requests need authentication
                    )
                    .oauth2ResourceServer(oauth2 ->
                            oauth2.jwt(Customizer.withDefaults()) // Enable JWT-based authentication for the resource server
                    );
            return http.build();
        }
    }
