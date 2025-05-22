package ru.muravin.bankapplication.notificationsService;

import org.mapstruct.factory.Mappers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.testcontainers.utility.TestcontainersConfiguration;
import ru.muravin.bankapplication.accountsService.mapper.AccountMapper;
import ru.muravin.bankapplication.accountsService.mapper.UserMapper;

@Configuration
//@EnableWebSecurity
public class TestApplicationConfiguration {

    /*@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }*/

    @Bean
    public AccountMapper notificationMapper() {
        return Mappers.getMapper(AccountMapper.class);
    }

    @Bean
    public UserMapper userMapper() { return  Mappers.getMapper(UserMapper.class); }
}
