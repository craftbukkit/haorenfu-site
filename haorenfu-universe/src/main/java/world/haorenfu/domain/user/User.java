/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          USER DOMAIN MODEL
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * The foundation of our community - each user represents a unique individual
 * who has chosen to be part of our Minecraft journey.
 *
 * This model follows Domain-Driven Design principles, encapsulating both
 * data and behavior related to user management.
 */
package world.haorenfu.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

/**
 * User entity representing a community member.
 *
 * Fields are carefully designed to capture both the technical aspects
 * (authentication, authorization) and the social aspects (profile,
 * reputation) of a user.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username", unique = true),
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_minecraft_uuid", columnList = "minecraftUuid")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度应在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", message = "用户名只能包含字母、数字、下划线和中文")
    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "请输入有效的邮箱地址")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String passwordHash;

    // Minecraft integration
    @Size(max = 16, message = "Minecraft ID最长16个字符")
    @Column(length = 16)
    private String minecraftId;

    @Column(length = 36)
    private String minecraftUuid;

    // Profile information
    @Size(max = 200, message = "个性签名最长200个字符")
    private String signature;

    @Column(length = 500)
    private String avatarUrl;

    @Size(max = 2000, message = "个人简介最长2000个字符")
    @Column(length = 2000)
    private String bio;

    // Role and permissions
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.MEMBER;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "permission")
    @Enumerated(EnumType.STRING)
    private Set<Permission> permissions = new HashSet<>();

    // Reputation and engagement metrics
    @Column(nullable = false)
    private int reputation = 0;

    @Column(nullable = false)
    private int postCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Column(nullable = false)
    private long playTimeMinutes = 0;

    // Account status
    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false)
    private boolean whitelisted = false;

    @Column(nullable = false)
    private boolean banned = false;

    @Column
    private String banReason;

    @Column
    private Instant bannedUntil;

    // Timestamps
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant lastLoginAt;

    @Column
    private Instant lastActiveAt;

    // Settings stored as JSON
    @Column(columnDefinition = "TEXT")
    private String settingsJson;

    // Constructors
    public User() {
        this.createdAt = Instant.now();
    }

    public User(String username, String email, String passwordHash) {
        this();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Business logic methods

    /**
     * Checks if the user has a specific permission.
     */
    public boolean hasPermission(Permission permission) {
        // Admins have all permissions
        if (role == Role.ADMIN || role == Role.OWNER) {
            return true;
        }
        return permissions.contains(permission);
    }

    /**
     * Grants a permission to the user.
     */
    public void grantPermission(Permission permission) {
        permissions.add(permission);
    }

    /**
     * Revokes a permission from the user.
     */
    public void revokePermission(Permission permission) {
        permissions.remove(permission);
    }

    /**
     * Checks if the user can currently access the server.
     */
    public boolean canAccessServer() {
        if (banned && (bannedUntil == null || bannedUntil.isAfter(Instant.now()))) {
            return false;
        }
        return whitelisted && emailVerified;
    }

    /**
     * Adds reputation points.
     */
    public void addReputation(int points) {
        this.reputation = Math.max(0, this.reputation + points);
    }

    /**
     * Gets the user's rank based on reputation.
     */
    public String getReputationRank() {
        if (reputation >= 10000) return "传奇";
        if (reputation >= 5000) return "大师";
        if (reputation >= 2000) return "专家";
        if (reputation >= 1000) return "精英";
        if (reputation >= 500) return "资深";
        if (reputation >= 200) return "活跃";
        if (reputation >= 50) return "新秀";
        return "新手";
    }

    /**
     * Updates the last active timestamp.
     */
    public void updateActivity() {
        this.lastActiveAt = Instant.now();
    }

    /**
     * Records a login event.
     */
    public void recordLogin() {
        this.lastLoginAt = Instant.now();
        this.lastActiveAt = Instant.now();
    }

    /**
     * Bans the user.
     */
    public void ban(String reason, Instant until) {
        this.banned = true;
        this.banReason = reason;
        this.bannedUntil = until;
    }

    /**
     * Unbans the user.
     */
    public void unban() {
        this.banned = false;
        this.banReason = null;
        this.bannedUntil = null;
    }

    /**
     * Increments post count.
     */
    public void incrementPostCount() {
        this.postCount++;
    }

    /**
     * Increments comment count.
     */
    public void incrementCommentCount() {
        this.commentCount++;
    }

    /**
     * Adds play time.
     */
    public void addPlayTime(long minutes) {
        this.playTimeMinutes += minutes;
    }

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getMinecraftId() {
        return minecraftId;
    }

    public void setMinecraftId(String minecraftId) {
        this.minecraftId = minecraftId;
    }

    public String getMinecraftUuid() {
        return minecraftUuid;
    }

    public void setMinecraftUuid(String minecraftUuid) {
        this.minecraftUuid = minecraftUuid;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public long getPlayTimeMinutes() {
        return playTimeMinutes;
    }

    public void setPlayTimeMinutes(long playTimeMinutes) {
        this.playTimeMinutes = playTimeMinutes;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isWhitelisted() {
        return whitelisted;
    }

    public void setWhitelisted(boolean whitelisted) {
        this.whitelisted = whitelisted;
    }

    public boolean isBanned() {
        return banned;
    }

    public String getBanReason() {
        return banReason;
    }

    public Instant getBannedUntil() {
        return bannedUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Instant getLastActiveAt() {
        return lastActiveAt;
    }

    public String getSettingsJson() {
        return settingsJson;
    }

    public void setSettingsJson(String settingsJson) {
        this.settingsJson = settingsJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
