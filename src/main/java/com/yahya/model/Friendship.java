package com.yahya.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Friendship {
    @Id
    private String id;

    private String requesterId;
    private String addresseeId;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    public static Friendship pending(String requesterId, String addresseeId) {
        return new Friendship(UUID.randomUUID().toString(), requesterId, addresseeId, FriendshipStatus.PENDING);
    }
}
