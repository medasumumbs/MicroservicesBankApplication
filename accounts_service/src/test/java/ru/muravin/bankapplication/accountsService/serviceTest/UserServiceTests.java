package ru.muravin.bankapplication.accountsService.serviceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.muravin.bankapplication.accountsService.AccountsServiceApplication;
import ru.muravin.bankapplication.accountsService.configuration.OAuth2SecurityConfig;
import ru.muravin.bankapplication.accountsService.dto.ChangePasswordDto;
import ru.muravin.bankapplication.accountsService.dto.UserDto;
import ru.muravin.bankapplication.accountsService.mapper.AccountMapper;
import ru.muravin.bankapplication.accountsService.mapper.UserMapper;
import ru.muravin.bankapplication.accountsService.model.User;
import ru.muravin.bankapplication.accountsService.repository.UsersRepository;
import ru.muravin.bankapplication.accountsService.service.NotificationsServiceClient;
import ru.muravin.bankapplication.accountsService.service.UserService;


import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.liquibase.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
        "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
        },
        classes =AccountsServiceApplication.class)
@TestPropertySource(locations = "classpath:application.yml")
@ExtendWith(MockitoExtension.class)
public class UserServiceTests {
    @MockitoBean
    private OAuth2SecurityConfig oAuth2SecurityConfig;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Autowired
    private UserService userService;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private NotificationsServiceClient notificationsServiceClient;

    @MockitoBean
    private AccountMapper accountMapperImpl;


    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByUsername_UserExists_ReturnsUserDto() {
        String login = "john_doe";
        User user = new User();
        user.setLogin(login);

        UserDto userDto = UserDto.builder().build();
        userDto.setLogin(login);

        when(usersRepository.findUserByLogin(login)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.findByUsername(login);

        assertNotNull(result);
        assertEquals(login, result.getLogin());
        verify(usersRepository).findUserByLogin(login);
    }

    @Test
    void testFindByUsername_UserNotFound_ReturnsNull() {
        String login = "unknown_user";

        //when(usersRepository.findUserByLogin(login)).thenReturn(Optional.empty());

        UserDto result = userService.findByUsername(login);

        assertNull(result);
    }

    @Test
    void testUpdateUser_PasswordChange_Success() {
        String login = "john_doe";
        String newPassword = "newSecurePass123";

        ChangePasswordDto dto = ChangePasswordDto.builder().build();
        dto.setLogin(login);
        dto.setNewPassword(newPassword);

        User user = new User();
        user.setLogin(login);
        user.setPassword("oldPass");

        when(usersRepository.findUserByLogin(any())).thenReturn(Optional.of(user));

        userService.updateUser(dto);

        verify(usersRepository).save(userCaptor.capture());
        assertEquals(newPassword, userCaptor.getValue().getPassword());
        verify(notificationsServiceClient).sendNotification("Password updated for user " + login);
    }

    @Test
    void testUpdateUser_UserNotFound_ThrowsException() {
        ChangePasswordDto dto = ChangePasswordDto.builder().build();
        dto.setLogin("unknown");

        when(usersRepository.findUserByLogin("unknown")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.updateUser(dto));
        assertEquals("Пользователь не существует", exception.getMessage());
    }

    @Test
    void testSaveUser_ValidData_SavesAndSendsNotification() {
        UserDto dto = UserDto.builder().build();
        dto.setLogin("john_doe");
        dto.setPassword("password123");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setPatronymic("Smith");
        dto.setDateOfBirth("1990-01-01");

        User userEntity = new User();
        userEntity.setId(1L);
        userEntity.setLogin(dto.getLogin());
        userEntity.setPassword(dto.getPassword());

        when(usersRepository.findUserByLogin(dto.getLogin())).thenReturn(Optional.empty());
        when(userMapper.toEntity(dto)).thenReturn(userEntity);
        when(usersRepository.save(any(User.class))).thenReturn(userEntity);

        Long userId = userService.saveUser(dto);

        assertNotNull(userId);
        verify(usersRepository).save(userCaptor.capture());
        verify(notificationsServiceClient).sendNotification("User registered " + dto);
    }

    @Test
    void testSaveUser_LoginAlreadyExists_ThrowsException() {
        UserDto dto = UserDto.builder().build();
        dto.setLogin("john_doe");
        dto.setPassword("password123");
        dto.setDateOfBirth("1990-01-01");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setPatronymic("Smith");
        when(userMapper.toEntity(dto)).thenReturn(User.builder().build());
        when(usersRepository.findUserByLogin(dto.getLogin())).thenReturn(Optional.of(new User()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.saveUser(dto));
        assertEquals("Пользователь с логином john_doe уже зарегистрирован", exception.getMessage());
    }

    @Test
    void testSaveUser_PasswordIsNull_ThrowsException() {
        UserDto dto = UserDto.builder().build();
        dto.setLogin("john_doe");
        dto.setPassword(null); // Нет пароля
        dto.setDateOfBirth("1990-01-01");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.saveUser(dto));
        assertEquals("Не передан пароль", exception.getMessage());
    }

    @Test
    void testUpdateUserInfo_ValidData_UpdatesUser() {
        String login = "john_doe";
        String newDateOfBirth = "1990-01-01";

        UserDto dto = UserDto.builder().build();
        dto.setLogin(login);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setPatronymic("Smith");
        dto.setDateOfBirth(newDateOfBirth);

        User user = new User();
        user.setLogin(login);

        when(usersRepository.findUserByLogin(login)).thenReturn(Optional.of(user));

        userService.updateUserInfo(dto);

        verify(usersRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("John", savedUser.getName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals("Smith", savedUser.getPatronymic());
        LocalDate expectedDate = LocalDate.of(1990, 1, 1);
        assertEquals(expectedDate, savedUser.getDateOfBirth());
        verify(notificationsServiceClient).sendNotification("User information updated " + user + " -> " + dto);
    }

    @Test
    void testUpdateUserInfo_UserNotFound_ThrowsException() {
        UserDto dto = UserDto.builder().build();
        dto.setLogin("unknown");
        dto.setDateOfBirth("1990-01-01");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setPatronymic("Smith");
        when(usersRepository.findUserByLogin("unknown")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.updateUserInfo(dto));
        assertEquals("Пользователь unknown не найден", exception.getMessage());
    }

}
