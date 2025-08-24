package com.tokio.financialtransfer.dto;

import com.tokio.financialtransfer.validation.FutureOrToday;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FeeCalculationRequestDTO {

    @NotNull(message = "Valor da transferência é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal transferAmount;

    @NotNull(message = "Data da transferência é obrigatória")
    @FutureOrToday
    private LocalDate transferDate;
}
