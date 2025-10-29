package smartcity.algorithms;

import smartcity.model.Graph;
import smartcity.model.Vertex;
import smartcity.model.Edge;
import java.util.*;
import java.util.stream.Collectors;
/**
 * Implements Strongly Connected Components (SCC) detection using Tarjan's and Kosaraju's algorithms.
 * Also builds the condensation graph (DAG of components) for topological processing.
 */
public class SCCFinder {
    private final Graph graph;
    private List<List<Vertex>> components;
    private Graph condensationGraph;
    private Map<Vertex, Integer> vertexToComponentId;

    // Metrics for performance analysis
    private int dfsCounter;
    private long executionTime;
    private int dfsVisits;
    private int edgesTraversed;

    /**
     * Creates an SCCFinder for the specified graph.
     *
     * @param graph the graph to analyze
     * @throws IllegalArgumentException if graph is null
     */
    public SCCFinder(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        this.graph = graph;
        this.components = new ArrayList<>();
        this.vertexToComponentId = new HashMap<>();
    }

    // ==================== TARJAN'S ALGORITHM ====================

    /**
     * Finds all strongly connected components using Tarjan's algorithm.
     *
     * @return list of strongly connected components
     */
    public List<List<Vertex>> findSCCsTarjan() {
        resetMetrics();
        long startTime = System.nanoTime();

        components.clear();
        vertexToComponentId.clear();

        Map<Vertex, Integer> indices = new HashMap<>();
        Map<Vertex, Integer> lowLinks = new HashMap<>();
        Stack<Vertex> stack = new Stack<>();
        Set<Vertex> onStack = new HashSet<>();

        dfsCounter = 0;

        for (Vertex vertex : graph.getVertices()) {
            vertex.markUnvisited();
        }

        for (Vertex vertex : graph.getVertices()) {
            if (!vertex.isVisited()) {
                strongConnectTarjan(vertex, indices, lowLinks, stack, onStack);
            }
        }

        executionTime = System.nanoTime() - startTime;
        return new ArrayList<>(components);
    }

    private void strongConnectTarjan(Vertex vertex, Map<Vertex, Integer> indices,
                                     Map<Vertex, Integer> lowLinks,
                                     Stack<Vertex> stack, Set<Vertex> onStack) {
        dfsVisits++;
        indices.put(vertex, dfsCounter);
        lowLinks.put(vertex, dfsCounter);
        dfsCounter++;

        stack.push(vertex);
        onStack.add(vertex);
        vertex.markVisited();

        // Consider all outgoing edges
        for (Edge edge : graph.getOutgoingEdges(vertex.getId())) {
            edgesTraversed++;
            Vertex neighbor = edge.getTo();

            if (!indices.containsKey(neighbor)) {
                // Neighbor has not been visited, recurse on it
                strongConnectTarjan(neighbor, indices, lowLinks, stack, onStack);
                lowLinks.put(vertex, Math.min(lowLinks.get(vertex), lowLinks.get(neighbor)));
            } else if (onStack.contains(neighbor)) {
                // Neighbor is in stack and hence in the current SCC
                lowLinks.put(vertex, Math.min(lowLinks.get(vertex), indices.get(neighbor)));
            }
        }

        // If vertex is a root node, pop the stack and generate an SCC
        if (Objects.equals(lowLinks.get(vertex), indices.get(vertex))) {
            List<Vertex> component = new ArrayList<>();
            Vertex poppedVertex;

            do {
                poppedVertex = stack.pop();
                onStack.remove(poppedVertex);
                component.add(poppedVertex);
                vertexToComponentId.put(poppedVertex, components.size());
            } while (!poppedVertex.equals(vertex));

            components.add(component);
        }
    }

    // ==================== KOSARAJU'S ALGORITHM ====================

