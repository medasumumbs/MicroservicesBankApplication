package ru.muravin.bankapplication.accountsService.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.muravin.bankapplication.accountsService.dto.UserDto;
import ru.muravin.bankapplication.accountsService.service.UserService;

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
}
