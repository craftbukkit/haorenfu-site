/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         POST CATEGORIES
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Forum post categories for organizing discussions.
 */
package world.haorenfu.domain.forum;

/**
 * Categories for forum posts.
 */
public enum PostCategory {

    ANNOUNCEMENT("公告", "📢", "服务器官方公告", true),
    GENERAL("综合讨论", "💬", "一般话题讨论", false),
    GAMEPLAY("游戏玩法", "🎮", "游戏技巧与攻略", false),
    BUILD("建筑展示", "🏗️", "展示你的建筑作品", false),
    REDSTONE("红石科技", "⚡", "红石机械与自动化", false),
    SURVIVAL("生存日记", "📖", "记录你的生存冒险", false),
    TRADE("交易市场", "💰", "玩家间物品交易", false),
    HELP("求助问答", "❓", "提问与解答", false),
    SUGGESTION("建议反馈", "💡", "服务器改进建议", false),
    BUG("问题报告", "🐛", "报告Bug和问题", false),
    RESOURCE("资源分享", "📦", "材质包、光影等资源", false),
    OFF_TOPIC("水区", "🌊", "与MC无关的闲聊", false);

    private final String displayName;
    private final String icon;
    private final String description;
    private final boolean adminOnly;

    PostCategory(String displayName, String icon, String description, boolean adminOnly) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.adminOnly = adminOnly;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }

    public String getFullName() {
        return icon + " " + displayName;
    }
}
