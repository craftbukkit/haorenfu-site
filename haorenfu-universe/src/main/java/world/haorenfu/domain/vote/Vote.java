/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                              VOTE ENTITY
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Represents a community vote/poll with multiple options.
 * Supports weighted voting based on user reputation.
 */
package world.haorenfu.domain.vote;

import jakarta.persistence.*;
import world.haorenfu.domain.user.User;

import java.time.Instant;
import java.util.*;

/**
 * Community vote/poll entity.
 */
@Entity
@Table(name = "votes")
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<VoteOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL)
    private Set<VoteCast> voteCasts = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteType type = VoteType.SINGLE_CHOICE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteStatus status = VoteStatus.ACTIVE;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant deadline;

    @Column
    private Instant closedAt;

    // Minimum reputation required to vote
    @Column(nullable = false)
    private int minReputation = 0;

    // Whether results are visible before deadline
    @Column(nullable = false)
    private boolean showResultsBeforeClose = true;

    // Whether vote is anonymous
    @Column(nullable = false)
    private boolean anonymous = false;

    // Total number of votes cast
    @Column(nullable = false)
    private int totalVotes = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Getters and Setters
    // ═══════════════════════════════════════════════════════════════════════

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public List<VoteOption> getOptions() { return options; }
    public void setOptions(List<VoteOption> options) { this.options = options; }

    public Set<VoteCast> getVoteCasts() { return voteCasts; }

    public VoteType getType() { return type; }
    public void setType(VoteType type) { this.type = type; }

    public VoteStatus getStatus() { return status; }
    public void setStatus(VoteStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getDeadline() { return deadline; }
    public void setDeadline(Instant deadline) { this.deadline = deadline; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }

    public int getMinReputation() { return minReputation; }
    public void setMinReputation(int minReputation) { this.minReputation = minReputation; }

    public boolean isShowResultsBeforeClose() { return showResultsBeforeClose; }
    public void setShowResultsBeforeClose(boolean show) { this.showResultsBeforeClose = show; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public int getTotalVotes() { return totalVotes; }
    public void setTotalVotes(int totalVotes) { this.totalVotes = totalVotes; }

    // ═══════════════════════════════════════════════════════════════════════
    // Business Methods
    // ═══════════════════════════════════════════════════════════════════════

    public void addOption(VoteOption option) {
        options.add(option);
        option.setVote(this);
        option.setDisplayOrder(options.size());
    }

    public boolean isActive() {
        return status == VoteStatus.ACTIVE && Instant.now().isBefore(deadline);
    }

    public boolean hasUserVoted(User user) {
        return voteCasts.stream()
            .anyMatch(vc -> vc.getUser().getId().equals(user.getId()));
    }

    public void incrementTotalVotes() {
        this.totalVotes++;
    }
}

/**
 * Vote option within a poll.
 */
@Entity
@Table(name = "vote_options")
class VoteOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private int voteCount = 0;

    // ═══════════════════════════════════════════════════════════════════════

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Vote getVote() { return vote; }
    public void setVote(Vote vote) { this.vote = vote; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int order) { this.displayOrder = order; }

    public int getVoteCount() { return voteCount; }
    public void setVoteCount(int count) { this.voteCount = count; }

    public void incrementCount() { this.voteCount++; }

    /**
     * Calculates percentage of total votes.
     */
    public double getPercentage() {
        int total = vote.getTotalVotes();
        if (total == 0) return 0.0;
        return (voteCount * 100.0) / total;
    }
}

/**
 * Individual vote cast by a user.
 */
@Entity
@Table(name = "vote_casts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"vote_id", "user_id"})
})
class VoteCast {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private VoteOption selectedOption;

    @Column(nullable = false)
    private Instant castAt;

    // User's reputation at time of voting (for weighted voting)
    @Column(nullable = false)
    private int userReputationAtVote;

    @PrePersist
    protected void onCreate() {
        castAt = Instant.now();
    }

    // ═══════════════════════════════════════════════════════════════════════

    public UUID getId() { return id; }
    public Vote getVote() { return vote; }
    public void setVote(Vote vote) { this.vote = vote; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public VoteOption getSelectedOption() { return selectedOption; }
    public void setSelectedOption(VoteOption option) { this.selectedOption = option; }

    public Instant getCastAt() { return castAt; }

    public int getUserReputationAtVote() { return userReputationAtVote; }
    public void setUserReputationAtVote(int rep) { this.userReputationAtVote = rep; }
}

/**
 * Vote type enumeration.
 */
enum VoteType {
    SINGLE_CHOICE("单选"),
    MULTIPLE_CHOICE("多选"),
    RANKED_CHOICE("排序投票");

    private final String displayName;

    VoteType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}

/**
 * Vote status enumeration.
 */
enum VoteStatus {
    DRAFT("草稿"),
    ACTIVE("进行中"),
    CLOSED("已结束"),
    CANCELLED("已取消");

    private final String displayName;

    VoteStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
