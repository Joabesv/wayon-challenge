package com.tokio.financialtransfer.service;

import com.tokio.financialtransfer.dto.TransferRequestDTO;
import com.tokio.financialtransfer.dto.TransferResponseDTO;
import com.tokio.financialtransfer.model.FinancialTransfer;
import com.tokio.financialtransfer.repository.FinancialTransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Financial Transfer Service Tests")
class FinancialTransferServiceTest {

    @Mock
    private FinancialTransferRepository repository;

    @Mock
    private FeeCalculationService feeCalculationService;

    @InjectMocks
    private FinancialTransferService financialTransferService;

    private TransferRequestDTO validTransferRequest;
    private FinancialTransfer savedTransfer;

    @BeforeEach
    void setUp() {
        validTransferRequest = new TransferRequestDTO();
        validTransferRequest.setSourceAccount("1234567890");
        validTransferRequest.setDestinationAccount("0987654321");
        validTransferRequest.setTransferAmount(new BigDecimal("1000.00"));
        validTransferRequest.setTransferDate(LocalDate.now().plusDays(5));

        savedTransfer = FinancialTransfer.builder()
                .id(1L)
                .sourceAccount("1234567890")
                .destinationAccount("0987654321")
                .transferAmount(new BigDecimal("1000.00"))
                .fee(new BigDecimal("12.00"))
                .transferDate(LocalDate.now().plusDays(5))
                .scheduleDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should schedule transfer successfully")
    void shouldScheduleTransferSuccessfully() {
         
        BigDecimal expectedFee = new BigDecimal("12.00");
        when(feeCalculationService.calculateFee(any(BigDecimal.class), any(LocalDate.class)))
                .thenReturn(expectedFee);
        when(repository.save(any(FinancialTransfer.class))).thenReturn(savedTransfer);

        // When
        TransferResponseDTO result = financialTransferService.scheduleTransfer(validTransferRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("1234567890", result.getSourceAccount());
        assertEquals("0987654321", result.getDestinationAccount());
        assertEquals(0, result.getTransferAmount().compareTo(new BigDecimal("1000.00")));
        assertEquals(0, result.getFee().compareTo(new BigDecimal("12.00")));
        assertEquals(validTransferRequest.getTransferDate(), result.getTransferDate());
        assertNotNull(result.getScheduleDate());

        // Verify interactions
        verify(feeCalculationService).calculateFee(eq(validTransferRequest.getTransferAmount()), eq(validTransferRequest.getTransferDate()));
        verify(repository).save(any(FinancialTransfer.class));
    }

    @Test
    @DisplayName("Should save transfer with calculated fee")
    void shouldSaveTransferWithCalculatedFee() {
         
        BigDecimal expectedFee = new BigDecimal("28.00");
        when(feeCalculationService.calculateFee(any(BigDecimal.class), any(LocalDate.class)))
                .thenReturn(expectedFee);
        when(repository.save(any(FinancialTransfer.class))).thenReturn(savedTransfer);

        // When
        financialTransferService.scheduleTransfer(validTransferRequest);

        // Then
        verify(repository).save(argThat(transfer -> {
            assertEquals("1234567890", transfer.getSourceAccount());
            assertEquals("0987654321", transfer.getDestinationAccount());
            assertEquals(0, transfer.getTransferAmount().compareTo(new BigDecimal("1000.00")));
            assertEquals(0, transfer.getFee().compareTo(expectedFee));
            assertEquals(validTransferRequest.getTransferDate(), transfer.getTransferDate());
            return true;
        }));
    }

    @Test
    @DisplayName("Should get all transfers successfully")
    void shouldGetAllTransfersSuccessfully() {
         
        FinancialTransfer transfer2 = FinancialTransfer.builder()
                .id(2L)
                .sourceAccount("1111111111")
                .destinationAccount("2222222222")
                .transferAmount(new BigDecimal("500.00"))
                .fee(new BigDecimal("6.00"))
                .transferDate(LocalDate.now().plusDays(3))
                .scheduleDate(LocalDateTime.now().minusHours(1))
                .build();

        List<FinancialTransfer> transfers = Arrays.asList(savedTransfer, transfer2);
        when(repository.findAllOrderByScheduleDateDesc()).thenReturn(transfers);

        // When
        List<TransferResponseDTO> result = financialTransferService.getAllTransfers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        TransferResponseDTO firstTransfer = result.get(0);
        assertEquals(1L, firstTransfer.getId());
        assertEquals("1234567890", firstTransfer.getSourceAccount());

        TransferResponseDTO secondTransfer = result.get(1);
        assertEquals(2L, secondTransfer.getId());
        assertEquals("1111111111", secondTransfer.getSourceAccount());

        verify(repository).findAllOrderByScheduleDateDesc();
    }

    @Test
    @DisplayName("Should return empty list when no transfers exist")
    void shouldReturnEmptyListWhenNoTransfersExist() {
         
        when(repository.findAllOrderByScheduleDateDesc()).thenReturn(Arrays.asList());

        // When
        List<TransferResponseDTO> result = financialTransferService.getAllTransfers();

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(repository).findAllOrderByScheduleDateDesc();
    }

    @Test
    @DisplayName("Should calculate fee using fee calculation service")
    void shouldCalculateFeeUsingFeeCalculationService() {
         
        BigDecimal transferAmount = new BigDecimal("2000.00");
        LocalDate transferDate = LocalDate.now().plusDays(15);
        BigDecimal expectedFee = new BigDecimal("164.00");

        when(feeCalculationService.calculateFee(transferAmount, transferDate))
                .thenReturn(expectedFee);

        // When
        BigDecimal result = financialTransferService.calculateFee(transferAmount, transferDate);

        // Then
        assertEquals(0, result.compareTo(expectedFee));
        verify(feeCalculationService).calculateFee(transferAmount, transferDate);
    }

    @Test
    @DisplayName("Should handle null transfer request gracefully")
    void shouldHandleNullTransferRequestGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            financialTransferService.scheduleTransfer(null);
        });
    }

    @Test
    @DisplayName("Should preserve transfer date in response")
    void shouldPreserveTransferDateInResponse() {
         
        LocalDate futureDate = LocalDate.now().plusDays(30);
        validTransferRequest.setTransferDate(futureDate);
        
        savedTransfer.setTransferDate(futureDate);
        
        when(feeCalculationService.calculateFee(any(BigDecimal.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("69.00"));
        when(repository.save(any(FinancialTransfer.class))).thenReturn(savedTransfer);

        // When
        TransferResponseDTO result = financialTransferService.scheduleTransfer(validTransferRequest);

        // Then
        assertEquals(futureDate, result.getTransferDate());
    }

    @Test
    @DisplayName("Should map entity to response DTO correctly")
    void shouldMapEntityToResponseDtoCorrectly() {
         
        when(feeCalculationService.calculateFee(any(BigDecimal.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("12.00"));
        when(repository.save(any(FinancialTransfer.class))).thenReturn(savedTransfer);

        // When
        TransferResponseDTO result = financialTransferService.scheduleTransfer(validTransferRequest);

        // Then
        assertEquals(savedTransfer.getId(), result.getId());
        assertEquals(savedTransfer.getSourceAccount(), result.getSourceAccount());
        assertEquals(savedTransfer.getDestinationAccount(), result.getDestinationAccount());
        assertEquals(savedTransfer.getTransferAmount(), result.getTransferAmount());
        assertEquals(savedTransfer.getFee(), result.getFee());
        assertEquals(savedTransfer.getTransferDate(), result.getTransferDate());
        assertEquals(savedTransfer.getScheduleDate(), result.getScheduleDate());
    }
}
