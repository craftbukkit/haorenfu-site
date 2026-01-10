/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          COMMENT ENTITY
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Represents a comment on a forum post.
 * Supports nested replies through parent-child relationships.
 */
package world.haorenfu.domain.forum;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import world.haorenfu.domain.user.User;

import java.time.Instant;
import java.util.*;

/**
 * Comment entity with support for nested replies.
 */
@Entity
@Table(name = "forum_comments", indexes = {
    @Index(name = "idx_comment_post", columnList = "post_id"),
    @Index(name = "idx_comment_author", columnList = "author_id"),
    @Index(name = "idx_comment_parent", columnList = "parent_id"),
    @Index(name = "idx_comment_created", columnList = "createdAt")
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 10000, message = "评论长度应在1-10000个字符之间")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private ForumPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // Support for nested replies
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    private List<Comment> replies = new ArrayList<>();

    // Voting
    @Column(nullable = false)
    private int upvotes = 0;

    @Column(nullable = false)
    private int downvotes = 0;

    @ElementCollection
    @CollectionTable(name = "comment_votes", joinColumns = @JoinColumn(name = "comment_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "vote_value")
    private Map<UUID, Integer> votes = new HashMap<>();

    // Status
    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private boolean edited = false;

    // Timestamps
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant editedAt;

    // Constructors
    public Comment() {
        this.createdAt = Instant.now();
    }

    public Comment(String content, ForumPost post, User author) {
        this();
        this.content = content;
        this.post = post;
        this.author = author;
    }

    public Comment(String content, ForumPost post, User author, Comment parent) {
        this(content, post, author);
        this.parent = parent;
    }

    // Business logic

    /**
     * Registers a vote.
     */
    public int vote(UUID userId, int value) {
        Integer previousVote = votes.get(userId);
        int change = 0;

        if (previousVote != null) {
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
     * Gets user's vote.
     */
    public int getUserVote(UUID userId) {
        return votes.getOrDefault(userId, 0);
    }

    /**
     * Adds a reply.
     */
    public void addReply(Comment reply) {
        replies.add(reply);
        reply.setParent(this);
    }

    /**
     * Marks as edited.
     */
    public void markEdited() {
        this.edited = true;
        this.editedAt = Instant.now();
    }

    /**
     * Soft deletes.
     */
    public void softDelete() {
        this.deleted = true;
        this.content = "[评论已删除]";
    }

    /**
     * Gets the nesting depth.
     */
    public int getDepth() {
        int depth = 0;
        Comment current = this.parent;
        while (current != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    /**
     * Checks if this is a reply.
     */
    public boolean isReply() {
        return parent != null;
    }

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ForumPost getPost() {
        return post;
    }

    public void setPost(ForumPost post) {
        this.post = post;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Comment getParent() {
        return parent;
    }

    public void setParent(Comment parent) {
        this.parent = parent;
    }

    public List<Comment> getReplies() {
        return Collections.unmodifiableList(replies);
    }

    public int getUpvotes() {
        return upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public int getVoteDifferential() {
        return upvotes - downvotes;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isEdited() {
        return edited;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getEditedAt() {
        return editedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
