package com.ai.knowledge.aspect;

import com.ai.knowledge.entity.OperationLog;
import com.ai.knowledge.repository.OperationLogRepository;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogRepository logRepository;

    @Pointcut("@annotation(com.ai.knowledge.annotation.OperationLog)")
    public void logPointCut() {
    }

    @AfterReturning(pointcut = "logPointCut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        handleLog(joinPoint, null);
    }

    private void handleLog(JoinPoint joinPoint, Exception e) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            com.ai.knowledge.annotation.OperationLog annotation = method.getAnnotation(com.ai.knowledge.annotation.OperationLog.class);
            if (annotation == null) {
                return;
            }

            OperationLog log = new OperationLog();

            log.setOperation(annotation.operation());
            log.setOperationType(annotation.type());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof JwtAuthenticationFilter.UserInfo) {
                JwtAuthenticationFilter.UserInfo userInfo = (JwtAuthenticationFilter.UserInfo) authentication.getPrincipal();
                log.setUserId(userInfo.getUserId());
                log.setUsername(userInfo.getUsername());
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(getIpAddress(request));
            }

            logRepository.save(log);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
