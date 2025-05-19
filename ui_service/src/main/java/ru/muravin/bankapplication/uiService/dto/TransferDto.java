package ru.muravin.bankapplication.uiService.dto;

import lombok.Data;

@Data
public class TransferDto {
    private String fromAccount;
    private String toAccount;
    private String amount;
    private String fromCurrency;
    private String toCurrency;
    private String action = "TRANSFER";
}
