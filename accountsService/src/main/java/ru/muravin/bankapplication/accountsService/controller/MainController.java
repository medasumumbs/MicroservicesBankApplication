package ru.muravin.bankapplication.accountsService.controller;

import org.springframework.web.bind.annotation.*;
import ru.muravin.bankapplication.accountsService.dto.AccountDto;
import ru.muravin.bankapplication.accountsService.dto.ChangePasswordDto;
import ru.muravin.bankapplication.accountsService.dto.UserDto;
import ru.muravin.bankapplication.accountsService.service.UserService;
import java.util.List;

@RestController
@RequestMapping
public class MainController {
    private final UserService userService;

    public MainController(UserService userService) {
        this.userService = userService;
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
        return userService.findAccountsByUsername(username);
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
}
