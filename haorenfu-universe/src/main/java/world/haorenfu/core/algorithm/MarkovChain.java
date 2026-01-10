/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         MARKOV CHAIN
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Implementation of Discrete-Time Markov Chain for behavior prediction.
 * Models state transitions and predicts future states based on history.
 *
 * Mathematical Foundation:
 * A Markov chain is a stochastic model describing a sequence of possible
 * events where the probability of each event depends only on the state
 * attained in the previous event (Markov property).
 *
 * Key Properties:
 *   - Memoryless: P(Xₙ₊₁|Xₙ, Xₙ₋₁, ..., X₀) = P(Xₙ₊₁|Xₙ)
 *   - Transition Matrix: P[i,j] = P(Xₙ₊₁=j|Xₙ=i)
 *   - Stationary Distribution: π = πP (eigenvector with eigenvalue 1)
 *
 * Applications:
 * - Player activity prediction (when will they log in?)
 * - Content recommendation (what might they view next?)
 * - Churn prediction (will they stop playing?)
 */
package world.haorenfu.core.algorithm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generic Markov Chain implementation with learning capabilities.
 *
 * @param <S> State type (must have proper equals/hashCode)
 */
public class MarkovChain<S> {

    // State -> (NextState -> Count)
    private final Map<S, Map<S, AtomicLong>> transitionCounts;

    // State -> Total outgoing transitions
    private final Map<S, AtomicLong> stateTotals;

    // All observed states
    private final Set<S> states;

    // Random for sampling
    private final Random random;

    /**
     * Creates a new Markov Chain.
     */
    public MarkovChain() {
        this.transitionCounts = new ConcurrentHashMap<>();
        this.stateTotals = new ConcurrentHashMap<>();
        this.states = ConcurrentHashMap.newKeySet();
        this.random = new Random();
    }

