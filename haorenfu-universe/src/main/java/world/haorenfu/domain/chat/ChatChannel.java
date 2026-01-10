/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                        CHAT MESSAGE ENTITY
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Real-time chat system supporting public channels and private messages.
 * Integrates with WebSocket for live updates.
 */
package world.haorenfu.domain.chat;

import jakarta.persistence.*;
import world.haorenfu.domain.user.User;

import java.time.Instant;
import java.util.*;

/**
 * Chat channel entity.
 */
@Entity
@Table(name = "chat_channels")
public class ChatChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType type = ChannelType.PUBLIC;

    @ManyToMany
    @JoinTable(
        name = "channel_members",
        joinColumns = @JoinColumn(name = "channel_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL)
    @OrderBy("sentAt DESC")
    private List<ChatMessage> messages = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // ═══════════════════════════════════════════════════════════════════════

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ChannelType getType() { return type; }
    public void setType(ChannelType type) { this.type = type; }

    public Set<User> getMembers() { return members; }
    public List<ChatMessage> getMessages() { return messages; }

    public Instant getCreatedAt() { return createdAt; }

    public void addMember(User user) { members.add(user); }
    public void removeMember(User user) { members.remove(user); }

    public boolean isMember(User user) {
        return type == ChannelType.PUBLIC || members.contains(user);
    }
}

/**
 * Chat message entity.
 */
@Entity
@Table(name = "chat_messages")
class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private ChatChannel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;

    @Column(nullable = false)
    private Instant sentAt;

    private Instant editedAt;

    @Column(nullable = false)
    private boolean deleted = false;

    // For replies
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private ChatMessage replyTo;

    @PrePersist
    protected void onCreate() {
        sentAt = Instant.now();
    }

    // ═══════════════════════════════════════════════════════════════════════

    public UUID getId() { return id; }

    public ChatChannel getChannel() { return channel; }
    public void setChannel(ChatChannel channel) { this.channel = channel; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public Instant getSentAt() { return sentAt; }

    public Instant getEditedAt() { return editedAt; }

    public boolean isDeleted() { return deleted; }

    public ChatMessage getReplyTo() { return replyTo; }
    public void setReplyTo(ChatMessage replyTo) { this.replyTo = replyTo; }

    public void edit(String newContent) {
        this.content = newContent;
        this.editedAt = Instant.now();
    }

    public void softDelete() {
        this.deleted = true;
        this.content = "[消息已删除]";
    }
}

/**
 * Private message between users.
 */
@Entity
@Table(name = "chat_private_messages")
class PrivateMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false, length = 5000)
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

    // ═══════════════════════════════════════════════════════════════════════

    public UUID getId() { return id; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getSentAt() { return sentAt; }

    public Instant getReadAt() { return readAt; }

    public boolean isRead() { return readAt != null; }

    public void markAsRead() {
        if (readAt == null) {
            readAt = Instant.now();
        }
    }

    public boolean isDeletedBySender() { return deletedBySender; }
    public void setDeletedBySender(boolean deleted) { this.deletedBySender = deleted; }

    public boolean isDeletedByRecipient() { return deletedByRecipient; }
    public void setDeletedByRecipient(boolean deleted) { this.deletedByRecipient = deleted; }
}

/**
 * Channel type enumeration.
 */
enum ChannelType {
    PUBLIC("公开"),
    PRIVATE("私密"),
    ANNOUNCEMENT("公告");

    private final String displayName;

    ChannelType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}

/**
 * Message type enumeration.
 */
enum MessageType {
    TEXT("文本"),
    IMAGE("图片"),
    SYSTEM("系统"),
    ANNOUNCEMENT("公告");

    private final String displayName;

    MessageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
