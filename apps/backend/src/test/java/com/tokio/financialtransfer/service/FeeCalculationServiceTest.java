package com.tokio.financialtransfer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Fee Calculation Service Tests")
class FeeCalculationServiceTest {

    private FeeCalculationService feeCalculationService;

    @BeforeEach
    void setUp() {
        feeCalculationService = new FeeCalculationService();
    }

    @Test
    @DisplayName("Should calculate fee for same day transfer (fixed + percentage)")
    void shouldCalculateFeeForSameDayTransfer() {
         
        BigDecimal transferAmount = new BigDecimal("1000.00");
        LocalDate transferDate = LocalDate.now();

        // When
        BigDecimal fee = feeCalculationService.calculateFee(transferAmount, transferDate);

        // Then
        // Fixed fee: 3.00 + Percentage fee: 1000 * 0.025 = 25.00
        // Total: 28.00
        assertEquals(0, fee.compareTo(new BigDecimal("28.00")));
    }

    @Test
    @DisplayName("Should calculate fee for 1-10 days transfer (fixed fee only)")
    void shouldCalculateFeeFor1To10DaysTransfer() {
         
        BigDecimal transferAmount = new BigDecimal("1000.00");
        LocalDate transferDate = LocalDate.now().plusDays(5);

        // When
        BigDecimal fee = feeCalculationService.calculateFee(transferAmount, transferDate);

        // Then
        // Fixed fee: 12.00
        assertEquals(0, fee.compareTo(new BigDecimal("12.00")));
    }

    @Test
    @DisplayName("Should calculate fee for 11-20 days transfer (percentage only)")
    void shouldCalculateFeeFor11To20DaysTransfer() {
         
        BigDecimal transferAmount = new BigDecimal("1000.00");
        LocalDate transferDate = LocalDate.now().plusDays(15);

        // When
        BigDecimal fee = feeCalculationService.calculateFee(transferAmount, transferDate);

        // Then
        // Percentage fee: 1000 * 0.082 = 82.00
        assertEquals(0, fee.compareTo(new BigDecimal("82.00")));
    }

    @Test
    @DisplayName("Should calculate fee for 21-30 days transfer")
    void shouldCalculateFeeFor21To30DaysTransfer() {
         
        BigDecimal transferAmount = new BigDecimal("1000.00");
        LocalDate transferDate = LocalDate.now().plusDays(25);

        // When
        BigDecimal fee = feeCalculationService.calculateFee(transferAmount, transferDate);

        // Then
        // Percentage fee: 1000 * 0.069 = 69.00
        assertEquals(0, fee.compareTo(new BigDecimal("69.00")));
    }

    @Test
    @DisplayName("Should calculate fee for 31-40 days transfer")
    void shouldCalculateFeeFor31To40DaysTransfer() {
         
        BigDecimal transferAmount = new BigDecimal("1000.00");
        LocalDate transferDate = LocalDate.now().plusDays(35);

        // When
        BigDecimal fee = feeCalculationService.calculateFee(transferAmount, transferDate);

        // Then
        // Percentage fee: 1000 * 0.047 = 47.00
        assertEquals(0, fee.compareTo(new BigDecimal("47.00")));
    }

    @Test
    @DisplayName("Should calculate fee for 41-50 days transfer")
    void shouldCalculateFeeFor41To50DaysTransfer() {
         
        BigDecimal transferAmount = new BigDecimal("1000.00");
        LocalDate transferDate = LocalDate.now().plusDays(45);

        // When
        BigDecimal fee = feeCalculationService.calculateFee(transferAmount, transferDate);

        // Then
        // Percentage fee: 1000 * 0.017 = 17.00
        assertEquals(0, fee.compareTo(new BigDecimal("17.00")));
    }

    @Test
    @DisplayName("Should throw exception for transfers beyond 50 days")
    void shouldThrowExceptionForTransfersBeyond50Days() {
         
        BigDecimal transferAmount = new BigDecimal("1000.00");
        LocalDate transferDate = LocalDate.now().plusDays(51);

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> feeCalculationService.calculateFee(transferAmount, transferDate)
        );

        assertEquals("Não há taxa aplicável para transferências com mais de 50 dias", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle boundary cases for day ranges")
    void shouldHandleBoundaryCasesForDayRanges() {
        BigDecimal transferAmount = new BigDecimal("1000.00");

        // Test boundary for 1-10 days range
        BigDecimal fee1Day = feeCalculationService.calculateFee(transferAmount, LocalDate.now().plusDays(1));
        BigDecimal fee10Days = feeCalculationService.calculateFee(transferAmount, LocalDate.now().plusDays(10));
        assertEquals(0, fee1Day.compareTo(new BigDecimal("12.00")));
        assertEquals(0, fee10Days.compareTo(new BigDecimal("12.00")));

        // Test boundary for 11-20 days range
        BigDecimal fee11Days = feeCalculationService.calculateFee(transferAmount, LocalDate.now().plusDays(11));
        BigDecimal fee20Days = feeCalculationService.calculateFee(transferAmount, LocalDate.now().plusDays(20));
        assertEquals(0, fee11Days.compareTo(new BigDecimal("82.00")));
        assertEquals(0, fee20Days.compareTo(new BigDecimal("82.00")));
    }

    @Test
    @DisplayName("Should handle decimal precision correctly")
    void shouldHandleDecimalPrecisionCorrectly() {
         
        BigDecimal transferAmount = new BigDecimal("123.45");
        LocalDate transferDate = LocalDate.now();

        // When
        BigDecimal fee = feeCalculationService.calculateFee(transferAmount, transferDate);

        // Then
        // Fixed fee: 3.00 + Percentage fee: 123.45 * 0.025 = 3.08625 -> 3.09 (rounded)
        // Total: 6.09
        assertEquals(0, fee.compareTo(new BigDecimal("6.09")));
        assertEquals(2, fee.scale()); // Should have 2 decimal places
    }

    @Test
    @DisplayName("Should calculate correctly for minimum transfer amount")
    void shouldCalculateCorrectlyForMinimumTransferAmount() {
         
        BigDecimal transferAmount = new BigDecimal("0.01");
        LocalDate transferDate = LocalDate.now();

        // When
        BigDecimal fee = feeCalculationService.calculateFee(transferAmount, transferDate);

        // Then
        // Fixed fee: 3.00 + Percentage fee: 0.01 * 0.025 = 0.00025 -> 0.00 (rounded)
        // Total: 3.00
        assertEquals(0, fee.compareTo(new BigDecimal("3.00")));
    }
}
