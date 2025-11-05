package com.zeremonos.wastecollection.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for auditing important business operations
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger auditLog = LoggerFactory.getLogger("audit");

    /**
     * Audit service request creation
     */
    @Around("execution(* com.zeremonos.wastecollection.service.ServiceRequestService.createServiceRequest(..))")
    public Object auditCreateRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        
        auditLog.info("ACTION=CREATE_REQUEST,USER=citizen,DATA={}", 
            args.length > 0 ? args[0] : "N/A");
        
        Object result = joinPoint.proceed();
        
        auditLog.info("ACTION=CREATE_REQUEST,STATUS=SUCCESS,RESULT={}", result);
        
        return result;
    }

    /**
     * Audit service request cancellation
     */
    @Around("execution(* com.zeremonos.wastecollection.service.ServiceRequestService.cancelServiceRequest(..))")
    public Object auditCancelRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String token = args.length > 0 ? args[0].toString() : "UNKNOWN";
        
        auditLog.info("ACTION=CANCEL_REQUEST,USER=citizen,TOKEN={}", token);
        
        try {
            Object result = joinPoint.proceed();
            auditLog.info("ACTION=CANCEL_REQUEST,STATUS=SUCCESS,TOKEN={}", token);
            return result;
        } catch (Exception e) {
            auditLog.warn("ACTION=CANCEL_REQUEST,STATUS=FAILED,TOKEN={},ERROR={}", 
                token, e.getMessage());
            throw e;
        }
    }

    /**
     * Audit status updates (staff actions)
     */
    @Around("execution(* com.zeremonos.wastecollection.service.ServiceRequestService.updateStatus(..))")
    public Object auditUpdateStatus(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long requestId = args.length > 0 ? (Long) args[0] : null;
        Object newStatus = args.length > 1 ? args[1] : "UNKNOWN";
        
        auditLog.info("ACTION=UPDATE_STATUS,USER=staff,REQUEST_ID={},NEW_STATUS={}", 
            requestId, newStatus);
        
        try {
            Object result = joinPoint.proceed();
            auditLog.info("ACTION=UPDATE_STATUS,STATUS=SUCCESS,REQUEST_ID={},NEW_STATUS={}", 
                requestId, newStatus);
            return result;
        } catch (Exception e) {
            auditLog.warn("ACTION=UPDATE_STATUS,STATUS=FAILED,REQUEST_ID={},ERROR={}", 
                requestId, e.getMessage());
            throw e;
        }
    }
}

