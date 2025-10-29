package smartcity.algorithms;

import smartcity.model.Graph;
import smartcity.model.Vertex;
import smartcity.model.Edge;
import java.util.*;

/**
 * Implements topological sorting algorithms for Directed Acyclic Graphs (DAGs).
 * Provides both Kahn's algorithm and DFS-based topological sort.
 */
public class TopologicalSort {
    private final Graph graph;

    // Metrics for performance analysis
    private long executionTime;
    private int operationsCount;
    private boolean hasCycle;

    /**
     * Creates a TopologicalSort solver for the specified graph.
     *
     * @param graph the graph to analyze
     * @throws IllegalArgumentException if graph is null
     */
    public TopologicalSort(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        this.graph = graph;
    }

    // ==================== KAHN'S ALGORITHM ====================

    /**
     * Performs topological sort using Kahn's algorithm.
     *
     * @return list of vertices in topological order
     * @throws IllegalStateException if graph contains cycles
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
            operationsCount++;

            if (degree == 0) {
                queue.offer(vertex);
            }
        }

        // Process vertices with zero in-degree
        while (!queue.isEmpty()) {
            operationsCount++;
            Vertex current = queue.poll();
            topologicalOrder.add(current);

            // Reduce in-degree of neighbors
            for (Edge edge : graph.getOutgoingEdges(current.getId())) {
                operationsCount++;
                Vertex neighbor = edge.getTo();
                int newDegree = inDegree.get(neighbor.getId()) - 1;
                inDegree.put(neighbor.getId(), newDegree);

                if (newDegree == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // Check for cycles
        hasCycle = topologicalOrder.size() != graph.getVertexCount();
        if (hasCycle) {
            throw new IllegalStateException("Graph contains cycles - topological sort not possible");
        }

        executionTime = System.nanoTime() - startTime;
        return topologicalOrder;
    }

    // ==================== DFS-BASED ALGORITHM ====================

    /**
     * Performs topological sort using DFS-based algorithm.
     *
     * @return list of vertices in topological order
     * @throws IllegalStateException if graph contains cycles
     */
    public List<Vertex> topologicalSortDFS() {
        resetMetrics();
        long startTime = System.nanoTime();

        Stack<Vertex> stack = new Stack<>();
        Set<Vertex> visited = new HashSet<>();
        Set<Vertex> recursionStack = new HashSet<>();
        List<Vertex> topologicalOrder = new ArrayList<>();

        // Reset visited state
        for (Vertex vertex : graph.getVertices()) {
            vertex.markUnvisited();
        }

        // Perform DFS for all unvisited vertices
        for (Vertex vertex : graph.getVertices()) {
            if (!vertex.isVisited()) {
                if (!topologicalDFSUtil(vertex, visited, recursionStack, stack)) {
                    hasCycle = true;
                    throw new IllegalStateException("Graph contains cycles - topological sort not possible");
                }
            }
        }

        // Pop from stack to get topological order
        while (!stack.isEmpty()) {
            operationsCount++;
            topologicalOrder.add(stack.pop());
        }

        executionTime = System.nanoTime() - startTime;
        return topologicalOrder;
    }

    private boolean topologicalDFSUtil(Vertex vertex, Set<Vertex> visited,
                                       Set<Vertex> recursionStack, Stack<Vertex> stack) {
        operationsCount++;

        // Check for cycle
        if (recursionStack.contains(vertex)) {
            return false;
        }

        // If already visited and processed, skip
        if (visited.contains(vertex)) {
            return true;
        }

        vertex.markVisited();
        visited.add(vertex);
        recursionStack.add(vertex);

        // Visit all neighbors
        for (Edge edge : graph.getOutgoingEdges(vertex.getId())) {
            operationsCount++;
            Vertex neighbor = edge.getTo();
            if (!topologicalDFSUtil(neighbor, visited, recursionStack, stack)) {
                return false;
            }
        }

        recursionStack.remove(vertex);
        stack.push(vertex);
        return true;
    }

    // ==================== CONDENSATION GRAPH ORDERING ====================

