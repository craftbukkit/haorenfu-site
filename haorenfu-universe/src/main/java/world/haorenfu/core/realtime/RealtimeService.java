/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                    REAL-TIME MESSAGE SERVICE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Service for broadcasting real-time updates to connected clients.
 */
package world.haorenfu.core.realtime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for real-time message broadcasting.
 */
@Service
public class RealtimeService {

    private final SimpMessagingTemplate messagingTemplate;

    // Track connected users
    private final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();
    private final AtomicInteger connectionCount = new AtomicInteger(0);

    public RealtimeService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                      BROADCAST METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Broadcasts server status to all connected clients.
     */
    public void broadcastServerStatus(ServerStatusMessage status) {
        messagingTemplate.convertAndSend("/topic/server-status", status);
    }

    /**
     * Broadcasts online player count.
     */
    public void broadcastPlayerCount(int count) {
        messagingTemplate.convertAndSend("/topic/player-count",
            new PlayerCountMessage(count, Instant.now())
        );
    }

    /**
     * Broadcasts new forum activity.
     */
    public void broadcastForumActivity(ForumActivityMessage activity) {
        messagingTemplate.convertAndSend("/topic/forum-activity", activity);
    }

    /**
     * Broadcasts achievement unlock notification.
     */
    public void broadcastAchievementUnlock(AchievementUnlockMessage achievement) {
        messagingTemplate.convertAndSend("/topic/achievements", achievement);
    }

    /**
     * Sends a notification to a specific user.
     */
    public void sendToUser(String username, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(username, destination, payload);
    }

    /**
     * Sends a private notification.
     */
    public void sendNotification(String username, NotificationMessage notification) {
        sendToUser(username, "/queue/notifications", notification);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                     SESSION MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════

    public void registerSession(String sessionId, String username) {
        activeSessions.put(sessionId, new UserSession(sessionId, username, Instant.now()));
        connectionCount.incrementAndGet();
        broadcastConnectionCount();
    }

    public void unregisterSession(String sessionId) {
        if (activeSessions.remove(sessionId) != null) {
            connectionCount.decrementAndGet();
            broadcastConnectionCount();
        }
    }

    public int getActiveConnectionCount() {
        return connectionCount.get();
    }

    public List<String> getOnlineUsers() {
        return activeSessions.values().stream()
            .map(UserSession::username)
            .distinct()
            .toList();
    }

    private void broadcastConnectionCount() {
        messagingTemplate.convertAndSend("/topic/connections",
            new ConnectionCountMessage(connectionCount.get(), Instant.now())
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                    SCHEDULED BROADCASTS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Periodic heartbeat to keep connections alive.
     */
    @Scheduled(fixedRate = 30000)  // Every 30 seconds
    public void sendHeartbeat() {
        messagingTemplate.convertAndSend("/topic/heartbeat",
            new HeartbeatMessage(Instant.now())
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                      MESSAGE RECORDS
    // ═══════════════════════════════════════════════════════════════════════

    public record ServerStatusMessage(
        boolean online,
        int playerCount,
        int maxPlayers,
        String motd,
        long latencyMs,
        Instant timestamp
    ) {}

    public record PlayerCountMessage(
        int count,
        Instant timestamp
    ) {}

    public record ForumActivityMessage(
        String type,  // NEW_POST, NEW_COMMENT, VOTE
        UUID postId,
        String postTitle,
        String username,
        Instant timestamp
    ) {}

    public record AchievementUnlockMessage(
        String username,
        String achievementCode,
        String achievementName,
        String rarity,
        Instant timestamp
    ) {}

    public record NotificationMessage(
        String type,
        String title,
        String message,
        String link,
        Instant timestamp
    ) {}

    public record ConnectionCountMessage(
        int count,
        Instant timestamp
    ) {}

    public record HeartbeatMessage(
        Instant timestamp
    ) {}

    public record UserSession(
        String sessionId,
        String username,
        Instant connectedAt
    ) {}
}
