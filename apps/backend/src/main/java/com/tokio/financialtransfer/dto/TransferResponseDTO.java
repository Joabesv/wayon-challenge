package com.tokio.financialtransfer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDTO {

    private Long id;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal transferAmount;
    private BigDecimal fee;
    private LocalDate transferDate;
    private LocalDateTime scheduleDate;
}
