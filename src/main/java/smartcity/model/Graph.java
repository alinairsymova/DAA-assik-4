package smartcity.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a directed graph for smart city task scheduling.
 * Supports both weighted edges and node durations for task processing.
 * Provides comprehensive graph operations and algorithm support.
 */
public class Graph {
    private final Map<Integer, Vertex> vertices;
    private final Map<Integer, List<Edge>> adjacencyList;
    private final List<Edge> edges;
    private boolean useNodeDurations;

    // ==================== CONSTRUCTORS ====================

    /**
     * Constructs an empty graph with node durations enabled by default.
     */
    public Graph() {
        this.vertices = new HashMap<>();
        this.adjacencyList = new HashMap<>();
        this.edges = new ArrayList<>();
        this.useNodeDurations = true;
    }

    /**
     * Constructs an empty graph with specified weight mode.
     *
     * @param useNodeDurations true to use node durations, false to use edge weights
     */
    public Graph(boolean useNodeDurations) {
        this();
        this.useNodeDurations = useNodeDurations;
    }

    /**
     * Constructs a graph from existing collections (copy constructor).
     *
     * @param vertices map of vertices
     * @param edges list of edges
     * @param useNodeDurations weight mode flag
     */
    private Graph(Map<Integer, Vertex> vertices, List<Edge> edges, boolean useNodeDurations) {
        this.vertices = new HashMap<>(vertices);
        this.edges = new ArrayList<>(edges);
        this.useNodeDurations = useNodeDurations;
        this.adjacencyList = new HashMap<>();

        // Rebuild adjacency list
        for (Vertex vertex : vertices.values()) {
            adjacencyList.put(vertex.getId(), new ArrayList<>());
        }
        for (Edge edge : edges) {
            adjacencyList.get(edge.getFrom().getId()).add(edge);
        }
    }

    // ==================== VERTEX OPERATIONS ====================

    /**
     * Adds a vertex to the graph.
     *
     * @param vertex the vertex to add
     * @throws IllegalArgumentException if vertex is null or already exists
     */
    public void addVertex(Vertex vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("Vertex cannot be null");
        }
        if (vertices.containsKey(vertex.getId())) {
            throw new IllegalArgumentException("Vertex with ID " + vertex.getId() + " already exists");
        }

