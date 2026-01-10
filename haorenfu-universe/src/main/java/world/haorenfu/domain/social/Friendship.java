/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                        FRIENDSHIP SYSTEM
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Social connection management between players.
 * Uses graph-based algorithms for friend recommendations.
 */
package world.haorenfu.domain.social;

import jakarta.persistence.*;
import world.haorenfu.domain.user.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a friendship between two users.
 */
@Entity
@Table(name = "friendships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "friend_id"})
})
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant acceptedAt;

    @Column(length = 200)
    private String note;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // Business methods

    public void accept() {
        if (status != FriendshipStatus.PENDING) {
            throw new IllegalStateException("只能接受待处理的好友请求");
        }
        this.status = FriendshipStatus.ACCEPTED;
        this.acceptedAt = Instant.now();
    }

    public void reject() {
        if (status != FriendshipStatus.PENDING) {
            throw new IllegalStateException("只能拒绝待处理的好友请求");
        }
        this.status = FriendshipStatus.REJECTED;
    }

    public void block() {
        this.status = FriendshipStatus.BLOCKED;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public User getFriend() { return friend; }
    public void setFriend(User friend) { this.friend = friend; }

    public FriendshipStatus getStatus() { return status; }
    public void setStatus(FriendshipStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getAcceptedAt() { return acceptedAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    /**
     * Friendship states.
     */
    public enum FriendshipStatus {
        PENDING("待确认"),
        ACCEPTED("已接受"),
        REJECTED("已拒绝"),
        BLOCKED("已屏蔽");

        private final String displayName;

        FriendshipStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
