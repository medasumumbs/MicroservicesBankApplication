package ru.muravin.bankapplication.accountsService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewAccountDto {
    private String currencyCode;
    private String login;
}
