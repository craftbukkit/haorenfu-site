/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          FORUM SERVICE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Business logic for forum operations.
 * Integrates popularity algorithms for post ranking.
 */
package world.haorenfu.domain.forum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.haorenfu.core.algorithm.PopularityEngine;
import world.haorenfu.domain.user.User;
import world.haorenfu.domain.user.UserRepository;
import world.haorenfu.domain.user.UserService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Service for forum operations.
 */
@Service
@Transactional
public class ForumService {

    private static final Logger log = LoggerFactory.getLogger(ForumService.class);

    private final ForumPostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    // Reputation rewards
    private static final int REP_POST_CREATED = 5;
    private static final int REP_COMMENT_CREATED = 2;
    private static final int REP_UPVOTE_RECEIVED = 3;
    private static final int REP_DOWNVOTE_RECEIVED = -2;

    public ForumService(
            ForumPostRepository postRepository,
            CommentRepository commentRepository,
            UserRepository userRepository,
            UserService userService
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * Creates a new post.
     */
    @CacheEvict(value = "posts", allEntries = true)
    public ForumPost createPost(CreatePostRequest request, UUID authorId) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        ForumPost post = new ForumPost(
            request.title(),
            request.content(),
            author,
            request.category()
        );

        // Add tags
        if (request.tags() != null) {
            request.tags().forEach(post::addTag);
        }

        // Calculate initial hot score
        double hotScore = PopularityEngine.calculateHotScore(0, 0, Instant.now(), 0);
        post.updateHotScore(hotScore);

        post = postRepository.save(post);

        // Award reputation
        userService.addReputation(authorId, REP_POST_CREATED);
        author.incrementPostCount();
        userRepository.save(author);

        return post;
    }

    /**
     * Creates or updates a post from entity.
     */
    @CacheEvict(value = "posts", allEntries = true)
    public ForumPost createPost(ForumPost post) {
        if (post.getAuthor() == null) {
            throw new IllegalArgumentException("帖子必须有作者");
        }

        // Calculate hot score
        double hotScore = PopularityEngine.calculateHotScore(
            post.getUpvotes(), post.getDownvotes(), 
            post.getCreatedAt() != null ? post.getCreatedAt() : Instant.now(), 
            post.getViews()
        );
        post.updateHotScore(hotScore);

        post = postRepository.save(post);

        // Award reputation for new posts
        if (post.getCreatedAt() == null || post.getId() == null) {
            userService.addReputation(post.getAuthor().getId(), REP_POST_CREATED);
        }

        return post;
    }

    /**
     * Finds a post by ID (alias for getPost).
     */
    @Transactional(readOnly = true)
    public Optional<ForumPost> findById(UUID id) {
        return getPost(id);
    }

    /**
     * Gets a post by ID.
     */
    @Transactional(readOnly = true)
    public Optional<ForumPost> getPost(UUID id) {
        return postRepository.findById(id);
    }

    /**
     * Gets a post and increments view count.
     */
    public Optional<ForumPost> getPostAndIncrementViews(UUID id) {
        Optional<ForumPost> postOpt = postRepository.findById(id);
        postOpt.ifPresent(post -> {
            post.incrementViews();
            postRepository.save(post);
        });
        return postOpt;
    }

    /**
     * Increments view count for a post.
     */
    public void incrementViewCount(UUID postId) {
        postRepository.findById(postId).ifPresent(post -> {
            post.incrementViews();
            postRepository.save(post);
        });
    }

    /**
     * Gets posts by category.
     */
    @Cacheable(value = "posts", key = "'category:' + #category + ':' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<ForumPost> getPostsByCategory(PostCategory category, Pageable pageable) {
        return postRepository.findByCategorySorted(category, pageable);
    }

    /**
     * Gets all posts sorted by hot score.
     */
    @Cacheable(value = "posts", key = "'hot:' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<ForumPost> getHotPosts(Pageable pageable) {
        return postRepository.findAllSortedByPinnedAndHot(pageable);
    }

