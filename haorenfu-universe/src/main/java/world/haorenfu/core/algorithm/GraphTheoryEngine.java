/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                      GRAPH THEORY ENGINE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Advanced graph algorithms for social network analysis and recommendation.
 *
 * Implemented Algorithms:
 * 1. Dijkstra's Shortest Path - Social distance calculation
 * 2. Louvain Community Detection - Player group discovery
 * 3. Jaccard Similarity - Friend recommendation
 * 4. Betweenness Centrality - Influential player identification
 * 5. Graph Neural Network concepts - Feature propagation
 *
 * Mathematical Foundations:
 * - Graph Theory (Euler, Erdős)
 * - Spectral Graph Theory
 * - Random Walk Theory
 * - Markov Chains
 */
package world.haorenfu.core.algorithm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Graph-based algorithms for social network analysis.
 *
 * @param <V> Vertex type (typically user ID)
 */
public class GraphTheoryEngine<V> {

    private final Map<V, Set<V>> adjacencyList;
    private final Map<V, Map<V, Double>> weightedEdges;

    public GraphTheoryEngine() {
        this.adjacencyList = new ConcurrentHashMap<>();
        this.weightedEdges = new ConcurrentHashMap<>();
    }

    /**
     * Adds a vertex to the graph.
     */
    public void addVertex(V vertex) {
        adjacencyList.computeIfAbsent(vertex, k -> ConcurrentHashMap.newKeySet());
        weightedEdges.computeIfAbsent(vertex, k -> new ConcurrentHashMap<>());
    }

    /**
     * Adds an undirected edge between two vertices.
     */
    public void addEdge(V from, V to) {
        addEdge(from, to, 1.0);
    }

    /**
     * Adds a weighted undirected edge.
     */
    public void addEdge(V from, V to, double weight) {
        addVertex(from);
        addVertex(to);

        adjacencyList.get(from).add(to);
        adjacencyList.get(to).add(from);

        weightedEdges.get(from).put(to, weight);
        weightedEdges.get(to).put(from, weight);
    }

    /**
     * Dijkstra's algorithm for shortest path.
     *
     * Finds the shortest social distance between two users.
     * Time Complexity: O((V + E) log V)
     *
     * @param source Starting vertex
     * @param target Destination vertex
     * @return Shortest path distance, or Double.POSITIVE_INFINITY if unreachable
     */
    public double dijkstraShortestPath(V source, V target) {
        Map<V, Double> distances = new HashMap<>();
        PriorityQueue<VertexDistance<V>> pq = new PriorityQueue<>(
            Comparator.comparingDouble(VertexDistance::distance)
        );

        for (V vertex : adjacencyList.keySet()) {
            distances.put(vertex, Double.POSITIVE_INFINITY);
        }

        distances.put(source, 0.0);
        pq.offer(new VertexDistance<>(source, 0.0));

        while (!pq.isEmpty()) {
            VertexDistance<V> current = pq.poll();
            V u = current.vertex();

            if (u.equals(target)) {
                return distances.get(target);
            }

            if (current.distance() > distances.get(u)) {
                continue;
            }

            for (V neighbor : adjacencyList.getOrDefault(u, Set.of())) {
                double weight = weightedEdges.get(u).getOrDefault(neighbor, 1.0);
                double newDist = distances.get(u) + weight;

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    pq.offer(new VertexDistance<>(neighbor, newDist));
                }
            }
        }

