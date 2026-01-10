/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                        ACHIEVEMENT SYSTEM
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * A gamification layer to reward and engage community members.
 * Achievements provide goals and recognition for various activities.
 */
package world.haorenfu.domain.achievement;

import jakarta.persistence.*;
import world.haorenfu.domain.user.User;

import java.time.Instant;
import java.util.*;

/**
 * Achievement definition entity.
 */
@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 10)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementRarity rarity;

    @Column(nullable = false)
    private int reputationReward = 0;

    @Column(nullable = false)
    private boolean hidden = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "achievement", cascade = CascadeType.ALL)
    private List<UserAchievement> userAchievements = new ArrayList<>();

    public Achievement() {
        this.createdAt = Instant.now();
    }

    public Achievement(String code, String name, String description, String icon,
                       AchievementCategory category, AchievementRarity rarity, int reputationReward) {
        this();
        this.code = code;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.category = category;
        this.rarity = rarity;
        this.reputationReward = reputationReward;
    }

    // Getters
    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public AchievementCategory getCategory() { return category; }
    public AchievementRarity getRarity() { return rarity; }
    public int getReputationReward() { return reputationReward; }
    public boolean isHidden() { return hidden; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setReputationReward(int reputationReward) { this.reputationReward = reputationReward; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Achievement that = (Achievement) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

/**
 * User achievement unlock record.
 */
@Entity
@Table(name = "user_achievements", indexes = {
    @Index(name = "idx_ua_user", columnList = "user_id"),
    @Index(name = "idx_ua_achievement", columnList = "achievement_id")
})
class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(nullable = false)
    private Instant unlockedAt;

    @Column
    private String unlockedContext;

    public UserAchievement() {
        this.unlockedAt = Instant.now();
    }

    public UserAchievement(User user, Achievement achievement) {
        this();
        this.user = user;
        this.achievement = achievement;
    }

    public UserAchievement(User user, Achievement achievement, String context) {
        this(user, achievement);
        this.unlockedContext = context;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public Achievement getAchievement() { return achievement; }
    public Instant getUnlockedAt() { return unlockedAt; }
    public String getUnlockedContext() { return unlockedContext; }
}

/**
 * Achievement categories.
 */
enum AchievementCategory {
    COMMUNITY("社区", "💬"),
    EXPLORATION("探索", "🗺️"),
    BUILDING("建造", "🏗️"),
    COMBAT("战斗", "⚔️"),
    SURVIVAL("生存", "🏕️"),
    REDSTONE("红石", "⚡"),
    COLLECTION("收集", "📦"),
    SPECIAL("特殊", "⭐");

    private final String displayName;
    private final String icon;

    AchievementCategory(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
}

/**
 * Achievement rarity levels.
 */
enum AchievementRarity {
    COMMON("普通", "#AAAAAA", 1.0),
    UNCOMMON("稀有", "#1EFF00", 0.5),
    RARE("精良", "#0070DD", 0.25),
    EPIC("史诗", "#A335EE", 0.1),
    LEGENDARY("传说", "#FF8000", 0.05);

    private final String displayName;
    private final String color;
    private final double dropRate;

    AchievementRarity(String displayName, String color, double dropRate) {
        this.displayName = displayName;
        this.color = color;
        this.dropRate = dropRate;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public double getDropRate() { return dropRate; }
}