        vertices.put(vertex.getId(), vertex);
        adjacencyList.put(vertex.getId(), new ArrayList<>());
    }

    /**
     * Adds a vertex with the specified ID and name.
     *
     * @param id the vertex ID
     * @param name the vertex name
     */
    public void addVertex(int id, String name) {
        addVertex(new Vertex(id, name));
    }

    /**
     * Adds a vertex with the specified ID, name, and duration.
     *
     * @param id the vertex ID
     * @param name the vertex name
     * @param duration the task duration
     */
    public void addVertex(int id, String name, int duration) {
        Vertex vertex = new Vertex(id, name);
        vertex.setDuration(duration);
        addVertex(vertex);
    }

    /**
     * Returns the vertex with the specified ID.
     *
     * @param id the vertex ID
     * @return the vertex, or null if not found
     */
    public Vertex getVertex(int id) {
        return vertices.get(id);
    }

    /**
     * Checks if the graph contains a vertex with the specified ID.
     *
     * @param id the vertex ID
     * @return true if the vertex exists, false otherwise
     */
    public boolean containsVertex(int id) {
        return vertices.containsKey(id);
    }

    /**
     * Removes a vertex and all associated edges from the graph.
     *
     * @param id the vertex ID to remove
     * @return true if the vertex was removed, false if not found
     */
    public boolean removeVertex(int id) {
        if (!vertices.containsKey(id)) {
            return false;
        }

        // Remove vertex
        vertices.remove(id);
        adjacencyList.remove(id);

        // Remove all edges involving this vertex
        edges.removeIf(edge -> edge.getFrom().getId() == id || edge.getTo().getId() == id);

        // Remove from other vertices' adjacency lists
        for (List<Edge> edgeList : adjacencyList.values()) {
            edgeList.removeIf(edge -> edge.getTo().getId() == id);
        }

        return true;
    }

    // ==================== EDGE OPERATIONS ====================

    /**
     * Adds an edge to the graph.
     *
     * @param edge the edge to add
     * @throws IllegalArgumentException if edge is null or vertices don't exist
     */
    public void addEdge(Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("Edge cannot be null");
        }
        if (!vertices.containsKey(edge.getFrom().getId()) || !vertices.containsKey(edge.getTo().getId())) {
            throw new IllegalArgumentException("Both vertices must exist in the graph before adding an edge");
        }

        edges.add(edge);
        adjacencyList.get(edge.getFrom().getId()).add(edge);
    }

    /**
     * Adds an edge between two vertices with the specified weight.
     *
     * @param fromId the source vertex ID
     * @param toId the target vertex ID
     * @param weight the edge weight
     * @throws IllegalArgumentException if vertices don't exist
     */
    public void addEdge(int fromId, int toId, int weight) {
        Vertex from = vertices.get(fromId);
        Vertex to = vertices.get(toId);

        if (from == null || to == null) {
            throw new IllegalArgumentException("Vertex not found for ID: " + fromId + " or " + toId);
        }

        addEdge(new Edge(from, to, weight));
    }

    /**
     * Adds an unweighted edge between two vertices.
     *
     * @param fromId the source vertex ID
     * @param toId the target vertex ID
     */
    public void addEdge(int fromId, int toId) {
        addEdge(fromId, toId, 0);
    }

    /**
     * Removes an edge from the graph.
     *
     * @param fromId the source vertex ID
     * @param toId the target vertex ID
     * @return true if the edge was removed, false if not found
     */
    public boolean removeEdge(int fromId, int toId) {
        List<Edge> edgesFrom = adjacencyList.get(fromId);
        if (edgesFrom == null) {
            return false;
        }

        boolean removed = edgesFrom.removeIf(edge -> edge.getTo().getId() == toId);
        if (removed) {
            edges.removeIf(edge -> edge.getFrom().getId() == fromId && edge.getTo().getId() == toId);
        }

        return removed;
    }

    // ==================== GRAPH PROPERTIES ====================

    /**
     * Returns the number of vertices in the graph.
     *
     * @return vertex count
     */
    public int getVertexCount() {
        return vertices.size();
    }

    /**
     * Returns the number of edges in the graph.
     *
     * @return edge count
     */
    public int getEdgeCount() {
        return edges.size();
    }

    /**
     * Returns an unmodifiable set of all vertex IDs.
     *
     * @return set of vertex IDs
     */
    public Set<Integer> getVertexIds() {
        return Collections.unmodifiableSet(vertices.keySet());
    }

    /**
     * Returns an unmodifiable collection of all vertices.
     *
     * @return collection of vertices
     */
    public Collection<Vertex> getVertices() {
        return Collections.unmodifiableCollection(vertices.values());
    }

    /**
     * Returns an unmodifiable list of all edges.
     *
     * @return list of edges
     */
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Returns an unmodifiable list of outgoing edges from a vertex.
     *
     * @param vertexId the source vertex ID
     * @return list of outgoing edges
     */
    public List<Edge> getOutgoingEdges(int vertexId) {
        return Collections.unmodifiableList(adjacencyList.getOrDefault(vertexId, new ArrayList<>()));
    }

    /**
     * Returns an unmodifiable list of incoming edges to a vertex.
     *
     * @param vertexId the target vertex ID
     * @return list of incoming edges
     */
    public List<Edge> getIncomingEdges(int vertexId) {
        return edges.stream()
                .filter(edge -> edge.getTo().getId() == vertexId)
                .collect(Collectors.toList());
    }

    /**
     * Returns the in-degree of a vertex (number of incoming edges).
     *
     * @param vertexId the vertex ID
     * @return in-degree count
     */
    public int getInDegree(int vertexId) {
        return (int) edges.stream()
                .filter(edge -> edge.getTo().getId() == vertexId)
                .count();
    }

    /**
     * Returns the out-degree of a vertex (number of outgoing edges).
     *
     * @param vertexId the vertex ID
     * @return out-degree count
     */
    public int getOutDegree(int vertexId) {
        return adjacencyList.getOrDefault(vertexId, new ArrayList<>()).size();
    }

    // ==================== GRAPH ALGORITHMS SUPPORT ====================

    /**
     * Returns the transpose (reverse) of this graph.
     *
     * @return a new graph with all edges reversed
     */
    public Graph getTranspose() {
        Graph transpose = new Graph(this.useNodeDurations);

        // Add all vertices (create copies to avoid reference sharing)
        for (Vertex vertex : vertices.values()) {
            Vertex vertexCopy = new Vertex(
                    vertex.getId(),
                    vertex.getName(),
                    vertex.getDuration(),
                    vertex.getType(),
                    vertex.getDescription()
            );
            transpose.addVertex(vertexCopy);
        }

        // Reverse all edges
        for (Edge edge : edges) {
            transpose.addEdge(edge.getTo().getId(), edge.getFrom().getId(), edge.getWeight());
        }

        return transpose;
    }

    /**
     * Creates a deep copy of this graph.
     *
     * @return a new graph with the same structure
     */
    public Graph copy() {
        return new Graph(vertices, edges, useNodeDurations);
    }

    /**
     * Resets the visited state of all vertices.
     */
    public void resetVisited() {
        for (Vertex vertex : vertices.values()) {
            vertex.markUnvisited();
        }
    }

    // ==================== WEIGHT/DURATION ACCESSORS ====================

    /**
     * Returns whether the graph uses node durations or edge weights.
     *
     * @return true if using node durations, false if using edge weights
     */
    public boolean useNodeDurations() {
        return useNodeDurations;
    }

    /**
     * Sets the weight mode for the graph.
     *
     * @param useNodeDurations true to use node durations, false to use edge weights
     */
    public void setUseNodeDurations(boolean useNodeDurations) {
        this.useNodeDurations = useNodeDurations;
    }

    /**
     * Returns the duration of a vertex.
     *
     * @param vertexId the vertex ID
     * @return vertex duration, or 0 if vertex not found
     */
    public int getNodeDuration(int vertexId) {
        Vertex vertex = vertices.get(vertexId);
        return vertex != null ? vertex.getDuration() : 0;
    }

    /**
     * Returns the weight of an edge between two vertices.
     *
     * @param fromId source vertex ID
     * @param toId target vertex ID
     * @return edge weight, or 0 if edge not found
     */
    public int getEdgeWeight(int fromId, int toId) {
        return adjacencyList.getOrDefault(fromId, new ArrayList<>()).stream()
                .filter(edge -> edge.getTo().getId() == toId)
                .map(Edge::getWeight)
                .findFirst()
                .orElse(0);
    }

    // ==================== VALIDATION AND UTILITY METHODS ====================

    /**
     * Checks if the graph contains any cycles.
     *
     * @return true if cycle exists, false otherwise
     */
    public boolean hasCycle() {
        resetVisited();
        Set<Integer> recursionStack = new HashSet<>();

        for (Integer vertexId : vertices.keySet()) {
            if (!vertices.get(vertexId).isVisited()) {
                if (hasCycleDFS(vertexId, recursionStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasCycleDFS(int vertexId, Set<Integer> recursionStack) {
        Vertex vertex = vertices.get(vertexId);
        vertex.markVisited();
        recursionStack.add(vertexId);

        for (Edge edge : getOutgoingEdges(vertexId)) {
            int neighborId = edge.getTo().getId();
            if (recursionStack.contains(neighborId)) {
                return true;
            }
            if (!vertices.get(neighborId).isVisited() && hasCycleDFS(neighborId, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(vertexId);
        return false;
    }

    /**
     * Checks if the graph is weakly connected (considering edges as undirected).
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        if (vertices.isEmpty()) {
            return true;
        }

        resetVisited();
        Integer startVertex = vertices.keySet().iterator().next();
        dfsUndirected(startVertex);

        return vertices.values().stream().allMatch(Vertex::isVisited);
    }

    private void dfsUndirected(int vertexId) {
        Vertex vertex = vertices.get(vertexId);
        vertex.markVisited();

        // Visit all neighbors (both outgoing and incoming)
        for (Edge edge : getOutgoingEdges(vertexId)) {
            if (!edge.getTo().isVisited()) {
                dfsUndirected(edge.getTo().getId());
            }
        }

        for (Edge edge : getIncomingEdges(vertexId)) {
            if (!edge.getFrom().isVisited()) {
                dfsUndirected(edge.getFrom().getId());
            }
        }
    }

    // ==================== STATISTICS ====================

    /**
     * Returns comprehensive statistics about the graph.
     *
     * @return map of graph statistics
     */
    public Map<String, Object> getGraphStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("vertexCount", getVertexCount());
        stats.put("edgeCount", getEdgeCount());
        stats.put("density", calculateDensity());
        stats.put("hasCycle", hasCycle());
        stats.put("isConnected", isConnected());
        stats.put("useNodeDurations", useNodeDurations);

        // Degree statistics
        int maxInDegree = vertices.keySet().stream().mapToInt(this::getInDegree).max().orElse(0);
        int maxOutDegree = vertices.keySet().stream().mapToInt(this::getOutDegree).max().orElse(0);
        stats.put("maxInDegree", maxInDegree);
        stats.put("maxOutDegree", maxOutDegree);

        // Vertex type distribution
        Map<Vertex.VertexType, Long> typeDistribution = vertices.values().stream()
                .collect(Collectors.groupingBy(Vertex::getType, Collectors.counting()));
        stats.put("typeDistribution", typeDistribution);

        return stats;
    }

    private double calculateDensity() {
        int n = getVertexCount();
        if (n <= 1) return 0.0;
        return (double) getEdgeCount() / (n * (n - 1));
    }

    // ==================== OBJECT METHODS ====================

    /**
     * Returns a string representation of the graph.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph {\n")
                .append("  vertices: ").append(vertices.size()).append("\n")
                .append("  edges: ").append(edges.size()).append("\n")
                .append("  useNodeDurations: ").append(useNodeDurations).append("\n")
                .append("  density: ").append(String.format("%.3f", calculateDensity())).append("\n")
                .append("}\n");

        for (Vertex vertex : vertices.values()) {
            sb.append("  ").append(vertex.toString()).append(" -> ");
            List<Edge> outgoing = getOutgoingEdges(vertex.getId());
            if (outgoing.isEmpty()) {
                sb.append("[]");
            } else {
                sb.append(outgoing.stream()
                        .map(edge -> String.format("%d(w:%d)", edge.getTo().getId(), edge.getWeight()))
                        .collect(Collectors.joining(", ")));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Compares this graph with another object for equality.
     *
     * @param obj the object to compare with
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Graph graph = (Graph) obj;
        return useNodeDurations == graph.useNodeDurations &&
                vertices.equals(graph.vertices) &&
                edges.equals(graph.edges);
    }

    /**
     * Returns the hash code of this graph.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(vertices, edges, useNodeDurations);
    }

    // ==================== BUILDER PATTERN ====================

    /**
     * Builder for creating Graph instances with fluent API.
     */
    public static class Builder {
        private final Graph graph;

        public Builder() {
            this.graph = new Graph();
        }

        public Builder(boolean useNodeDurations) {
            this.graph = new Graph(useNodeDurations);
        }

        public Builder addVertex(int id, String name) {
            graph.addVertex(id, name);
            return this;
        }

        public Builder addVertex(int id, String name, int duration) {
            graph.addVertex(id, name, duration);
            return this;
        }

        public Builder addVertex(Vertex vertex) {
            graph.addVertex(vertex);
            return this;
        }

        public Builder addEdge(int fromId, int toId) {
            graph.addEdge(fromId, toId);
            return this;
        }

        public Builder addEdge(int fromId, int toId, int weight) {
            graph.addEdge(fromId, toId, weight);
            return this;
        }

        public Builder addEdge(Edge edge) {
            graph.addEdge(edge);
            return this;
        }

        /**
         * Builds the Graph instance.
         *
         * @return the constructed graph
         */
        public Graph build() {
            return graph;
        }
    }
}