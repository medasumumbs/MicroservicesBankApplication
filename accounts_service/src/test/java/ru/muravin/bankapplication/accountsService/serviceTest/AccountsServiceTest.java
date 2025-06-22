package ru.muravin.bankapplication.accountsService.serviceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.muravin.bankapplication.accountsService.configuration.OAuth2SecurityConfig;
import ru.muravin.bankapplication.accountsService.dto.*;
import ru.muravin.bankapplication.accountsService.mapper.AccountMapper;
import ru.muravin.bankapplication.accountsService.model.Account;
import ru.muravin.bankapplication.accountsService.model.User;
import ru.muravin.bankapplication.accountsService.repository.AccountsRepository;
import ru.muravin.bankapplication.accountsService.repository.UsersRepository;
import ru.muravin.bankapplication.accountsService.service.AccountsService;
import ru.muravin.bankapplication.accountsService.service.NotificationsServiceClient;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.liquibase.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
})
@TestPropertySource(locations = "classpath:application.yml")
public class AccountsServiceTest {
    @MockitoBean
    private OAuth2SecurityConfig oAuth2SecurityConfig;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;
    @Autowired
    private AccountsService accountsService;

    @MockitoBean
    private AccountsRepository accountsRepository;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private AccountMapper accountMapper;

    @MockitoBean
    private NotificationsServiceClient notificationsServiceClient;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // === addAccount ===
    @Test
    void testAddAccount_UserNotFound_ReturnsErrorMessage() {
        NewAccountDto dto = new NewAccountDto("USD", "unknown");

        when(usersRepository.findUserByLogin("unknown")).thenReturn(Optional.empty());

        String result = accountsService.addAccount(dto);

        assertEquals("Пользователь unknown не найден", result);
    }

    @Test
    void testAddAccount_AccountAlreadyExists_ReturnsErrorMessage() {
        NewAccountDto dto = new NewAccountDto("USD", "john_doe");

        User user = new User();
        user.setId(1L);
        user.setLogin("john_doe");

        when(usersRepository.findUserByLogin("john_doe")).thenReturn(Optional.of(user));
        when(accountsRepository.findByUserIdAndCurrency("1", "USD")).thenReturn(new Account());

        String result = accountsService.addAccount(dto);

        assertEquals("У пользователя john_doe уже открыт счёт в выбранной валюте", result);
    }

    @Test
    void testAddAccount_ValidData_CreatesAccountAndSendsNotification() {
        NewAccountDto dto = new NewAccountDto("USD", "john_doe");

        User user = new User();
        user.setId(1L);
        user.setLogin("john_doe");

        when(usersRepository.findUserByLogin("john_doe")).thenReturn(Optional.of(user));
        when(accountsRepository.findByUserIdAndCurrency("1", "USD")).thenReturn(null);

        String result = accountsService.addAccount(dto);

        assertEquals("OK", result);
        verify(accountsRepository).save(accountCaptor.capture());
        Account savedAccount = accountCaptor.getValue();
        assertEquals("USD", savedAccount.getCurrency());
        assertEquals("1", savedAccount.getUserId());
        verify(notificationsServiceClient).sendNotification(eq("Registered user john_doe"), any());
    }

    // === cashIn ===
    @Test
    void testCashIn_UserNotFound_ReturnsError() {
        OperationDto dto = new OperationDto();
        dto.setLogin("unknown");
        dto.setCurrencyCode("USD");
        dto.setAmount("100");

        String result = accountsService.cashIn(dto);

        assertEquals("Пользователь unknown не найден", result);
    }

    @Test
    void testCashIn_ValidData_AddsToBalance() {
        OperationDto dto = new OperationDto();
        dto.setLogin("john_doe");
        dto.setCurrencyCode("USD");
        dto.setAmount("100");

        User user = new User();
        user.setId(1L);
        Account account = new Account();
        account.setId(1L);
        account.setBalance(0f);

        when(usersRepository.findUserByLogin("john_doe")).thenReturn(Optional.of(user));
        when(accountsRepository.findByUserIdAndCurrency("1", "USD")).thenReturn(account);

        String result = accountsService.cashIn(dto);

        assertEquals("OK", result);
        verify(accountsRepository).save(accountCaptor.capture());
        assertEquals(100f, accountCaptor.getValue().getBalance());
        verify(notificationsServiceClient).sendNotification(eq("Cash In john_doe sum = 100"), any());
    }

    // === cashOut ===
    @Test
    void testCashOut_InsufficientFunds_ReturnsError() {
        OperationDto dto = new OperationDto();
        dto.setLogin("john_doe");
        dto.setCurrencyCode("USD");
        dto.setAmount("200");

        User user = new User();
        user.setId(1L);
        Account account = new Account();
        account.setBalance(100f);

        when(usersRepository.findUserByLogin("john_doe")).thenReturn(Optional.of(user));
        when(accountsRepository.findByUserIdAndCurrency("1", "USD")).thenReturn(account);

        String result = accountsService.cashOut(dto);

        assertTrue(result.contains("На счету недостаточно средств"));
    }

    // === transfer ===
    @Test
    void testTransfer_AccountNotFound_ReturnsError() {
        CheckedTransferDto dto = new CheckedTransferDto();
        dto.setFromAccountNumber("999");
        dto.setToAccountNumber("888");
        dto.setAmountFrom(100d);
        dto.setAmountTo(100d);

        when(accountsRepository.findById(999L)).thenReturn(Optional.empty());

        String result = accountsService.transfer(dto);

        assertEquals("Не найден счет № 999; Счет был закрыт.", result);
    }

    @Test
    void testTransfer_InsufficientFunds_ReturnsError() {
        CheckedTransferDto dto = new CheckedTransferDto();
        dto.setFromAccountNumber("1");
        dto.setToAccountNumber("2");
        dto.setAmountFrom(200d);
        dto.setAmountTo(200d);

        Account fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setBalance(100f);

        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setBalance(50f);

        when(accountsRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountsRepository.findById(2L)).thenReturn(Optional.of(toAccount));

        String result = accountsService.transfer(dto);

        assertEquals("Недостаточно средств для перевода на счету № 1", result);
    }

    @Test
    void testTransfer_ValidData_TransfersSuccessfully() {
        CheckedTransferDto dto = new CheckedTransferDto();
        dto.setFromAccountNumber("1");
        dto.setToAccountNumber("2");
        dto.setAmountFrom(100d);
        dto.setAmountTo(100d);

        Account fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setBalance(200f);

        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setBalance(50f);

        when(accountsRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountsRepository.findById(2L)).thenReturn(Optional.of(toAccount));

        String result = accountsService.transfer(dto);

        assertEquals("OK", result);
        verify(accountsRepository, times(2)).save(any(Account.class));
        assertEquals(100f, fromAccount.getBalance());
        assertEquals(150f, toAccount.getBalance());
        verify(notificationsServiceClient).sendNotification(eq("Transfer: " + dto), any());
    }
}