    /**
     * Gets recent posts.
     */
    @Transactional(readOnly = true)
    public Page<ForumPost> getRecentPosts(Pageable pageable) {
        return postRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable);
    }

    /**
     * Gets posts by author.
     */
    @Transactional(readOnly = true)
    public Page<ForumPost> getPostsByAuthor(UUID authorId, Pageable pageable) {
        return postRepository.findByAuthorIdAndDeletedFalse(authorId, pageable);
    }

    /**
     * Searches posts.
     */
    @Transactional(readOnly = true)
    public Page<ForumPost> searchPosts(String query, Pageable pageable) {
        return postRepository.searchPosts(query, pageable);
    }

    /**
     * Updates a post.
     */
    @CacheEvict(value = "posts", allEntries = true)
    public ForumPost updatePost(UUID postId, UpdatePostRequest request, UUID userId) {
        ForumPost post = postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException("帖子不存在"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedActionException("无权编辑此帖子");
        }

        if (post.isLocked()) {
            throw new PostLockedException("帖子已锁定，无法编辑");
        }

        post.setTitle(request.title());
        post.setContent(request.content());
        post.markEdited();

        return postRepository.save(post);
    }

    /**
     * Deletes a post (soft delete).
     */
    @CacheEvict(value = "posts", allEntries = true)
    public void deletePost(UUID postId, UUID userId, boolean isAdmin) {
        ForumPost post = postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException("帖子不存在"));

        if (!isAdmin && !post.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedActionException("无权删除此帖子");
        }

        post.softDelete();
        postRepository.save(post);
    }

    /**
     * Votes on a post.
     */
    @CacheEvict(value = "posts", allEntries = true)
    public VoteResult votePost(UUID postId, UUID userId, int value) {
        ForumPost post = postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException("帖子不存在"));

        int previousVote = post.getUserVote(userId);
        int change = post.vote(userId, value);

        // Update hot score
        double hotScore = PopularityEngine.calculateHotScore(
            post.getUpvotes(),
            post.getDownvotes(),
            post.getCreatedAt(),
            post.getViews()
        );
        post.updateHotScore(hotScore);

        postRepository.save(post);

        // Award/deduct reputation to author
        if (change != 0 && !post.getAuthor().getId().equals(userId)) {
            int repChange = change > 0 ? REP_UPVOTE_RECEIVED : REP_DOWNVOTE_RECEIVED;
            userService.addReputation(post.getAuthor().getId(), repChange * Math.abs(change));
        }

        return new VoteResult(post.getUpvotes(), post.getDownvotes(), value);
    }

    /**
     * Pins a post.
     */
    @CacheEvict(value = "posts", allEntries = true)
    public void pinPost(UUID postId, boolean pinned) {
        ForumPost post = postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException("帖子不存在"));

        post.setPinned(pinned);
        postRepository.save(post);
    }

    /**
     * Locks a post.
     */
    @CacheEvict(value = "posts", allEntries = true)
    public void lockPost(UUID postId, boolean locked) {
        ForumPost post = postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException("帖子不存在"));

        post.setLocked(locked);
        postRepository.save(post);
    }

    // Comment operations

    /**
     * Creates a comment.
     */
    public Comment createComment(CreateCommentRequest request, UUID authorId) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        ForumPost post = postRepository.findById(request.postId())
            .orElseThrow(() -> new PostNotFoundException("帖子不存在"));

        if (post.isLocked()) {
            throw new PostLockedException("帖子已锁定，无法评论");
        }

        Comment parent = null;
        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                .orElseThrow(() -> new CommentNotFoundException("父评论不存在"));
        }

        Comment comment = new Comment(request.content(), post, author, parent);

        if (parent != null) {
            parent.addReply(comment);
        } else {
            post.addComment(comment);
        }

        comment = commentRepository.save(comment);
        postRepository.save(post);

        // Award reputation
        userService.addReputation(authorId, REP_COMMENT_CREATED);
        author.incrementCommentCount();
        userRepository.save(author);

        return comment;
    }

    /**
     * Gets comments for a post.
     */
    @Transactional(readOnly = true)
    public Page<Comment> getCommentsForPost(UUID postId, Pageable pageable) {
        return commentRepository.findTopLevelCommentsByPost(postId, pageable);
    }

    /**
     * Votes on a comment.
     */
    public VoteResult voteComment(UUID commentId, UUID userId, int value) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new CommentNotFoundException("评论不存在"));

        int change = comment.vote(userId, value);
        commentRepository.save(comment);

        // Award/deduct reputation
        if (change != 0 && !comment.getAuthor().getId().equals(userId)) {
            int repChange = change > 0 ? REP_UPVOTE_RECEIVED : REP_DOWNVOTE_RECEIVED;
            userService.addReputation(comment.getAuthor().getId(), repChange * Math.abs(change));
        }

        return new VoteResult(comment.getUpvotes(), comment.getDownvotes(), value);
    }

    /**
     * Deletes a comment.
     */
    public void deleteComment(UUID commentId, UUID userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new CommentNotFoundException("评论不存在"));

        if (!isAdmin && !comment.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedActionException("无权删除此评论");
        }

        comment.softDelete();
        commentRepository.save(comment);
    }

    // Statistics and maintenance

    /**
     * Gets forum statistics.
     */
    @Transactional(readOnly = true)
    public ForumStatistics getStatistics() {
        Instant today = Instant.now().minus(Duration.ofDays(1));

        long totalPosts = postRepository.count();
        long postsToday = postRepository.countPostsSince(today);
        long totalComments = commentRepository.count();

        Map<PostCategory, Long> postsByCategory = new EnumMap<>(PostCategory.class);
        for (PostCategory cat : PostCategory.values()) {
            postsByCategory.put(cat, postRepository.countByCategoryAndDeletedFalse(cat));
        }

        return new ForumStatistics(totalPosts, postsToday, totalComments, postsByCategory);
    }

    /**
     * Gets trending posts.
     */
    @Transactional(readOnly = true)
    public List<ForumPost> getTrendingPosts(int limit) {
        Instant since = Instant.now().minus(Duration.ofHours(24));
        return postRepository.findTrendingPosts(since, PageRequest.of(0, limit));
    }

    /**
     * Gets popular tags.
     */
    @Transactional(readOnly = true)
    public List<TagCount> getPopularTags(int limit) {
        return postRepository.findPopularTags(PageRequest.of(0, limit))
            .stream()
            .map(arr -> new TagCount((String) arr[0], ((Number) arr[1]).longValue()))
            .toList();
    }

    /**
     * Scheduled task to update hot scores.
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @CacheEvict(value = "posts", allEntries = true)
    public void updateAllHotScores() {
        Instant since = Instant.now().minus(Duration.ofDays(7));
        List<ForumPost> recentPosts = postRepository.findRecentPosts(since);

        for (ForumPost post : recentPosts) {
            double hotScore = PopularityEngine.calculateHotScore(
                post.getUpvotes(),
                post.getDownvotes(),
                post.getCreatedAt(),
                post.getViews()
            );
            postRepository.updateHotScore(post.getId(), hotScore);
        }
    }

    // Records and DTOs

    public record CreatePostRequest(
        String title,
        String content,
        PostCategory category,
        Set<String> tags
    ) {}

    public record UpdatePostRequest(
        String title,
        String content
    ) {}

    public record CreateCommentRequest(
        UUID postId,
        UUID parentId,
        String content
    ) {}

    public record VoteResult(
        int upvotes,
        int downvotes,
        int userVote
    ) {}

    public record ForumStatistics(
        long totalPosts,
        long postsToday,
        long totalComments,
        Map<PostCategory, Long> postsByCategory
    ) {}

    public record TagCount(
        String tag,
        long count
    ) {}

    // Exceptions

    public static class PostNotFoundException extends RuntimeException {
        public PostNotFoundException(String message) { super(message); }
    }

    public static class CommentNotFoundException extends RuntimeException {
        public CommentNotFoundException(String message) { super(message); }
    }

    public static class UnauthorizedActionException extends RuntimeException {
        public UnauthorizedActionException(String message) { super(message); }
    }

    public static class PostLockedException extends RuntimeException {
        public PostLockedException(String message) { super(message); }
    }
}
