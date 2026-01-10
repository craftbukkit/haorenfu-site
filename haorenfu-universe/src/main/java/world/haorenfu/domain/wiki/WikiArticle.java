/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          WIKI ARTICLE ENTITY
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Community-editable wiki system with version control.
 * Supports Markdown content and collaborative editing.
 */
package world.haorenfu.domain.wiki;

import jakarta.persistence.*;
import world.haorenfu.domain.user.User;

import java.time.Instant;
import java.util.*;

/**
 * Wiki article entity with version history.
 */
@Entity
@Table(name = "wiki_articles")
public class WikiArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_editor_id")
    private User lastEditor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private WikiCategory category;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    @OrderBy("createdAt DESC")
    private List<WikiRevision> revisions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "wiki_article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Column(nullable = false)
    private int version = 1;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private boolean featured = false;

    @Column(nullable = false)
    private boolean locked = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArticleStatus status = ArticleStatus.PUBLISHED;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        generateSlug();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    private void generateSlug() {
        if (slug == null && title != null) {
            slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Getters and Setters
    // ═══════════════════════════════════════════════════════════════════════

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public User getLastEditor() { return lastEditor; }
    public void setLastEditor(User lastEditor) { this.lastEditor = lastEditor; }

    public WikiCategory getCategory() { return category; }
    public void setCategory(WikiCategory category) { this.category = category; }

    public List<WikiRevision> getRevisions() { return revisions; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }

    public int getVersion() { return version; }

    public int getViewCount() { return viewCount; }
    public void incrementViewCount() { this.viewCount++; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public ArticleStatus getStatus() { return status; }
    public void setStatus(ArticleStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // ═══════════════════════════════════════════════════════════════════════
    // Business Methods
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Creates a new revision and updates the article.
     */
    public WikiRevision createRevision(User editor, String newContent, String editSummary) {
        WikiRevision revision = new WikiRevision();
        revision.setArticle(this);
        revision.setEditor(editor);
        revision.setContent(this.content); // Store previous content
        revision.setVersion(this.version);
        revision.setEditSummary(editSummary);

        revisions.add(revision);
        this.content = newContent;
        this.lastEditor = editor;
        this.version++;

        return revision;
    }

    /**
     * Calculates word count.
     */
    public int getWordCount() {
        if (content == null) return 0;
        // Count Chinese characters and English words
        int chineseCount = content.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
        int englishCount = content.replaceAll("[\\u4e00-\\u9fa5]", "")
            .trim().split("\\s+").length;
        return chineseCount + englishCount;
    }

    /**
     * Gets estimated reading time in minutes.
     */
    public int getReadingTime() {
        // Average reading speed: 400 Chinese chars or 200 English words per minute
        return Math.max(1, getWordCount() / 300);
    }
}

/**
 * Wiki article revision for version history.
 */
@Entity
@Table(name = "wiki_revisions")
class WikiRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private WikiArticle article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editor_id", nullable = false)
    private User editor;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int version;

    @Column(length = 500)
    private String editSummary;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }

    public WikiArticle getArticle() { return article; }
    public void setArticle(WikiArticle article) { this.article = article; }

    public User getEditor() { return editor; }
    public void setEditor(User editor) { this.editor = editor; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public String getEditSummary() { return editSummary; }
    public void setEditSummary(String summary) { this.editSummary = summary; }

    public Instant getCreatedAt() { return createdAt; }
}

/**
 * Wiki category for organization.
 */
@Entity
@Table(name = "wiki_categories")
class WikiCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String description;

    private String icon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private WikiCategory parent;

    @OneToMany(mappedBy = "parent")
    private List<WikiCategory> children = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    private List<WikiArticle> articles = new ArrayList<>();

    @Column(nullable = false)
    private int displayOrder = 0;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public WikiCategory getParent() { return parent; }
    public void setParent(WikiCategory parent) { this.parent = parent; }

    public List<WikiCategory> getChildren() { return children; }
    public List<WikiArticle> getArticles() { return articles; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int order) { this.displayOrder = order; }

    public int getArticleCount() { return articles.size(); }
}

/**
 * Article status enumeration.
 */
enum ArticleStatus {
    DRAFT("草稿"),
    PUBLISHED("已发布"),
    ARCHIVED("已归档"),
    DELETED("已删除");

    private final String displayName;

    ArticleStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
