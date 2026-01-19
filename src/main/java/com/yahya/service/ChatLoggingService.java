package com.yahya.service;

import com.yahya.commonlogger.Loggable;
import com.yahya.model.Users;
import org.springframework.stereotype.Service;

@Service
public class ChatLoggingService {

    private final UsersService usersService;

    public ChatLoggingService(UsersService usersService) {
        this.usersService = usersService;
    }

    @Loggable
    public void logChatTransaction(String senderId, String recipientId, String message) {
        // No-op: LoggingAspect handles structured log emission. We resolve user info for enrichment below.
    }

    public Users resolveUser(String userId) {
        return userId == null ? null : usersService.findById(userId);
    }
}
