package com.tokio.financialtransfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeCalculationResponseDTO {
    
    @NotNull(message = "Taxa é obrigatória")
    @DecimalMin(value = "0.00", message = "Taxa deve ser maior ou igual a zero")
    private BigDecimal fee;
}
