package ru.muravin.bankapplication.transferService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.muravin.bankapplication.transferService.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class MainController {

    private final RestTemplate restTemplate;

    public MainController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping(value = "/transfer")
    public ResponseEntity<HttpResponseDto> transfer(@RequestBody OperationDto transfer) {
        if (transfer.getToAccount().equals(transfer.getFromAccount()) && transfer.getFromCurrency().equals(transfer.getToCurrency())) {
            return ResponseEntity.ok(new HttpResponseDto(
                "ERROR",
                "Счет получателя и счёт отправителя - один и тот же счёт. Перевод отклонён"
            ));
        }
        var fromAccountInfo = restTemplate.getForObject(
                "http://gateway/accountsService/getAccountInfo?username="
                        +transfer.getFromAccount()+"&currency="+transfer.getFromCurrency(),
                AccountInfoDto.class
        );
        if (fromAccountInfo == null) {
            return ResponseEntity.ok(
                new HttpResponseDto(
                    "ERROR",
                    "У пользователя " + transfer.getFromAccount() + " не открыт счет в выбранной валюте"
                )
            );
        }

        var toAccountInfo = restTemplate.getForObject(
                "http://gateway/accountsService/getAccountInfo?username="
                        +transfer.getToAccount()+"&currency="+transfer.getToCurrency(),
                AccountInfoDto.class
        );
        if (toAccountInfo == null) {
            return ResponseEntity.ok(
                new HttpResponseDto(
                    "ERROR",
                    "У пользователя " + transfer.getToAccount() + " не открыт счет в выбранной валюте"
                )
            );
        }

        var antifraudResponse = restTemplate.postForObject(
                "http://gateway/antifraudService/checkOperations",transfer,HttpResponseDto.class
        );
        if (!antifraudResponse.getStatusCode().equals("OK")) {
            return ResponseEntity.ok(antifraudResponse);
        }

        float finalAmountFrom = Float.parseFloat(transfer.getAmount());;
        float finalAmountTo = 0f;
        if (transfer.getFromCurrency().equals(transfer.getToCurrency())) {
            finalAmountTo = finalAmountFrom;

        } else {
            List<CurrencyRateDto> currencyRates = restTemplate.getForObject(
                    "http://gateway/currencyExchangeService/rates",List.class);
            Map<String, CurrencyRateDto> currencyRatesMap = new HashMap();
            currencyRates.forEach(currencyRateDto -> {
                currencyRatesMap.put(currencyRateDto.getCurrencyCode(), currencyRateDto);
            });
            finalAmountTo =  (currencyRatesMap.get(transfer.getFromCurrency()).getSellRate() * finalAmountFrom)
                    / (currencyRatesMap.get(transfer.getToCurrency()).getBuyRate());
        }

        if (finalAmountFrom > fromAccountInfo.getBalance()) {
            Map<String, String> currencies = new HashMap<>();
            currencies.put("RUB", "Российский рубль");
            currencies.put("EUR", "Евро");
            currencies.put("USD", "Доллар США");
            return ResponseEntity.ok(
                new HttpResponseDto(
                    "ERROR",
                    "На счёте недостаточно средств для списания. Не хватает: " +
                            (finalAmountFrom - fromAccountInfo.getBalance()) + " в валюте " +
                            currencies.get(transfer.getFromCurrency()))
            );
        }

        var toAccountNumber = toAccountInfo.getAccountNumber();
        var fromAccountNumber = fromAccountInfo.getAccountNumber();

        CheckedTransferDto finalRequestDto = new CheckedTransferDto();
        finalRequestDto.setAmountFrom(finalAmountFrom);
        finalRequestDto.setAmountTo(finalAmountTo);
        finalRequestDto.setFromAccountNumber(fromAccountNumber);
        finalRequestDto.setToAccountNumber(toAccountNumber);

        var accountsServiceResponse = restTemplate.postForObject(
                "http://gateway/accountsService/transfer",finalRequestDto,String.class
        );
        if (accountsServiceResponse == null) {
            return ResponseEntity.ok(
                new HttpResponseDto("ERROR","Не получен ответ от сервиса исполнения транзакций")
            );
        }
        if (!accountsServiceResponse.equals("OK")) {
            return ResponseEntity.ok(
                new HttpResponseDto("ERROR", accountsServiceResponse)
            );
        }

        return ResponseEntity.ok(new HttpResponseDto("OK","Перевод успешен"));
    }
    @ExceptionHandler
    public HttpResponseDto handleException(Exception ex) {
        ex.printStackTrace();
        return new HttpResponseDto("ERROR",ex.getLocalizedMessage());
    }
}
