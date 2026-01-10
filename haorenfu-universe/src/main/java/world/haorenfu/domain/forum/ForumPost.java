/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         FORUM POST ENTITY
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Represents a discussion thread in the community forum.
 * Posts are the primary unit of community interaction.
 */
package world.haorenfu.domain.forum;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import world.haorenfu.domain.user.User;

import java.time.Instant;
import java.util.*;

/**
 * Forum post entity.
 */
@Entity
@Table(name = "forum_posts", indexes = {
    @Index(name = "idx_post_category", columnList = "category"),
    @Index(name = "idx_post_author", columnList = "author_id"),
    @Index(name = "idx_post_created", columnList = "createdAt"),
    @Index(name = "idx_post_hot_score", columnList = "hotScore")
})
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 100, message = "标题长度应在2-100个字符之间")
    @Column(nullable = false, length = 100)
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(min = 10, max = 50000, message = "内容长度应在10-50000个字符之间")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostCategory category = PostCategory.GENERAL;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    // Voting
    @Column(nullable = false)
    private int upvotes = 0;

    @Column(nullable = false)
    private int downvotes = 0;

    @ElementCollection
    @CollectionTable(name = "post_votes", joinColumns = @JoinColumn(name = "post_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "vote_value")
    private Map<UUID, Integer> votes = new HashMap<>();

    // Engagement metrics
    @Column(nullable = false)
    private long views = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Comment> comments = new ArrayList<>();

    // Status flags
    @Column(nullable = false)
    private boolean pinned = false;

    @Column(nullable = false)
    private boolean locked = false;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private boolean edited = false;

    // Ranking score (pre-calculated for performance)
    @Column(nullable = false)
    private double hotScore = 0.0;

    // Timestamps
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant editedAt;

    @Column
    private Instant lastActivityAt;

    // Constructors
    public ForumPost() {
        this.createdAt = Instant.now();
        this.lastActivityAt = Instant.now();
    }

    public ForumPost(String title, String content, User author, PostCategory category) {
        this();
        this.title = title;
        this.content = content;
        this.author = author;
        this.category = category;
    }

    // Business logic methods

    /**
     * Registers a vote from a user.
     *
     * @param userId User ID
     * @param value  Vote value (+1 for upvote, -1 for downvote)
     * @return The change in vote differential
     */
    public int vote(UUID userId, int value) {
        Integer previousVote = votes.get(userId);
        int change = 0;

        if (previousVote != null) {
            // Remove previous vote
            if (previousVote > 0) upvotes--;
            else if (previousVote < 0) downvotes--;
            change -= previousVote;
        }

        if (value != 0) {
            votes.put(userId, value);
            if (value > 0) upvotes++;
            else downvotes++;
            change += value;
        } else {
            votes.remove(userId);
        }

        return change;
    }

    /**
     * Gets the user's vote on this post.
     */
    public int getUserVote(UUID userId) {
        return votes.getOrDefault(userId, 0);
    }

    /**
     * Increments view count.
     */
    public void incrementViews() {
        this.views++;
    }

    /**
     * Adds a comment to the post.
     */
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
        this.commentCount++;
        this.lastActivityAt = Instant.now();
    }

    /**
     * Removes a comment from the post.
     */
    public void removeComment(Comment comment) {
        comments.remove(comment);
        this.commentCount = Math.max(0, this.commentCount - 1);
    }

    /**
     * Marks the post as edited.
     */
    public void markEdited() {
        this.edited = true;
        this.editedAt = Instant.now();
    }

    /**
     * Soft deletes the post.
     */
    public void softDelete() {
        this.deleted = true;
    }

    /**
     * Calculates and updates the hot score.
     */
    public void updateHotScore(double score) {
        this.hotScore = score;
    }

    /**
     * Adds a tag.
     */
    public void addTag(String tag) {
        tags.add(tag.toLowerCase());
    }

    /**
     * Removes a tag.
     */
    public void removeTag(String tag) {
        tags.remove(tag.toLowerCase());
    }

    /**
     * Gets the vote differential.
     */
    public int getVoteDifferential() {
        return upvotes - downvotes;
    }

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public PostCategory getCategory() {
        return category;
    }

    public void setCategory(PostCategory category) {
        this.category = category;
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public int getUpvotes() {
        return upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public long getViews() {
        return views;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isEdited() {
        return edited;
    }

    public double getHotScore() {
        return hotScore;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getEditedAt() {
        return editedAt;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Instant lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForumPost post = (ForumPost) o;
        return Objects.equals(id, post.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
