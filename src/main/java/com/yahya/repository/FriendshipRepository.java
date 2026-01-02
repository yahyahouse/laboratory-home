package com.yahya.repository;

import com.yahya.model.Friendship;
import com.yahya.model.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, String> {
    Optional<Friendship> findByRequesterIdAndAddresseeId(String requesterId, String addresseeId);

    @Query("SELECT f FROM Friendship f WHERE f.status = :status AND (f.requesterId = :userId OR f.addresseeId = :userId)")
    List<Friendship> findByUserAndStatus(String userId, FriendshipStatus status);

    List<Friendship> findByAddresseeIdAndStatus(String addresseeId, FriendshipStatus status);

    List<Friendship> findByRequesterIdAndStatus(String requesterId, FriendshipStatus status);
}
