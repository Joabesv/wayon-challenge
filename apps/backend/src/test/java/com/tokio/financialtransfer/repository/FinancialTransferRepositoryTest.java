package com.tokio.financialtransfer.repository;

import com.tokio.financialtransfer.model.FinancialTransfer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Financial Transfer Repository Tests")
class FinancialTransferRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FinancialTransferRepository repository;

    private FinancialTransfer transfer1;
    private FinancialTransfer transfer2;
    private FinancialTransfer transfer3;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        transfer1 = FinancialTransfer.builder()
                .sourceAccount("1234567890")
                .destinationAccount("0987654321")
                .transferAmount(new BigDecimal("1000.00"))
                .fee(new BigDecimal("12.00"))
                .transferDate(LocalDate.now().plusDays(5))
                .scheduleDate(now.minusHours(2))
                .build();

        transfer2 = FinancialTransfer.builder()
                .sourceAccount("1111111111")
                .destinationAccount("2222222222")
                .transferAmount(new BigDecimal("500.00"))
                .fee(new BigDecimal("6.00"))
                .transferDate(LocalDate.now().plusDays(3))
                .scheduleDate(now.minusHours(1))
                .build();

        transfer3 = FinancialTransfer.builder()
                .sourceAccount("3333333333")
                .destinationAccount("4444444444")
                .transferAmount(new BigDecimal("2000.00"))
                .fee(new BigDecimal("24.00"))
                .transferDate(LocalDate.now().plusDays(10))
                .scheduleDate(now.minusHours(3))
                .build();
    }

    @Test
    @DisplayName("Should save transfer successfully")
    void shouldSaveTransferSuccessfully() {
        // When
        FinancialTransfer savedTransfer = repository.save(transfer1);

        // Then
        assertNotNull(savedTransfer);
        assertNotNull(savedTransfer.getId());
        assertEquals("1234567890", savedTransfer.getSourceAccount());
        assertEquals("0987654321", savedTransfer.getDestinationAccount());
        assertEquals(0, savedTransfer.getTransferAmount().compareTo(new BigDecimal("1000.00")));
        assertEquals(0, savedTransfer.getFee().compareTo(new BigDecimal("12.00")));
        assertNotNull(savedTransfer.getScheduleDate());
    }

    @Test
    @DisplayName("Should find transfer by id")
    void shouldFindTransferById() {
         
        FinancialTransfer savedTransfer = entityManager.persistAndFlush(transfer1);

        // When
        Optional<FinancialTransfer> found = repository.findById(savedTransfer.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(savedTransfer.getId(), found.get().getId());
        assertEquals("1234567890", found.get().getSourceAccount());
    }

    @Test
    @DisplayName("Should return empty when transfer not found by id")
    void shouldReturnEmptyWhenTransferNotFoundById() {
        // When
        Optional<FinancialTransfer> found = repository.findById(999L);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find all transfers ordered by schedule date desc")
    void shouldFindAllTransfersOrderedByScheduleDateDesc() {
         
        entityManager.persistAndFlush(transfer1); // oldest (now - 2h)
        entityManager.persistAndFlush(transfer2); // newest (now - 1h)
        entityManager.persistAndFlush(transfer3); // middle (now - 3h)

        // When
        List<FinancialTransfer> transfers = repository.findAllOrderByScheduleDateDesc();

        // Then
        assertEquals(3, transfers.size());
        
        // Should be ordered by schedule date descending (newest first)
        assertEquals("1111111111", transfers.get(0).getSourceAccount()); // transfer2 (newest)
        assertEquals("1234567890", transfers.get(1).getSourceAccount()); // transfer1 (middle)
        assertEquals("3333333333", transfers.get(2).getSourceAccount()); // transfer3 (oldest)
    }

    @Test
    @DisplayName("Should return empty list when no transfers exist")
    void shouldReturnEmptyListWhenNoTransfersExist() {
        // When
        List<FinancialTransfer> transfers = repository.findAllOrderByScheduleDateDesc();

        // Then
        assertTrue(transfers.isEmpty());
    }

    @Test
    @DisplayName("Should count all transfers")
    void shouldCountAllTransfers() {
         
        entityManager.persistAndFlush(transfer1);
        entityManager.persistAndFlush(transfer2);

        // When
        long count = repository.count();

        // Then
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should delete transfer by id")
    void shouldDeleteTransferById() {
         
        FinancialTransfer savedTransfer = entityManager.persistAndFlush(transfer1);
        Long transferId = savedTransfer.getId();

        // When
        repository.deleteById(transferId);
        entityManager.flush();

        // Then
        Optional<FinancialTransfer> found = repository.findById(transferId);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should auto-generate schedule date on persist")
    void shouldAutoGenerateScheduleDateOnPersist() {
         
        transfer1.setScheduleDate(null);

        // When
        FinancialTransfer savedTransfer = repository.save(transfer1);

        // Then
        assertNotNull(savedTransfer.getScheduleDate());
        assertTrue(savedTransfer.getScheduleDate().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(savedTransfer.getScheduleDate().isAfter(LocalDateTime.now().minusSeconds(10)));
    }

    @Test
    @DisplayName("Should preserve manually set schedule date")
    void shouldPreserveManuallySetScheduleDate() {
         
        LocalDateTime customScheduleDate = LocalDateTime.now().minusDays(1);
        transfer1.setScheduleDate(customScheduleDate);

        // When
        FinancialTransfer savedTransfer = repository.save(transfer1);

        // Then
        assertEquals(customScheduleDate, savedTransfer.getScheduleDate());
    }

    @Test
    @DisplayName("Should handle large transfer amounts")
    void shouldHandleLargeTransferAmounts() {
         
        transfer1.setTransferAmount(new BigDecimal("999999999999.99"));
        transfer1.setFee(new BigDecimal("999999999.99"));

        // When
        FinancialTransfer savedTransfer = repository.save(transfer1);

        // Then
        assertNotNull(savedTransfer.getId());
        assertEquals(0, savedTransfer.getTransferAmount().compareTo(new BigDecimal("999999999999.99")));
        assertEquals(0, savedTransfer.getFee().compareTo(new BigDecimal("999999999.99")));
    }

    @Test
    @DisplayName("Should handle minimal transfer amounts")
    void shouldHandleMinimalTransferAmounts() {
         
        transfer1.setTransferAmount(new BigDecimal("0.01"));
        transfer1.setFee(new BigDecimal("0.00"));

        // When
        FinancialTransfer savedTransfer = repository.save(transfer1);

        // Then
        assertNotNull(savedTransfer.getId());
        assertEquals(0, savedTransfer.getTransferAmount().compareTo(new BigDecimal("0.01")));
        assertEquals(0, savedTransfer.getFee().compareTo(new BigDecimal("0.00")));
    }

    @Test
    @DisplayName("Should persist and retrieve dates correctly")
    void shouldPersistAndRetrieveDatesCorrectly() {
         
        LocalDate futureDate = LocalDate.now().plusDays(30);
        LocalDateTime scheduleDateTime = LocalDateTime.now().minusHours(5);
        
        transfer1.setTransferDate(futureDate);
        transfer1.setScheduleDate(scheduleDateTime);

        // When
        FinancialTransfer savedTransfer = repository.save(transfer1);
        entityManager.flush();
        entityManager.clear();
        
        Optional<FinancialTransfer> retrieved = repository.findById(savedTransfer.getId());

        // Then
        assertTrue(retrieved.isPresent());
        assertEquals(futureDate, retrieved.get().getTransferDate());
        assertEquals(scheduleDateTime, retrieved.get().getScheduleDate());
    }
}