    /**
     * Finds all strongly connected components using Kosaraju's algorithm.
     *
     * @return list of strongly connected components
     */
    public List<List<Vertex>> findSCCsKosaraju() {
        resetMetrics();
        long startTime = System.nanoTime();

        components.clear();
        vertexToComponentId.clear();

        // Step 1: First DFS to compute finishing times
        Stack<Vertex> finishOrder = new Stack<>();
        Set<Vertex> visited = new HashSet<>();

        for (Vertex vertex : graph.getVertices()) {
            vertex.markUnvisited();
        }

        for (Vertex vertex : graph.getVertices()) {
            if (!vertex.isVisited()) {
                dfsKosarajuFirstPass(vertex, finishOrder, visited);
            }
        }

        // Step 2: Compute transpose graph
        Graph transpose = graph.getTranspose();

        // Step 3: Second DFS on transpose in reverse finish order
        visited.clear();
        for (Vertex vertex : graph.getVertices()) {
            vertex.markUnvisited();
        }

        while (!finishOrder.isEmpty()) {
            Vertex vertex = finishOrder.pop();
            if (!vertex.isVisited()) {
                List<Vertex> component = new ArrayList<>();
                dfsKosarajuSecondPass(vertex, transpose, component, visited);
                components.add(component);

                // Update component mapping
                for (Vertex compVertex : component) {
                    vertexToComponentId.put(compVertex, components.size() - 1);
                }
            }
        }

        executionTime = System.nanoTime() - startTime;
        return new ArrayList<>(components);
    }

    private void dfsKosarajuFirstPass(Vertex vertex, Stack<Vertex> finishOrder, Set<Vertex> visited) {
        dfsVisits++;
        vertex.markVisited();
        visited.add(vertex);

        for (Edge edge : graph.getOutgoingEdges(vertex.getId())) {
            edgesTraversed++;
            Vertex neighbor = edge.getTo();
            if (!visited.contains(neighbor)) {
                dfsKosarajuFirstPass(neighbor, finishOrder, visited);
            }
        }

        finishOrder.push(vertex);
    }

    private void dfsKosarajuSecondPass(Vertex vertex, Graph transpose, List<Vertex> component, Set<Vertex> visited) {
        dfsVisits++;
        vertex.markVisited();
        visited.add(vertex);
        component.add(vertex);

        for (Edge edge : transpose.getOutgoingEdges(vertex.getId())) {
            edgesTraversed++;
            Vertex neighbor = edge.getTo();
            if (!visited.contains(neighbor)) {
                dfsKosarajuSecondPass(neighbor, transpose, component, visited);
            }
        }
    }

    // ==================== CONDENSATION GRAPH ====================

    /**
     * Builds the condensation graph (DAG of SCCs).
     * Each SCC becomes a vertex in the condensation graph.
     *
     * @return the condensation graph
     */
    public Graph buildCondensationGraph() {
        if (components.isEmpty()) {
            findSCCsTarjan(); // Use Tarjan as default if no components found
        }

        condensationGraph = new Graph(graph.useNodeDurations());

        // Create vertices for each component
        for (int i = 0; i < components.size(); i++) {
            List<Vertex> component = components.get(i);
            String componentName = "SCC-" + i + " (size: " + component.size() + ")";
            int totalDuration = component.stream().mapToInt(Vertex::getDuration).sum();
            condensationGraph.addVertex(i, componentName, totalDuration);
        }

        // Create edges between components
        Set<String> addedEdges = new HashSet<>();

        for (int fromCompId = 0; fromCompId < components.size(); fromCompId++) {
            List<Vertex> fromComponent = components.get(fromCompId);

            for (Vertex fromVertex : fromComponent) {
                for (Edge edge : graph.getOutgoingEdges(fromVertex.getId())) {
                    Vertex toVertex = edge.getTo();
                    int toCompId = vertexToComponentId.get(toVertex);

                    // Only add edge if connecting different components and not already added
                    if (fromCompId != toCompId) {
                        String edgeKey = fromCompId + "->" + toCompId;
                        if (!addedEdges.contains(edgeKey)) {
                            condensationGraph.addEdge(fromCompId, toCompId, edge.getWeight());
                            addedEdges.add(edgeKey);
                        }
                    }
                }
            }
        }

        return condensationGraph;
    }

    // ==================== COMPONENT ANALYSIS ====================

    /**
     * Returns the component ID for a given vertex.
     *
     * @param vertex the vertex to check
     * @return component ID, or -1 if not found
     */
    public int getComponentId(Vertex vertex) {
        return vertexToComponentId.getOrDefault(vertex, -1);
    }

    /**
     * Returns the component containing the specified vertex.
     *
     * @param vertex the vertex to find
     * @return the component list, or empty list if not found
     */
    public List<Vertex> getComponent(Vertex vertex) {
        int compId = getComponentId(vertex);
        return (compId != -1) ? components.get(compId) : new ArrayList<>();
    }

    /**
     * Returns statistics about the SCCs.
     *
     * @return map of SCC statistics
     */
    public Map<String, Object> getSCCStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalComponents", components.size());
        stats.put("executionTimeNs", executionTime);
        stats.put("dfsVisits", dfsVisits);
        stats.put("edgesTraversed", edgesTraversed);

