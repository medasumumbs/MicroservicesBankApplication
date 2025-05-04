package ru.muravin.bankapplication.uiService.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String login;
    private String password;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String dateOfBirth;
}