    /**
     * Computes topological order for the condensation graph (SCC components).
     *
     * @param sccFinder the SCCFinder containing components and condensation graph
     * @return list of component IDs in topological order
     */
    public List<Integer> topologicalOrderCondensation(SCCFinder sccFinder) {
        resetMetrics();
        long startTime = System.nanoTime();

        Graph condensationGraph = sccFinder.getCondensationGraph();
        if (condensationGraph == null) {
            throw new IllegalArgumentException("Condensation graph not built. Call buildCondensationGraph() first.");
        }

        TopologicalSort condensationSorter = new TopologicalSort(condensationGraph);
        List<Vertex> componentVertices = condensationSorter.topologicalSortKahn();

        // Extract component IDs from condensation graph vertices
        List<Integer> componentOrder = new ArrayList<>();
        for (Vertex compVertex : componentVertices) {
            operationsCount++;
            componentOrder.add(compVertex.getId());
        }

        executionTime = System.nanoTime() - startTime;
        return componentOrder;
    }

    /**
     * Derives topological order of original tasks from condensation graph order.
     *
     * @param sccFinder the SCCFinder containing components
     * @param componentOrder topological order of component IDs
     * @return list of all original vertices in topological order
     */
    public List<Vertex> deriveOriginalTaskOrder(SCCFinder sccFinder, List<Integer> componentOrder) {
        resetMetrics();
        long startTime = System.nanoTime();

        List<Vertex> originalOrder = new ArrayList<>();
        List<List<Vertex>> components = sccFinder.getComponents();

        for (int compId : componentOrder) {
            operationsCount++;
            if (compId >= 0 && compId < components.size()) {
                originalOrder.addAll(components.get(compId));
            }
        }

        executionTime = System.nanoTime() - startTime;
        return originalOrder;
    }

    /**
     * Computes complete topological processing including SCC compression.
     *
     * @param sccFinder the SCCFinder with computed components
     * @return complete topological result
     */
    public TopologicalResult completeTopologicalProcessing(SCCFinder sccFinder) {
        resetMetrics();
        long startTime = System.nanoTime();

        // Build condensation graph if not already built
        if (sccFinder.getCondensationGraph() == null) {
            sccFinder.buildCondensationGraph();
        }

        // Get topological order of components
        List<Integer> componentOrder = topologicalOrderCondensation(sccFinder);

        // Derive original task order
        List<Vertex> originalOrder = deriveOriginalTaskOrder(sccFinder, componentOrder);

        executionTime = System.nanoTime() - startTime;

        return new TopologicalResult(componentOrder, originalOrder, executionTime, operationsCount);
    }

    // ==================== VALIDATION AND UTILITY METHODS ====================

