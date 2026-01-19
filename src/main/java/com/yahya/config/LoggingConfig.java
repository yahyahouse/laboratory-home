package com.yahya.config;

import com.yahya.commonlogger.CommonLoggerProperties;
import com.yahya.commonlogger.StructuredLogCustomizer;
import com.yahya.model.Users;
import com.yahya.service.UsersService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class LoggingConfig {

    private final UsersService usersService;

    public LoggingConfig(UsersService usersService) {
        this.usersService = usersService;
    }

    @Bean
    CommonLoggerProperties commonLoggerProperties() {
        CommonLoggerProperties props = new CommonLoggerProperties();
        props.setApiId("");
        props.setCorrelationIdHeader("X-Correlation-Id");
        props.setCorrelationIdMdcKey("correlationId");
        props.setTransactionIdMdcKey("correlationId");
        props.setInternalTransactionIdMdcKey("correlationId");
        props.setLogLevel(LogLevel.INFO);
        props.setSuccessHttpStatusCode(200);
        props.setErrorHttpStatusCode(500);
        return props;
    }

    /**
     * Enrich every structured log with stable fields so they always show up in the terminal output.
     */
    @Bean
    StructuredLogCustomizer defaultLogEnricher() {
        return (Map<String, Object> payload, ProceedingJoinPoint jp, Object result, long duration, boolean success, Throwable failure) -> {
            // No default enrichment.
        };
    }

    @Bean
    StructuredLogCustomizer apiIdFromMethodCustomizer() {
        return (payload, jp, result, duration, success, failure) -> payload.put("apiId", jp.getSignature().getName());
    }

    @Bean
    StructuredLogCustomizer removeInternalTransactionId() {
        return (payload, jp, result, duration, success, failure) -> payload.remove("internalTransactionId");
    }

    @Bean
    StructuredLogCustomizer chatLogCustomizer() {
        return (Map<String, Object> payload, ProceedingJoinPoint jp, Object result, long duration, boolean success, Throwable failure) -> {
            if ("logChatTransaction".equals(jp.getSignature().getName())) {
                payload.put("apiId", "chat-send");
                payload.put("logMessage", Boolean.TRUE.equals(success) ? "chat-send-Completed" : "chat-send-Failed");
                payload.put("logPoint", "chat-send");
                Object[] args = jp.getArgs();
                if (args.length > 0 && args[0] != null) {
                    payload.put("senderId", toDisplayName((String) args[0]));
                }
                if (args.length > 1 && args[1] != null) {
                    payload.put("recipientId", args[1]);
                }
                if (args.length > 2 && args[2] instanceof String msg && !msg.isEmpty()) {
                    payload.put("message", maskMessage(msg));
                }
            }
        };
    }

    @Bean
    StructuredLogCustomizer httpStatusMapper() {
        return (payload, jp, result, duration, success, failure) -> {
            if (failure == null) {
                return;
            }
            int status = 500;
            if (failure instanceof IllegalArgumentException) {
                status = 400;
            } else if (failure instanceof IllegalStateException) {
                status = 409;
            }
            payload.put("httpStatusCode", status);
        };
    }

    @Bean
    StructuredLogCustomizer loginLogCustomizer() {
        return (payload, jp, result, duration, success, failure) -> {
            if ("logLoginAttempt".equals(jp.getSignature().getName())) {
                payload.put("apiId", "login");
                payload.put("logMessage", Boolean.TRUE.equals(success) ? "login-Completed" : "login-Failed");
                payload.put("logPoint", "login");
                Object[] args = jp.getArgs();
                if (args.length > 0 && args[0] instanceof String email && !email.isEmpty()) {
                    payload.put("email", email);
                }
                if (args.length > 1 && args[1] instanceof Boolean ok) {
                    payload.put("success", ok);
                }
            }
        };
    }

    private String toDisplayName(String userId) {
        if (userId == null) {
            return null;
        }
        Users user = usersService.findById(userId);
        if (user == null) {
            return userId;
        }
        if (user.getName() != null && !user.getName().isEmpty()) {
            return user.getName();
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            return user.getEmail();
        }
        return userId;
    }

    private String maskMessage(String message) {
        if (message == null) {
            return null;
        }
        StringBuilder masked = new StringBuilder(message.length());
        for (char c : message.toCharArray()) {
            masked.append(Character.isWhitespace(c) ? c : '*');
        }
        return masked.toString();
    }
}
