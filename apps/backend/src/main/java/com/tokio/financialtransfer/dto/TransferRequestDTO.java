package com.tokio.financialtransfer.dto;

import com.tokio.financialtransfer.validation.FutureOrToday;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransferRequestDTO {

    @NotBlank(message = "Conta de origem é obrigatória")
    @Pattern(regexp = "\\d{10}", message = "Conta de origem deve ter 10 dígitos")
    private String sourceAccount;

    @NotBlank(message = "Conta de destino é obrigatória")
    @Pattern(regexp = "\\d{10}", message = "Conta de destino deve ter 10 dígitos")
    private String destinationAccount;

    @NotNull(message = "Valor da transferência é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal transferAmount;

    @NotNull(message = "Data da transferência é obrigatória")
    @FutureOrToday
    private LocalDate transferDate;
}
