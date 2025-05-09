package ru.muravin.bankapplication.notificationsService.dto;

import lombok.Data;

@Data
public class CashInCashOutDto {
    private String currencyCode;
    private String login;
    private String action;
    private String amount;
}
