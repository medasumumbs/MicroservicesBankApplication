package ru.muravin.bankapplication.currencyExchangeService.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "currency_rates")
@Setter
public class CurrencyRate {
    @Id
    private String currencyCode;
    private Float buyRate;
    private Float sellRate;
}
