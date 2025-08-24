package com.tokio.financialtransfer.service;

import com.tokio.financialtransfer.model.FeeRule;
import com.tokio.financialtransfer.util.LoggingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class FeeCalculationService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    private static final List<FeeRule> FEE_RULES = Arrays.asList(
        new FeeRule(0, 0, "same_day", new BigDecimal("3.00"), new BigDecimal("0.025")),
        new FeeRule(1, 10, "1_to_10_days", new BigDecimal("12.00"), null),
        new FeeRule(11, 20, "11_to_20_days", null, new BigDecimal("0.082")),
        new FeeRule(21, 30, "21_to_30_days", null, new BigDecimal("0.069")),
        new FeeRule(31, 40, "31_to_40_days", null, new BigDecimal("0.047")),
        new FeeRule(41, 50, "41_to_50_days", null, new BigDecimal("0.017"))
    );

    public BigDecimal calculateFee(BigDecimal transferAmount, LocalDate transferDate) {
        LocalDate today = LocalDate.now();
        long daysDifference = ChronoUnit.DAYS.between(today, transferDate);
        
        logCalculationStart(today, daysDifference);
        
        var applicableRule = findApplicableRule(daysDifference);
        if (applicableRule == null) {
            throw new IllegalArgumentException("Não há taxa aplicável para transferências com mais de 50 dias");
        }
        
        BigDecimal fee = calculateFeeByRule(transferAmount, applicableRule);
        
        logCalculationSuccess(applicableRule.getType(), fee);
        return fee;
    }
    
    private void logCalculationStart(LocalDate today, long daysDifference) {
        LoggingContext.set("currentDate", today);
        LoggingContext.set("daysDifference", daysDifference);
        log.debug("Starting fee calculation for {} days difference", daysDifference);
    }
    
    private FeeRule findApplicableRule(long daysDifference) {
        return FEE_RULES.stream()
                .filter(rule -> rule.appliesTo(daysDifference))
                .findFirst()
                .orElse(null);
    }
    
    private BigDecimal calculateFeeByRule(BigDecimal transferAmount, FeeRule rule) {        
        if (rule.hasFixedAndPercentageFee()) {
            var fixedComponent = rule.getFixedFee();
            var percentageComponent = transferAmount.multiply(rule.getPercentageFee());
            var fee = fixedComponent.add(percentageComponent);
            
            LoggingContext.set("fixedFee", fixedComponent);
            LoggingContext.set("percentageFee", percentageComponent);
            return fee.setScale(SCALE, ROUNDING_MODE);
        }

        if (rule.hasOnlyFixedFee()) {
            return rule.getFixedFee().setScale(SCALE, ROUNDING_MODE);
        }

        return transferAmount.multiply(rule.getPercentageFee()).setScale(SCALE, ROUNDING_MODE);
    }
    
    private void logCalculationSuccess(String feeType, BigDecimal fee) {
        LoggingContext.set("feeType", feeType);
        LoggingContext.set(LoggingContext.FEE_AMOUNT, fee);
        log.info("Fee calculation completed successfully for type: {}", feeType);
    }
}
