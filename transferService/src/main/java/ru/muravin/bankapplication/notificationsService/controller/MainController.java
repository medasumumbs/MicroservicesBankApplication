package ru.muravin.bankapplication.notificationsService.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.notificationsService.dto.AccountInfoDto;
import ru.muravin.bankapplication.notificationsService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.notificationsService.dto.HttpResponseDto;
import ru.muravin.bankapplication.notificationsService.dto.OperationDto;

import java.util.List;

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
        var toAccountNumber = toAccountInfo.getAccountNumber();
        var fromAccountNumber = fromAccountInfo.getAccountNumber();
        float finalAmount = 0f;
        if (transfer.getFromCurrency().equals(transfer.getToCurrency())) {
            finalAmount = Float.parseFloat(transfer.getAmount());
        } else {
            List<CurrencyRateDto> currencyRates = restTemplate.getForObject("http://gateway/currencyExchangeService/rates",List.class);
            System.out.println(currencyRates);
            /// TODO сформировать две суммы - одну для списания с исходного счёта, вторую - для зачисления на целевой счет
            /// TODO первая сумма = первой сумме, вторая - умножить исходную на курс продажи, разделить на курс покупки
        }

        var antifraudResponse = restTemplate.postForObject(
                "http://gateway/antifraudService/checkOperations",transfer,HttpResponseDto.class
        );
        if (!antifraudResponse.getStatusCode().equals("OK")) {
            return ResponseEntity.ok(antifraudResponse);
        }
        return ResponseEntity.ok(new HttpResponseDto("OK","Перевод успешен"));
        /*
        var accountsServiceResponse = restTemplate.postForObject(
                "http://gateway/accountsService/transfer",transfer,String.class
        );
        /*
        if (!accountsServiceResponse.equals("OK")) {
            return ResponseEntity.ok(new HttpResponseDto("ERROR", accountsServiceResponse));
        }
        if (action.equals("GET")) {
            return ResponseEntity.ok(new HttpResponseDto("OK","Деньги успешно списаны"));
        }
        if (action.equals("PUT")) {
            return ResponseEntity.ok(new HttpResponseDto("OK","Деньги успешно зачислены"));
        }*/
        //return ResponseEntity.notFound().build();
    }
    @ExceptionHandler
    public HttpResponseDto handleException(Exception ex) {
        ex.printStackTrace();
        return new HttpResponseDto("ERROR",ex.getLocalizedMessage());
    }
}
