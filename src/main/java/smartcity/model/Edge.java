package smartcity.model;

import java.util.Objects;

/**
 * Represents a directed edge between two vertices in the smart city task graph.
 * Edges can represent dependencies, transitions, or relationships between tasks.
 * Supports weighted edges for representing costs, distances, or dependency strengths.
 */
public class Edge {
    private final Vertex from;
    private final Vertex to;
    private final int weight;
    private String description;
    private EdgeType type;

    /**
     * Enum representing different types of edges/dependencies in the smart city system.
     */
    public enum EdgeType {
        TASK_DEPENDENCY("Task Dependency", "Tasks must be executed in sequence"),
        RESOURCE_SHARING("Resource Sharing", "Tasks share common resources"),
        TEMPORAL_CONSTRAINT("Temporal Constraint", "Time-based dependency"),
        DATA_FLOW("Data Flow", "Data transfer between tasks"),
        PHYSICAL_CONSTRAINT("Physical Constraint", "Physical location dependency"),
        OTHER("Other", "General dependency");

        private final String displayName;
        private final String description;

        EdgeType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    // ==================== CONSTRUCTORS ====================

    /**
     * Constructs an edge with specified vertices and weight.
     *
     * @param from the source vertex
     * @param to the target vertex
     * @param weight the edge weight
     * @throws IllegalArgumentException if vertices are null
     */
    public Edge(Vertex from, Vertex to, int weight) {
        this(from, to, weight, EdgeType.OTHER, "");
    }

    /**
     * Constructs an unweighted edge between two vertices.
     *
     * @param from the source vertex
     * @param to the target vertex
     * @throws IllegalArgumentException if vertices are null
     */
    public Edge(Vertex from, Vertex to) {
        this(from, to, 0, EdgeType.OTHER, "");
    }

    /**
     * Constructs an edge with all properties.
     *
     * @param from the source vertex
     * @param to the target vertex
     * @param weight the edge weight
     * @param type the type of edge/dependency
     * @param description detailed description of the edge
     * @throws IllegalArgumentException if vertices are null
     */
    public Edge(Vertex from, Vertex to, int weight, EdgeType type, String description) {
        validateVertices(from, to);

        this.from = from;
        this.to = to;
        this.weight = weight;
        this.type = (type != null) ? type : EdgeType.OTHER;
        this.description = (description != null) ? description.trim() : "";
    }

    // ==================== VALIDATION ====================

    private void validateVertices(Vertex from, Vertex to) {
        if (from == null) {
            throw new IllegalArgumentException("Source vertex cannot be null");
        }
        if (to == null) {
            throw new IllegalArgumentException("Target vertex cannot be null");
        }
    }

    // ==================== GETTERS ====================

    /**
     * Returns the source vertex of this edge.
     *
     * @return the source vertex
     */
    public Vertex getFrom() {
        return from;
    }

    /**
     * Returns the target vertex of this edge.
     *
     * @return the target vertex
     */
    public Vertex getTo() {
        return to;
    }

    /**
     * Returns the weight of this edge.
     *
     * @return the edge weight
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Returns the type of this edge.
     *
     * @return the edge type
     */
    public EdgeType getType() {
        return type;
    }

    /**
     * Returns the description of this edge.
     *
     * @return the edge description
     */
    public String getDescription() {
        return description;
    }

    // ==================== SETTERS ====================

    /**
     * Sets the type of this edge.
     *
     * @param type the edge type (if null, defaults to OTHER)
     */
    public void setType(EdgeType type) {
        this.type = (type != null) ? type : EdgeType.OTHER;
    }

