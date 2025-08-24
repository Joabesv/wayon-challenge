package com.tokio.financialtransfer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokio.financialtransfer.dto.FeeCalculationRequestDTO;
import com.tokio.financialtransfer.dto.TransferRequestDTO;
import com.tokio.financialtransfer.model.FinancialTransfer;
import com.tokio.financialtransfer.repository.FinancialTransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@DisplayName("Financial Transfer Integration Tests")
class FinancialTransferIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FinancialTransferRepository repository;

    private TransferRequestDTO validTransferRequest;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        
        validTransferRequest = new TransferRequestDTO();
        validTransferRequest.setSourceAccount("1234567890");
        validTransferRequest.setDestinationAccount("0987654321");
        validTransferRequest.setTransferAmount(new BigDecimal("1000.00"));
        validTransferRequest.setTransferDate(LocalDate.now().plusDays(5));
    }

    @Test
    @DisplayName("Should complete full transfer workflow successfully")
    void shouldCompleteFullTransferWorkflowSuccessfully() throws Exception {
        // Step 1: Calculate fee first
        FeeCalculationRequestDTO feeRequest = new FeeCalculationRequestDTO();
        feeRequest.setTransferAmount(new BigDecimal("1000.00"));
        feeRequest.setTransferDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/transfers/calculate-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.fee").value(12.00));

        // Step 2: Schedule the transfer
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.sourceAccount").value("1234567890"))
                .andExpect(jsonPath("$.data.destinationAccount").value("0987654321"))
                .andExpect(jsonPath("$.data.transferAmount").value(1000.00))
                .andExpect(jsonPath("$.data.fee").value(12.00));

        // Step 3: Verify transfer was saved to database
        List<FinancialTransfer> transfers = repository.findAll();
        assertEquals(1, transfers.size());
        
        FinancialTransfer savedTransfer = transfers.get(0);
        assertEquals("1234567890", savedTransfer.getSourceAccount());
        assertEquals("0987654321", savedTransfer.getDestinationAccount());
        assertEquals(0, savedTransfer.getTransferAmount().compareTo(new BigDecimal("1000.00")));
        assertEquals(0, savedTransfer.getFee().compareTo(new BigDecimal("12.00")));

        // Step 4: Retrieve all transfers
        mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].sourceAccount").value("1234567890"));
    }

    @Test
    @DisplayName("Should handle multiple transfers with different fee calculations")
    void shouldHandleMultipleTransfersWithDifferentFeeCalculations() throws Exception {
        // Same day transfer
        TransferRequestDTO sameDayTransfer = createTransferRequest("1111111111", "2222222222", 
                new BigDecimal("1000.00"), LocalDate.now());

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sameDayTransfer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fee").value(28.00)); // 3.00 + (1000 * 0.025)

        // 1-10 days transfer
        TransferRequestDTO shortTermTransfer = createTransferRequest("3333333333", "4444444444", 
                new BigDecimal("500.00"), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortTermTransfer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fee").value(12.00)); // Fixed fee

        // 11-20 days transfer
        TransferRequestDTO mediumTermTransfer = createTransferRequest("5555555555", "6666666666", 
                new BigDecimal("2000.00"), LocalDate.now().plusDays(15));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mediumTermTransfer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fee").value(164.00)); // 2000 * 0.082

        // Verify all transfers are saved
        List<FinancialTransfer> transfers = repository.findAll();
        assertEquals(3, transfers.size());

        // Get all transfers via API
        mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @DisplayName("Should validate business rules end-to-end")
    void shouldValidateBusinessRulesEndToEnd() throws Exception {
        // Test transfer beyond 50 days should fail fee calculation
        FeeCalculationRequestDTO invalidFeeRequest = new FeeCalculationRequestDTO();
        invalidFeeRequest.setTransferAmount(new BigDecimal("1000.00"));
        invalidFeeRequest.setTransferDate(LocalDate.now().plusDays(51));

        mockMvc.perform(post("/api/transfers/calculate-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFeeRequest)))
                .andExpect(status().isBadRequest());

        // Test invalid account format
        TransferRequestDTO invalidAccountTransfer = createTransferRequest("123", "0987654321", 
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAccountTransfer)))
                .andExpect(status().isBadRequest());

        // Test past date
        TransferRequestDTO pastDateTransfer = createTransferRequest("1234567890", "0987654321", 
                new BigDecimal("1000.00"), LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pastDateTransfer)))
                .andExpect(status().isBadRequest());

        // Verify no transfers were saved due to validation errors
        List<FinancialTransfer> transfers = repository.findAll();
        assertEquals(0, transfers.size());
    }

    @Test
    @DisplayName("Should handle edge cases for fee calculation")
    void shouldHandleEdgeCasesForFeeCalculation() throws Exception {
        TransferRequestDTO minAmountTransfer = createTransferRequest("1234567890", "0987654321", 
                new BigDecimal("0.01"), LocalDate.now());

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minAmountTransfer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fee").value(3.00)); // 3.00 + (0.01 * 0.025) rounded

        TransferRequestDTO largeAmountTransfer = createTransferRequest("1111111111", "2222222222", 
                new BigDecimal("999999.99"), LocalDate.now().plusDays(15));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(largeAmountTransfer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fee").value(82000.00)); // 999999.99 * 0.082 = 81999.9918 rounded to 82000.00

        TransferRequestDTO boundaryTransfer = createTransferRequest("3333333333", "4444444444", 
                new BigDecimal("1000.00"), LocalDate.now().plusDays(50));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boundaryTransfer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fee").value(17.00)); // 1000 * 0.017
    }

    @Test
    @DisplayName("Should maintain transfer order by schedule date")
    void shouldMaintainTransferOrderByScheduleDate() throws Exception {
        // Create transfers with small delays to ensure different schedule dates
        TransferRequestDTO transfer1 = createTransferRequest("1111111111", "2222222222", 
                new BigDecimal("100.00"), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer1)))
                .andExpect(status().isCreated());

        Thread.sleep(10); // Small delay to ensure different timestamps

        TransferRequestDTO transfer2 = createTransferRequest("3333333333", "4444444444", 
                new BigDecimal("200.00"), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer2)))
                .andExpect(status().isCreated());

        // Get transfers - should be ordered by schedule date DESC (newest first)
        mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sourceAccount").value("3333333333")) // Latest transfer first
                .andExpect(jsonPath("$.data[1].sourceAccount").value("1111111111")); // Earlier transfer second
    }

    private TransferRequestDTO createTransferRequest(String sourceAccount, String destinationAccount, 
                                                     BigDecimal amount, LocalDate transferDate) {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setSourceAccount(sourceAccount);
        request.setDestinationAccount(destinationAccount);
        request.setTransferAmount(amount);
        request.setTransferDate(transferDate);
        return request;
    }
}
