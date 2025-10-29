package smartcity.model;

import java.util.Objects;

/**
 * Represents a vertex (node) in the graph for smart city task scheduling.
 * Each vertex corresponds to a city service task with properties like duration, type, and metadata.
 */
public class Vertex {
    private final int id;
    private String name;
    private int duration;
    private VertexType type;
    private String description;
    private boolean visited; // Temporary state for graph algorithms

    /**
     * Enum representing different types of city service tasks.
     */
    public enum VertexType {
        STREET_CLEANING("Street Cleaning", "SC"),
        REPAIRS("Repairs", "REP"),
        MAINTENANCE("Maintenance", "MAINT"),
        SENSOR_MONITORING("Sensor Monitoring", "SENSOR"),
        DATA_ANALYTICS("Data Analytics", "DATA"),
        TRANSPORT("Transport", "TRANS"),
        SAFETY("Safety", "SAFE"),
        UTILITIES("Utilities", "UTIL"),
        OTHER("Other", "OTH");

        private final String displayName;
        private final String code;

        VertexType(String displayName, String code) {
            this.displayName = displayName;
            this.code = code;
        }

        public String getDisplayName() { return displayName; }
        public String getCode() { return code; }
    }

    // ==================== CONSTRUCTORS ====================

    /**
     * Constructs a Vertex with minimal required fields.
     *
     * @param id the unique identifier for the vertex
     * @param name the name of the vertex/task
     * @throws IllegalArgumentException if id is negative or name is null/empty
     */
    public Vertex(int id, String name) {
        this(id, name, 0, VertexType.OTHER, "");
    }

    /**
     * Constructs a Vertex with name and duration.
     *
     * @param id the unique identifier
     * @param name the name of the task
     * @param duration the duration required to complete the task
     */
    public Vertex(int id, String name, int duration) {
        this(id, name, duration, VertexType.OTHER, "");
    }

    /**
     * Constructs a Vertex with all properties.
     *
     * @param id the unique identifier
     * @param name the name of the task
     * @param duration the task duration
     * @param type the type of city service task
     * @param description detailed description of the task
     */
    public Vertex(int id, String name, int duration, VertexType type, String description) {
        validateConstructorArgs(id, name);

        this.id = id;
        this.name = name.trim();
        this.duration = Math.max(0, duration); // Ensure non-negative
        this.type = (type != null) ? type : VertexType.OTHER;
        this.description = (description != null) ? description.trim() : "";
        this.visited = false;
    }

    // ==================== VALIDATION ====================

