/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                      REAL-TIME NOTIFICATION SERVICE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Handles broadcasting of real-time events to connected clients.
 * Supports server status, chat, forum updates, and user notifications.
 */
package world.haorenfu.core.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Service for sending real-time notifications via WebSocket.
 */
@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcasts server status update to all connected clients.
     */
    public void broadcastServerStatus(ServerStatusUpdate status) {
        messagingTemplate.convertAndSend("/topic/server-status", status);
    }

    /**
     * Broadcasts a global notification to all users.
     */
    public void broadcastGlobalNotification(GlobalNotification notification) {
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    /**
     * Sends a notification to a specific user.
     */
    public void sendUserNotification(UUID userId, UserNotification notification) {
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/notifications",
            notification
        );
    }

    /**
     * Broadcasts forum activity (new post, comment, etc.).
     */
    public void broadcastForumActivity(ForumActivityEvent event) {
        messagingTemplate.convertAndSend("/topic/forum", event);
    }

    /**
     * Broadcasts chat message.
     */
    public void broadcastChatMessage(ChatMessage message) {
        messagingTemplate.convertAndSend("/topic/chat", message);
    }

    /**
     * Broadcasts player join/leave events.
     */
    public void broadcastPlayerEvent(PlayerEvent event) {
        messagingTemplate.convertAndSend("/topic/players", event);
    }

    /**
     * Broadcasts achievement unlock.
     */
    public void broadcastAchievement(AchievementUnlockEvent event) {
        messagingTemplate.convertAndSend("/topic/achievements", event);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Event Data Classes
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Server status update event.
     */
    public record ServerStatusUpdate(
        boolean online,
        int playerCount,
        int maxPlayers,
        String motd,
        double tps,
        long latency,
        Instant timestamp
    ) {}

    /**
     * Global notification for all users.
     */
    public record GlobalNotification(
        String id,
        NotificationType type,
        String title,
        String message,
        String link,
        Instant timestamp
    ) {
        public GlobalNotification(NotificationType type, String title, String message) {
            this(UUID.randomUUID().toString(), type, title, message, null, Instant.now());
        }

        public GlobalNotification(NotificationType type, String title, String message, String link) {
            this(UUID.randomUUID().toString(), type, title, message, link, Instant.now());
        }
    }

    /**
     * User-specific notification.
     */
    public record UserNotification(
        String id,
        NotificationType type,
        String title,
        String message,
        String link,
        boolean read,
        Instant timestamp
    ) {
        public UserNotification(NotificationType type, String title, String message, String link) {
            this(UUID.randomUUID().toString(), type, title, message, link, false, Instant.now());
        }
    }

    /**
     * Forum activity event.
     */
    public record ForumActivityEvent(
        ForumEventType eventType,
        UUID postId,
        String postTitle,
        UUID userId,
        String username,
        String preview,
        Instant timestamp
    ) {}

    /**
     * Chat message.
     */
    public record ChatMessage(
        String id,
        UUID userId,
        String username,
        String avatarUrl,
        String content,
        ChatMessageType type,
        Instant timestamp
    ) {
        public ChatMessage(UUID userId, String username, String content) {
            this(UUID.randomUUID().toString(), userId, username, null, content,
                 ChatMessageType.USER, Instant.now());
        }
    }

    /**
     * Player join/leave event.
     */
    public record PlayerEvent(
        PlayerEventType eventType,
        String playerName,
        UUID uuid,
        Instant timestamp
    ) {}

    /**
     * Achievement unlock broadcast.
     */
    public record AchievementUnlockEvent(
        UUID userId,
        String username,
        String achievementName,
        String achievementIcon,
        String achievementRarity,
        Instant timestamp
    ) {}

    /**
     * Notification types.
     */
    public enum NotificationType {
        INFO,           // General information
        SUCCESS,        // Success message
        WARNING,        // Warning
        ERROR,          // Error
        ACHIEVEMENT,    // Achievement unlocked
        MENTION,        // User mentioned
        REPLY,          // Post reply
        LIKE,           // Content liked
        FOLLOW,         // New follower
        SYSTEM,         // System announcement
        EVENT           // Event notification
    }

    /**
     * Forum event types.
     */
    public enum ForumEventType {
        NEW_POST,
        NEW_COMMENT,
        POST_EDITED,
        POST_DELETED,
        POST_PINNED,
        POST_FEATURED
    }

    /**
     * Chat message types.
     */
    public enum ChatMessageType {
        USER,           // Regular user message
        SYSTEM,         // System message
        JOIN,           // User joined
        LEAVE,          // User left
        ANNOUNCEMENT    // Admin announcement
    }

    /**
     * Player event types.
     */
    public enum PlayerEventType {
        JOIN,
        LEAVE,
        DEATH,
        ACHIEVEMENT
    }
}
