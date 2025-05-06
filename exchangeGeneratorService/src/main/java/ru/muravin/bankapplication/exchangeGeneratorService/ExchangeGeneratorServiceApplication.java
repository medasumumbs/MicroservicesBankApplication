package ru.muravin.bankapplication.exchangeGeneratorService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;


@SpringBootApplication
@EnableScheduling
public class ExchangeGeneratorServiceApplication {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }


    public static void main(String[] args) {
        SpringApplication.run(ExchangeGeneratorServiceApplication.class, args);
    }

}
