package ru.muravin.bankapplication.antifraudService.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.muravin.bankapplication.antifraudService.AntifraudServiceApplication;
import ru.muravin.bankapplication.antifraudService.controller.MainController;
import ru.muravin.bankapplication.antifraudService.dto.OperationDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
@ContextConfiguration(classes = AntifraudServiceApplication.class)
public class MainControllerMockMvcTest {

    @MockitoBean
    private MeterRegistry meterRegistry;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCheckOperation_GET_Action_ReturnsOK() throws Exception {
        OperationDto dto = new OperationDto();
        dto.setAction("GET");
        dto.setAmount("500");

        String jsonRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/checkOperations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("OK"))
                .andExpect(jsonPath("$.statusMessage").value("Деньги можно снять"));
    }

    @Test
    void testCheckOperation_PUT_Action_ReturnsOK() throws Exception {
        OperationDto dto = new OperationDto();
        dto.setAction("PUT");
        dto.setAmount("500");

        String jsonRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/checkOperations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("OK"))
                .andExpect(jsonPath("$.statusMessage").value("Деньги могут быть зачислить на счёт"));
    }

    @Test
    void testCheckOperation_TRANSFER_Action_ReturnsOK() throws Exception {
        OperationDto dto = new OperationDto();
        dto.setAction("TRANSFER");
        dto.setAmount("500");

        String jsonRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/checkOperations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("OK"))
                .andExpect(jsonPath("$.statusMessage").value("Деньги могут быть переведены"));
    }

    @Test
    void testCheckOperation_HighAmount_ReturnsFraud() throws Exception {
        OperationDto dto = new OperationDto();
        dto.setAction("GET");
        dto.setAmount("100001");

        String jsonRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/checkOperations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("FRAUD"))
                .andExpect(jsonPath("$.statusMessage").value("Слишком большая сумма"));
    }

    @Test
    void testCheckOperation_InvalidAction_ReturnsNotFound() throws Exception {
        OperationDto dto = new OperationDto();
        dto.setAction("INVALID");
        dto.setAmount("500");

        String jsonRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/checkOperations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCheckOperation_NullAction_ReturnsNotFound() throws Exception {
        OperationDto dto = new OperationDto();
        dto.setAction(null);
        dto.setAmount("500");

        String jsonRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/checkOperations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCheckOperation_InvalidAmountFormat_ReturnsError() throws Exception {
        OperationDto dto = new OperationDto();
        dto.setAction("GET");
        dto.setAmount("not_a_number");

        String jsonRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/checkOperations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("ERROR"));
    }
}