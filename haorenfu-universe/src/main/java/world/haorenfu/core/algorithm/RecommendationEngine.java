/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                    RECOMMENDATION ENGINE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Advanced recommendation system using cutting-edge mathematical techniques:
 *
 * 1. Matrix Factorization (SVD-like decomposition)
 *    - Latent factor models for user-item interactions
 *    - Alternating Least Squares (ALS) optimization
 *
 * 2. Cosine Similarity
 *    - User-based collaborative filtering
 *    - Item-based collaborative filtering
 *
 * 3. TF-IDF with BM25
 *    - Content-based recommendations
 *    - Probabilistic relevance model
 *
 * 4. Locality-Sensitive Hashing (LSH)
 *    - Approximate nearest neighbor search
 *    - MinHash for Jaccard similarity
 *
 * Mathematical Foundation:
 * - Linear Algebra: SVD, eigendecomposition
 * - Probability Theory: Bayesian inference
 * - Information Retrieval: TF-IDF, BM25
 * - Approximation Algorithms: LSH
 */
package world.haorenfu.core.algorithm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Recommendation engine implementing multiple algorithms for content discovery.
 *
 * @param <U> User type identifier
 * @param <I> Item type identifier
 */
public class RecommendationEngine<U, I> {

    // ═══════════════════════════════════════════════════════════════════════
    //                         MATRIX FACTORIZATION
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Latent factor model using Stochastic Gradient Descent.
     *
     * Decomposes the user-item interaction matrix R ≈ P × Q^T
     * where:
     *   - P: User latent factors (users × k)
     *   - Q: Item latent factors (items × k)
     *   - k: Number of latent factors
     *
     * Optimization objective:
     *   min Σ (r_ui - p_u · q_i)² + λ(||p_u||² + ||q_i||²)
     */
    public static class MatrixFactorization {

        private final int numFactors;
        private final double learningRate;
        private final double regularization;
        private final int maxIterations;

        private double[][] userFactors;  // P matrix
        private double[][] itemFactors;  // Q matrix

        private final Map<Integer, Integer> userIndex = new HashMap<>();
        private final Map<Integer, Integer> itemIndex = new HashMap<>();

        /**
         * Creates a new matrix factorization model.
         *
         * @param numFactors Number of latent factors (typically 10-100)
         * @param learningRate SGD learning rate (typically 0.001-0.01)
         * @param regularization L2 regularization parameter
         * @param maxIterations Maximum training iterations
         */
        public MatrixFactorization(int numFactors, double learningRate,
                                   double regularization, int maxIterations) {
            this.numFactors = numFactors;
            this.learningRate = learningRate;
            this.regularization = regularization;
            this.maxIterations = maxIterations;
        }

        /**
         * Trains the model on user-item ratings.
         *
         * @param ratings List of (userId, itemId, rating) tuples
         */
        public void train(List<Rating> ratings) {
            // Build indices
            Set<Integer> users = ratings.stream().map(r -> r.userId).collect(Collectors.toSet());
            Set<Integer> items = ratings.stream().map(r -> r.itemId).collect(Collectors.toSet());

            int userIdx = 0;
            for (int u : users) userIndex.put(u, userIdx++);

            int itemIdx = 0;
            for (int i : items) itemIndex.put(i, itemIdx++);

            // Initialize factor matrices with small random values
            // Using Xavier initialization: N(0, sqrt(2/(fan_in + fan_out)))
            Random random = new Random(42);
            double initScale = Math.sqrt(2.0 / (users.size() + items.size()));

            userFactors = new double[users.size()][numFactors];
            itemFactors = new double[items.size()][numFactors];

            for (int u = 0; u < users.size(); u++) {
                for (int f = 0; f < numFactors; f++) {
                    userFactors[u][f] = random.nextGaussian() * initScale;
                }
            }

            for (int i = 0; i < items.size(); i++) {
                for (int f = 0; f < numFactors; f++) {
                    itemFactors[i][f] = random.nextGaussian() * initScale;
                }
            }

            // Stochastic Gradient Descent
            for (int iter = 0; iter < maxIterations; iter++) {
                Collections.shuffle(ratings, random);

                double totalError = 0;

                for (Rating rating : ratings) {
                    int u = userIndex.get(rating.userId);
                    int i = itemIndex.get(rating.itemId);

                    // Predict and compute error
                    double prediction = dotProduct(userFactors[u], itemFactors[i]);
                    double error = rating.value - prediction;
                    totalError += error * error;

                    // Update factors using gradient descent
                    for (int f = 0; f < numFactors; f++) {
                        double userFactor = userFactors[u][f];
                        double itemFactor = itemFactors[i][f];

                        // Gradient with L2 regularization
                        userFactors[u][f] += learningRate * (error * itemFactor - regularization * userFactor);
                        itemFactors[i][f] += learningRate * (error * userFactor - regularization * itemFactor);
                    }
                }

                // Early stopping if converged
                double rmse = Math.sqrt(totalError / ratings.size());
                if (rmse < 0.001) break;
            }
        }

