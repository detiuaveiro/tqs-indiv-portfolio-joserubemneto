package com.zeremonos.wastecollection.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * HTTP Request/Response logging interceptor
 */
@Component
public class HttpLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HttpLoggingInterceptor.class);
    private static final String REQUEST_ID = "requestId";
    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, 
                            @NonNull HttpServletResponse response, 
                            @NonNull Object handler) {
        
        // Generate unique request ID
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(REQUEST_ID, requestId);
        
        // Store start time
        request.setAttribute(START_TIME, System.currentTimeMillis());
        
        // Log incoming request
        log.info(">>> HTTP {} {} from {} - Request ID: {}", 
            request.getMethod(), 
            request.getRequestURI(),
            request.getRemoteAddr(),
            requestId);
        
        // Log query parameters if present
        if (request.getQueryString() != null) {
            log.debug("Query parameters: {}", request.getQueryString());
        }
        
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, 
                               @NonNull HttpServletResponse response, 
                               @NonNull Object handler, 
                               Exception ex) {
        
        Long startTime = (Long) request.getAttribute(START_TIME);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
        
        String requestId = MDC.get(REQUEST_ID);
        
        if (ex != null) {
            log.error("<<< HTTP {} {} - Status: {} - Duration: {}ms - Request ID: {} - ERROR: {}", 
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration,
                requestId,
                ex.getMessage());
        } else {
            log.info("<<< HTTP {} {} - Status: {} - Duration: {}ms - Request ID: {}", 
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration,
                requestId);
        }
        
        // Log slow requests
        if (duration > 2000) {
            log.warn("SLOW REQUEST: {} {} took {}ms - Request ID: {}", 
                request.getMethod(), 
                request.getRequestURI(), 
                duration,
                requestId);
        }
        
        // Clean up MDC
        MDC.remove(REQUEST_ID);
    }
}

