package ru.muravin.bankapplication.currencyExchangeService.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.muravin.bankapplication.currencyExchangeService.controller.RatesController;
import ru.muravin.bankapplication.currencyExchangeService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.currencyExchangeService.dto.HttpResponseDto;
import ru.muravin.bankapplication.currencyExchangeService.service.CurrencyRatesService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RatesController.class)
public class RatesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrencyRatesService currencyRatesService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<CurrencyRateDto> ratesList;

    @BeforeEach
    void setUp() {
        CurrencyRateDto rate1 = new CurrencyRateDto("USD", 1.0f, 1.0f);
        CurrencyRateDto rate2 = new CurrencyRateDto("EUR", 0.85f, 0.85f);

        ratesList = Arrays.asList(rate1, rate2);
    }

    @Test
    void receiveRates_ShouldReturnOkAndSuccessMessage() throws Exception {
        // Given
        HttpResponseDto expectedResponse = new HttpResponseDto("OK", "Rates received");

        // When & Then
        mockMvc.perform(post("/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratesList)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(expectedResponse.getStatusCode()))
                .andExpect(jsonPath("$.statusMessage").value(expectedResponse.getStatusMessage()));

        verify(currencyRatesService, times(1)).saveRates(eq(ratesList));
    }

    @Test
    void getRates_ShouldReturnListOfRates() throws Exception {
        // Given
        when(currencyRatesService.findAll()).thenReturn(ratesList);

        // When & Then
        mockMvc.perform(get("/rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(ratesList.size()))
                .andExpect(jsonPath("$[0].currencyCode").value("USD"))
                .andExpect(jsonPath("$[0].buyRate").value(1.0f))
                .andExpect(jsonPath("$[1].currencyCode").value("EUR"))
                .andExpect(jsonPath("$[1].sellRate").value(0.85f));
    }

    @Test
    void receiveRates_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Given
        doThrow(new RuntimeException("Database error")).when(currencyRatesService).saveRates(anyList());

        // When & Then
        mockMvc.perform(post("/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratesList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("ERROR"))
                .andExpect(jsonPath("$.statusMessage").value("Database error"));

        verify(currencyRatesService, times(1)).saveRates(anyList());
    }
}
