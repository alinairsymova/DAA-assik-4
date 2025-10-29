package smartcity.algorithms;

import smartcity.model.Graph;
import smartcity.model.Vertex;
import smartcity.model.Edge;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements shortest and longest path algorithms for Directed Acyclic Graphs (DAGs).
 * Supports single-source shortest paths, longest paths (critical path), and path reconstruction.
 */
public class DAGShortestPath {
    private final Graph graph;

    // Metrics for performance analysis
    private long executionTime;
    private int relaxations;
    private int topologicalOperations;

    /**
     * Creates a DAGShortestPath solver for the specified graph.
     *
     * @param graph the DAG to analyze
     * @throws IllegalArgumentException if graph is null or contains cycles
     */
    public DAGShortestPath(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (graph.hasCycle()) {
            throw new IllegalArgumentException("Graph must be acyclic for DAG shortest path algorithms");
        }
        this.graph = graph;
    }

    // ==================== TOPOLOGICAL SORT ====================

    /**
     * Performs topological sort using Kahn's algorithm.
     *
     * @return list of vertices in topological order
     */
    public List<Vertex> topologicalSortKahn() {
        resetMetrics();
        long startTime = System.nanoTime();

        Map<Integer, Integer> inDegree = new HashMap<>();
        Queue<Vertex> queue = new LinkedList<>();
        List<Vertex> topologicalOrder = new ArrayList<>();

        // Initialize in-degree for all vertices
        for (Vertex vertex : graph.getVertices()) {
            int degree = graph.getInDegree(vertex.getId());
            inDegree.put(vertex.getId(), degree);
            if (degree == 0) {
                queue.offer(vertex);
            }
        }

        // Process vertices with zero in-degree
        while (!queue.isEmpty()) {
            topologicalOperations++;
            Vertex current = queue.poll();
            topologicalOrder.add(current);

            // Reduce in-degree of neighbors
            for (Edge edge : graph.getOutgoingEdges(current.getId())) {
                topologicalOperations++;
                Vertex neighbor = edge.getTo();
                int newDegree = inDegree.get(neighbor.getId()) - 1;
                inDegree.put(neighbor.getId(), newDegree);

                if (newDegree == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // Check for cycles (should not happen if graph is truly a DAG)
        if (topologicalOrder.size() != graph.getVertexCount()) {
            throw new IllegalStateException("Graph contains cycles - not a DAG");
        }

        executionTime = System.nanoTime() - startTime;
        return topologicalOrder;
    }

    /**
     * Performs topological sort using DFS-based algorithm.
     *
     * @return list of vertices in topological order
     */
    public List<Vertex> topologicalSortDFS() {
        resetMetrics();
        long startTime = System.nanoTime();

        Stack<Vertex> stack = new Stack<>();
        Set<Vertex> visited = new HashSet<>();
        List<Vertex> topologicalOrder = new ArrayList<>();

        // Reset visited state
        for (Vertex vertex : graph.getVertices()) {
            vertex.markUnvisited();
        }

        // Perform DFS for all unvisited vertices
        for (Vertex vertex : graph.getVertices()) {
            if (!vertex.isVisited()) {
                topologicalDFSUtil(vertex, visited, stack);
            }
        }

        // Pop from stack to get topological order
        while (!stack.isEmpty()) {
            topologicalOperations++;
            topologicalOrder.add(stack.pop());
        }

        executionTime = System.nanoTime() - startTime;
        return topologicalOrder;
    }

    private void topologicalDFSUtil(Vertex vertex, Set<Vertex> visited, Stack<Vertex> stack) {
        topologicalOperations++;
        vertex.markVisited();
        visited.add(vertex);

        for (Edge edge : graph.getOutgoingEdges(vertex.getId())) {
            topologicalOperations++;
            Vertex neighbor = edge.getTo();
            if (!visited.contains(neighbor)) {
                topologicalDFSUtil(neighbor, visited, stack);
            }
        }

        stack.push(vertex);
    }

    // ==================== SHORTEST PATH ALGORITHMS ====================

    /**
     * Computes single-source shortest paths from a source vertex.
     *
     * @param sourceId the source vertex ID
     * @return map of distances from source to each vertex
     */
    public Map<Integer, Integer> singleSourceShortestPath(int sourceId) {
        return singleSourceShortestPath(sourceId, false);
    }

    /**
     * Computes single-source shortest paths with optional path reconstruction info.
     *
     * @param sourceId the source vertex ID
     * @return shortest path results
     */
    public ShortestPathResult singleSourceShortestPathWithInfo(int sourceId) {
        return computeShortestPaths(sourceId, false, true);
    }

    private Map<Integer, Integer> singleSourceShortestPath(int sourceId, boolean isLongestPath) {
        ShortestPathResult result = computeShortestPaths(sourceId, isLongestPath, false);
        return result.getDistances();
    }

    private ShortestPathResult computeShortestPaths(int sourceId, boolean isLongestPath, boolean includePathInfo) {
        resetMetrics();
        long startTime = System.nanoTime();

        List<Vertex> topologicalOrder = topologicalSortKahn();
        Map<Integer, Integer> distances = new HashMap<>();
        Map<Integer, Integer> predecessors = includePathInfo ? new HashMap<>() : null;

        // Initialize distances
        for (Vertex vertex : graph.getVertices()) {
            if (vertex.getId() == sourceId) {
                distances.put(vertex.getId(), 0);
            } else {
                distances.put(vertex.getId(), isLongestPath ? Integer.MIN_VALUE : Integer.MAX_VALUE);
            }
        }

        // Process vertices in topological order
        for (Vertex vertex : topologicalOrder) {
            int currentId = vertex.getId();

            // Skip if vertex is unreachable (for shortest path)
            if (!isLongestPath && distances.get(currentId) == Integer.MAX_VALUE) {
                continue;
            }

            // Relax all outgoing edges
            for (Edge edge : graph.getOutgoingEdges(currentId)) {
                relaxations++;
                Vertex neighbor = edge.getTo();
                int neighborId = neighbor.getId();

                int edgeWeight = getEffectiveWeight(edge, vertex, isLongestPath);
                int newDistance = distances.get(currentId) + edgeWeight;

                if (isLongestPath) {
                    if (newDistance > distances.get(neighborId)) {
                        distances.put(neighborId, newDistance);
                        if (includePathInfo) {
                            predecessors.put(neighborId, currentId);
                        }
                    }
                } else {
                    if (newDistance < distances.get(neighborId)) {
                        distances.put(neighborId, newDistance);
                        if (includePathInfo) {
                            predecessors.put(neighborId, currentId);
                        }
                    }
                }
            }
        }

        executionTime = System.nanoTime() - startTime;

        if (includePathInfo) {
            return new ShortestPathResult(distances, predecessors, sourceId, isLongestPath, executionTime, relaxations);
        } else {
            return new ShortestPathResult(distances, null, sourceId, isLongestPath, executionTime, relaxations);
        }
    }

    // ==================== LONGEST PATH (CRITICAL PATH) ====================

    /**
     * Computes the longest path in the DAG (critical path).
     *
     * @return CriticalPathResult containing the longest path and its length
     */
    public CriticalPathResult findCriticalPath() {
        // For critical path, we need to find the longest path from any source to any sink
        resetMetrics();
        long startTime = System.nanoTime();

        // Find all sources (vertices with in-degree 0)
        List<Vertex> sources = graph.getVertices().stream()
                .filter(v -> graph.getInDegree(v.getId()) == 0)
                .collect(Collectors.toList());

        int maxLength = Integer.MIN_VALUE;
        List<Vertex> criticalPath = new ArrayList<>();
        int bestSourceId = -1;
        int bestSinkId = -1;

        // Try each source to find the overall longest path
        for (Vertex source : sources) {
            ShortestPathResult result = computeShortestPaths(source.getId(), true, true);
            Map<Integer, Integer> distances = result.getDistances();
            Map<Integer, Integer> predecessors = result.getPredecessors();

            // Find the sink with maximum distance
            for (Map.Entry<Integer, Integer> entry : distances.entrySet()) {
                if (entry.getValue() > maxLength && entry.getValue() != Integer.MIN_VALUE) {
                    maxLength = entry.getValue();
                    bestSinkId = entry.getKey();
                    bestSourceId = source.getId();

                    // Reconstruct the critical path
                    criticalPath = reconstructPath(predecessors, bestSourceId, bestSinkId);
                }
            }
        }

        executionTime = System.nanoTime() - startTime;

        return new CriticalPathResult(criticalPath, maxLength, bestSourceId, bestSinkId,
                executionTime, relaxations, topologicalOperations);
    }

    /**
     * Computes single-source longest paths from a source vertex.
     *
     * @param sourceId the source vertex ID
     * @return map of longest distances from source to each vertex
     */
    public Map<Integer, Integer> singleSourceLongestPath(int sourceId) {
        return singleSourceShortestPath(sourceId, true);
    }

    /**
     * Computes single-source longest paths with path reconstruction info.
     *
     * @param sourceId the source vertex ID
     * @return longest path results
     */
    public ShortestPathResult singleSourceLongestPathWithInfo(int sourceId) {
        return computeShortestPaths(sourceId, true, true);
    }

    // ==================== PATH RECONSTRUCTION ====================

    /**
     * Reconstructs the shortest path from source to target.
     *
     * @param result the shortest path result
     * @param targetId the target vertex ID
     * @return list of vertices in the path
     */
    public List<Vertex> reconstructPath(ShortestPathResult result, int targetId) {
        return reconstructPath(result.getPredecessors(), result.getSourceId(), targetId);
    }

    private List<Vertex> reconstructPath(Map<Integer, Integer> predecessors, int sourceId, int targetId) {
        if (predecessors == null) {
            throw new IllegalArgumentException("Path reconstruction requires predecessor information");
        }

        List<Vertex> path = new ArrayList<>();
        Integer currentId = targetId;

        // Backtrack from target to source
        while (currentId != null && currentId != sourceId) {
            path.add(0, graph.getVertex(currentId));
            currentId = predecessors.get(currentId);
        }

        if (currentId == null) {
            return new ArrayList<>(); // No path exists
        }

        // Add source vertex
        path.add(0, graph.getVertex(sourceId));
        return path;
    }

    // ==================== UTILITY METHODS ====================

    private int getEffectiveWeight(Edge edge, Vertex fromVertex, boolean isLongestPath) {
        if (graph.useNodeDurations()) {
            // For critical path/longest path with node durations, use node duration
            // For shortest path with node durations, we typically don't use node durations in edges
            return isLongestPath ? fromVertex.getDuration() : edge.getWeight();
        } else {
            // For edge weights, use the edge weight (inverted for longest path)
            return isLongestPath ? -edge.getWeight() : edge.getWeight();
        }
    }

    private void resetMetrics() {
        executionTime = 0;
        relaxations = 0;
        topologicalOperations = 0;
    }

    // ==================== RESULT CLASSES ====================

    /**
     * Represents the result of a shortest/longest path computation.
     */
    public static class ShortestPathResult {
        private final Map<Integer, Integer> distances;
        private final Map<Integer, Integer> predecessors;
        private final int sourceId;
        private final boolean isLongestPath;
        private final long executionTime;
        private final int relaxations;

        public ShortestPathResult(Map<Integer, Integer> distances, Map<Integer, Integer> predecessors,
                                  int sourceId, boolean isLongestPath, long executionTime, int relaxations) {
            this.distances = Collections.unmodifiableMap(new HashMap<>(distances));
            this.predecessors = predecessors != null ? Collections.unmodifiableMap(new HashMap<>(predecessors)) : null;
            this.sourceId = sourceId;
            this.isLongestPath = isLongestPath;
            this.executionTime = executionTime;
            this.relaxations = relaxations;
        }

        // Getters
        public Map<Integer, Integer> getDistances() { return distances; }
        public Map<Integer, Integer> getPredecessors() { return predecessors; }
        public int getSourceId() { return sourceId; }
        public boolean isLongestPath() { return isLongestPath; }
        public long getExecutionTime() { return executionTime; }
        public int getRelaxations() { return relaxations; }

        public int getDistanceTo(int targetId) {
            return distances.getOrDefault(targetId, isLongestPath ? Integer.MIN_VALUE : Integer.MAX_VALUE);
        }

        public boolean isReachable(int targetId) {
            int distance = getDistanceTo(targetId);
            return isLongestPath ? distance != Integer.MIN_VALUE : distance != Integer.MAX_VALUE;
        }
    }

    /**
     * Represents the result of a critical path analysis.
     */
    public static class CriticalPathResult {
        private final List<Vertex> path;
        private final int length;
        private final int sourceId;
        private final int sinkId;
        private final long executionTime;
        private final int relaxations;
        private final int topologicalOperations;

        public CriticalPathResult(List<Vertex> path, int length, int sourceId, int sinkId,
                                  long executionTime, int relaxations, int topologicalOperations) {
            this.path = Collections.unmodifiableList(new ArrayList<>(path));
            this.length = length;
            this.sourceId = sourceId;
            this.sinkId = sinkId;
            this.executionTime = executionTime;
            this.relaxations = relaxations;
            this.topologicalOperations = topologicalOperations;
        }

        // Getters
        public List<Vertex> getPath() { return path; }
        public int getLength() { return length; }
        public int getSourceId() { return sourceId; }
        public int getSinkId() { return sinkId; }
        public long getExecutionTime() { return executionTime; }
        public int getRelaxations() { return relaxations; }
        public int getTopologicalOperations() { return topologicalOperations; }
        public int getPathSize() { return path.size(); }

        public boolean isEmpty() { return path.isEmpty(); }

        @Override
        public String toString() {
            return String.format("CriticalPath{length=%d, vertices=%d, source=%d, sink=%d}",
                    length, path.size(), sourceId, sinkId);
        }
    }

    // ==================== ANALYSIS AND METRICS ====================

    /**
     * Returns performance metrics for the last operation.
     *
     * @return map of performance metrics
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("executionTimeNs", executionTime);
        metrics.put("relaxations", relaxations);
        metrics.put("topologicalOperations", topologicalOperations);
        metrics.put("vertexCount", graph.getVertexCount());
        metrics.put("edgeCount", graph.getEdgeCount());
        return metrics;
    }

    /**
     * Prints analysis results for critical path.
     *
     * @param result the critical path result to analyze
     */
    public void printCriticalPathAnalysis(CriticalPathResult result) {
        System.out.println("=== Critical Path Analysis ===");
        System.out.println("Critical path length: " + result.getLength());
        System.out.println("Path size: " + result.getPathSize());
        System.out.println("Source: " + result.getSourceId() + " (" + graph.getVertex(result.getSourceId()).getName() + ")");
        System.out.println("Sink: " + result.getSinkId() + " (" + graph.getVertex(result.getSinkId()).getName() + ")");
        System.out.printf("Execution time: %.3f ms%n", result.getExecutionTime() / 1_000_000.0);
        System.out.println("Relaxations: " + result.getRelaxations());
        System.out.println("Topological operations: " + result.getTopologicalOperations());

        System.out.println("\nCritical path:");
        for (int i = 0; i < result.getPath().size(); i++) {
            Vertex vertex = result.getPath().get(i);
            System.out.printf("  %d. %s (ID: %d, Duration: %d)%n",
                    i + 1, vertex.getName(), vertex.getId(), vertex.getDuration());
        }
    }

    // ==================== BUILDER PATTERN ====================

    /**
     * Builder for DAGShortestPath with optional configuration.
     */
    public static class Builder {
        private Graph graph;
        private boolean useDFSTopological = false;

        public Builder(Graph graph) {
            this.graph = graph;
        }

        public Builder useDFSTopological() {
            this.useDFSTopological = true;
            return this;
        }

        public Builder useKahnTopological() {
            this.useDFSTopological = false;
            return this;
        }

        public DAGShortestPath build() {
            return new DAGShortestPath(graph);
        }
    }
}