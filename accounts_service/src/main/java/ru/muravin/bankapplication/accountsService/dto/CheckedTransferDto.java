package ru.muravin.bankapplication.accountsService.dto;

import lombok.Data;

@Data
public class CheckedTransferDto {
    private String fromAccountNumber;
    private String toAccountNumber;
    private Double amountFrom;
    private Double amountTo;
}
