/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                       FORUM REPOSITORIES
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Data access layer for forum entities.
 */
package world.haorenfu.domain.forum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for forum posts.
 */
@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, UUID> {

    /**
     * Finds a post by ID with author eagerly loaded (avoids N+1).
     */
    @Query("SELECT p FROM ForumPost p LEFT JOIN FETCH p.author WHERE p.id = :id")
    Optional<ForumPost> findByIdWithAuthor(@Param("id") UUID id);

    /**
     * Finds posts by category.
     */
    Page<ForumPost> findByCategoryAndDeletedFalse(PostCategory category, Pageable pageable);

    /**
     * Finds posts by author.
     */
    Page<ForumPost> findByAuthorIdAndDeletedFalse(UUID authorId, Pageable pageable);

    /**
     * Finds all non-deleted posts, ordered by hot score.
     */
    Page<ForumPost> findByDeletedFalseOrderByHotScoreDesc(Pageable pageable);

    /**
     * Finds all non-deleted posts, pinned first then by hot score.
     */
    @Query("SELECT p FROM ForumPost p WHERE p.deleted = false ORDER BY p.pinned DESC, p.hotScore DESC")
    Page<ForumPost> findAllSortedByPinnedAndHot(Pageable pageable);

    /**
     * Finds recent posts.
     */
    Page<ForumPost> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Finds posts by category, sorted.
     */
    @Query("SELECT p FROM ForumPost p WHERE p.category = :category AND p.deleted = false " +
           "ORDER BY p.pinned DESC, p.hotScore DESC")
    Page<ForumPost> findByCategorySorted(@Param("category") PostCategory category, Pageable pageable);

    /**
     * Searches posts by title or content.
     */
    @Query("SELECT p FROM ForumPost p WHERE p.deleted = false AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<ForumPost> searchPosts(@Param("query") String query, Pageable pageable);

    /**
     * Finds posts by tag.
     */
    @Query("SELECT p FROM ForumPost p JOIN p.tags t WHERE t = :tag AND p.deleted = false")
    Page<ForumPost> findByTag(@Param("tag") String tag, Pageable pageable);

    /**
     * Finds pinned posts.
     */
    List<ForumPost> findByPinnedTrueAndDeletedFalseOrderByCreatedAtDesc();

    /**
     * Counts posts by category.
     */
    long countByCategoryAndDeletedFalse(PostCategory category);

    /**
     * Counts posts by author.
     */
    long countByAuthorIdAndDeletedFalse(UUID authorId);

    /**
     * Gets posts created since a timestamp.
     */
    @Query("SELECT p FROM ForumPost p WHERE p.deleted = false AND p.createdAt >= :since")
    List<ForumPost> findRecentPosts(@Param("since") Instant since);

    /**
     * Counts posts since timestamp.
     */
    @Query("SELECT COUNT(p) FROM ForumPost p WHERE p.createdAt >= :since")
    long countPostsSince(@Param("since") Instant since);

    /**
     * Updates hot score for a post.
     */
    @Modifying
    @Query("UPDATE ForumPost p SET p.hotScore = :score WHERE p.id = :id")
    void updateHotScore(@Param("id") UUID id, @Param("score") double score);

    /**
     * Gets trending posts (most activity recently).
     */
    @Query("SELECT p FROM ForumPost p WHERE p.deleted = false AND p.lastActivityAt >= :since " +
           "ORDER BY p.commentCount DESC, p.upvotes DESC")
    List<ForumPost> findTrendingPosts(@Param("since") Instant since, Pageable pageable);

    /**
     * Gets popular tags.
     */
    @Query("SELECT t, COUNT(p) as cnt FROM ForumPost p JOIN p.tags t WHERE p.deleted = false " +
           "GROUP BY t ORDER BY cnt DESC")
    List<Object[]> findPopularTags(Pageable pageable);
}

/**
 * Repository for comments.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Finds top-level comments for a post.
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL AND c.deleted = false " +
           "ORDER BY c.createdAt ASC")
    Page<Comment> findTopLevelCommentsByPost(@Param("postId") UUID postId, Pageable pageable);

    /**
     * Finds replies to a comment.
     */
    List<Comment> findByParentIdAndDeletedFalseOrderByCreatedAtAsc(UUID parentId);

    /**
     * Finds comments by author.
     */
    Page<Comment> findByAuthorIdAndDeletedFalse(UUID authorId, Pageable pageable);

    /**
     * Counts comments for a post.
     */
    long countByPostIdAndDeletedFalse(UUID postId);

    /**
     * Counts comments by author.
     */
    long countByAuthorIdAndDeletedFalse(UUID authorId);

    /**
     * Gets recent comments.
     */
    @Query("SELECT c FROM Comment c WHERE c.deleted = false AND c.createdAt >= :since")
    List<Comment> findRecentComments(@Param("since") Instant since);
}
