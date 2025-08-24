package com.tokio.financialtransfer.controller;

import com.tokio.financialtransfer.dto.ApiResponse;
import com.tokio.financialtransfer.dto.TransferRequestDTO;
import com.tokio.financialtransfer.dto.TransferResponseDTO;
import com.tokio.financialtransfer.dto.FeeCalculationRequestDTO;
import com.tokio.financialtransfer.dto.FeeCalculationResponseDTO;
import com.tokio.financialtransfer.service.FinancialTransferService;
import com.tokio.financialtransfer.util.LoggingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
@RequiredArgsConstructor
@Slf4j
public class FinancialTransferController {

    private final FinancialTransferService financialTransferService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransferResponseDTO>> scheduleTransfer(@Valid @RequestBody TransferRequestDTO request) {
        LoggingContext.setOperation("schedule_transfer");
        LoggingContext.set(LoggingContext.SOURCE_ACCOUNT, request.getSourceAccount());
        LoggingContext.set(LoggingContext.DESTINATION_ACCOUNT, request.getDestinationAccount());
        LoggingContext.set(LoggingContext.TRANSFER_AMOUNT, request.getTransferAmount());
        LoggingContext.set(LoggingContext.TRANSFER_DATE, request.getTransferDate());
        
        log.info("Starting transfer scheduling");
        
        var response = financialTransferService.scheduleTransfer(request);
        
        LoggingContext.set(LoggingContext.TRANSFER_ID, response.getId());
        LoggingContext.set(LoggingContext.FEE_AMOUNT, response.getFee());
        log.info("Transfer scheduled successfully");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Transfer scheduled successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransferResponseDTO>>> getAllTransfers() {
        LoggingContext.setOperation("get_all_transfers");
        log.info("Fetching all transfers");
        
        List<TransferResponseDTO> transfers = financialTransferService.getAllTransfers();
        
        LoggingContext.set("transferCount", transfers.size());
        log.info("Successfully retrieved transfers");
        
        return ResponseEntity.ok(ApiResponse.success(transfers, "Transfers retrieved successfully"));
    }

    @PostMapping("/calculate-fee")
    public ResponseEntity<ApiResponse<FeeCalculationResponseDTO>> calculateFee(@Valid @RequestBody FeeCalculationRequestDTO request) {
        LoggingContext.setOperation("calculate_fee");
        LoggingContext.set(LoggingContext.TRANSFER_AMOUNT, request.getTransferAmount());
        LoggingContext.set(LoggingContext.TRANSFER_DATE, request.getTransferDate());
        
        log.info("Starting fee calculation");
        
        BigDecimal fee = financialTransferService.calculateFee(request.getTransferAmount(), request.getTransferDate());
        
        LoggingContext.set(LoggingContext.FEE_AMOUNT, fee);
        log.info("Fee calculated successfully");
        
        return ResponseEntity.ok(ApiResponse.success(new FeeCalculationResponseDTO(fee), "Fee calculated successfully"));
    }
}
