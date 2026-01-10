/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                       MARKOV CHAIN PREDICTOR
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Markov Chain implementation for player behavior prediction and
 * content recommendation based on sequential patterns.
 *
 * Mathematical Foundation:
 * A Markov Chain is a stochastic process where the probability of
 * transitioning to any state depends only on the current state.
 *
 * P(Xn+1 = j | Xn = i, Xn-1 = i', ...) = P(Xn+1 = j | Xn = i) = pij
 *
 * Applications:
 * - Predicting next player action
 * - Content recommendation sequences
 * - Session behavior analysis
 * - Churn prediction
 */
package world.haorenfu.core.algorithm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Markov Chain for sequential prediction and analysis.
 *
 * @param <S> State type
 */
public class MarkovChainPredictor<S> {

    // Transition matrix: state -> (next_state -> count)
    private final Map<S, Map<S, Long>> transitionCounts;
    private final Map<S, Long> stateCounts;

    // Higher-order Markov (considers previous N states)
    private final int order;
    private final Map<List<S>, Map<S, Long>> higherOrderTransitions;

    /**
     * Creates a first-order Markov Chain predictor.
     */
    public MarkovChainPredictor() {
        this(1);
    }

    /**
     * Creates a higher-order Markov Chain predictor.
     *
     * @param order The number of previous states to consider
     */
    public MarkovChainPredictor(int order) {
        this.order = Math.max(1, order);
        this.transitionCounts = new ConcurrentHashMap<>();
        this.stateCounts = new ConcurrentHashMap<>();
        this.higherOrderTransitions = new ConcurrentHashMap<>();
    }

    /**
     * Trains the model with a sequence of states.
     *
     * @param sequence List of states in order
     */
    public void train(List<S> sequence) {
        if (sequence.size() < 2) return;

        // First-order transitions
        for (int i = 0; i < sequence.size() - 1; i++) {
            S current = sequence.get(i);
            S next = sequence.get(i + 1);

            stateCounts.merge(current, 1L, Long::sum);
            transitionCounts
                .computeIfAbsent(current, k -> new ConcurrentHashMap<>())
                .merge(next, 1L, Long::sum);
        }

        // Higher-order transitions
        if (order > 1) {
            for (int i = order - 1; i < sequence.size() - 1; i++) {
                List<S> history = new ArrayList<>(sequence.subList(i - order + 1, i + 1));
                S next = sequence.get(i + 1);

                higherOrderTransitions
                    .computeIfAbsent(history, k -> new ConcurrentHashMap<>())
                    .merge(next, 1L, Long::sum);
            }
        }

        // Count last state
        stateCounts.merge(sequence.get(sequence.size() - 1), 1L, Long::sum);
    }

    /**
     * Gets the probability of transitioning from one state to another.
     *
     * P(next | current) = count(current -> next) / count(current)
     *
     * @param current Current state
     * @param next    Next state
     * @return Transition probability
     */
    public double getTransitionProbability(S current, S next) {
        Map<S, Long> transitions = transitionCounts.get(current);
        if (transitions == null) return 0.0;

        long total = transitions.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) return 0.0;

        return (double) transitions.getOrDefault(next, 0L) / total;
    }

    /**
     * Gets transition probability considering history (higher-order).
     *
     * @param history Previous states (most recent last)
     * @param next    Next state
     * @return Transition probability
     */
    public double getTransitionProbability(List<S> history, S next) {
        if (history.size() < order) {
            // Fall back to first-order if not enough history
            return history.isEmpty() ? 0.0
                : getTransitionProbability(history.get(history.size() - 1), next);
        }

        List<S> relevantHistory = history.subList(history.size() - order, history.size());
        Map<S, Long> transitions = higherOrderTransitions.get(relevantHistory);

        if (transitions == null) {
            // Fall back to lower order
            return getTransitionProbability(history.get(history.size() - 1), next);
        }

        long total = transitions.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) return 0.0;

        return (double) transitions.getOrDefault(next, 0L) / total;
    }

    /**
     * Predicts the most likely next state.
     *
     * @param current Current state
     * @return Most probable next state, or empty if unknown
     */
    public Optional<S> predictNext(S current) {
        Map<S, Long> transitions = transitionCounts.get(current);
        if (transitions == null || transitions.isEmpty()) {
            return Optional.empty();
        }

        return transitions.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey);
    }

    /**
     * Predicts next state with history (higher-order prediction).
     *
     * @param history Previous states
     * @return Most probable next state
     */
    public Optional<S> predictNext(List<S> history) {
        if (history.isEmpty()) return Optional.empty();

        if (order > 1 && history.size() >= order) {
            List<S> relevantHistory = history.subList(history.size() - order, history.size());
            Map<S, Long> transitions = higherOrderTransitions.get(relevantHistory);

            if (transitions != null && !transitions.isEmpty()) {
                return transitions.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey);
            }
        }

        return predictNext(history.get(history.size() - 1));
    }

    /**
     * Gets top N most likely next states with probabilities.
     *
     * @param current Current state
     * @param n       Number of predictions
     * @return List of predictions with probabilities
     */
    public List<Prediction<S>> predictTopN(S current, int n) {
        Map<S, Long> transitions = transitionCounts.get(current);
        if (transitions == null || transitions.isEmpty()) {
            return Collections.emptyList();
        }

        long total = transitions.values().stream().mapToLong(Long::longValue).sum();

        return transitions.entrySet().stream()
            .sorted(Map.Entry.<S, Long>comparingByValue().reversed())
            .limit(n)
            .map(e -> new Prediction<>(e.getKey(), (double) e.getValue() / total))
            .collect(Collectors.toList());
    }

    /**
     * Generates a sequence of states starting from given state.
     *
     * @param start  Starting state
     * @param length Desired sequence length
     * @return Generated sequence
     */
    public List<S> generateSequence(S start, int length) {
        List<S> sequence = new ArrayList<>();
        sequence.add(start);

        S current = start;
        Random random = new Random();

        for (int i = 1; i < length; i++) {
            Map<S, Long> transitions = transitionCounts.get(current);
            if (transitions == null || transitions.isEmpty()) break;

            // Weighted random selection
            long total = transitions.values().stream().mapToLong(Long::longValue).sum();
            long rand = (long) (random.nextDouble() * total);
            long cumulative = 0;

            S next = current;
            for (Map.Entry<S, Long> entry : transitions.entrySet()) {
                cumulative += entry.getValue();
                if (rand < cumulative) {
                    next = entry.getKey();
                    break;
                }
            }

            sequence.add(next);
            current = next;
        }

        return sequence;
    }

    /**
     * Calculates the stationary distribution of the Markov Chain.
     *
     * The stationary distribution π satisfies: π = π * P
     * where P is the transition matrix.
     *
     * Uses power iteration method.
     *
     * @param iterations Number of iterations
     * @return Stationary distribution (state -> probability)
     */
    public Map<S, Double> calculateStationaryDistribution(int iterations) {
        Set<S> states = transitionCounts.keySet();
        if (states.isEmpty()) return Collections.emptyMap();

        // Initialize uniform distribution
        Map<S, Double> distribution = new HashMap<>();
        double initial = 1.0 / states.size();
        for (S state : states) {
            distribution.put(state, initial);
        }

        // Power iteration
        for (int iter = 0; iter < iterations; iter++) {
            Map<S, Double> newDistribution = new HashMap<>();

            for (S state : states) {
                newDistribution.put(state, 0.0);
            }

            for (S from : states) {
                double fromProb = distribution.get(from);
                Map<S, Long> transitions = transitionCounts.get(from);

                if (transitions != null) {
                    long total = transitions.values().stream().mapToLong(Long::longValue).sum();

                    for (Map.Entry<S, Long> entry : transitions.entrySet()) {
                        S to = entry.getKey();
                        double transProb = (double) entry.getValue() / total;
                        newDistribution.merge(to, fromProb * transProb, Double::sum);
                    }
                }
            }

            distribution = newDistribution;
        }

        return distribution;
    }

    /**
     * Calculates the entropy of transitions from a state.
     *
     * H(X) = -Σ p(x) * log2(p(x))
     *
     * High entropy indicates unpredictable behavior.
     *
     * @param state The state to analyze
     * @return Shannon entropy of transitions
     */
    public double getTransitionEntropy(S state) {
        Map<S, Long> transitions = transitionCounts.get(state);
        if (transitions == null || transitions.isEmpty()) return 0.0;

        long total = transitions.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) return 0.0;

        double entropy = 0.0;
        for (long count : transitions.values()) {
            if (count > 0) {
                double p = (double) count / total;
                entropy -= p * Math.log(p) / Math.log(2);
            }
        }

        return entropy;
    }

    /**
     * Calculates the expected number of steps to reach target from start.
     *
     * Uses first-step analysis:
     * E[T] = 1 + Σ p_ij * E[T_j]
     *
     * @param start  Starting state
     * @param target Target state
     * @return Expected steps, or Double.POSITIVE_INFINITY if unreachable
     */
    public double expectedStepsTo(S start, S target) {
        if (start.equals(target)) return 0.0;

        // Use simulation for approximation
        int simulations = 10000;
        int maxSteps = 1000;
        long totalSteps = 0;
        int successCount = 0;

        Random random = new Random();

        for (int sim = 0; sim < simulations; sim++) {
            S current = start;
            int steps = 0;

            while (steps < maxSteps && !current.equals(target)) {
                Map<S, Long> transitions = transitionCounts.get(current);
                if (transitions == null || transitions.isEmpty()) break;

                long total = transitions.values().stream().mapToLong(Long::longValue).sum();
                long rand = (long) (random.nextDouble() * total);
                long cumulative = 0;

                for (Map.Entry<S, Long> entry : transitions.entrySet()) {
                    cumulative += entry.getValue();
                    if (rand < cumulative) {
                        current = entry.getKey();
                        break;
                    }
                }

                steps++;
            }

            if (current.equals(target)) {
                totalSteps += steps;
                successCount++;
            }
        }

        if (successCount == 0) return Double.POSITIVE_INFINITY;
        return (double) totalSteps / successCount;
    }

    /**
     * Gets the probability of a specific sequence occurring.
     *
     * P(s1, s2, ..., sn) = P(s1) * P(s2|s1) * P(s3|s2) * ...
     *
     * @param sequence The sequence to evaluate
     * @return Probability of the sequence
     */
    public double getSequenceProbability(List<S> sequence) {
        if (sequence.isEmpty()) return 1.0;
        if (sequence.size() == 1) {
            long total = stateCounts.values().stream().mapToLong(Long::longValue).sum();
            return total > 0 ? (double) stateCounts.getOrDefault(sequence.get(0), 0L) / total : 0.0;
        }

        double probability = 1.0;

        // Initial state probability
        long total = stateCounts.values().stream().mapToLong(Long::longValue).sum();
        probability *= (double) stateCounts.getOrDefault(sequence.get(0), 0L) / total;

        // Transition probabilities
        for (int i = 0; i < sequence.size() - 1; i++) {
            probability *= getTransitionProbability(sequence.get(i), sequence.get(i + 1));
            if (probability == 0) break;
        }

        return probability;
    }

    /**
     * Gets all known states.
     */
    public Set<S> getStates() {
        return Collections.unmodifiableSet(transitionCounts.keySet());
    }

    /**
     * Gets the total number of transitions observed.
     */
    public long getTotalTransitions() {
        return transitionCounts.values().stream()
            .flatMap(m -> m.values().stream())
            .mapToLong(Long::longValue)
            .sum();
    }

    /**
     * Prediction result with probability.
     */
    public record Prediction<S>(S state, double probability) implements Comparable<Prediction<S>> {
        @Override
        public int compareTo(Prediction<S> other) {
            return Double.compare(other.probability, this.probability);
        }
    }
}
