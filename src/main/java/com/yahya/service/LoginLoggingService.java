package com.yahya.service;

import com.yahya.commonlogger.Loggable;
import org.springframework.stereotype.Service;

@Service
public class LoginLoggingService {

    @Loggable
    public void logLoginAttempt(String email, boolean success) {
        // No-op body; LoggingAspect handles structured output.
    }
}
