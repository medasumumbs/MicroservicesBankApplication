package ru.muravin.bankapplication.accountsService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.muravin.bankapplication.accountsService.dto.UserDto;
import ru.muravin.bankapplication.accountsService.mapper.UserMapper;
import ru.muravin.bankapplication.accountsService.model.User;
import ru.muravin.bankapplication.accountsService.repository.UsersRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final UsersRepository usersRepository;

    @Autowired
    public UserService(UserMapper userMapper, UsersRepository usersRepository) {
        this.userMapper = userMapper;
        this.usersRepository = usersRepository;
    }

    public Long saveUser(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        if (userDto.getPassword() == null) {
            throw new IllegalArgumentException("Не передан пароль");
        }
        if (userDto.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Не передана дата рождения пользователя");
        }
        if (userDto.getLogin() == null) {
            throw new IllegalArgumentException("Не передан логин");
        }
        if (userDto.getFirstName() == null) {
            throw new IllegalArgumentException("Не передано имя");
        }
        if (userDto.getLastName() == null) {
            throw new IllegalArgumentException("Не передана фамилия");
        }
        Date dateOfBirth = null;
        try {
            dateOfBirth = new SimpleDateFormat("yyyy-MM-dd").parse(userDto.getDateOfBirth());
        } catch (Exception e) {
            throw new IllegalArgumentException("Использован некорректный формат даты. Корректный формат - yyyy-MM-dd");
        }
        if (dateOfBirth != null) {
            if (Period.between(
                    LocalDate.ofInstant(dateOfBirth.toInstant(), ZoneId.systemDefault()), LocalDate.now()).get(ChronoUnit.YEARS) < 18) {
                throw new IllegalArgumentException("Пользователь не может быть несовершеннолетним");
            }
        }
        user.setCreatedAt(LocalDateTime.now());
        var foundUser = usersRepository.findUserByLogin(userDto.getLogin());
        if (foundUser != null) {
            throw new RuntimeException("Пользователь с логином " + userDto.getLogin() + " уже зарегистрирован");
        }
        User savedUser = usersRepository.save(user);
        return savedUser.getId();
    }
}