    private void validateConstructorArgs(int id, String name) {
        if (id < 0) {
            throw new IllegalArgumentException("Vertex ID cannot be negative: " + id);
        }
        if (name == null) {
            throw new IllegalArgumentException("Vertex name cannot be null for ID: " + id);
        }
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Vertex name cannot be empty for ID: " + id);
        }
    }

    // ==================== GETTERS ====================

    /**
     * Returns the unique identifier of this vertex.
     *
     * @return the vertex ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of this vertex/task.
     *
     * @return the vertex name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the duration required to complete this task.
     *
     * @return the task duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Returns the type of city service task.
     *
     * @return the vertex type
     */
    public VertexType getType() {
        return type;
    }

    /**
     * Returns the detailed description of this task.
     *
     * @return the task description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns whether this vertex has been visited during graph traversal.
     *
     * @return true if visited, false otherwise
     */
    public boolean isVisited() {
        return visited;
    }

    // ==================== SETTERS ====================

    /**
     * Sets the name of this vertex.
     *
     * @param name the new name (cannot be null or empty)
     * @throws IllegalArgumentException if name is null or empty
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Vertex name cannot be null or empty");
        }
        this.name = name.trim();
    }

    /**
     * Sets the duration required for this task.
     *
     * @param duration the new duration (non-negative)
     */
    public void setDuration(int duration) {
        this.duration = Math.max(0, duration);
    }

    /**
     * Sets the type of this vertex.
     *
     * @param type the vertex type (if null, defaults to OTHER)
     */
    public void setType(VertexType type) {
        this.type = (type != null) ? type : VertexType.OTHER;
    }

    /**
     * Sets the description of this task.
     *
     * @param description the task description
     */
    public void setDescription(String description) {
        this.description = (description != null) ? description.trim() : "";
    }

    /**
     * Sets the visited state of this vertex.
     *
     * @param visited the visited state
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Marks this vertex as visited.
     */
    public void markVisited() {
        this.visited = true;
    }

    /**
     * Marks this vertex as unvisited.
     */
    public void markUnvisited() {
        this.visited = false;
    }

    /**
     * Checks if this task is considered critical based on type and duration.
     *
     * @return true if critical, false otherwise
     */
    public boolean isCriticalTask() {
        return duration > 10 ||
                type == VertexType.SAFETY ||
                type == VertexType.UTILITIES;
    }

    /**
     * Returns a color code for visualization based on vertex type.
     *
     * @return color name as string
     */
    public String getTypeColor() {
        switch (type) {
            case STREET_CLEANING: return "GREEN";
            case REPAIRS: return "ORANGE";
            case MAINTENANCE: return "BLUE";
            case SENSOR_MONITORING: return "PURPLE";
            case DATA_ANALYTICS: return "CYAN";
            case TRANSPORT: return "RED";
            case SAFETY: return "RED";
            case UTILITIES: return "BROWN";
            default: return "GRAY";
        }
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Creates a street cleaning vertex.
     *
     * @param id the vertex ID
     * @param location the location for cleaning
     * @param duration the cleaning duration
     * @return a new Vertex instance
     */
    public static Vertex createStreetCleaning(int id, String location, int duration) {
        return new Vertex(id, "Street Cleaning: " + location, duration,
                VertexType.STREET_CLEANING, "Cleaning task for " + location);
    }

    /**
     * Creates a repair task vertex.
     *
     * @param id the vertex ID
     * @param asset the asset to repair
     * @param duration the repair duration
     * @return a new Vertex instance
     */
    public static Vertex createRepairTask(int id, String asset, int duration) {
        return new Vertex(id, "Repair: " + asset, duration,
                VertexType.REPAIRS, "Repair task for " + asset);
    }

    /**
     * Creates a maintenance task vertex.
     *
     * @param id the vertex ID
     * @param equipment the equipment to maintain
     * @param duration the maintenance duration
     * @return a new Vertex instance
     */
    public static Vertex createMaintenanceTask(int id, String equipment, int duration) {
        return new Vertex(id, "Maintenance: " + equipment, duration,
                VertexType.MAINTENANCE, "Maintenance task for " + equipment);
    }

    // ==================== OBJECT METHODS ====================

    /**
     * Compares this vertex with another object for equality based on ID.
     *
     * @param obj the object to compare with
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vertex vertex = (Vertex) obj;
        return id == vertex.id;
    }

    /**
     * Returns the hash code of this vertex based on ID.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of this vertex.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("Vertex{id=%d, name='%s', duration=%d, type=%s}",
                id, name, duration, type);
    }

    /**
     * Returns a detailed string representation for debugging.
     *
     * @return detailed string representation
     */
    public String toDetailedString() {
        return String.format(
                "Vertex Details:%n" +
                        "  ID: %d%n" +
                        "  Name: %s%n" +
                        "  Duration: %d%n" +
                        "  Type: %s%n" +
                        "  Description: %s%n" +
                        "  Visited: %s%n" +
                        "  Critical: %s",
                id, name, duration, type.getDisplayName(), description, visited, isCriticalTask()
        );
    }

    /**
     * Returns a JSON representation of this vertex.
     *
     * @return JSON string
     */
    public String toJson() {
        return String.format(
                "{\"id\": %d, \"name\": \"%s\", \"duration\": %d, \"type\": \"%s\", \"description\": \"%s\"}",
                id, escapeJson(name), duration, type, escapeJson(description)
        );
    }

    private String escapeJson(String str) {
        return str.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    /**
     * Creates a deep copy of this vertex.
     *
     * @return a new Vertex instance with the same properties
     */
    public Vertex copy() {
        Vertex copy = new Vertex(this.id, this.name, this.duration, this.type, this.description);
        copy.visited = this.visited;
        return copy;
    }

    // ==================== BUILDER PATTERN ====================

    /**
     * Builder for creating Vertex instances with fluent API.
     */
    public static class Builder {
        private final int id;
        private String name;
        private int duration = 0;
        private VertexType type = VertexType.OTHER;
        private String description = "";

        /**
         * Creates a new Builder with required fields.
         *
         * @param id the vertex ID
         * @param name the vertex name
         */
        public Builder(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder type(VertexType type) {
            this.type = type;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Builds the Vertex instance.
         *
         * @return a new Vertex instance
         * @throws IllegalArgumentException if validation fails
         */
        public Vertex build() {
            return new Vertex(id, name, duration, type, description);
        }
    }
}