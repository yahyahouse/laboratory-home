package com.yahya.service;

import com.yahya.model.Friendship;
import com.yahya.model.FriendshipStatus;
import com.yahya.model.Users;
import com.yahya.repository.FriendshipRepository;
import com.yahya.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UsersRepository usersRepository;

    public FriendshipServiceImpl(FriendshipRepository friendshipRepository, UsersRepository usersRepository) {
        this.friendshipRepository = friendshipRepository;
        this.usersRepository = usersRepository;
    }

    @Override
    @Transactional
    public Friendship sendRequest(String requesterId, String targetEmail) {
        Users target = usersRepository.findByEmail(targetEmail).orElseThrow(() -> new IllegalArgumentException("Pengguna tidak ditemukan"));
        if (target.getId().equals(requesterId)) {
            throw new IllegalArgumentException("Tidak bisa menambahkan diri sendiri");
        }

        Optional<Friendship> existingForward = friendshipRepository.findByRequesterIdAndAddresseeId(requesterId, target.getId());
        Optional<Friendship> existingReverse = friendshipRepository.findByRequesterIdAndAddresseeId(target.getId(), requesterId);

        if (existingForward.isPresent()) {
            Friendship friendship = existingForward.get();
            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new IllegalStateException("Sudah berteman");
            }
            return friendship;
        }
        if (existingReverse.isPresent()) {
            Friendship friendship = existingReverse.get();
            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new IllegalStateException("Sudah berteman");
            }
            // Target sudah meminta pertemanan, otomatis setujui
            friendship.setStatus(FriendshipStatus.ACCEPTED);
            return friendshipRepository.save(friendship);
        }

        Friendship friendship = Friendship.pending(requesterId, target.getId());
        return friendshipRepository.save(friendship);
    }

    @Override
    @Transactional
    public Friendship accept(String friendshipId, String currentUserId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Permintaan tidak ditemukan"));
        if (!friendship.getAddresseeId().equals(currentUserId)) {
            throw new IllegalStateException("Tidak berhak menerima permintaan ini");
        }
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        return friendshipRepository.save(friendship);
    }

    @Override
    public List<Users> getFriends(String userId) {
        List<Friendship> accepted = friendshipRepository.findByUserAndStatus(userId, FriendshipStatus.ACCEPTED);
        return accepted.stream()
                .map(f -> {
                    String otherId = f.getRequesterId().equals(userId) ? f.getAddresseeId() : f.getRequesterId();
                    return usersRepository.findById(otherId).orElse(null);
                })
                .filter(u -> u != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Friendship> getIncoming(String userId) {
        return friendshipRepository.findByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING);
    }

    @Override
    public List<Friendship> getOutgoing(String userId) {
        return friendshipRepository.findByRequesterIdAndStatus(userId, FriendshipStatus.PENDING);
    }

    @Override
    public String topicFor(String userAId, String userBId) {
        return "chat/" + List.of(userAId, userBId).stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining("_"));
    }
}
