package com.tokio.financialtransfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokio.financialtransfer.dto.FeeCalculationRequestDTO;
import com.tokio.financialtransfer.dto.TransferRequestDTO;
import com.tokio.financialtransfer.dto.TransferResponseDTO;
import com.tokio.financialtransfer.service.FinancialTransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FinancialTransferController.class)
@ActiveProfiles("test")
@DisplayName("Financial Transfer Controller Integration Tests")
class FinancialTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinancialTransferService financialTransferService;

    @Autowired
    private ObjectMapper objectMapper;

    private TransferRequestDTO validTransferRequest;
    private TransferResponseDTO transferResponse;
    private FeeCalculationRequestDTO feeCalculationRequest;

    @BeforeEach
    void setUp() {
        validTransferRequest = new TransferRequestDTO();
        validTransferRequest.setSourceAccount("1234567890");
        validTransferRequest.setDestinationAccount("0987654321");
        validTransferRequest.setTransferAmount(new BigDecimal("1000.00"));
        validTransferRequest.setTransferDate(LocalDate.now().plusDays(5));

        transferResponse = new TransferResponseDTO(
                1L,
                "1234567890",
                "0987654321",
                new BigDecimal("1000.00"),
                new BigDecimal("12.00"),
                LocalDate.now().plusDays(5),
                LocalDateTime.now()
        );

        feeCalculationRequest = new FeeCalculationRequestDTO();
        feeCalculationRequest.setTransferAmount(new BigDecimal("1000.00"));
        feeCalculationRequest.setTransferDate(LocalDate.now().plusDays(5));
    }

    @Test
    @DisplayName("Should schedule transfer successfully")
    void shouldScheduleTransferSuccessfully() throws Exception {
         
        when(financialTransferService.scheduleTransfer(any(TransferRequestDTO.class)))
                .thenReturn(transferResponse);

        
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Transfer scheduled successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.sourceAccount").value("1234567890"))
                .andExpect(jsonPath("$.data.destinationAccount").value("0987654321"))
                .andExpect(jsonPath("$.data.transferAmount").value(1000.00))
                .andExpect(jsonPath("$.data.fee").value(12.00));
    }

    @Test
    @DisplayName("Should return validation error for invalid source account")
    void shouldReturnValidationErrorForInvalidSourceAccount() throws Exception {
         
        validTransferRequest.setSourceAccount("123"); // Invalid format

        
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return validation error for invalid destination account")
    void shouldReturnValidationErrorForInvalidDestinationAccount() throws Exception {
         
        validTransferRequest.setDestinationAccount("invalid"); // Invalid format

        
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return validation error for negative transfer amount")
    void shouldReturnValidationErrorForNegativeTransferAmount() throws Exception {
         
        validTransferRequest.setTransferAmount(new BigDecimal("-100.00"));

        
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return validation error for zero transfer amount")
    void shouldReturnValidationErrorForZeroTransferAmount() throws Exception {
         
        validTransferRequest.setTransferAmount(BigDecimal.ZERO);

        
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return validation error for past transfer date")
    void shouldReturnValidationErrorForPastTransferDate() throws Exception {
         
        validTransferRequest.setTransferDate(LocalDate.now().minusDays(1));

        
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return validation error for null transfer date")
    void shouldReturnValidationErrorForNullTransferDate() throws Exception {
         
        validTransferRequest.setTransferDate(null);

        
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all transfers successfully")
    void shouldGetAllTransfersSuccessfully() throws Exception {
         
        TransferResponseDTO transfer2 = new TransferResponseDTO(
                2L,
                "1111111111",
                "2222222222",
                new BigDecimal("500.00"),
                new BigDecimal("6.00"),
                LocalDate.now().plusDays(3),
                LocalDateTime.now()
        );

        List<TransferResponseDTO> transfers = Arrays.asList(transferResponse, transfer2);
        when(financialTransferService.getAllTransfers()).thenReturn(transfers);

        
        mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Transfers retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[1].id").value(2));
    }

    @Test
    @DisplayName("Should return empty list when no transfers exist")
    void shouldReturnEmptyListWhenNoTransfersExist() throws Exception {
         
        when(financialTransferService.getAllTransfers()).thenReturn(Arrays.asList());

        
        mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Should calculate fee successfully")
    void shouldCalculateFeeSuccessfully() throws Exception {
         
        BigDecimal expectedFee = new BigDecimal("12.00");
        when(financialTransferService.calculateFee(any(BigDecimal.class), any(LocalDate.class)))
                .thenReturn(expectedFee);

        
        mockMvc.perform(post("/api/transfers/calculate-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feeCalculationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Fee calculated successfully"))
                .andExpect(jsonPath("$.data.fee").value(12.00));
    }

    @Test
    @DisplayName("Should return validation error for fee calculation with null amount")
    void shouldReturnValidationErrorForFeeCalculationWithNullAmount() throws Exception {
         
        feeCalculationRequest.setTransferAmount(null);

        
        mockMvc.perform(post("/api/transfers/calculate-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feeCalculationRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return validation error for fee calculation with negative amount")
    void shouldReturnValidationErrorForFeeCalculationWithNegativeAmount() throws Exception {
         
        feeCalculationRequest.setTransferAmount(new BigDecimal("-100.00"));

        
        mockMvc.perform(post("/api/transfers/calculate-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feeCalculationRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return validation error for fee calculation with past date")
    void shouldReturnValidationErrorForFeeCalculationWithPastDate() throws Exception {
         
        feeCalculationRequest.setTransferDate(LocalDate.now().minusDays(1));

        
        mockMvc.perform(post("/api/transfers/calculate-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feeCalculationRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle malformed JSON request")
    void shouldHandleMalformedJsonRequest() throws Exception {
        
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Should handle missing request body")
    void shouldHandleMissingRequestBody() throws Exception {
        
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Should handle CORS preflight request")
    void shouldHandleCorPreflightRequest() throws Exception {
        
        mockMvc.perform(options("/api/transfers")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk());
    }
}
