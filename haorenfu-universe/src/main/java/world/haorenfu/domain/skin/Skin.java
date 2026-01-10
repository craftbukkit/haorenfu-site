/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          SKIN MANAGEMENT SYSTEM
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Minecraft skin gallery and management system.
 * Supports skin uploads, previews, and community sharing.
 */
package world.haorenfu.domain.skin;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Minecraft player skin entity.
 */
@Entity
@Table(name = "skins")
public class Skin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;

    // Base64 encoded skin image (64x64 PNG)
    @Column(name = "skin_data", columnDefinition = "TEXT", nullable = false)
    private String skinData;

    // Base64 encoded cape image (optional, 64x32 PNG)
    @Column(name = "cape_data", columnDefinition = "TEXT")
    private String capeData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkinModel model = SkinModel.CLASSIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkinVisibility visibility = SkinVisibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkinCategory category = SkinCategory.ORIGINAL;

    @ElementCollection
    @CollectionTable(name = "skin_tags", joinColumns = @JoinColumn(name = "skin_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    // Statistics
    @Column(nullable = false)
    private int downloads = 0;

    @Column(nullable = false)
    private int likes = 0;

    @Column(nullable = false)
    private int views = 0;

    // Timestamps
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Featured flag (admin set)
    @Column(nullable = false)
    private boolean featured = false;

    // NSFW flag
    @Column(nullable = false)
    private boolean nsfw = false;

    // Verification status
    @Column(nullable = false)
    private boolean verified = false;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Constructors
    public Skin() {}

    public Skin(String name, UUID ownerId, String ownerUsername, String skinData) {
        this.name = name;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.skinData = skinData;
    }

    // Business methods
    public void incrementDownloads() {
        this.downloads++;
    }

    public void incrementViews() {
        this.views++;
    }

    public void like() {
        this.likes++;
    }

    public void unlike() {
        if (this.likes > 0) this.likes--;
    }

    public void addTag(String tag) {
        this.tags.add(tag.toLowerCase().trim());
    }

    public void removeTag(String tag) {
        this.tags.remove(tag.toLowerCase().trim());
    }

    // Getters and setters
    public UUID getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getOwnerId() { return ownerId; }

    public String getOwnerUsername() { return ownerUsername; }

    public String getSkinData() { return skinData; }
    public void setSkinData(String skinData) { this.skinData = skinData; }

    public String getCapeData() { return capeData; }
    public void setCapeData(String capeData) { this.capeData = capeData; }

    public SkinModel getModel() { return model; }
    public void setModel(SkinModel model) { this.model = model; }

    public SkinVisibility getVisibility() { return visibility; }
    public void setVisibility(SkinVisibility visibility) { this.visibility = visibility; }

    public SkinCategory getCategory() { return category; }
    public void setCategory(SkinCategory category) { this.category = category; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }

    public int getDownloads() { return downloads; }
    public int getLikes() { return likes; }
    public int getViews() { return views; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }

    public boolean isNsfw() { return nsfw; }
    public void setNsfw(boolean nsfw) { this.nsfw = nsfw; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    /**
     * Skin model types (arm width).
     */
    public enum SkinModel {
        CLASSIC("经典", "Steve model with 4px arms"),
        SLIM("纤细", "Alex model with 3px arms");

        private final String displayName;
        private final String description;

        SkinModel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    /**
     * Visibility options.
     */
    public enum SkinVisibility {
        PUBLIC("公开", "Anyone can view and download"),
        UNLISTED("未列出", "Only accessible via direct link"),
        PRIVATE("私密", "Only visible to owner");

        private final String displayName;
        private final String description;

        SkinVisibility(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    /**
     * Skin categories.
     */
    public enum SkinCategory {
        ORIGINAL("原创", "Original designs"),
        ANIME("动漫", "Anime characters"),
        GAME("游戏", "Game characters"),
        MOVIE("影视", "Movie/TV characters"),
        CELEBRITY("名人", "Real people"),
        MEME("梗图", "Memes and jokes"),
        HOLIDAY("节日", "Holiday themed"),
        ANIMAL("动物", "Animals and creatures"),
        ROBOT("机械", "Robots and machines"),
        FANTASY("奇幻", "Fantasy creatures"),
        HISTORICAL("历史", "Historical figures"),
        OTHER("其他", "Other");

        private final String displayName;
        private final String description;

        SkinCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
}
