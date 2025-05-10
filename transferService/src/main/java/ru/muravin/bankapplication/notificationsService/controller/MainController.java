package ru.muravin.bankapplication.notificationsService.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.notificationsService.dto.HttpResponseDto;
import ru.muravin.bankapplication.notificationsService.dto.OperationDto;

@RestController
@RequestMapping
public class MainController {

    private final RestTemplate restTemplate;

    public MainController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping(value = "/transfer")
    public ResponseEntity<HttpResponseDto> transfer(@RequestBody OperationDto transfer) {
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
