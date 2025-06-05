package ru.muravin.bankapplication.transferService.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.muravin.bankapplication.transferService.dto.*;
import ru.muravin.bankapplication.transferService.service.NotificationsServiceClient;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class MainController {

    private final RestTemplate restTemplate;
    private final NotificationsServiceClient notificationsServiceClient;

    @Value("${gatewayHost:gateway}")
    private String gatewayHost;

    public MainController(RestTemplate restTemplate, NotificationsServiceClient notificationsServiceClient) {
        this.restTemplate = restTemplate;
        this.notificationsServiceClient = notificationsServiceClient;
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
                "http://"+gatewayHost+"/accountsService/getAccountInfo?username="
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
                "http://"+gatewayHost+"/accountsService/getAccountInfo?username="
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
            "http://"+gatewayHost+"/antifraudService/checkOperations",transfer,HttpResponseDto.class
        );
        if (!antifraudResponse.getStatusCode().equals("OK")) {
            return ResponseEntity.ok(antifraudResponse);
        }

        Double finalAmountFrom = Double.parseDouble(transfer.getAmount());;
        Double finalAmountTo = 0d;
        if (transfer.getFromCurrency().equals(transfer.getToCurrency())) {
            finalAmountTo = finalAmountFrom;

        } else {
            List<LinkedHashMap> currencyRates = restTemplate.getForObject(
                    "http://"+gatewayHost+"/currencyExchangeService/rates",List.class);
            Map<String, LinkedHashMap> currencyRatesMap = new HashMap<>();
            currencyRates.forEach(currencyRateDto -> {
                currencyRatesMap.put((String) currencyRateDto.get("currencyCode"), currencyRateDto);
            });
            finalAmountTo =  ((Double)currencyRatesMap.get(transfer.getFromCurrency()).get("sellRate") * finalAmountFrom)
                    / ((Double)currencyRatesMap.get(transfer.getToCurrency()).get("buyRate"));
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
                "http://"+gatewayHost+"/accountsService/transfer",finalRequestDto,String.class
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

        notificationsServiceClient.sendNotification("Transfer Successful " + transfer);

        return ResponseEntity.ok(new HttpResponseDto("OK","Перевод успешен"));
    }
    @ExceptionHandler
    public HttpResponseDto handleException(Exception ex) {
        ex.printStackTrace();
        return new HttpResponseDto("ERROR",ex.getLocalizedMessage());
    }
}
