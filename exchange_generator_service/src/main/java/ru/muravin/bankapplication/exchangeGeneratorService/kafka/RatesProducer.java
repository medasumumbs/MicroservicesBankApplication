package ru.muravin.bankapplication.exchangeGeneratorService.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import ru.muravin.bankapplication.exchangeGeneratorService.dto.CurrencyRateDto;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class RatesProducer {
    private KafkaTemplate<String, List<CurrencyRateDto>> kafkaTemplate;

    @Autowired
    public RatesProducer(KafkaTemplate<String, List<CurrencyRateDto>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${kafkaTopic:currency-rates}")
    private String kafkaTopic;

    @Value("${kafkaSecondsTimeout:10}")
    private Integer kafkaSecondsTimeout;

    public String sendCurrencyRates(List<CurrencyRateDto> rates) {
        log.info("Sending rates: {}", rates);
        var guid = UUID.randomUUID().toString();
        Message<List<CurrencyRateDto>> message = MessageBuilder
                .withPayload(rates)  // Объект в качестве значения
                .setHeader(KafkaHeaders.TOPIC, kafkaTopic)  // Топик
                .setHeader(KafkaHeaders.KEY, guid)
                .setHeader("idempotencyKey", guid)  // Произвольный заголовок
                .build();
        try {
            kafkaTemplate.send(message).get(kafkaSecondsTimeout, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return e.getMessage() + ":" + e.getCause().getMessage();
        }
        return "OK";
    }
}
