package com.zeremonos.wastecollection.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for logging execution of service and controller methods
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
    private static final Logger perfLog = LoggerFactory.getLogger("performance");

    /**
     * Pointcut for all methods in controller package
     */
    @Pointcut("within(com.zeremonos.wastecollection.controller..*)")
    public void controllerPointcut() {
        // Method is empty as this is just a pointcut
    }

    /**
     * Pointcut for all methods in service package
     */
    @Pointcut("within(com.zeremonos.wastecollection.service..*)")
    public void servicePointcut() {
        // Method is empty as this is just a pointcut
    }

    /**
     * Log around controller methods
     */
    @Around("controllerPointcut()")
    public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        
        log.debug(">>> Controller method called: {}", methodName);
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            log.debug("<<< Controller method completed: {} in {}ms", methodName, duration);
            perfLog.info("CONTROLLER,{},{}", methodName, duration);
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("!!! Controller method failed: {} after {}ms - Error: {}", 
                methodName, duration, e.getMessage());
            throw e;
        }
    }

    /**
     * Log around service methods
     */
    @Around("servicePointcut()")
    public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        
        if (log.isDebugEnabled()) {
            log.debug(">>> Service method called: {} with args: {}", 
                methodName, Arrays.toString(args));
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            log.debug("<<< Service method completed: {} in {}ms", methodName, duration);
            
            // Log slow operations (> 1 second)
            if (duration > 1000) {
                log.warn("SLOW OPERATION: {} took {}ms", methodName, duration);
            }
            
            perfLog.info("SERVICE,{},{}", methodName, duration);
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("!!! Service method failed: {} after {}ms - Error: {} - Cause: {}", 
                methodName, duration, e.getMessage(), 
                e.getCause() != null ? e.getCause().getMessage() : "N/A");
            throw e;
        }
    }
}

