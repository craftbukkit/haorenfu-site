/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                    PRIVATE MESSAGE REPOSITORY
 * ═══════════════════════════════════════════════════════════════════════════
 */
package world.haorenfu.domain.social;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for private message operations.
 */
@Repository
public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, UUID> {

    Page<PrivateMessage> findByConversationIdOrderBySentAtDesc(UUID conversationId, Pageable pageable);

    @Query("SELECT pm FROM PrivateMessage pm WHERE pm.conversationId = :conversationId " +
           "AND pm.sentAt > :since ORDER BY pm.sentAt ASC")
    List<PrivateMessage> findNewMessagesSince(UUID conversationId, Instant since);

    @Query("SELECT DISTINCT pm.conversationId FROM PrivateMessage pm " +
           "WHERE (pm.sender.id = :userId1 AND pm.recipient.id = :userId2) " +
           "OR (pm.sender.id = :userId2 AND pm.recipient.id = :userId1)")
    Optional<UUID> findConversationIdBetweenUsers(UUID userId1, UUID userId2);

    @Query(value = "SELECT * FROM private_messages pm WHERE pm.id IN " +
           "(SELECT pm2.id FROM private_messages pm2 WHERE " +
           "(pm2.sender_id = :userId OR pm2.recipient_id = :userId) " +
           "GROUP BY pm2.conversation_id " +
           "ORDER BY MAX(pm2.sent_at) DESC)", nativeQuery = true)
    List<PrivateMessage> findLatestMessagesByUser(UUID userId);

    @Query("SELECT COUNT(pm) FROM PrivateMessage pm WHERE " +
           "pm.conversationId = :conversationId AND " +
           "pm.recipient.id = :userId AND pm.readAt IS NULL")
    int countUnreadInConversation(UUID conversationId, UUID userId);

    @Query("SELECT COUNT(pm) FROM PrivateMessage pm WHERE " +
           "pm.recipient.id = :userId AND pm.readAt IS NULL AND " +
           "pm.deletedByRecipient = false")
    int countUnreadByRecipient(UUID userId);

    @Modifying
    @Query("UPDATE PrivateMessage pm SET pm.readAt = CURRENT_TIMESTAMP " +
           "WHERE pm.conversationId = :conversationId AND " +
           "pm.recipient.id = :userId AND pm.readAt IS NULL")
    void markAllAsReadInConversation(UUID conversationId, UUID userId);
}
