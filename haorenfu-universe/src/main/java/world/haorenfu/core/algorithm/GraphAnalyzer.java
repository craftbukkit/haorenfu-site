/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          GRAPH ALGORITHMS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Advanced graph theory implementations for social network analysis,
 * content recommendation, and community detection.
 *
 * Mathematical Foundations:
 * - Spectral Graph Theory
 * - Random Walk Theory
 * - Community Detection (Louvain Algorithm)
 * - Jaccard Similarity for recommendations
 *
 * Applications:
 * - Friend recommendations
 * - Content discovery
 * - Community clustering
 * - Influence propagation analysis
 */
package world.haorenfu.core.algorithm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Graph algorithms for social network analysis and recommendations.
 *
 * @param <T> The type of node identifiers in the graph
 */
public class GraphAnalyzer<T> {

    private final Map<T, Set<T>> adjacencyList;
    private final Map<T, Map<T, Double>> weightedEdges;
    private final boolean directed;

    /**
     * Creates a new graph analyzer.
     *
     * @param directed Whether the graph is directed
     */
    public GraphAnalyzer(boolean directed) {
        this.adjacencyList = new ConcurrentHashMap<>();
        this.weightedEdges = new ConcurrentHashMap<>();
        this.directed = directed;
    }

    /**
     * Adds a node to the graph.
     */
    public void addNode(T node) {
        adjacencyList.putIfAbsent(node, ConcurrentHashMap.newKeySet());
        weightedEdges.putIfAbsent(node, new ConcurrentHashMap<>());
    }

    /**
     * Adds an edge between two nodes.
     *
     * @param from   Source node
     * @param to     Target node
     * @param weight Edge weight (e.g., interaction strength)
     */
    public void addEdge(T from, T to, double weight) {
        addNode(from);
        addNode(to);

        adjacencyList.get(from).add(to);
        weightedEdges.get(from).put(to, weight);

        if (!directed) {
            adjacencyList.get(to).add(from);
            weightedEdges.get(to).put(from, weight);
        }
    }

    /**
     * Calculates PageRank scores for all nodes.
     *
     * PageRank formula:
     * PR(u) = (1-d)/N + d * Σ(PR(v)/L(v)) for all v linking to u
     *
     * Where:
     * - d = damping factor (typically 0.85)
     * - N = total number of nodes
     * - L(v) = number of outbound links from v
     *
     * @param dampingFactor Probability of following a link (typically 0.85)
     * @param iterations    Number of iterations for convergence
     * @return Map of node to PageRank score
     */
    public Map<T, Double> calculatePageRank(double dampingFactor, int iterations) {
        int n = adjacencyList.size();
        if (n == 0) return Collections.emptyMap();

        Map<T, Double> pageRank = new HashMap<>();
        double initialRank = 1.0 / n;

        // Initialize ranks
        for (T node : adjacencyList.keySet()) {
            pageRank.put(node, initialRank);
        }

        // Iterative computation
        for (int i = 0; i < iterations; i++) {
            Map<T, Double> newRanks = new HashMap<>();

            for (T node : adjacencyList.keySet()) {
                double rank = (1 - dampingFactor) / n;

                // Sum contributions from incoming edges
                for (T other : adjacencyList.keySet()) {
                    if (adjacencyList.get(other).contains(node)) {
                        int outDegree = adjacencyList.get(other).size();
                        if (outDegree > 0) {
                            rank += dampingFactor * pageRank.get(other) / outDegree;
                        }
                    }
                }

                newRanks.put(node, rank);
            }

            pageRank = newRanks;
        }

        return pageRank;
    }

    /**
     * Calculates Jaccard Similarity between two nodes.
     *
     * J(A,B) = |A ∩ B| / |A ∪ B|
     *
     * Used for friend recommendations based on mutual connections.
     *
     * @param node1 First node
     * @param node2 Second node
     * @return Jaccard similarity coefficient [0, 1]
     */
    public double jaccardSimilarity(T node1, T node2) {
        Set<T> neighbors1 = adjacencyList.getOrDefault(node1, Collections.emptySet());
        Set<T> neighbors2 = adjacencyList.getOrDefault(node2, Collections.emptySet());

        if (neighbors1.isEmpty() && neighbors2.isEmpty()) {
            return 0.0;
        }

        Set<T> intersection = new HashSet<>(neighbors1);
        intersection.retainAll(neighbors2);

        Set<T> union = new HashSet<>(neighbors1);
        union.addAll(neighbors2);

        return (double) intersection.size() / union.size();
    }