    /**
     * Sets the description of this edge.
     *
     * @param description the edge description
     */
    public void setDescription(String description) {
        this.description = (description != null) ? description.trim() : "";
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Checks if this edge connects the specified vertices.
     *
     * @param fromId the source vertex ID
     * @param toId the target vertex ID
     * @return true if this edge connects the specified vertices, false otherwise
     */
    public boolean connects(int fromId, int toId) {
        return this.from.getId() == fromId && this.to.getId() == toId;
    }

    /**
     * Checks if this edge connects the specified vertices (vertex objects).
     *
     * @param fromVertex the source vertex
     * @param toVertex the target vertex
     * @return true if this edge connects the specified vertices, false otherwise
     */
    public boolean connects(Vertex fromVertex, Vertex toVertex) {
        return this.from.equals(fromVertex) && this.to.equals(toVertex);
    }

    /**
     * Checks if this edge is a self-loop (connects a vertex to itself).
     *
     * @return true if self-loop, false otherwise
     */
    public boolean isSelfLoop() {
        return from.equals(to);
    }

    /**
     * Checks if this edge is critical based on weight and type.
     *
     * @return true if critical, false otherwise
     */
    public boolean isCritical() {
        return weight > 5 ||
                type == EdgeType.TEMPORAL_CONSTRAINT ||
                type == EdgeType.PHYSICAL_CONSTRAINT;
    }

    /**
     * Returns the inverse weight (useful for longest path calculations).
     *
     * @return negative of the weight
     */
    public int getInverseWeight() {
        return -weight;
    }

    /**
     * Returns a color code for visualization based on edge type.
     *
     * @return color name as string
     */
    public String getTypeColor() {
        switch (type) {
            case TASK_DEPENDENCY: return "BLUE";
            case RESOURCE_SHARING: return "GREEN";
            case TEMPORAL_CONSTRAINT: return "RED";
            case DATA_FLOW: return "PURPLE";
            case PHYSICAL_CONSTRAINT: return "ORANGE";
            default: return "GRAY";
        }
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Creates a task dependency edge.
     *
     * @param from the source vertex
     * @param to the target vertex
     * @param weight the dependency weight
     * @return a new Edge instance
     */
    public static Edge createTaskDependency(Vertex from, Vertex to, int weight) {
        return new Edge(from, to, weight, EdgeType.TASK_DEPENDENCY,
                "Task dependency: " + from.getName() + " → " + to.getName());
    }

    /**
     * Creates a temporal constraint edge.
     *
     * @param from the source vertex
     * @param to the target vertex
     * @param timeConstraint the time constraint weight
     * @return a new Edge instance
     */
    public static Edge createTemporalConstraint(Vertex from, Vertex to, int timeConstraint) {
        return new Edge(from, to, timeConstraint, EdgeType.TEMPORAL_CONSTRAINT,
                "Temporal constraint: " + timeConstraint + " units");
    }

    /**
     * Creates a resource sharing edge.
     *
     * @param from the source vertex
     * @param to the target vertex
     * @param resourceCost the resource cost weight
     * @return a new Edge instance
     */
    public static Edge createResourceSharing(Vertex from, Vertex to, int resourceCost) {
        return new Edge(from, to, resourceCost, EdgeType.RESOURCE_SHARING,
                "Resource sharing cost: " + resourceCost);
    }

    // ==================== OBJECT METHODS ====================

    /**
     * Compares this edge with another object for equality based on vertices and weight.
     *
     * @param obj the object to compare with
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Edge edge = (Edge) obj;
        return weight == edge.weight &&
                from.equals(edge.from) &&
                to.equals(edge.to);
    }

    /**
     * Returns the hash code of this edge based on vertices and weight.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(from, to, weight);
    }

    /**
     * Returns a string representation of this edge.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("Edge{%d -> %d, weight=%d, type=%s}",
                from.getId(), to.getId(), weight, type.getDisplayName());
    }

    /**
     * Returns a detailed string representation for debugging.
     *
     * @return detailed string representation
     */
    public String toDetailedString() {
        return String.format(
                "Edge Details:%n" +
                        "  From: %s (ID: %d)%n" +
                        "  To: %s (ID: %d)%n" +
                        "  Weight: %d%n" +
                        "  Type: %s%n" +
                        "  Description: %s%n" +
                        "  Self-loop: %s%n" +
                        "  Critical: %s",
                from.getName(), from.getId(),
                to.getName(), to.getId(),
                weight, type.getDisplayName(), description,
                isSelfLoop(), isCritical()
        );
    }

    /**
     * Returns a JSON representation of this edge.
     *
     * @return JSON string
     */
    public String toJson() {
        return String.format(
                "{\"from\": %d, \"to\": %d, \"weight\": %d, \"type\": \"%s\", \"description\": \"%s\"}",
                from.getId(), to.getId(), weight, type, escapeJson(description)
        );
    }

    private String escapeJson(String str) {
        return str.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    /**
     * Creates a deep copy of this edge.
     *
     * @return a new Edge instance with the same properties
     */
    public Edge copy() {
        // Note: Vertex references are shared - this is intentional for graph structure
        Edge copy = new Edge(this.from, this.to, this.weight, this.type, this.description);
        return copy;
    }

    /**
     * Creates a reversed copy of this edge (swaps from and to).
     *
     * @return a new Edge with reversed direction
     */
    public Edge reversed() {
        return new Edge(this.to, this.from, this.weight, this.type,
                "Reversed: " + this.description);
    }

    // ==================== BUILDER PATTERN ====================

    /**
     * Builder for creating Edge instances with fluent API.
     */
    public static class Builder {
        private Vertex from;
        private Vertex to;
        private int weight = 0;
        private EdgeType type = EdgeType.OTHER;
        private String description = "";

        /**
         * Creates a new Builder with required fields.
         *
         * @param from the source vertex
         * @param to the target vertex
         */
        public Builder(Vertex from, Vertex to) {
            this.from = from;
            this.to = to;
        }

        /**
         * Creates a new Builder with vertex IDs (requires graph context for resolution).
         *
         * @param fromId the source vertex ID
         * @param toId the target vertex ID
         * @param graph the graph containing the vertices
         */
        public Builder(int fromId, int toId, Graph graph) {
            this.from = graph.getVertex(fromId);
            this.to = graph.getVertex(toId);
            if (this.from == null || this.to == null) {
                throw new IllegalArgumentException("Vertices not found in graph");
            }
        }

        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder type(EdgeType type) {
            this.type = type;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Builds the Edge instance.
         *
         * @return a new Edge instance
         * @throws IllegalArgumentException if validation fails
         */
        public Edge build() {
            return new Edge(from, to, weight, type, description);
        }
    }
}