        if (!components.isEmpty()) {
            // Component size statistics
            List<Integer> sizes = components.stream()
                    .map(List::size)
                    .collect(Collectors.toList());

            stats.put("largestComponent", Collections.max(sizes));
            stats.put("smallestComponent", Collections.min(sizes));
            stats.put("averageComponentSize", sizes.stream().mapToInt(Integer::intValue).average().orElse(0));
            stats.put("componentSizes", sizes);

            // Count trivial components (size 1)
            long trivialCount = components.stream().filter(comp -> comp.size() == 1).count();
            stats.put("trivialComponents", trivialCount);
            stats.put("nonTrivialComponents", components.size() - trivialCount);

            // Check if graph is strongly connected
            stats.put("isStronglyConnected", components.size() == 1);
        }

        return stats;
    }

    /**
     * Returns detailed information about each component.
     *
     * @return list of component details
     */
    public List<Map<String, Object>> getComponentDetails() {
        List<Map<String, Object>> details = new ArrayList<>();

        for (int i = 0; i < components.size(); i++) {
            List<Vertex> component = components.get(i);
            Map<String, Object> compInfo = new HashMap<>();

            compInfo.put("id", i);
            compInfo.put("size", component.size());
            compInfo.put("vertices", component.stream()
                    .map(Vertex::getId)
                    .collect(Collectors.toList()));
            compInfo.put("vertexNames", component.stream()
                    .map(Vertex::getName)
                    .collect(Collectors.toList()));
            compInfo.put("totalDuration", component.stream()
                    .mapToInt(Vertex::getDuration)
                    .sum());
            compInfo.put("isTrivial", component.size() == 1);

            details.add(compInfo);
        }

        return details;
    }

    // ==================== UTILITY METHODS ====================

    private void resetMetrics() {
        dfsVisits = 0;
        edgesTraversed = 0;
        executionTime = 0;
    }

    /**
     * Returns the condensation graph.
     *
     * @return condensation graph, or null if not built
     */
    public Graph getCondensationGraph() {
        return condensationGraph;
    }

    /**
     * Returns all found components.
     *
     * @return list of components
     */
    public List<List<Vertex>> getComponents() {
        return new ArrayList<>(components);
    }

    /**
     * Prints a summary of SCC analysis results.
     */
    public void printAnalysis() {
        System.out.println("=== Strongly Connected Components Analysis ===");
        System.out.println("Total components found: " + components.size());
        System.out.printf("Execution time: %.3f ms%n", executionTime / 1_000_000.0);
        System.out.println("DFS visits: " + dfsVisits);
        System.out.println("Edges traversed: " + edgesTraversed);

        Map<String, Object> stats = getSCCStatistics();
        System.out.println("Largest component: " + stats.get("largestComponent"));
        System.out.println("Smallest component: " + stats.get("smallestComponent"));
        System.out.printf("Average component size: %.2f%n", stats.get("averageComponentSize"));
        System.out.println("Trivial components: " + stats.get("trivialComponents"));
        System.out.println("Non-trivial components: " + stats.get("nonTrivialComponents"));
        System.out.println("Graph is strongly connected: " + stats.get("isStronglyConnected"));

        System.out.println("\nComponent details:");
        for (int i = 0; i < components.size(); i++) {
            List<Vertex> component = components.get(i);
            System.out.printf("  Component %d: size=%d, vertices=%s%n",
                    i, component.size(),
                    component.stream()
                            .map(v -> String.valueOf(v.getId()))
                            .collect(Collectors.joining(", ")));
        }
    }

    // ==================== BUILDER PATTERN ====================

    /**
     * Builder for SCCFinder with optional configuration.
     */
    public static class Builder {
        private Graph graph;
        private boolean useTarjan = true; // Default to Tarjan's algorithm

        public Builder(Graph graph) {
            this.graph = graph;
        }

        public Builder useKosaraju() {
            this.useTarjan = false;
            return this;
        }

        public Builder useTarjan() {
            this.useTarjan = true;
            return this;
        }

        public SCCFinder build() {
            SCCFinder finder = new SCCFinder(graph);
            // Run the selected algorithm immediately
            if (useTarjan) {
                finder.findSCCsTarjan();
            } else {
                finder.findSCCsKosaraju();
            }
            finder.buildCondensationGraph();
            return finder;
        }
    }
}