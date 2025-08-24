package com.tokio.financialtransfer.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class FeeRule {
    private final int minDays;
    private final int maxDays;
    private final String type;
    private final BigDecimal fixedFee;
    private final BigDecimal percentageFee;
    
    public boolean appliesTo(long daysDifference) {
        return daysDifference >= minDays && daysDifference <= maxDays;
    }
    
    public boolean hasFixedAndPercentageFee() {
        return fixedFee != null && percentageFee != null;
    }
    
    public boolean hasOnlyFixedFee() {
        return fixedFee != null && percentageFee == null;
    }
    
    public boolean hasOnlyPercentageFee() {
        return fixedFee == null && percentageFee != null;
    }
}
