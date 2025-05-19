package ru.muravin.bankapplication.accountsService.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangePasswordDto {
    private String newPassword;
    private String login;
}