    /**
     * Records a state transition for learning.
     *
     * @param fromState Source state
     * @param toState Target state
     */
    public void recordTransition(S fromState, S toState) {
        states.add(fromState);
        states.add(toState);

        transitionCounts
            .computeIfAbsent(fromState, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(toState, k -> new AtomicLong(0))
            .incrementAndGet();

        stateTotals
            .computeIfAbsent(fromState, k -> new AtomicLong(0))
            .incrementAndGet();
    }

    /**
     * Records a sequence of transitions.
     *
     * @param sequence List of states in order
     */
    public void recordSequence(List<S> sequence) {
        if (sequence.size() < 2) return;

        for (int i = 0; i < sequence.size() - 1; i++) {
            recordTransition(sequence.get(i), sequence.get(i + 1));
        }
    }

    /**
     * Gets the transition probability P(toState | fromState).
     *
     * @param fromState Source state
     * @param toState Target state
     * @return Probability [0, 1]
     */
    public double getTransitionProbability(S fromState, S toState) {
        AtomicLong total = stateTotals.get(fromState);
        if (total == null || total.get() == 0) return 0.0;

        Map<S, AtomicLong> transitions = transitionCounts.get(fromState);
        if (transitions == null) return 0.0;

        AtomicLong count = transitions.get(toState);
        if (count == null) return 0.0;

        return (double) count.get() / total.get();
    }

    /**
     * Gets all transition probabilities from a state.
     *
     * @param fromState Source state
     * @return Map of target states to probabilities
     */
    public Map<S, Double> getTransitionProbabilities(S fromState) {
        Map<S, Double> probabilities = new HashMap<>();

        AtomicLong total = stateTotals.get(fromState);
        if (total == null || total.get() == 0) return probabilities;

        Map<S, AtomicLong> transitions = transitionCounts.get(fromState);
        if (transitions == null) return probabilities;

        for (Map.Entry<S, AtomicLong> entry : transitions.entrySet()) {
            probabilities.put(entry.getKey(),
                (double) entry.getValue().get() / total.get());
        }

        return probabilities;
    }

    /**
     * Predicts the most likely next state.
     *
     * @param currentState Current state
     * @return Most probable next state, or null if unknown
     */
    public S predictNextState(S currentState) {
        Map<S, Double> probs = getTransitionProbabilities(currentState);
        if (probs.isEmpty()) return null;

        return probs.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    /**
     * Predicts top-k most likely next states.
     *
     * @param currentState Current state
     * @param k Number of predictions
     * @return List of (state, probability) pairs
     */
    public List<StateProbability<S>> predictTopK(S currentState, int k) {
        Map<S, Double> probs = getTransitionProbabilities(currentState);

        return probs.entrySet().stream()
            .map(e -> new StateProbability<>(e.getKey(), e.getValue()))
            .sorted((a, b) -> Double.compare(b.probability(), a.probability()))
            .limit(k)
            .toList();
    }

    /**
     * Samples a random next state according to transition probabilities.
     *
     * @param currentState Current state
     * @return Sampled next state, or null if unknown
     */
    public S sampleNextState(S currentState) {
        Map<S, Double> probs = getTransitionProbabilities(currentState);
        if (probs.isEmpty()) return null;

        double rand = random.nextDouble();
        double cumulative = 0.0;

        for (Map.Entry<S, Double> entry : probs.entrySet()) {
            cumulative += entry.getValue();
            if (rand <= cumulative) {
                return entry.getKey();
            }
        }

        // Fallback (shouldn't happen with proper probabilities)
        return probs.keySet().iterator().next();
    }

    /**
     * Generates a random walk of specified length.
     *
     * @param startState Starting state
     * @param length Number of states to generate
     * @return List of states in the walk
     */
    public List<S> generateWalk(S startState, int length) {
        List<S> walk = new ArrayList<>();
        walk.add(startState);

        S current = startState;
        for (int i = 1; i < length; i++) {
            S next = sampleNextState(current);
            if (next == null) break;
            walk.add(next);
            current = next;
        }

        return walk;
    }

    /**
     * Calculates the probability of a specific sequence.
     *
     * @param sequence The sequence to evaluate
     * @return Log probability (to avoid underflow)
     */
    public double sequenceLogProbability(List<S> sequence) {
        if (sequence.size() < 2) return 0.0;

        double logProb = 0.0;
        for (int i = 0; i < sequence.size() - 1; i++) {
            double p = getTransitionProbability(sequence.get(i), sequence.get(i + 1));
            if (p == 0) return Double.NEGATIVE_INFINITY;
            logProb += Math.log(p);
        }

        return logProb;
    }

    /**
     * Computes the stationary distribution using power iteration.
     * The stationary distribution π satisfies: π = πP
     *
     * @param maxIterations Maximum iterations
     * @param tolerance Convergence tolerance
     * @return Map of state to stationary probability
     */
    public Map<S, Double> computeStationaryDistribution(int maxIterations, double tolerance) {
        if (states.isEmpty()) return Collections.emptyMap();

        List<S> stateList = new ArrayList<>(states);
        int n = stateList.size();

        // Initialize uniform distribution
        double[] pi = new double[n];
        Arrays.fill(pi, 1.0 / n);

        // Power iteration
        for (int iter = 0; iter < maxIterations; iter++) {
            double[] newPi = new double[n];

            // π' = πP
            for (int j = 0; j < n; j++) {
                for (int i = 0; i < n; i++) {
                    double p = getTransitionProbability(stateList.get(i), stateList.get(j));
                    newPi[j] += pi[i] * p;
                }
            }

            // Normalize
            double sum = 0;
            for (double v : newPi) sum += v;
            if (sum > 0) {
                for (int i = 0; i < n; i++) newPi[i] /= sum;
            }

            // Check convergence
            double maxDiff = 0;
            for (int i = 0; i < n; i++) {
                maxDiff = Math.max(maxDiff, Math.abs(newPi[i] - pi[i]));
            }

            pi = newPi;
            if (maxDiff < tolerance) break;
        }

        // Build result map
        Map<S, Double> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            result.put(stateList.get(i), pi[i]);
        }
        return result;
    }

    /**
     * Gets all observed states.
     */
    public Set<S> getStates() {
        return Collections.unmodifiableSet(states);
    }

    /**
     * Gets the total number of recorded transitions.
     */
    public long getTotalTransitions() {
        return stateTotals.values().stream()
            .mapToLong(AtomicLong::get)
            .sum();
    }

    /**
     * Clears all learned data.
     */
    public void clear() {
        transitionCounts.clear();
        stateTotals.clear();
        states.clear();
    }

    /**
     * State with its probability.
     */
    public record StateProbability<S>(S state, double probability) {}

    // ═══════════════════════════════════════════════════════════════════════
    // Factory methods for common use cases
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Creates a Markov chain for player activity states.
     */
    public static MarkovChain<PlayerActivity> forPlayerActivity() {
        return new MarkovChain<>();
    }

    /**
     * Player activity states for prediction.
     */
    public enum PlayerActivity {
        ONLINE,
        OFFLINE,
        IDLE,
        BUILDING,
        MINING,
        FIGHTING,
        TRADING,
        CHATTING,
        EXPLORING
    }
}
