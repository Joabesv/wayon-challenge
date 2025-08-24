package com.tokio.financialtransfer.config;

import com.tokio.financialtransfer.util.LoggingContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LoggingFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        var httpRequest = (HttpServletRequest) request;
        var httpResponse = (HttpServletResponse) response;
        
        // Generate correlation ID for this request
        String correlationId = UUID.randomUUID().toString();
        LoggingContext.setCorrelationId(correlationId);
        LoggingContext.set(LoggingContext.REQUEST_ID, correlationId);
        
        httpResponse.setHeader("X-Correlation-ID", correlationId);        
        LoggingContext.set("method", httpRequest.getMethod());
        LoggingContext.set("uri", httpRequest.getRequestURI());
        LoggingContext.set("userAgent", httpRequest.getHeader("User-Agent"));
        LoggingContext.set("remoteAddr", httpRequest.getRemoteAddr());
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Incoming HTTP request");
            chain.doFilter(request, response);            
        } finally {
            long duration = System.currentTimeMillis() - startTime;            
            LoggingContext.set("status", httpResponse.getStatus());
            LoggingContext.set("duration", duration + "ms");
            
            if (httpResponse.getStatus() >= 400) {
                log.warn("HTTP request completed with error");
            } else {
                log.info("HTTP request completed successfully");
            }
            
            LoggingContext.clear();
        }
    }
}
