package com.tokio.financialtransfer.util;

import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

/**
 * Utility class for managing structured logging context with MDC
 */
public class LoggingContext {
    
    public static final String CORRELATION_ID = "correlationId";
    public static final String OPERATION = "operation";
    public static final String USER_ID = "userId";
    public static final String REQUEST_ID = "requestId";
    public static final String TRANSFER_ID = "transferId";
    public static final String SOURCE_ACCOUNT = "sourceAccount";
    public static final String DESTINATION_ACCOUNT = "destinationAccount";
    public static final String TRANSFER_AMOUNT = "transferAmount";
    public static final String FEE_AMOUNT = "feeAmount";
    public static final String TRANSFER_DATE = "transferDate";
    
    /**
     * Set correlation ID for request tracking
     */
    public static void setCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID, correlationId);
    }
    
    /**
     * Generate and set a new correlation ID
     */
    public static String generateAndSetCorrelationId() {
        String correlationId = UUID.randomUUID().toString();
        setCorrelationId(correlationId);
        return correlationId;
    }
    
    /**
     * Set operation name for tracking business operations
     */
    public static void setOperation(String operation) {
        MDC.put(OPERATION, operation);
    }
    
    /**
     * Set multiple context values at once
     */
    public static void setContext(Map<String, String> context) {
        context.forEach(MDC::put);
    }
    
    /**
     * Set a single context value
     */
    public static void set(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        }
    }
    
    /**
     * Set a single context value from object
     */
    public static void set(String key, Object value) {
        if (value != null) {
            MDC.put(key, value.toString());
        }
    }
    
    /**
     * Clear all MDC context
     */
    public static void clear() {
        MDC.clear();
    }
    
    /**
     * Clear specific key from MDC
     */
    public static void remove(String key) {
        MDC.remove(key);
    }
}