    /**
     * Checks if the graph is a DAG (no cycles).
     *
     * @return true if graph is acyclic, false otherwise
     */
    public boolean isDAG() {
        try {
            topologicalSortKahn();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Finds all source vertices (in-degree 0).
     *
     * @return list of source vertices
     */
    public List<Vertex> findSourceVertices() {
        List<Vertex> sources = new ArrayList<>();
        for (Vertex vertex : graph.getVertices()) {
            if (graph.getInDegree(vertex.getId()) == 0) {
                sources.add(vertex);
            }
        }
        return sources;
    }

    /**
     * Finds all sink vertices (out-degree 0).
     *
     * @return list of sink vertices
     */
    public List<Vertex> findSinkVertices() {
        List<Vertex> sinks = new ArrayList<>();
        for (Vertex vertex : graph.getVertices()) {
            if (graph.getOutDegree(vertex.getId()) == 0) {
                sinks.add(vertex);
            }
        }
        return sinks;
    }

    /**
     * Returns the level of each vertex in the topological order.
     *
     * @param topologicalOrder the topological order of vertices
     * @return map of vertex ID to level
     */
    public Map<Integer, Integer> computeLevels(List<Vertex> topologicalOrder) {
        Map<Integer, Integer> levels = new HashMap<>();

        for (Vertex vertex : topologicalOrder) {
            int maxPredecessorLevel = -1;

            // Find maximum level among predecessors
            for (Edge edge : graph.getIncomingEdges(vertex.getId())) {
                int predLevel = levels.getOrDefault(edge.getFrom().getId(), -1);
                maxPredecessorLevel = Math.max(maxPredecessorLevel, predLevel);
            }

            levels.put(vertex.getId(), maxPredecessorLevel + 1);
        }

        return levels;
    }

    // ==================== METRICS AND ANALYSIS ====================

    private void resetMetrics() {
        executionTime = 0;
        operationsCount = 0;
        hasCycle = false;
    }

    /**
     * Returns performance metrics for the last operation.
     *
     * @return map of performance metrics
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("executionTimeNs", executionTime);
        metrics.put("operationsCount", operationsCount);
        metrics.put("vertexCount", graph.getVertexCount());
        metrics.put("edgeCount", graph.getEdgeCount());
        metrics.put("hasCycle", hasCycle);
        metrics.put("isDAG", !hasCycle);
        return metrics;
    }

    /**
     * Prints analysis results for topological sort.
     *
     * @param topologicalOrder the computed topological order
     */
    public void printAnalysis(List<Vertex> topologicalOrder) {
        System.out.println("=== Topological Sort Analysis ===");
        System.out.println("Graph is DAG: " + (!hasCycle));
        System.out.printf("Execution time: %.3f ms%n", executionTime / 1_000_000.0);
        System.out.println("Operations count: " + operationsCount);
        System.out.println("Vertex count: " + graph.getVertexCount());
        System.out.println("Edge count: " + graph.getEdgeCount());

        List<Vertex> sources = findSourceVertices();
        List<Vertex> sinks = findSinkVertices();
        System.out.println("Source vertices: " + sources.size());
        System.out.println("Sink vertices: " + sinks.size());

        Map<Integer, Integer> levels = computeLevels(topologicalOrder);
        int maxLevel = levels.values().stream().max(Integer::compareTo).orElse(-1);
        System.out.println("Maximum level: " + maxLevel);

        System.out.println("\nTopological order:");
        for (int i = 0; i < topologicalOrder.size(); i++) {
            Vertex vertex = topologicalOrder.get(i);
            int level = levels.get(vertex.getId());
            System.out.printf("  %d. %s (ID: %d, Level: %d)%n",
                    i + 1, vertex.getName(), vertex.getId(), level);
        }
    }

    // ==================== RESULT CLASSES ====================

    /**
     * Represents the complete result of topological processing.
     */
    public static class TopologicalResult {
        private final List<Integer> componentOrder;
        private final List<Vertex> originalOrder;
        private final long executionTime;
        private final int operationsCount;

        public TopologicalResult(List<Integer> componentOrder, List<Vertex> originalOrder,
                                 long executionTime, int operationsCount) {
            this.componentOrder = Collections.unmodifiableList(new ArrayList<>(componentOrder));
            this.originalOrder = Collections.unmodifiableList(new ArrayList<>(originalOrder));
            this.executionTime = executionTime;
            this.operationsCount = operationsCount;
        }

        // Getters
        public List<Integer> getComponentOrder() { return componentOrder; }
        public List<Vertex> getOriginalOrder() { return originalOrder; }
        public long getExecutionTime() { return executionTime; }
        public int getOperationsCount() { return operationsCount; }
        public int getComponentCount() { return componentOrder.size(); }
        public int getOriginalVertexCount() { return originalOrder.size(); }

        @Override
        public String toString() {
            return String.format("TopologicalResult{components=%d, vertices=%d, time=%.3fms}",
                    componentOrder.size(), originalOrder.size(), executionTime / 1_000_000.0);
        }
    }

    // ==================== BUILDER PATTERN ====================

    /**
     * Builder for TopologicalSort with optional configuration.
     */
    public static class Builder {
        private Graph graph;
        private boolean useDFS = false;

        public Builder(Graph graph) {
            this.graph = graph;
        }

        public Builder useDFS() {
            this.useDFS = true;
            return this;
        }

        public Builder useKahn() {
            this.useDFS = false;
            return this;
        }

        public TopologicalSort build() {
            return new TopologicalSort(graph);
        }

        /**
         * Builds and immediately performs topological sort.
         *
         * @return topological order
         */
        public List<Vertex> buildAndSort() {
            TopologicalSort sorter = build();
            return useDFS ? sorter.topologicalSortDFS() : sorter.topologicalSortKahn();
        }
    }
}