package com.tokio.financialtransfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_transfers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Conta de origem é obrigatória")
    @Pattern(regexp = "\\d{10}", message = "Conta de origem deve ter 10 dígitos")
    @Column(name = "source_account", nullable = false, length = 10)
    private String sourceAccount;

    @NotBlank(message = "Conta de destino é obrigatória")
    @Pattern(regexp = "\\d{10}", message = "Conta de destino deve ter 10 dígitos")
    @Column(name = "destination_account", nullable = false, length = 10)
    private String destinationAccount;

    @NotNull(message = "Valor da transferência é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Column(name = "transfer_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal transferAmount;

    @NotNull(message = "Taxa é obrigatória")
    @Column(name = "fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal fee;

    @NotNull(message = "Data da transferência é obrigatória")
    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @NotNull(message = "Data de agendamento é obrigatória")
    @Column(name = "schedule_date", nullable = false)
    private LocalDateTime scheduleDate;

    @PrePersist
    private void prePersist() {
        if (this.scheduleDate == null) {
            this.scheduleDate = LocalDateTime.now();
        }
    }
}
