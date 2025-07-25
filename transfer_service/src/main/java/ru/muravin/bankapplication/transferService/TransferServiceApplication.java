package ru.muravin.bankapplication.transferService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class TransferServiceApplication {

    @LoadBalanced  // Делает RestTemplate "discovery-aware"
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }


    public static void main(String[] args) {
        SpringApplication.run(TransferServiceApplication.class, args);
    }

}
