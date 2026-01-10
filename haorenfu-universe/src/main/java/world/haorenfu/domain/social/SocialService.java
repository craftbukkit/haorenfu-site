/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         SOCIAL SERVICE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Manages friendships, messages, and social interactions.
 * Integrates graph algorithms for friend recommendations.
 */
package world.haorenfu.domain.social;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.haorenfu.core.algorithm.GraphTheoryEngine;
import world.haorenfu.domain.user.User;
import world.haorenfu.domain.user.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for social features including friends and messaging.
 */
@Service
@Transactional
public class SocialService {

    private final FriendshipRepository friendshipRepository;
    private final PrivateMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GraphTheoryEngine<UUID> socialGraph;

    public SocialService(FriendshipRepository friendshipRepository,
                         PrivateMessageRepository messageRepository,
                         UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.socialGraph = new GraphTheoryEngine<>();

        // Build initial social graph
        rebuildSocialGraph();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                         FRIENDSHIP MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Sends a friend request.
     */
    public Friendship sendFriendRequest(User from, User to) {
        // Check if already friends or request exists
        if (areFriends(from, to)) {
            throw new IllegalStateException("已经是好友了");
        }

        Optional<Friendship> existing = friendshipRepository
            .findByUserAndFriend(from, to);

        if (existing.isPresent()) {
            Friendship.FriendshipStatus status = existing.get().getStatus();
            if (status == Friendship.FriendshipStatus.PENDING) {
                throw new IllegalStateException("已发送过好友请求");
            }
            if (status == Friendship.FriendshipStatus.BLOCKED) {
                throw new IllegalStateException("无法添加该用户");
            }
        }

        // Check if they sent us a request
        Optional<Friendship> reverse = friendshipRepository
            .findByUserAndFriend(to, from);

        if (reverse.isPresent() && reverse.get().getStatus() == Friendship.FriendshipStatus.PENDING) {
            // Auto-accept if mutual request
            return acceptFriendRequest(reverse.get().getId(), from);
        }

        Friendship friendship = new Friendship();
        friendship.setUser(from);
        friendship.setFriend(to);
        friendship.setStatus(Friendship.FriendshipStatus.PENDING);

        return friendshipRepository.save(friendship);
    }

    /**
     * Accepts a friend request.
     */
    public Friendship acceptFriendRequest(UUID requestId, User acceptingUser) {
        Friendship friendship = friendshipRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("请求不存在"));

        if (!friendship.getFriend().getId().equals(acceptingUser.getId())) {
            throw new IllegalStateException("无权操作此请求");
        }

        friendship.accept();

        // Create reverse friendship record for bidirectional relationship
        Friendship reverse = new Friendship();
        reverse.setUser(friendship.getFriend());
        reverse.setFriend(friendship.getUser());
        reverse.setStatus(Friendship.FriendshipStatus.ACCEPTED);
        friendshipRepository.save(reverse);

        // Update social graph
        socialGraph.addEdge(friendship.getUser().getId(), friendship.getFriend().getId());

        return friendshipRepository.save(friendship);
    }

    /**
     * Rejects a friend request.
     */
    public void rejectFriendRequest(UUID requestId, User rejectingUser) {
        Friendship friendship = friendshipRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("请求不存在"));

        if (!friendship.getFriend().getId().equals(rejectingUser.getId())) {
            throw new IllegalStateException("无权操作此请求");
        }

