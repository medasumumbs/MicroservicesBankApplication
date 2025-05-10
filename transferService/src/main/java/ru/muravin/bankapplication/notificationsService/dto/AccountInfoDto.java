package ru.muravin.bankapplication.notificationsService.dto;

import lombok.Data;

@Data
public class AccountInfoDto {
    private String accountNumber;
    private String login;
    private String currency;
    private Float balance;
}
