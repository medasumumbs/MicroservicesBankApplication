package ru.muravin.bankapplication.accountsService.service;

import brave.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.muravin.bankapplication.accountsService.dto.ChangePasswordDto;
import ru.muravin.bankapplication.accountsService.dto.UserDto;
import ru.muravin.bankapplication.accountsService.mapper.AccountMapper;
import ru.muravin.bankapplication.accountsService.mapper.UserMapper;
import ru.muravin.bankapplication.accountsService.model.User;
import ru.muravin.bankapplication.accountsService.repository.AccountsRepository;
import ru.muravin.bankapplication.accountsService.repository.UsersRepository;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountsRepository accountsRepository;
    private final AccountMapper accountMapper;
    private final NotificationsServiceClient notificationsServiceClient;
    private final Tracer tracer;

    @Autowired
    public UserService(UserMapper userMapper, UsersRepository usersRepository, PasswordEncoder passwordEncoder, AccountsRepository accountsRepository, AccountMapper accountMapper, NotificationsServiceClient notificationsServiceClient, Tracer tracer) {
        this.userMapper = userMapper;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountsRepository = accountsRepository;
        this.accountMapper = accountMapper;
        this.notificationsServiceClient = notificationsServiceClient;
        this.tracer = tracer;
    }

    public UserDto findByUsername(String username) {
        var span = tracer.nextSpan().name("db user find by username").start();
        var result = usersRepository.findUserByLogin(username).map(userMapper::toDto).orElse(null);
        span.finish();
        return result;
    }

    public List<UserDto> findAll() {
        var span = tracer.nextSpan().name("db user findAll").start();
        var result = usersRepository.findAll().stream().map(userMapper::toDto).collect(Collectors.toList());
        span.finish();
        return result;
    }

    public void updateUser(ChangePasswordDto changePasswordDto) {
        var span = tracer.nextSpan().name("db findUserByLogin").start();
        var user = usersRepository.findUserByLogin(changePasswordDto.getLogin()).orElseThrow(
                () -> new RuntimeException("Пользователь не существует")
        );
        span.finish();
        user.setPassword(changePasswordDto.getNewPassword());
        span = tracer.nextSpan().name("db Saving user").start();
        usersRepository.save(user);
        span.finish();
        notificationsServiceClient.sendNotification("Password updated for user " + user.getLogin(), user.getLogin());
    }

    public Long saveUser(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        if (userDto.getPassword() == null) {
            throw new IllegalArgumentException("Не передан пароль");
        }
        validateUserAndThrowIfError(userDto);
        user.setCreatedAt(LocalDateTime.now());
        var span = tracer.nextSpan().name("db findUserByLogin").start();
        var foundUser = usersRepository.findUserByLogin(userDto.getLogin());
        span.finish();
        if (foundUser.isPresent()) {
            throw new RuntimeException("Пользователь с логином " + userDto.getLogin() + " уже зарегистрирован");
        }
        span = tracer.nextSpan().name("db Saving user").start();
        User savedUser = usersRepository.save(user);
        span.finish();
        notificationsServiceClient.sendNotification("User registered " + userDto, user.getLogin());
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
        notificationsServiceClient.sendNotification("User information updated "
                + user.toString() + " -> " + userDto.toString(), user.getLogin());
        var span = tracer.nextSpan().name("db Saving user").start();
        usersRepository.save(user);
        span.finish();
    }

    public void validateUserAndThrowIfError(UserDto userDto) {
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