        friendship.reject();
        friendshipRepository.save(friendship);
    }

    /**
     * Removes a friendship.
     */
    public void removeFriend(User user, User friend) {
        friendshipRepository.deleteByUserAndFriend(user, friend);
        friendshipRepository.deleteByUserAndFriend(friend, user);

        // Rebuild graph after removal
        rebuildSocialGraph();
    }

    /**
     * Blocks a user.
     */
    public void blockUser(User blocker, User blocked) {
        // Remove existing friendship if any
        removeFriend(blocker, blocked);

        Friendship block = new Friendship();
        block.setUser(blocker);
        block.setFriend(blocked);
        block.setStatus(Friendship.FriendshipStatus.BLOCKED);
        friendshipRepository.save(block);
    }

    /**
     * Checks if two users are friends.
     */
    public boolean areFriends(User user1, User user2) {
        return friendshipRepository
            .findByUserAndFriendAndStatus(user1, user2, Friendship.FriendshipStatus.ACCEPTED)
            .isPresent();
    }

    /**
     * Gets all friends of a user.
     */
    public List<User> getFriends(User user) {
        return friendshipRepository
            .findAllByUserAndStatus(user, Friendship.FriendshipStatus.ACCEPTED)
            .stream()
            .map(Friendship::getFriend)
            .collect(Collectors.toList());
    }

    /**
     * Gets pending friend requests received.
     */
    public List<Friendship> getPendingRequests(User user) {
        return friendshipRepository
            .findAllByFriendAndStatus(user, Friendship.FriendshipStatus.PENDING);
    }

    /**
     * Gets sent friend requests.
     */
    public List<Friendship> getSentRequests(User user) {
        return friendshipRepository
            .findAllByUserAndStatus(user, Friendship.FriendshipStatus.PENDING);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                       FRIEND RECOMMENDATIONS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Recommends friends using graph-based algorithms.
     *
     * Uses multiple signals:
     * - Common friends (Jaccard similarity)
     * - Network proximity (Personalized PageRank)
     * - Influence-weighted common friends (Adamic-Adar)
     */
    public List<User> recommendFriends(User user, int limit) {
        List<UUID> recommendedIds = socialGraph.recommendFriends(user.getId(), limit);
        return recommendedIds.stream()
            .map(userRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    /**
     * Gets mutual friends between two users.
     */
    public List<User> getMutualFriends(User user1, User user2) {
        Set<UUID> friends1 = getFriends(user1).stream()
            .map(User::getId)
            .collect(Collectors.toSet());

        return getFriends(user2).stream()
            .filter(friend -> friends1.contains(friend.getId()))
            .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                        PRIVATE MESSAGING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Sends a private message.
     */
    public PrivateMessage sendMessage(User sender, User recipient, String content) {
        // Check if blocked
        if (isBlocked(sender, recipient)) {
            throw new IllegalStateException("无法发送消息给该用户");
        }

        // Generate or get conversation ID
        UUID conversationId = getOrCreateConversationId(sender, recipient);

        PrivateMessage message = new PrivateMessage();
        message.setConversationId(conversationId);
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);

        return messageRepository.save(message);
    }

    /**
     * Gets conversation between two users.
     */
    public List<PrivateMessage> getConversation(User user1, User user2, Pageable pageable) {
        UUID conversationId = getConversationId(user1, user2);
        if (conversationId == null) {
            return List.of();
        }

        return messageRepository.findByConversationIdOrderBySentAtDesc(conversationId, pageable)
            .getContent()
            .stream()
            .filter(msg -> msg.isVisibleToUser(user1))
            .collect(Collectors.toList());
    }

    /**
     * Gets all conversations for a user.
     */
    public List<ConversationSummary> getConversations(User user) {
        List<PrivateMessage> latestMessages = messageRepository
            .findLatestMessagesByUser(user.getId());

        return latestMessages.stream()
            .map(msg -> {
                User otherUser = msg.getSender().getId().equals(user.getId())
                    ? msg.getRecipient() : msg.getSender();
                int unreadCount = messageRepository
                    .countUnreadInConversation(msg.getConversationId(), user.getId());
                return new ConversationSummary(
                    msg.getConversationId(),
                    otherUser,
                    msg.getContent(),
                    msg.getSentAt(),
                    unreadCount
                );
            })
            .collect(Collectors.toList());
    }

    /**
     * Marks messages as read.
     */
    public void markConversationAsRead(UUID conversationId, User user) {
        messageRepository.markAllAsReadInConversation(conversationId, user.getId());
    }

    /**
     * Gets unread message count.
     */
    public int getUnreadCount(User user) {
        return messageRepository.countUnreadByRecipient(user.getId());
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                           HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════

    private boolean isBlocked(User user1, User user2) {
        return friendshipRepository
            .findByUserAndFriendAndStatus(user1, user2, Friendship.FriendshipStatus.BLOCKED)
            .isPresent()
            || friendshipRepository
            .findByUserAndFriendAndStatus(user2, user1, Friendship.FriendshipStatus.BLOCKED)
            .isPresent();
    }

    private UUID getOrCreateConversationId(User user1, User user2) {
        UUID existing = getConversationId(user1, user2);
        if (existing != null) {
            return existing;
        }

        // Create deterministic conversation ID based on user IDs
        String combined = user1.getId().compareTo(user2.getId()) < 0
            ? user1.getId().toString() + user2.getId().toString()
            : user2.getId().toString() + user1.getId().toString();

        return UUID.nameUUIDFromBytes(combined.getBytes());
    }

    private UUID getConversationId(User user1, User user2) {
        return messageRepository
            .findConversationIdBetweenUsers(user1.getId(), user2.getId())
            .orElse(null);
    }

    private void rebuildSocialGraph() {
        List<Friendship> allFriendships = friendshipRepository
            .findAllByStatus(Friendship.FriendshipStatus.ACCEPTED);

        for (Friendship friendship : allFriendships) {
            socialGraph.addEdge(
                friendship.getUser().getId(),
                friendship.getFriend().getId()
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                              DTOs
    // ═══════════════════════════════════════════════════════════════════════

    public record ConversationSummary(
        UUID conversationId,
        User otherUser,
        String lastMessage,
        java.time.Instant lastMessageTime,
        int unreadCount
    ) {}
}
