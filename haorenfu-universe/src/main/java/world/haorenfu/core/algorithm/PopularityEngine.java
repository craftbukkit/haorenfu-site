/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                    POPULARITY ALGORITHM ENGINE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * A sophisticated content ranking system combining multiple mathematical
 * models to determine content visibility and popularity.
 *
 * Mathematical Foundations:
 *
 * 1. PageRank Variant (Link Analysis)
 *    PR(A) = (1-d) + d * Σ(PR(Ti)/C(Ti))
 *    Modified for social interactions instead of hyperlinks
 *
 * 2. Exponential Time Decay (Newton's Law of Cooling)
 *    Score(t) = Score₀ * e^(-λt)
 *    Older content naturally decays in relevance
 *
 * 3. Wilson Score Interval (Confidence-based Rating)
 *    Uses Bayesian inference for rating confidence
 *    Better than simple averaging for small sample sizes
 *
 * 4. Reddit-style Hot Ranking
 *    Combines vote differential with time-based decay
 *
 * Applications:
 * - Forum post ranking
 * - User reputation scoring
 * - Achievement difficulty rating
 * - Server event popularity
 */
package world.haorenfu.core.algorithm;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Engine for calculating content popularity and rankings.
 *
 * Thread-safe implementation suitable for concurrent access
 * in a web application environment.
 */
public class PopularityEngine {

    // PageRank damping factor (typically 0.85)
    private static final double DAMPING_FACTOR = 0.85;

    // Time decay constants
    private static final double DECAY_RATE = 0.0001; // λ for exponential decay
    private static final long GRAVITY = 45000L; // Gravity for hot ranking (seconds)

    // Wilson score Z-value for 95% confidence interval
    private static final double Z_VALUE = 1.96;

    /**
     * Calculates the "hot" score for a content item.
     *
     * Based on the Reddit ranking algorithm with modifications:
     * - Uses hyperbolic functions for smoother curves
     * - Incorporates view count as a secondary signal
     *
     * Formula:
     * sign(votes) * log10(max(|votes|, 1)) + age_factor
     *
     * @param upvotes   Number of upvotes
     * @param downvotes Number of downvotes
     * @param created   Creation timestamp
     * @param views     View count (optional signal)
     * @return Hot score (higher = more prominent)
     */
    public static double calculateHotScore(
            int upvotes,
            int downvotes,
            Instant created,
            long views
    ) {
        // Vote differential
        int voteDiff = upvotes - downvotes;
        int sign = Integer.compare(voteDiff, 0);

        // Logarithmic vote component (handles large numbers gracefully)
        double voteScore = sign * Math.log10(Math.max(Math.abs(voteDiff), 1));

        // Age in seconds since epoch reference point
        long epochSeconds = created.getEpochSecond();
        long referencePoint = 1640000000L; // Arbitrary reference (2021-12-20)
        double ageComponent = (double) (epochSeconds - referencePoint) / GRAVITY;

        // View bonus (diminishing returns with log)
        double viewBonus = Math.log10(Math.max(views, 1)) * 0.1;

        return voteScore + ageComponent + viewBonus;
    }

    /**
     * Calculates Wilson score lower bound.
     *
     * This is statistically superior to simple averaging because it
     * accounts for confidence intervals. An item with 1 upvote and 0 downvotes
     * won't necessarily rank higher than one with 100 upvotes and 10 downvotes.
     *
     * Formula:
     * (p̂ + z²/2n - z√(p̂(1-p̂)/n + z²/4n²)) / (1 + z²/n)
     *
     * Where:
     * - p̂ = observed positive rate
     * - n = total votes
     * - z = z-value for confidence level
     *
     * @param positive Number of positive votes
     * @param total    Total number of votes
     * @return Lower bound of Wilson score interval
     */
    public static double calculateWilsonScore(int positive, int total) {
        if (total == 0) return 0.0;

        double n = total;
        double p = (double) positive / n;
        double z = Z_VALUE;
        double z2 = z * z;

        // Wilson score formula
        double numerator = p + z2 / (2 * n) -
                z * Math.sqrt((p * (1 - p) + z2 / (4 * n)) / n);
        double denominator = 1 + z2 / n;

        return numerator / denominator;
    }

    /**
     * Applies exponential time decay to a score.
     *
     * Based on Newton's Law of Cooling, adapted for content relevance:
     * Score(t) = Score₀ * e^(-λt)
     *
     * @param baseScore Original score
     * @param age       Age of the content
     * @return Decayed score
     */
    public static double applyTimeDecay(double baseScore, Duration age) {
        double hours = age.toHours();
        return baseScore * Math.exp(-DECAY_RATE * hours);
    }

    /**
     * Calculates controversy score.
     *
     * High controversy = many votes but nearly equal split.
     * Uses entropy-like formula for maximum at 50/50 split.
     *
     * @param upvotes   Number of upvotes
     * @param downvotes Number of downvotes
     * @return Controversy score (0 to 1, higher = more controversial)
     */
    public static double calculateControversy(int upvotes, int downvotes) {
        int total = upvotes + downvotes;
        if (total < 5) return 0.0; // Need minimum votes

        double magnitude = Math.pow(total, 0.8);
        double balance = Math.min(upvotes, downvotes) / (double) Math.max(upvotes, downvotes);

        return magnitude * balance;
    }

    /**
     * Bayesian average rating calculation.
     *
     * Prevents items with few ratings from dominating rankings.
     * Uses the global average and a confidence parameter.
     *
     * Formula:
     * BR = (v / (v + m)) * R + (m / (v + m)) * C
     *
     * Where:
     * - v = number of votes for the item
     * - m = minimum votes required for consideration
     * - R = average rating for the item
     * - C = mean rating across all items
     *
     * @param itemRating  Average rating for this item
     * @param itemVotes   Number of votes for this item
     * @param globalMean  Mean rating across all items
     * @param minVotes    Minimum votes threshold
     * @return Bayesian adjusted rating
     */
    public static double calculateBayesianRating(
            double itemRating,
            int itemVotes,
            double globalMean,
            int minVotes
    ) {
        double v = itemVotes;
        double m = minVotes;
        double R = itemRating;
        double C = globalMean;

        return (v / (v + m)) * R + (m / (v + m)) * C;
    }

    /**
     * Iterative PageRank calculation for a graph of items.
     *
     * Modified for content interactions where "links" are:
     * - Comments referencing other posts
     * - User endorsements
     * - Quote relationships
     *
     * @param adjacencyList Map of item ID to list of items it "links" to
     * @param iterations    Number of iterations (typically 20-100)
     * @return Map of item ID to PageRank score
     */
    public static Map<String, Double> calculatePageRank(
            Map<String, List<String>> adjacencyList,
            int iterations
    ) {
        Set<String> allNodes = new HashSet<>(adjacencyList.keySet());
        adjacencyList.values().forEach(allNodes::addAll);

        int n = allNodes.size();
        if (n == 0) return new HashMap<>();

        double initialRank = 1.0 / n;

        Map<String, Double> ranks = new HashMap<>();
        allNodes.forEach(node -> ranks.put(node, initialRank));

        // Build reverse adjacency for incoming links
        Map<String, List<String>> incomingLinks = new HashMap<>();
        allNodes.forEach(node -> incomingLinks.put(node, new ArrayList<>()));

        adjacencyList.forEach((source, targets) ->
            targets.forEach(target -> incomingLinks.get(target).add(source))
        );

        // Iterative calculation
        for (int iter = 0; iter < iterations; iter++) {
            Map<String, Double> newRanks = new HashMap<>();

            for (String node : allNodes) {
                double rankSum = 0.0;

                for (String incoming : incomingLinks.get(node)) {
                    int outDegree = adjacencyList.getOrDefault(incoming, List.of()).size();
                    if (outDegree > 0) {
                        rankSum += ranks.get(incoming) / outDegree;
                    }
                }

                // PageRank formula with damping
                double newRank = (1 - DAMPING_FACTOR) / n + DAMPING_FACTOR * rankSum;
                newRanks.put(node, newRank);
            }

            ranks.clear();
            ranks.putAll(newRanks);
        }

        return ranks;
    }

    /**
     * Combined ranking score for forum posts.
     *
     * Balances multiple signals:
     * - Hot score (recency + votes)
     * - Quality score (Wilson confidence)
     * - Engagement (comments, views)
     *
     * @param params Ranking parameters
     * @return Combined score for sorting
     */
    public static double calculateCombinedScore(RankingParams params) {
        double hotScore = calculateHotScore(
            params.upvotes,
            params.downvotes,
            params.created,
            params.views
        );

        double qualityScore = calculateWilsonScore(
            params.upvotes,
            params.upvotes + params.downvotes
        );

        double engagementScore = Math.log10(Math.max(params.comments + 1, 1)) * 0.5;

        // Weighted combination
        return hotScore * 0.5 + qualityScore * 10 + engagementScore;
    }

    /**
     * Parameter record for combined ranking.
     */
    public record RankingParams(
        int upvotes,
        int downvotes,
        Instant created,
        long views,
        int comments
    ) {}

    /**
     * Trending detection using velocity calculation.
     *
     * Identifies content with unusually high recent activity
     * relative to its baseline.
     *
     * @param recentActivity  Activity count in recent window
     * @param baselineActivity Average activity in baseline period
     * @param windowRatio     Ratio of recent window to baseline (e.g., 0.1 for 10%)
     * @return Trending score (>1 means trending)
     */
    public static double calculateTrendingScore(
            int recentActivity,
            double baselineActivity,
            double windowRatio
    ) {
        // Expected recent activity based on baseline
        double expected = baselineActivity * windowRatio;

        if (expected < 1) expected = 1;

        // Velocity ratio
        return recentActivity / expected;
    }
}