        /**
         * Predicts the rating for a user-item pair.
         */
        public double predict(int userId, int itemId) {
            Integer u = userIndex.get(userId);
            Integer i = itemIndex.get(itemId);

            if (u == null || i == null) return 0;

            return dotProduct(userFactors[u], itemFactors[i]);
        }

        /**
         * Gets top-N recommendations for a user.
         */
        public List<Integer> recommend(int userId, int n, Set<Integer> excludeItems) {
            Integer u = userIndex.get(userId);
            if (u == null) return Collections.emptyList();

            // Score all items
            Map<Integer, Double> scores = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : itemIndex.entrySet()) {
                int itemId = entry.getKey();
                if (excludeItems.contains(itemId)) continue;

                int i = entry.getValue();
                scores.put(itemId, dotProduct(userFactors[u], itemFactors[i]));
            }

            // Return top N
            return scores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }

        private double dotProduct(double[] a, double[] b) {
            double sum = 0;
            for (int i = 0; i < a.length; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                      COSINE SIMILARITY
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Cosine similarity calculator for vectors.
     *
     * cos(θ) = (A · B) / (||A|| × ||B||)
     *
     * Range: [-1, 1] where 1 means identical direction
     */
    public static class CosineSimilarity {

        /**
         * Computes cosine similarity between two sparse vectors.
         */
        public static double compute(Map<Integer, Double> vectorA, Map<Integer, Double> vectorB) {
            double dotProduct = 0;
            double normA = 0;
            double normB = 0;

            // Compute dot product for common dimensions
            for (Map.Entry<Integer, Double> entry : vectorA.entrySet()) {
                Double valueB = vectorB.get(entry.getKey());
                if (valueB != null) {
                    dotProduct += entry.getValue() * valueB;
                }
                normA += entry.getValue() * entry.getValue();
            }

            for (Double value : vectorB.values()) {
                normB += value * value;
            }

            if (normA == 0 || normB == 0) return 0;

            return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        }

        /**
         * Finds k most similar items to the target.
         */
        public static <T> List<T> findSimilar(
                T target,
                Map<T, Map<Integer, Double>> itemVectors,
                int k) {

            Map<Integer, Double> targetVector = itemVectors.get(target);
            if (targetVector == null) return Collections.emptyList();

            Map<T, Double> similarities = new HashMap<>();

            for (Map.Entry<T, Map<Integer, Double>> entry : itemVectors.entrySet()) {
                if (entry.getKey().equals(target)) continue;
                similarities.put(entry.getKey(), compute(targetVector, entry.getValue()));
            }

            return similarities.entrySet().stream()
                .sorted(Map.Entry.<T, Double>comparingByValue().reversed())
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                           BM25 RANKING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * BM25 (Best Matching 25) probabilistic relevance model.
     *
     * Improvement over TF-IDF with term frequency saturation
     * and document length normalization.
     *
     * Score(D, Q) = Σ IDF(qi) × (f(qi, D) × (k1 + 1)) /
     *               (f(qi, D) + k1 × (1 - b + b × |D|/avgdl))
     *
     * where:
     *   - f(qi, D): term frequency of qi in document D
     *   - |D|: document length
     *   - avgdl: average document length
     *   - k1: term frequency saturation parameter (typically 1.2-2.0)
     *   - b: length normalization parameter (typically 0.75)
     */
    public static class BM25 {

        private final double k1;
        private final double b;
        private final Map<String, Integer> documentFrequencies = new HashMap<>();
        private final List<Map<String, Integer>> documents = new ArrayList<>();
        private double avgDocLength = 0;

        public BM25() {
            this(1.5, 0.75);
        }

        public BM25(double k1, double b) {
            this.k1 = k1;
            this.b = b;
        }

        /**
         * Adds a document to the index.
         */
        public void addDocument(List<String> terms) {
            Map<String, Integer> termFreq = new HashMap<>();

            for (String term : terms) {
                termFreq.merge(term.toLowerCase(), 1, Integer::sum);
            }

            // Update document frequencies
            for (String term : termFreq.keySet()) {
                documentFrequencies.merge(term, 1, Integer::sum);
            }

            documents.add(termFreq);

            // Update average document length
            avgDocLength = documents.stream()
                .mapToInt(doc -> doc.values().stream().mapToInt(Integer::intValue).sum())
                .average()
                .orElse(0);
        }

        /**
         * Computes BM25 score for a query against a document.
         */
        public double score(List<String> query, int docIndex) {
            if (docIndex >= documents.size()) return 0;

            Map<String, Integer> doc = documents.get(docIndex);
            int docLength = doc.values().stream().mapToInt(Integer::intValue).sum();
            int N = documents.size();

            double score = 0;

            for (String term : query) {
                String normalizedTerm = term.toLowerCase();
                int df = documentFrequencies.getOrDefault(normalizedTerm, 0);
                int tf = doc.getOrDefault(normalizedTerm, 0);

                if (tf == 0 || df == 0) continue;

                // IDF with smoothing
                double idf = Math.log((N - df + 0.5) / (df + 0.5) + 1);

                // BM25 term score
                double tfNormalized = (tf * (k1 + 1)) /
                    (tf + k1 * (1 - b + b * docLength / avgDocLength));

                score += idf * tfNormalized;
            }

            return score;
        }

        /**
         * Searches documents and returns ranked results.
         */
        public List<Integer> search(List<String> query, int limit) {
            Map<Integer, Double> scores = new HashMap<>();

            for (int i = 0; i < documents.size(); i++) {
                double score = score(query, i);
                if (score > 0) {
                    scores.put(i, score);
                }
            }

            return scores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                    LOCALITY-SENSITIVE HASHING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * MinHash implementation for approximate Jaccard similarity.
     *
     * Jaccard similarity: J(A,B) = |A ∩ B| / |A ∪ B|
     *
     * MinHash provides an unbiased estimator:
     *   P(h(A) = h(B)) = J(A,B)
     *
     * Using k hash functions, we estimate J(A,B) ≈ (matches) / k
     */
    public static class MinHash {

        private final int numHashes;
        private final int[][] hashCoefficients;
        private final int prime = 2147483647; // Mersenne prime 2^31 - 1

        public MinHash(int numHashes) {
            this.numHashes = numHashes;
            this.hashCoefficients = new int[numHashes][2];

            Random random = new Random(42);
            for (int i = 0; i < numHashes; i++) {
                hashCoefficients[i][0] = random.nextInt(prime - 1) + 1; // a
                hashCoefficients[i][1] = random.nextInt(prime);          // b
            }
        }

        /**
         * Computes the MinHash signature for a set.
         */
        public int[] computeSignature(Set<Integer> set) {
            int[] signature = new int[numHashes];
            Arrays.fill(signature, Integer.MAX_VALUE);

            for (int element : set) {
                for (int i = 0; i < numHashes; i++) {
                    // h(x) = (ax + b) mod p
                    long hash = ((long) hashCoefficients[i][0] * element + hashCoefficients[i][1]) % prime;
                    signature[i] = Math.min(signature[i], (int) hash);
                }
            }

            return signature;
        }

        /**
         * Estimates Jaccard similarity from signatures.
         */
        public double estimateSimilarity(int[] sigA, int[] sigB) {
            int matches = 0;
            for (int i = 0; i < numHashes; i++) {
                if (sigA[i] == sigB[i]) matches++;
            }
            return (double) matches / numHashes;
        }

        /**
         * Exact Jaccard similarity for comparison.
         */
        public static double exactJaccard(Set<?> setA, Set<?> setB) {
            Set<Object> intersection = new HashSet<>(setA);
            intersection.retainAll(setB);

            Set<Object> union = new HashSet<>(setA);
            union.addAll(setB);

            return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                    COLLABORATIVE FILTERING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * User-based collaborative filtering with neighborhood approach.
     */
    public static class UserBasedCF {

        private final Map<Integer, Map<Integer, Double>> userRatings = new HashMap<>();
        private final int neighborhoodSize;

        public UserBasedCF(int neighborhoodSize) {
            this.neighborhoodSize = neighborhoodSize;
        }

        public void addRating(int userId, int itemId, double rating) {
            userRatings.computeIfAbsent(userId, k -> new HashMap<>()).put(itemId, rating);
        }

        /**
         * Predicts rating using k-nearest neighbors.
         *
         * r̂_ui = r̄_u + (Σ sim(u,v) × (r_vi - r̄_v)) / (Σ |sim(u,v)|)
         */
        public double predictRating(int userId, int itemId) {
            Map<Integer, Double> targetRatings = userRatings.get(userId);
            if (targetRatings == null) return 0;

            double targetMean = targetRatings.values().stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);

            // Find similar users who rated this item
            List<Map.Entry<Integer, Double>> neighbors = new ArrayList<>();

            for (Map.Entry<Integer, Map<Integer, Double>> entry : userRatings.entrySet()) {
                int otherUser = entry.getKey();
                if (otherUser == userId) continue;

                Map<Integer, Double> otherRatings = entry.getValue();
                if (!otherRatings.containsKey(itemId)) continue;

                double similarity = CosineSimilarity.compute(
                    targetRatings.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue())),
                    otherRatings.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()))
                );

                neighbors.add(new AbstractMap.SimpleEntry<>(otherUser, similarity));
            }

            // Sort by similarity and take top k
            neighbors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            neighbors = neighbors.subList(0, Math.min(neighborhoodSize, neighbors.size()));

            if (neighbors.isEmpty()) return targetMean;

            // Weighted average prediction
            double numerator = 0;
            double denominator = 0;

            for (Map.Entry<Integer, Double> neighbor : neighbors) {
                int neighborId = neighbor.getKey();
                double similarity = neighbor.getValue();

                Map<Integer, Double> neighborRatings = userRatings.get(neighborId);
                double neighborMean = neighborRatings.values().stream()
                    .mapToDouble(Double::doubleValue).average().orElse(0);

                double neighborRating = neighborRatings.get(itemId);

                numerator += similarity * (neighborRating - neighborMean);
                denominator += Math.abs(similarity);
            }

            return denominator == 0 ? targetMean : targetMean + numerator / denominator;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                         HELPER CLASSES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Rating tuple for matrix factorization.
     */
    public static class Rating {
        public final int userId;
        public final int itemId;
        public final double value;

        public Rating(int userId, int itemId, double value) {
            this.userId = userId;
            this.itemId = itemId;
            this.value = value;
        }
    }
}
