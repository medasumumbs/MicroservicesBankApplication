package ru.muravin.bankapplication.accountsService.controllerTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.muravin.bankapplication.accountsService.controller.MainController;
import ru.muravin.bankapplication.accountsService.dto.*;
import ru.muravin.bankapplication.accountsService.model.User;
import ru.muravin.bankapplication.accountsService.service.AccountsService;
import ru.muravin.bankapplication.accountsService.service.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
public class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AccountsService accountsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto userDto;
    private ChangePasswordDto changePasswordDto;
    private NewAccountDto newAccountDto;
    private OperationDto cashInDto;
    private OperationDto cashOutDto;
    private CheckedTransferDto transferDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder().build();
        userDto.setLogin("john_doe");
        userDto.setPassword("password123");

        changePasswordDto = ChangePasswordDto.builder().build();
        changePasswordDto.setLogin("john_doe");
        changePasswordDto.setNewPassword("newPass123");

        newAccountDto = new NewAccountDto("USD", "john_doe");

        cashInDto = new OperationDto();
        cashInDto.setAction("PUT");
        cashInDto.setLogin("john_doe");
        cashInDto.setAmount("100");
        cashInDto.setCurrencyCode("USD");

        cashOutDto = new OperationDto();
        cashOutDto.setAction("GET");
        cashOutDto.setLogin("john_doe");
        cashOutDto.setAmount("50");
        cashOutDto.setCurrencyCode("USD");

        transferDto = new CheckedTransferDto();
        transferDto.setFromAccountNumber("1");
        transferDto.setToAccountNumber("2");
        transferDto.setAmountFrom(100d);
        transferDto.setAmountTo(95d);
    }

    @Test
    void register_ShouldReturnOk_WhenUserIsSaved() throws Exception {
        when(userService.saveUser(any(UserDto.class))).thenReturn(1L);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        verify(userService, times(1)).saveUser(any(UserDto.class));
    }

    @Test
    void findByUsername_ShouldReturnUser() throws Exception {
        UserDto resultDto = UserDto.builder().login("john_doe").build();

        when(userService.findByUsername("john_doe")).thenReturn(resultDto);

        mockMvc.perform(get("/findByUsername")
                        .param("username", "john_doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("john_doe"));
    }

    @Test
    void changePassword_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/changePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        verify(userService, times(1)).updateUser(any(ChangePasswordDto.class));
    }

    @Test
    void updateUserInfo_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/updateUserInfo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        verify(userService, times(1)).updateUserInfo(any(UserDto.class));
    }

    @Test
    void addAccount_ShouldReturnOk() throws Exception {
        when(accountsService.addAccount(any(NewAccountDto.class))).thenReturn("OK");

        mockMvc.perform(post("/addAccount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAccountDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        verify(accountsService, times(1)).addAccount(any(NewAccountDto.class));
    }

    @Test
    void cashIn_ShouldReturnOk() throws Exception {
        when(accountsService.cashIn(any(OperationDto.class))).thenReturn("OK");

        mockMvc.perform(post("/cashInOrCashOut")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cashInDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void cashOut_ShouldReturnOk() throws Exception {
        when(accountsService.cashOut(any(OperationDto.class))).thenReturn("OK");

        mockMvc.perform(post("/cashInOrCashOut")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cashOutDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void getAccountsByUsername_ShouldReturnList() throws Exception {
        AccountDto accountDto = new AccountDto();
        List<AccountDto> list = Collections.singletonList(accountDto);

        when(accountsService.findAccountsByUsername("john_doe")).thenReturn(list);

        mockMvc.perform(get("/findAccountsByUsername")
                        .param("username", "john_doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getUsers_ShouldReturnListOfUsers() throws Exception {
        UserDto dto1 = UserDto.builder().build();
        dto1.setLogin("user1");

        UserDto dto2 = UserDto.builder().build();
        dto2.setLogin("user2");

        when(userService.findAll()).thenReturn(Arrays.asList(dto1, dto2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].login").value("user1"))
                .andExpect(jsonPath("$[1].login").value("user2"));
    }

    @Test
    void getAccountInfo_ShouldReturnAccountDto() throws Exception {
        AccountDto dto = new AccountDto();
        dto.setCurrency(CurrencyDto.builder().currencyCode("USD").build());

        when(accountsService.findAccountByUsernameAndCurrency("john_doe", "USD")).thenReturn(dto);

        mockMvc.perform(get("/getAccountInfo")
                        .param("username", "john_doe")
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency.currencyCode").value("USD"));
    }

    @Test
    void transfer_ShouldReturnOk() throws Exception {
        when(accountsService.transfer(any(CheckedTransferDto.class))).thenReturn("OK");

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}
