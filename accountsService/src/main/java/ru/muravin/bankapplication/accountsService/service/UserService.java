package ru.muravin.bankapplication.accountsService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.muravin.bankapplication.accountsService.dto.AccountDto;
import ru.muravin.bankapplication.accountsService.dto.ChangePasswordDto;
import ru.muravin.bankapplication.accountsService.dto.UserDto;
import ru.muravin.bankapplication.accountsService.mapper.AccountMapper;
import ru.muravin.bankapplication.accountsService.mapper.UserMapper;
import ru.muravin.bankapplication.accountsService.model.User;
import ru.muravin.bankapplication.accountsService.repository.AccountsRepository;
import ru.muravin.bankapplication.accountsService.repository.UsersRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountsRepository accountsRepository;
    private final AccountMapper accountMapper;

    @Autowired
    public UserService(UserMapper userMapper, UsersRepository usersRepository, PasswordEncoder passwordEncoder, AccountsRepository accountsRepository, AccountMapper accountMapper) {
        this.userMapper = userMapper;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountsRepository = accountsRepository;
        this.accountMapper = accountMapper;
    }

    public UserDto findByUsername(String username) {
        return usersRepository.findUserByLogin(username).map(userMapper::toDto).orElse(null);
    }

    public List<UserDto> findAll() {
        return usersRepository.findAll().stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    public void updateUser(ChangePasswordDto changePasswordDto) {
        var user = usersRepository.findUserByLogin(changePasswordDto.getLogin()).orElseThrow(
                () -> new RuntimeException("Пользователь не существует")
        );
        user.setPassword(changePasswordDto.getNewPassword());
        usersRepository.save(user);
    }

    public Long saveUser(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        if (userDto.getPassword() == null) {
            throw new IllegalArgumentException("Не передан пароль");
        }
        validateUserAndThrowIfError(userDto);
        user.setCreatedAt(LocalDateTime.now());
        var foundUser = usersRepository.findUserByLogin(userDto.getLogin());
        if (foundUser.isPresent()) {
            throw new RuntimeException("Пользователь с логином " + userDto.getLogin() + " уже зарегистрирован");
        }
        User savedUser = usersRepository.save(user);
        return savedUser.getId();
    }

    public void updateUserInfo(UserDto userDto) {
        validateUserAndThrowIfError(userDto);
        var user = usersRepository.findUserByLogin(userDto.getLogin()).orElseThrow(() -> {
            return new RuntimeException("Пользователь " + userDto.getLogin() + " не найден");
        });
        try {
            var date = new SimpleDateFormat("yyyy-MM-dd").parse(userDto.getDateOfBirth());
            var localDate = LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
            user.setDateOfBirth(localDate);
        } catch (Exception ignored) {}
        user.setName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPatronymic(userDto.getPatronymic());
        usersRepository.save(user);

    }

    private void validateUserAndThrowIfError(UserDto userDto) {
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
    }
}
