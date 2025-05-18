package ru.muravin.bankapplication.cashInCashOutService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.muravin.bankapplication.cashInCashOutService.dto.HttpResponseDto;
import ru.muravin.bankapplication.cashInCashOutService.dto.OperationDto;

@RestController
@RequestMapping
public class MainController {

    private final RestTemplate restTemplate;

    public MainController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping(value = "/withdrawCash") //username="+login+"&currency="+currency+"&amount="+amount
    public ResponseEntity<HttpResponseDto> withdrawCash(@RequestBody OperationDto cashInCashOutDto) {
        var action = cashInCashOutDto.getAction();

        var antifraudResponse = restTemplate.postForObject(
                "http://gateway/antifraudService/checkOperations",cashInCashOutDto,HttpResponseDto.class
        );
        if (!antifraudResponse.getStatusCode().equals("OK")) {
            return ResponseEntity.ok(antifraudResponse);
        }
        var accountsServiceResponse = restTemplate.postForObject(
                "http://gateway/accountsService/cashInOrCashOut",cashInCashOutDto,String.class
        );
        if (!accountsServiceResponse.equals("OK")) {
            return ResponseEntity.ok(new HttpResponseDto("ERROR", accountsServiceResponse));
        }
        if (action.equals("GET")) {
            return ResponseEntity.ok(new HttpResponseDto("OK","Деньги успешно списаны"));
        }
        if (action.equals("PUT")) {
            return ResponseEntity.ok(new HttpResponseDto("OK","Деньги успешно зачислены"));
        }
        return ResponseEntity.notFound().build();
    }
    @ExceptionHandler
    public HttpResponseDto handleException(Exception ex) {
        ex.printStackTrace();
        return new HttpResponseDto("ERROR",ex.getLocalizedMessage());
    }
}
