package com.yahya.service;

import com.yahya.model.Friendship;
import com.yahya.model.Users;

import java.util.List;

public interface FriendshipService {
    Friendship sendRequest(String requesterId, String targetEmail);

    Friendship accept(String friendshipId, String currentUserId);

    List<Users> getFriends(String userId);

    List<Friendship> getIncoming(String userId);

    List<Friendship> getOutgoing(String userId);

    String topicFor(String userAId, String userBId);
}
