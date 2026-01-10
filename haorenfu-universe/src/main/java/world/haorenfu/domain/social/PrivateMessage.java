/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                     PRIVATE MESSAGE SYSTEM
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Secure messaging between users with read receipts and encryption support.
 */
package world.haorenfu.domain.social;

import jakarta.persistence.*;
import world.haorenfu.domain.user.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a private message between users.
 */
@Entity
@Table(name = "private_messages", indexes = {
    @Index(name = "idx_pm_sender", columnList = "sender_id"),
    @Index(name = "idx_pm_recipient", columnList = "recipient_id"),
    @Index(name = "idx_pm_conversation", columnList = "conversation_id")
})
public class PrivateMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false, length = 10000)
    private String content;

    @Column(nullable = false)
    private Instant sentAt;

    private Instant readAt;

    @Column(nullable = false)
    private boolean deletedBySender = false;

    @Column(nullable = false)
    private boolean deletedByRecipient = false;

    @PrePersist
    protected void onCreate() {
        sentAt = Instant.now();
    }

    // Business methods

    public void markAsRead() {
        if (readAt == null) {
            readAt = Instant.now();
        }
    }

    public boolean isRead() {
        return readAt != null;
    }

    public void deleteForSender() {
        this.deletedBySender = true;
    }

    public void deleteForRecipient() {
        this.deletedByRecipient = true;
    }

    public boolean isVisibleToUser(User user) {
        if (user.getId().equals(sender.getId())) {
            return !deletedBySender;
        }
        if (user.getId().equals(recipient.getId())) {
            return !deletedByRecipient;
        }
        return false;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getSentAt() { return sentAt; }
    public Instant getReadAt() { return readAt; }

    public boolean isDeletedBySender() { return deletedBySender; }
    public boolean isDeletedByRecipient() { return deletedByRecipient; }
}
