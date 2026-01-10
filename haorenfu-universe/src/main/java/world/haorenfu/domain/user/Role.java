/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                           ROLE HIERARCHY
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Defines the hierarchical roles within our community.
 * Each role inherits permissions from lower roles.
 */
package world.haorenfu.domain.user;

/**
 * User roles with hierarchical permissions.
 */
public enum Role {
    /**
     * New users who haven't completed verification.
     */
    GUEST(0, "访客", "#888888"),

    /**
     * Verified community members.
     */
    MEMBER(1, "成员", "#4CAF50"),

    /**
     * Trusted members with extended privileges.
     */
    VIP(2, "VIP", "#9C27B0"),

    /**
     * Community moderators.
     */
    MODERATOR(3, "管理员", "#FF9800"),

    /**
     * Server administrators.
     */
    ADMIN(4, "超级管理员", "#F44336"),

    /**
     * Server owner with full access.
     */
    OWNER(5, "服主", "#FFD700");

    private final int level;
    private final String displayName;
    private final String color;

    Role(int level, String displayName, String color) {
        this.level = level;
        this.displayName = displayName;
        this.color = color;
    }

    /**
     * Gets the hierarchical level (higher = more permissions).
     */
    public int getLevel() {
        return level;
    }

    /**
     * Gets the display name in Chinese.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the color code for UI display.
     */
    public String getColor() {
        return color;
    }

    /**
     * Checks if this role is at least as privileged as another.
     */
    public boolean isAtLeast(Role other) {
        return this.level >= other.level;
    }

    /**
     * Checks if this role can manage another role.
     */
    public boolean canManage(Role other) {
        return this.level > other.level;
    }
}
