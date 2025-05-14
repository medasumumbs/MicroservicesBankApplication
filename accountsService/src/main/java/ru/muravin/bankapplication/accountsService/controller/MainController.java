package ru.muravin.bankapplication.accountsService.controller;

import org.springframework.web.bind.annotation.*;
import ru.muravin.bankapplication.accountsService.dto.*;
import ru.muravin.bankapplication.accountsService.service.AccountsService;
import ru.muravin.bankapplication.accountsService.service.UserService;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class MainController {
    private final UserService userService;
    private final AccountsService accountsService;

    public MainController(UserService userService, AccountsService accountsService) {
        this.userService = userService;
        this.accountsService = accountsService;
    }

    @PostMapping("/register")
    public String register(@RequestBody UserDto userDto) {
        Long id = null;
        try {
            id = userService.saveUser(userDto);
        } catch (Exception e) {
            return e.getMessage();
        }
        if (id != null) {
            return "OK";
        } else {
            return "UNKNOWN_PROBLEM";
        }
    }

    @GetMapping("/findAccountsByUsername")
    public List<AccountDto> getAccountsByUsername(@RequestParam("username") String username) {
        return accountsService.findAccountsByUsername(username);
    }


    @GetMapping("/findByUsername")
    public UserDto findByUsername(@RequestParam("username") String username) {
        System.out.println("Username received: " + username);
        return userService.findByUsername(username);
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        userService.updateUser(changePasswordDto);
        return "OK";
    }
    @PostMapping("/updateUserInfo")
    public String updateUserInfo(@RequestBody UserDto userDto) {
        userService.updateUserInfo(userDto);
        return "OK";
    }

    @PostMapping("/addAccount")
    public String addAccount(@RequestBody NewAccountDto newAccountDto) {
        return accountsService.addAccount(newAccountDto);
    }

    @PostMapping("/cashInOrCashOut")
    public String cashOperation(@RequestBody OperationDto operationDto) {
        if (operationDto.getAction().equals("PUT")) {
            return accountsService.cashIn(operationDto);
        }
        if (operationDto.getAction().equals("GET")) {
            return accountsService.cashOut(operationDto);
        }
        return "Не удалось распознать операцию " + operationDto.getAction();
    }
    @GetMapping("/users")
    public List<UserDto> getUsers() {
        return userService.findAll().stream().peek(userDto -> userDto.setPassword(null)).collect(Collectors.toList());
    }

    @GetMapping("/getAccountInfo")
    public AccountDto getAccountInfo(@RequestParam String currency, @RequestParam String username) {
        return accountsService.findAccountByUsernameAndCurrency(username, currency);
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody CheckedTransferDto transferDto) {
        return accountsService.transfer(transferDto);
    }
}