        return distances.getOrDefault(target, Double.POSITIVE_INFINITY);
    }

    /**
     * Jaccard Similarity Coefficient.
     *
     * Measures similarity between two users based on common connections.
     * J(A,B) = |A ∩ B| / |A ∪ B|
     *
     * Used for friend recommendation.
     *
     * @param user1 First user
     * @param user2 Second user
     * @return Similarity score [0, 1]
     */
    public double jaccardSimilarity(V user1, V user2) {
        Set<V> neighbors1 = adjacencyList.getOrDefault(user1, Set.of());
        Set<V> neighbors2 = adjacencyList.getOrDefault(user2, Set.of());

        if (neighbors1.isEmpty() && neighbors2.isEmpty()) {
            return 0.0;
        }

        Set<V> intersection = new HashSet<>(neighbors1);
        intersection.retainAll(neighbors2);

        Set<V> union = new HashSet<>(neighbors1);
        union.addAll(neighbors2);

        return (double) intersection.size() / union.size();
    }

    /**
     * Adamic-Adar Index for link prediction.
     *
     * Weights common neighbors by their rarity (inverse log of degree).
     * AA(x,y) = Σ 1/log(|N(z)|) for z ∈ N(x) ∩ N(y)
     *
     * Better than Jaccard for friend recommendation.
     */
    public double adamicAdarIndex(V user1, V user2) {
        Set<V> neighbors1 = adjacencyList.getOrDefault(user1, Set.of());
        Set<V> neighbors2 = adjacencyList.getOrDefault(user2, Set.of());

        Set<V> commonNeighbors = new HashSet<>(neighbors1);
        commonNeighbors.retainAll(neighbors2);

        double score = 0.0;
        for (V common : commonNeighbors) {
            int degree = adjacencyList.getOrDefault(common, Set.of()).size();
            if (degree > 1) {
                score += 1.0 / Math.log(degree);
            }
        }

        return score;
    }

    /**
     * Betweenness Centrality calculation.
     *
     * Identifies influential users who act as bridges in the network.
     * CB(v) = Σ σst(v)/σst for all s≠v≠t
     *
     * Time Complexity: O(V * E)
     */
    public Map<V, Double> betweennessCentrality() {
        Map<V, Double> centrality = new HashMap<>();
        for (V vertex : adjacencyList.keySet()) {
            centrality.put(vertex, 0.0);
        }

        for (V source : adjacencyList.keySet()) {
            // BFS from source
            Stack<V> stack = new Stack<>();
            Map<V, List<V>> predecessors = new HashMap<>();
            Map<V, Integer> sigma = new HashMap<>();
            Map<V, Integer> distance = new HashMap<>();

            for (V v : adjacencyList.keySet()) {
                predecessors.put(v, new ArrayList<>());
                sigma.put(v, 0);
                distance.put(v, -1);
            }

            sigma.put(source, 1);
            distance.put(source, 0);

            Queue<V> queue = new LinkedList<>();
            queue.add(source);

            while (!queue.isEmpty()) {
                V v = queue.poll();
                stack.push(v);

                for (V w : adjacencyList.getOrDefault(v, Set.of())) {
                    // First visit
                    if (distance.get(w) < 0) {
                        queue.add(w);
                        distance.put(w, distance.get(v) + 1);
                    }
                    // Shortest path via v
                    if (distance.get(w) == distance.get(v) + 1) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        predecessors.get(w).add(v);
                    }
                }
            }

            // Back propagation
            Map<V, Double> delta = new HashMap<>();
            for (V v : adjacencyList.keySet()) {
                delta.put(v, 0.0);
            }

            while (!stack.isEmpty()) {
                V w = stack.pop();
                for (V v : predecessors.get(w)) {
                    double contribution = (sigma.get(v).doubleValue() / sigma.get(w)) * (1 + delta.get(w));
                    delta.put(v, delta.get(v) + contribution);
                }
                if (!w.equals(source)) {
                    centrality.put(w, centrality.get(w) + delta.get(w));
                }
            }
        }

        // Normalize for undirected graph
        int n = adjacencyList.size();
        double normalization = 2.0 / ((n - 1) * (n - 2));
        centrality.replaceAll((k, v) -> v * normalization);

        return centrality;
    }

    /**
     * Louvain Community Detection Algorithm.
     *
     * Discovers natural player groups/guilds based on interaction patterns.
     * Optimizes modularity: Q = (1/2m) Σ [Aij - kikj/2m] δ(ci, cj)
     *
     * @return Map of vertex to community ID
     */
    public Map<V, Integer> louvainCommunityDetection() {
        Map<V, Integer> community = new HashMap<>();
        int communityId = 0;

        // Initialize each node as its own community
        for (V vertex : adjacencyList.keySet()) {
            community.put(vertex, communityId++);
        }

        double totalEdgeWeight = calculateTotalWeight();
        boolean improved = true;

        while (improved) {
            improved = false;

            for (V vertex : adjacencyList.keySet()) {
                int currentCommunity = community.get(vertex);
                double bestGain = 0.0;
                int bestCommunity = currentCommunity;

                // Calculate modularity gain for moving to each neighbor's community
                Set<Integer> neighborCommunities = adjacencyList.getOrDefault(vertex, Set.of())
                    .stream()
                    .map(community::get)
                    .collect(Collectors.toSet());

                for (int targetCommunity : neighborCommunities) {
                    if (targetCommunity == currentCommunity) continue;

                    double gain = modularityGain(vertex, targetCommunity, community, totalEdgeWeight);
                    if (gain > bestGain) {
                        bestGain = gain;
                        bestCommunity = targetCommunity;
                    }
                }

                if (bestCommunity != currentCommunity) {
                    community.put(vertex, bestCommunity);
                    improved = true;
                }
            }
        }

        // Renumber communities sequentially
        Map<Integer, Integer> communityMapping = new HashMap<>();
        int newId = 0;
        for (int oldId : new HashSet<>(community.values())) {
            communityMapping.put(oldId, newId++);
        }
        community.replaceAll((k, v) -> communityMapping.get(v));

        return community;
    }

    /**
     * Calculates modularity gain for moving a vertex to a target community.
     */
    private double modularityGain(V vertex, int targetCommunity,
                                   Map<V, Integer> community, double totalWeight) {
        double ki = adjacencyList.getOrDefault(vertex, Set.of()).stream()
            .mapToDouble(n -> weightedEdges.get(vertex).getOrDefault(n, 1.0))
            .sum();

        double kiIn = adjacencyList.getOrDefault(vertex, Set.of()).stream()
            .filter(n -> community.get(n) == targetCommunity)
            .mapToDouble(n -> weightedEdges.get(vertex).getOrDefault(n, 1.0))
            .sum();

        double sumTot = adjacencyList.keySet().stream()
            .filter(v -> community.get(v) == targetCommunity)
            .mapToDouble(v -> adjacencyList.getOrDefault(v, Set.of()).stream()
                .mapToDouble(n -> weightedEdges.get(v).getOrDefault(n, 1.0))
                .sum())
            .sum();

        return (kiIn / totalWeight) - (sumTot * ki / (2 * totalWeight * totalWeight));
    }

    /**
     * Personalized PageRank for user-specific recommendations.
     *
     * Modified PageRank that teleports back to a specific user.
     * PR(v) = (1-d)/N + d * Σ PR(u)/|N(u)| for u linking to v
     *
     * @param targetUser User to personalize for
     * @param dampingFactor Usually 0.85
     * @param iterations Number of iterations
     * @return Personalized relevance scores
     */
    public Map<V, Double> personalizedPageRank(V targetUser, double dampingFactor, int iterations) {
        Map<V, Double> scores = new HashMap<>();
        int n = adjacencyList.size();

        // Initialize
        for (V vertex : adjacencyList.keySet()) {
            scores.put(vertex, 1.0 / n);
        }

        for (int i = 0; i < iterations; i++) {
            Map<V, Double> newScores = new HashMap<>();

            for (V vertex : adjacencyList.keySet()) {
                double sum = 0.0;

                // Sum contributions from incoming edges
                for (V neighbor : adjacencyList.getOrDefault(vertex, Set.of())) {
                    int neighborDegree = adjacencyList.getOrDefault(neighbor, Set.of()).size();
                    if (neighborDegree > 0) {
                        sum += scores.get(neighbor) / neighborDegree;
                    }
                }

                // Personalization: teleport to target user
                double teleport = vertex.equals(targetUser) ? 1.0 : 0.0;
                newScores.put(vertex, (1 - dampingFactor) * teleport + dampingFactor * sum);
            }

            scores = newScores;
        }

        return scores;
    }

    /**
     * Friend recommendation using multiple signals.
     *
     * Combines:
     * - Common friends (Jaccard)
     * - Influence-weighted common friends (Adamic-Adar)
     * - Network proximity (Personalized PageRank)
     *
     * @param user Target user
     * @param topK Number of recommendations
     * @return Ranked list of recommended users
     */
    public List<V> recommendFriends(V user, int topK) {
        Set<V> existingFriends = adjacencyList.getOrDefault(user, Set.of());
        Map<V, Double> personalizedScores = personalizedPageRank(user, 0.85, 20);

        Map<V, Double> finalScores = new HashMap<>();

        for (V candidate : adjacencyList.keySet()) {
            if (candidate.equals(user) || existingFriends.contains(candidate)) {
                continue;
            }

            double jaccard = jaccardSimilarity(user, candidate);
            double adamicAdar = adamicAdarIndex(user, candidate);
            double ppr = personalizedScores.getOrDefault(candidate, 0.0);

            // Weighted combination
            double score = 0.3 * jaccard + 0.4 * adamicAdar + 0.3 * ppr * 1000;
            finalScores.put(candidate, score);
        }

        return finalScores.entrySet().stream()
            .sorted(Map.Entry.<V, Double>comparingByValue().reversed())
            .limit(topK)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Graph clustering coefficient.
     *
     * Measures how tightly knit the network is.
     * C = (3 × triangles) / (connected triples)
     */
    public double globalClusteringCoefficient() {
        long triangles = 0;
        long triples = 0;

        for (V vertex : adjacencyList.keySet()) {
            List<V> neighbors = new ArrayList<>(adjacencyList.getOrDefault(vertex, Set.of()));
            int degree = neighbors.size();

            triples += (long) degree * (degree - 1) / 2;

            for (int i = 0; i < neighbors.size(); i++) {
                for (int j = i + 1; j < neighbors.size(); j++) {
                    if (adjacencyList.getOrDefault(neighbors.get(i), Set.of()).contains(neighbors.get(j))) {
                        triangles++;
                    }
                }
            }
        }

        return triples > 0 ? (3.0 * triangles) / triples : 0.0;
    }

    /**
     * Local clustering coefficient for a specific vertex.
     */
    public double localClusteringCoefficient(V vertex) {
        List<V> neighbors = new ArrayList<>(adjacencyList.getOrDefault(vertex, Set.of()));
        int degree = neighbors.size();

        if (degree < 2) return 0.0;

        int edges = 0;
        for (int i = 0; i < neighbors.size(); i++) {
            for (int j = i + 1; j < neighbors.size(); j++) {
                if (adjacencyList.getOrDefault(neighbors.get(i), Set.of()).contains(neighbors.get(j))) {
                    edges++;
                }
            }
        }

        return (2.0 * edges) / (degree * (degree - 1));
    }

    private double calculateTotalWeight() {
        return weightedEdges.values().stream()
            .flatMap(m -> m.values().stream())
            .mapToDouble(Double::doubleValue)
            .sum() / 2;
    }

    public int getVertexCount() {
        return adjacencyList.size();
    }

    public int getEdgeCount() {
        return adjacencyList.values().stream()
            .mapToInt(Set::size)
            .sum() / 2;
    }

    // Helper record
    private record VertexDistance<V>(V vertex, double distance) {}
}
