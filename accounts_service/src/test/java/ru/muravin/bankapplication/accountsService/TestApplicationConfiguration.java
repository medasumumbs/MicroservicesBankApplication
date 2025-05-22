package ru.muravin.bankapplication.accountsService;

import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public AccountMapper accountMapper() {
        return Mappers.getMapper(AccountMapper.class);
    }

    @Bean
    public UserMapper userMapper() { return  Mappers.getMapper(UserMapper.class); }
}
