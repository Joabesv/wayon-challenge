package com.tokio.financialtransfer.service;

import com.tokio.financialtransfer.dto.TransferRequestDTO;
import com.tokio.financialtransfer.dto.TransferResponseDTO;
import com.tokio.financialtransfer.model.FinancialTransfer;
import com.tokio.financialtransfer.repository.FinancialTransferRepository;
import com.tokio.financialtransfer.util.LoggingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinancialTransferService {

    private final FinancialTransferRepository repository;
    private final FeeCalculationService feeCalculationService;

    @Transactional
    public TransferResponseDTO scheduleTransfer(TransferRequestDTO request) {
        log.debug("Processing transfer scheduling request");
        
        BigDecimal fee = feeCalculationService.calculateFee(request.getTransferAmount(), request.getTransferDate());
        
        LoggingContext.set(LoggingContext.FEE_AMOUNT, fee);
        log.debug("Fee calculated for transfer");
        
        FinancialTransfer transfer = new FinancialTransfer(
            request.getSourceAccount(),
            request.getDestinationAccount(),
            request.getTransferAmount(),
            fee,
            request.getTransferDate()
        );

        FinancialTransfer savedTransfer = repository.save(transfer);
        
        LoggingContext.set(LoggingContext.TRANSFER_ID, savedTransfer.getId());
        LoggingContext.set("scheduleDate", savedTransfer.getScheduleDate());
        log.info("Transfer successfully saved to database");
        
        return mapToResponseDTO(savedTransfer);
    }

    public List<TransferResponseDTO> getAllTransfers() {
        log.debug("Fetching all transfers from database");
        
        List<FinancialTransfer> transfers = repository.findAllOrderByScheduleDateDesc();
        
        LoggingContext.set("transferCount", transfers.size());
        log.debug("Successfully retrieved transfers from database");
        
        return transfers.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public BigDecimal calculateFee(BigDecimal transferAmount, LocalDate transferDate) {
        log.debug("Delegating fee calculation to FeeCalculationService");
        
        BigDecimal fee = feeCalculationService.calculateFee(transferAmount, transferDate);
        LoggingContext.set(LoggingContext.FEE_AMOUNT, fee);
        log.debug("Fee calculation completed");
        return fee;
    }

    private TransferResponseDTO mapToResponseDTO(FinancialTransfer transfer) {
        return new TransferResponseDTO(
            transfer.getId(),
            transfer.getSourceAccount(),
            transfer.getDestinationAccount(),
            transfer.getTransferAmount(),
            transfer.getFee(),
            transfer.getTransferDate(),
            transfer.getScheduleDate()
        );
    }
}
