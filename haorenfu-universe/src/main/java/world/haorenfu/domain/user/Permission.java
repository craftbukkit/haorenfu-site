/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                        PERMISSION DEFINITIONS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Fine-grained permissions for access control.
 * Following the principle of least privilege.
 */
package world.haorenfu.domain.user;

/**
 * Enumeration of all available permissions in the system.
 */
public enum Permission {

    // Forum permissions
    FORUM_READ("forum.read", "阅读论坛"),
    FORUM_POST("forum.post", "发布帖子"),
    FORUM_COMMENT("forum.comment", "发表评论"),
    FORUM_EDIT_OWN("forum.edit_own", "编辑自己的内容"),
    FORUM_DELETE_OWN("forum.delete_own", "删除自己的内容"),
    FORUM_EDIT_ANY("forum.edit_any", "编辑任何内容"),
    FORUM_DELETE_ANY("forum.delete_any", "删除任何内容"),
    FORUM_PIN("forum.pin", "置顶帖子"),
    FORUM_LOCK("forum.lock", "锁定帖子"),
    FORUM_MODERATE("forum.moderate", "论坛管理"),

    // User management
    USER_VIEW_PROFILE("user.view_profile", "查看用户资料"),
    USER_EDIT_OWN("user.edit_own", "编辑自己的资料"),
    USER_BAN("user.ban", "封禁用户"),
    USER_UNBAN("user.unban", "解封用户"),
    USER_MANAGE_ROLES("user.manage_roles", "管理用户角色"),
    USER_WHITELIST("user.whitelist", "管理白名单"),

    // Server management
    SERVER_VIEW_STATUS("server.view_status", "查看服务器状态"),
    SERVER_RESTART("server.restart", "重启服务器"),
    SERVER_BACKUP("server.backup", "备份服务器"),
    SERVER_CONSOLE("server.console", "访问控制台"),
    SERVER_MANAGE("server.manage", "服务器管理"),

    // Wiki permissions
    WIKI_READ("wiki.read", "阅读Wiki"),
    WIKI_EDIT("wiki.edit", "编辑Wiki"),
    WIKI_CREATE("wiki.create", "创建Wiki页面"),
    WIKI_DELETE("wiki.delete", "删除Wiki页面"),

    // Vote permissions
    VOTE_CAST("vote.cast", "参与投票"),
    VOTE_CREATE("vote.create", "创建投票"),
    VOTE_MANAGE("vote.manage", "管理投票"),

    // Achievement permissions
    ACHIEVEMENT_VIEW("achievement.view", "查看成就"),
    ACHIEVEMENT_CREATE("achievement.create", "创建成就"),
    ACHIEVEMENT_GRANT("achievement.grant", "授予成就"),

    // Activity permissions
    ACTIVITY_VIEW("activity.view", "查看活动"),
    ACTIVITY_CREATE("activity.create", "创建活动"),
    ACTIVITY_MANAGE("activity.manage", "管理活动"),

    // Admin permissions
    ADMIN_PANEL("admin.panel", "访问管理面板"),
    ADMIN_LOGS("admin.logs", "查看系统日志"),
    ADMIN_SETTINGS("admin.settings", "修改系统设置"),
    ADMIN_FULL("admin.full", "完全管理权限");

    private final String code;
    private final String description;

    Permission(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Finds a permission by its code.
     */
    public static Permission fromCode(String code) {
        for (Permission p : values()) {
            if (p.code.equals(code)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown permission code: " + code);
    }
}