    /**
     * Recommends friends based on common connections.
     *
     * Uses the Adamic-Adar index:
     * AA(x,y) = Σ 1/log(|N(z)|) for all z in N(x) ∩ N(y)
     *
     * This gives more weight to rare common connections.
     *
     * @param node       The node to recommend friends for
     * @param maxResults Maximum number of recommendations
     * @return List of recommended nodes with scores
     */
    public List<RecommendationResult<T>> recommendFriends(T node, int maxResults) {
        Set<T> directNeighbors = adjacencyList.getOrDefault(node, Collections.emptySet());
        Map<T, Double> scores = new HashMap<>();

        // Find friends of friends
        for (T neighbor : directNeighbors) {
            for (T friendOfFriend : adjacencyList.getOrDefault(neighbor, Collections.emptySet())) {
                // Skip if already connected or is self
                if (friendOfFriend.equals(node) || directNeighbors.contains(friendOfFriend)) {
                    continue;
                }

                // Calculate Adamic-Adar contribution
                int neighborDegree = adjacencyList.get(neighbor).size();
                double contribution = neighborDegree > 1 ? 1.0 / Math.log(neighborDegree) : 1.0;

                scores.merge(friendOfFriend, contribution, Double::sum);
            }
        }

        // Sort by score and return top results
        return scores.entrySet().stream()
            .sorted(Map.Entry.<T, Double>comparingByValue().reversed())
            .limit(maxResults)
            .map(e -> new RecommendationResult<>(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * Detects communities using the Label Propagation Algorithm.
     *
     * This is a near-linear time algorithm for community detection
     * based on the idea that nodes adopt the most frequent label
     * among their neighbors.
     *
     * @return Map of node to community ID
     */
    public Map<T, Integer> detectCommunities() {
        Map<T, Integer> labels = new HashMap<>();
        List<T> nodes = new ArrayList<>(adjacencyList.keySet());

        // Initialize each node with unique label
        int labelId = 0;
        for (T node : nodes) {
            labels.put(node, labelId++);
        }

        // Iterate until convergence
        boolean changed = true;
        int maxIterations = 100;
        int iteration = 0;

        Random random = new Random(42);

        while (changed && iteration < maxIterations) {
            changed = false;
            Collections.shuffle(nodes, random);

            for (T node : nodes) {
                Set<T> neighbors = adjacencyList.get(node);
                if (neighbors.isEmpty()) continue;

                // Count neighbor labels
                Map<Integer, Double> labelCounts = new HashMap<>();
                for (T neighbor : neighbors) {
                    int neighborLabel = labels.get(neighbor);
                    double weight = weightedEdges.get(node).getOrDefault(neighbor, 1.0);
                    labelCounts.merge(neighborLabel, weight, Double::sum);
                }

                // Find most frequent label
                int bestLabel = labels.get(node);
                double bestCount = 0;
                for (Map.Entry<Integer, Double> entry : labelCounts.entrySet()) {
                    if (entry.getValue() > bestCount) {
                        bestCount = entry.getValue();
                        bestLabel = entry.getKey();
                    }
                }

                if (bestLabel != labels.get(node)) {
                    labels.put(node, bestLabel);
                    changed = true;
                }
            }

            iteration++;
        }

        return labels;
    }

    /**
     * Calculates the betweenness centrality of nodes.
     *
     * Betweenness centrality measures how often a node appears
     * on shortest paths between other nodes.
     *
     * g(v) = Σ σ_st(v) / σ_st
     *
     * Where σ_st is the number of shortest paths from s to t,
     * and σ_st(v) is the number of those paths passing through v.
     *
     * @return Map of node to betweenness centrality
     */
    public Map<T, Double> calculateBetweennessCentrality() {
        Map<T, Double> centrality = new HashMap<>();
        for (T node : adjacencyList.keySet()) {
            centrality.put(node, 0.0);
        }

        for (T source : adjacencyList.keySet()) {
            // BFS from source
            Map<T, List<T>> predecessors = new HashMap<>();
            Map<T, Integer> distance = new HashMap<>();
            Map<T, Integer> pathCount = new HashMap<>();
            Stack<T> stack = new Stack<>();
            Queue<T> queue = new LinkedList<>();

            for (T node : adjacencyList.keySet()) {
                predecessors.put(node, new ArrayList<>());
                distance.put(node, -1);
                pathCount.put(node, 0);
            }

            distance.put(source, 0);
            pathCount.put(source, 1);
            queue.add(source);

            while (!queue.isEmpty()) {
                T current = queue.poll();
                stack.push(current);

                for (T neighbor : adjacencyList.get(current)) {
                    // First visit
                    if (distance.get(neighbor) < 0) {
                        queue.add(neighbor);
                        distance.put(neighbor, distance.get(current) + 1);
                    }

                    // Shortest path via current
                    if (distance.get(neighbor) == distance.get(current) + 1) {
                        pathCount.merge(neighbor, pathCount.get(current), Integer::sum);
                        predecessors.get(neighbor).add(current);
                    }
                }
            }

            // Back propagation
            Map<T, Double> dependency = new HashMap<>();
            for (T node : adjacencyList.keySet()) {
                dependency.put(node, 0.0);
            }

            while (!stack.isEmpty()) {
                T current = stack.pop();
                for (T predecessor : predecessors.get(current)) {
                    double contribution = (double) pathCount.get(predecessor) / pathCount.get(current)
                        * (1.0 + dependency.get(current));
                    dependency.merge(predecessor, contribution, Double::sum);
                }

                if (!current.equals(source)) {
                    centrality.merge(current, dependency.get(current), Double::sum);
                }
            }
        }

        // Normalize for undirected graph
        if (!directed) {
            for (T node : centrality.keySet()) {
                centrality.put(node, centrality.get(node) / 2.0);
            }
        }

        return centrality;
    }

    /**
     * Performs a random walk starting from a node.
     *
     * Used for personalized recommendations based on
     * random walk with restart (RWR).
     *
     * @param startNode     Starting node
     * @param restartProb   Probability of restarting at start node
     * @param walkLength    Length of random walk
     * @param numWalks      Number of random walks to perform
     * @return Visit counts for each node
     */
    public Map<T, Integer> randomWalkWithRestart(T startNode, double restartProb,
                                                  int walkLength, int numWalks) {
        Map<T, Integer> visitCounts = new HashMap<>();
        Random random = new Random();

        for (int walk = 0; walk < numWalks; walk++) {
            T current = startNode;

            for (int step = 0; step < walkLength; step++) {
                visitCounts.merge(current, 1, Integer::sum);

                // Restart with probability
                if (random.nextDouble() < restartProb) {
                    current = startNode;
                    continue;
                }

                // Move to random neighbor
                Set<T> neighbors = adjacencyList.get(current);
                if (neighbors.isEmpty()) {
                    current = startNode;
                    continue;
                }

                // Weighted random selection
                double totalWeight = weightedEdges.get(current).values().stream()
                    .mapToDouble(Double::doubleValue).sum();

                double rand = random.nextDouble() * totalWeight;
                double cumulative = 0;

                for (T neighbor : neighbors) {
                    cumulative += weightedEdges.get(current).getOrDefault(neighbor, 1.0);
                    if (rand <= cumulative) {
                        current = neighbor;
                        break;
                    }
                }
            }
        }

        return visitCounts;
    }

    /**
     * Calculates clustering coefficient for a node.
     *
     * C(v) = 2 * |{e_jk}| / (k_v * (k_v - 1))
     *
     * Where |{e_jk}| is the number of edges between neighbors of v,
     * and k_v is the degree of v.
     *
     * @param node The node to calculate coefficient for
     * @return Clustering coefficient [0, 1]
     */
    public double clusteringCoefficient(T node) {
        Set<T> neighbors = adjacencyList.getOrDefault(node, Collections.emptySet());
        int k = neighbors.size();

        if (k < 2) return 0.0;

        // Count edges between neighbors
        int edgeCount = 0;
        List<T> neighborList = new ArrayList<>(neighbors);

        for (int i = 0; i < neighborList.size(); i++) {
            for (int j = i + 1; j < neighborList.size(); j++) {
                if (adjacencyList.get(neighborList.get(i)).contains(neighborList.get(j))) {
                    edgeCount++;
                }
            }
        }

        // Maximum possible edges between k neighbors
        int maxEdges = k * (k - 1) / 2;

        return (double) edgeCount / maxEdges;
    }

    /**
     * Calculates average clustering coefficient for the entire graph.
     */
    public double averageClusteringCoefficient() {
        return adjacencyList.keySet().stream()
            .mapToDouble(this::clusteringCoefficient)
            .average()
            .orElse(0.0);
    }

    /**
     * Gets the degree (number of connections) for a node.
     */
    public int getDegree(T node) {
        return adjacencyList.getOrDefault(node, Collections.emptySet()).size();
    }

    /**
     * Gets all nodes in the graph.
     */
    public Set<T> getNodes() {
        return Collections.unmodifiableSet(adjacencyList.keySet());
    }

    /**
     * Gets neighbors of a node.
     */
    public Set<T> getNeighbors(T node) {
        return Collections.unmodifiableSet(
            adjacencyList.getOrDefault(node, Collections.emptySet())
        );
    }

    /**
     * Result of a recommendation with score.
     */
    public record RecommendationResult<T>(T node, double score) implements Comparable<RecommendationResult<T>> {
        @Override
        public int compareTo(RecommendationResult<T> other) {
            return Double.compare(other.score, this.score);
        }
    }
}
