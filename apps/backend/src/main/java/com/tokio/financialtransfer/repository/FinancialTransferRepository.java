package com.tokio.financialtransfer.repository;

import com.tokio.financialtransfer.model.FinancialTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialTransferRepository extends JpaRepository<FinancialTransfer, Long> {
    
    @Query("SELECT ft FROM FinancialTransfer ft ORDER BY ft.scheduleDate DESC")
    List<FinancialTransfer> findAllOrderByScheduleDateDesc();
}
