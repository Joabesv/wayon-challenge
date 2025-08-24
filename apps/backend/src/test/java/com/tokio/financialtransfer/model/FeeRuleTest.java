package com.tokio.financialtransfer.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Fee Rule Model Tests")
class FeeRuleTest {

    @Test
    @DisplayName("Should create fee rule with all parameters")
    void shouldCreateFeeRuleWithAllParameters() {
        // When
        FeeRule rule = new FeeRule(0, 0, "same_day", new BigDecimal("3.00"), new BigDecimal("0.025"));

        // Then
        assertEquals(0, rule.getMinDays());
        assertEquals(0, rule.getMaxDays());
        assertEquals("same_day", rule.getType());
        assertEquals(0, rule.getFixedFee().compareTo(new BigDecimal("3.00")));
        assertEquals(0, rule.getPercentageFee().compareTo(new BigDecimal("0.025")));
    }

    @Test
    @DisplayName("Should apply to days within range")
    void shouldApplyToDaysWithinRange() {
         
        FeeRule rule = new FeeRule(1, 10, "1_to_10_days", new BigDecimal("12.00"), null);

        // Then
        assertTrue(rule.appliesTo(1));
        assertTrue(rule.appliesTo(5));
        assertTrue(rule.appliesTo(10));
    }

    @Test
    @DisplayName("Should not apply to days outside range")
    void shouldNotApplyToDaysOutsideRange() {
         
        FeeRule rule = new FeeRule(1, 10, "1_to_10_days", new BigDecimal("12.00"), null);

        // Then
        assertFalse(rule.appliesTo(0));
        assertFalse(rule.appliesTo(11));
        assertFalse(rule.appliesTo(-1));
        assertFalse(rule.appliesTo(100));
    }

    @Test
    @DisplayName("Should apply to same day transfer (0 days)")
    void shouldApplyToSameDayTransfer() {
         
        FeeRule rule = new FeeRule(0, 0, "same_day", new BigDecimal("3.00"), new BigDecimal("0.025"));

        // Then
        assertTrue(rule.appliesTo(0));
        assertFalse(rule.appliesTo(1));
    }

    @Test
    @DisplayName("Should identify rule with fixed and percentage fee")
    void shouldIdentifyRuleWithFixedAndPercentageFee() {
         
        FeeRule rule = new FeeRule(0, 0, "same_day", new BigDecimal("3.00"), new BigDecimal("0.025"));

        // Then
        assertTrue(rule.hasFixedAndPercentageFee());
        assertFalse(rule.hasOnlyFixedFee());
        assertFalse(rule.hasOnlyPercentageFee());
    }

    @Test
    @DisplayName("Should identify rule with only fixed fee")
    void shouldIdentifyRuleWithOnlyFixedFee() {
         
        FeeRule rule = new FeeRule(1, 10, "1_to_10_days", new BigDecimal("12.00"), null);

        // Then
        assertFalse(rule.hasFixedAndPercentageFee());
        assertTrue(rule.hasOnlyFixedFee());
        assertFalse(rule.hasOnlyPercentageFee());
    }

    @Test
    @DisplayName("Should identify rule with only percentage fee")
    void shouldIdentifyRuleWithOnlyPercentageFee() {
         
        FeeRule rule = new FeeRule(11, 20, "11_to_20_days", null, new BigDecimal("0.082"));

        // Then
        assertFalse(rule.hasFixedAndPercentageFee());
        assertFalse(rule.hasOnlyFixedFee());
        assertTrue(rule.hasOnlyPercentageFee());
    }

    @Test
    @DisplayName("Should handle boundary values correctly")
    void shouldHandleBoundaryValuesCorrectly() {
         
        FeeRule rule = new FeeRule(21, 30, "21_to_30_days", null, new BigDecimal("0.069"));

        // Then
        assertFalse(rule.appliesTo(20));
        assertTrue(rule.appliesTo(21));
        assertTrue(rule.appliesTo(30));
        assertFalse(rule.appliesTo(31));
    }

    @Test
    @DisplayName("Should handle large day ranges")
    void shouldHandleLargeDayRanges() {
         
        FeeRule rule = new FeeRule(41, 50, "41_to_50_days", null, new BigDecimal("0.017"));

        // Then
        assertFalse(rule.appliesTo(40));
        assertTrue(rule.appliesTo(41));
        assertTrue(rule.appliesTo(45));
        assertTrue(rule.appliesTo(50));
        assertFalse(rule.appliesTo(51));
    }

    @Test
    @DisplayName("Should handle zero fees")
    void shouldHandleZeroFees() {
         
        FeeRule rule = new FeeRule(1, 10, "test", BigDecimal.ZERO, null);

        // Then
        assertTrue(rule.hasOnlyFixedFee());
        assertEquals(0, rule.getFixedFee().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should handle decimal percentage fees")
    void shouldHandleDecimalPercentageFees() {
         
        FeeRule rule = new FeeRule(11, 20, "test", null, new BigDecimal("0.082"));

        // Then
        assertTrue(rule.hasOnlyPercentageFee());
        assertEquals(0, rule.getPercentageFee().compareTo(new BigDecimal("0.082")));
    }

    @Test
    @DisplayName("Should handle negative day ranges")
    void shouldHandleNegativeDayRanges() {
         
        FeeRule rule = new FeeRule(-5, -1, "past_days", new BigDecimal("100.00"), null);

        // Then
        assertTrue(rule.appliesTo(-5));
        assertTrue(rule.appliesTo(-3));
        assertTrue(rule.appliesTo(-1));
        assertFalse(rule.appliesTo(0));
        assertFalse(rule.appliesTo(-6));
    }

    @Test
    @DisplayName("Should handle equals and hashCode correctly")
    void shouldHandleEqualsAndHashCodeCorrectly() {
         
        FeeRule rule1 = new FeeRule(1, 10, "test", new BigDecimal("12.00"), null);
        FeeRule rule2 = new FeeRule(1, 10, "test", new BigDecimal("12.00"), null);
        FeeRule rule3 = new FeeRule(11, 20, "test", new BigDecimal("12.00"), null);

        // Then
        assertEquals(rule1, rule2);
        assertEquals(rule1.hashCode(), rule2.hashCode());
        assertNotEquals(rule1, rule3);
    }

    @Test
    @DisplayName("Should handle toString correctly")
    void shouldHandleToStringCorrectly() {
         
        FeeRule rule = new FeeRule(1, 10, "1_to_10_days", new BigDecimal("12.00"), null);

        // When
        String toString = rule.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("10"));
        assertTrue(toString.contains("1_to_10_days"));
        assertTrue(toString.contains("12.00"));
    }
}
