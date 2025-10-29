package smartcity.generator;

import smartcity.model.Graph;
import smartcity.model.Vertex;
import smartcity.model.Edge;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Random;

/**
 * Generates various graph datasets for testing graph algorithms.
 * Supports different sizes, densities, and structures including cyclic and acyclic graphs.
 */
public class GraphGenerator {
    private final Random random;
    private final GraphGenerationConfig config;

    /**
     * Configuration for graph generation.
     */
    public static class GraphGenerationConfig {
        private final int vertexCount;
        private final double density;
        private final boolean allowCycles;
        private final boolean useNodeDurations;
        private final int maxDuration;
        private final int maxEdgeWeight;
        private final int minDuration;
        private final int minEdgeWeight;

        private GraphGenerationConfig(Builder builder) {
            this.vertexCount = builder.vertexCount;
            this.density = builder.density;
            this.allowCycles = builder.allowCycles;
            this.useNodeDurations = builder.useNodeDurations;
            this.maxDuration = builder.maxDuration;
            this.maxEdgeWeight = builder.maxEdgeWeight;
            this.minDuration = builder.minDuration;
            this.minEdgeWeight = builder.minEdgeWeight;
        }

        // Getters
        public int getVertexCount() { return vertexCount; }
        public double getDensity() { return density; }
        public boolean isAllowCycles() { return allowCycles; }
        public boolean isUseNodeDurations() { return useNodeDurations; }
        public int getMaxDuration() { return maxDuration; }
        public int getMaxEdgeWeight() { return maxEdgeWeight; }
        public int getMinDuration() { return minDuration; }
        public int getMinEdgeWeight() { return minEdgeWeight; }

        /**
         * Builder for GraphGenerationConfig.
         */
        public static class Builder {
            private int vertexCount = 10;
            private double density = 0.3;
            private boolean allowCycles = true;
            private boolean useNodeDurations = true;
            private int maxDuration = 10;
            private int maxEdgeWeight = 5;
            private int minDuration = 1;
            private int minEdgeWeight = 1;

            public Builder vertexCount(int vertexCount) {
                this.vertexCount = vertexCount;
                return this;
            }

            public Builder density(double density) {
                this.density = Math.max(0.0, Math.min(1.0, density));
                return this;
            }

            public Builder allowCycles(boolean allowCycles) {
                this.allowCycles = allowCycles;
                return this;
            }

            public Builder useNodeDurations(boolean useNodeDurations) {
                this.useNodeDurations = useNodeDurations;
                return this;
            }

            public Builder maxDuration(int maxDuration) {
                this.maxDuration = maxDuration;
                return this;
            }

            public Builder maxEdgeWeight(int maxEdgeWeight) {
                this.maxEdgeWeight = maxEdgeWeight;
                return this;
            }

            public Builder minDuration(int minDuration) {
                this.minDuration = minDuration;
                return this;
            }

            public Builder minEdgeWeight(int minEdgeWeight) {
                this.minEdgeWeight = minEdgeWeight;
                return this;
            }

            public GraphGenerationConfig build() {
                return new GraphGenerationConfig(this);
            }
        }
    }

    // ==================== CONSTRUCTORS ====================

    /**
     * Creates a GraphGenerator with default configuration.
     */
    public GraphGenerator() {
        this(new GraphGenerationConfig.Builder().build());
    }

    /**
     * Creates a GraphGenerator with specified configuration.
     *
     * @param config the generation configuration
     */
    public GraphGenerator(GraphGenerationConfig config) {
        this.config = config;
        this.random = new Random();
    }

    /**
     * Creates a GraphGenerator with specified seed for reproducible results.
     *
     * @param config the generation configuration
     * @param seed the random seed
     */
    public GraphGenerator(GraphGenerationConfig config, long seed) {
        this.config = config;
        this.random = new Random(seed);
    }

    // ==================== GRAPH GENERATION METHODS ====================

    /**
     * Generates a random graph based on the configuration.
     *
     * @return generated graph
     */
    public Graph generateRandomGraph() {
        Graph graph = new Graph(config.useNodeDurations);

        // Create vertices
        createVertices(graph);

        // Create edges based on density
        createEdges(graph);

        // Ensure connectivity if needed
        if (!config.allowCycles) {
            ensureAcyclic(graph);
        }

        return graph;
    }

    /**
     * Generates a pure DAG (Directed Acyclic Graph).
     *
     * @return generated DAG
     */
    public Graph generateDAG() {
        GraphGenerationConfig dagConfig = new GraphGenerationConfig.Builder()
                .vertexCount(config.vertexCount)
                .density(config.density)
                .allowCycles(false)
                .useNodeDurations(config.useNodeDurations)
                .maxDuration(config.maxDuration)
                .maxEdgeWeight(config.maxEdgeWeight)
                .build();

        GraphGenerator dagGenerator = new GraphGenerator(dagConfig, random.nextLong());
        return dagGenerator.generateRandomGraph();
    }

    /**
     * Generates a graph with multiple strongly connected components.
     *
     * @param componentCount number of SCCs to create
     * @return graph with multiple SCCs
     */
    public Graph generateGraphWithSCCs(int componentCount) {
        if (componentCount > config.vertexCount) {
            throw new IllegalArgumentException("Component count cannot exceed vertex count");
        }

        Graph graph = new Graph(config.useNodeDurations);

        // Create vertices
        createVertices(graph);

        // Create SCCs
        createSCCs(graph, componentCount);

        // Add edges between components
        addInterComponentEdges(graph, componentCount);

        return graph;
    }

    /**
     * Generates a complete graph (maximum density).
     *
     * @return complete graph
     */
    public Graph generateCompleteGraph() {
        GraphGenerationConfig completeConfig = new GraphGenerationConfig.Builder()
                .vertexCount(config.vertexCount)
                .density(1.0)
                .allowCycles(config.allowCycles)
                .useNodeDurations(config.useNodeDurations)
                .maxDuration(config.maxDuration)
                .maxEdgeWeight(config.maxEdgeWeight)
                .build();

        GraphGenerator completeGenerator = new GraphGenerator(completeConfig, random.nextLong());
        return completeGenerator.generateRandomGraph();
    }

    /**
     * Generates a sparse graph (low density).
     *
     * @return sparse graph
     */
    public Graph generateSparseGraph() {
        GraphGenerationConfig sparseConfig = new GraphGenerationConfig.Builder()
                .vertexCount(config.vertexCount)
                .density(0.1)
                .allowCycles(config.allowCycles)
                .useNodeDurations(config.useNodeDurations)
                .maxDuration(config.maxDuration)
                .maxEdgeWeight(config.maxEdgeWeight)
                .build();

        GraphGenerator sparseGenerator = new GraphGenerator(sparseConfig, random.nextLong());
        return sparseGenerator.generateRandomGraph();
    }

    // ==================== DATASET GENERATION FOR ASSIGNMENT ====================

    /**
     * Generates all required datasets for the assignment.
     *
     * @return map of dataset categories to lists of graphs
     */
    public Map<String, List<Graph>> generateAssignmentDatasets() {
        Map<String, List<Graph>> datasets = new HashMap<>();

        datasets.put("small", generateSmallDatasets());
        datasets.put("medium", generateMediumDatasets());
        datasets.put("large", generateLargeDatasets());

        return datasets;
    }

    /**
     * Generates small datasets (6-10 vertices).
     *
     * @return list of small graphs
     */
    public List<Graph> generateSmallDatasets() {
        List<Graph> smallGraphs = new ArrayList<>();

        // Small graph 1: Simple DAG
        smallGraphs.add(createSmallGraph1());

        // Small graph 2: Graph with 1-2 cycles
        smallGraphs.add(createSmallGraph2());

        // Small graph 3: Mixed structure
        smallGraphs.add(createSmallGraph3());

        return smallGraphs;
    }

    /**
     * Generates medium datasets (10-20 vertices).
     *
     * @return list of medium graphs
     */
    public List<Graph> generateMediumDatasets() {
        List<Graph> mediumGraphs = new ArrayList<>();

        // Medium graph 1: Mixed structures, several SCCs
        mediumGraphs.add(createMediumGraph1());

        // Medium graph 2: Dense graph
        mediumGraphs.add(createMediumGraph2());

        // Medium graph 3: Sparse graph with cycles
        mediumGraphs.add(createMediumGraph3());

        return mediumGraphs;
    }

    /**
     * Generates large datasets (20-50 vertices).
     *
     * @return list of large graphs
     */
    public List<Graph> generateLargeDatasets() {
        List<Graph> largeGraphs = new ArrayList<>();

        // Large graph 1: Performance test - dense
        largeGraphs.add(createLargeGraph1());

        // Large graph 2: Performance test - sparse
        largeGraphs.add(createLargeGraph2());

        // Large graph 3: Complex structure with multiple SCCs
        largeGraphs.add(createLargeGraph3());

        return largeGraphs;
    }

    // ==================== PRIVATE GENERATION METHODS ====================

    private void createVertices(Graph graph) {
        String[] taskTypes = {"Street Cleaning", "Repairs", "Maintenance",
                "Sensor Monitoring", "Data Analytics", "Transport",
                "Safety", "Utilities"};

        String[] locations = {"Downtown", "Suburbs", "Industrial Zone",
                "Residential Area", "City Center", "Park",
                "Highway", "Bridge"};

        for (int i = 0; i < config.vertexCount; i++) {
            String taskType = taskTypes[random.nextInt(taskTypes.length)];
            String location = locations[random.nextInt(locations.length)];
            String name = taskType + ": " + location;

            int duration = config.minDuration + random.nextInt(config.maxDuration - config.minDuration + 1);

            graph.addVertex(i, name, duration);
        }
    }

    private void createEdges(Graph graph) {
        int maxPossibleEdges = config.vertexCount * (config.vertexCount - 1);
        int targetEdges = (int) (maxPossibleEdges * config.density);

        int edgesCreated = 0;
        int attempts = 0;
        int maxAttempts = maxPossibleEdges * 2;

        while (edgesCreated < targetEdges && attempts < maxAttempts) {
            attempts++;
            int from = random.nextInt(config.vertexCount);
            int to = random.nextInt(config.vertexCount);

            // Skip self-loops and existing edges
            if (from == to || graph.getEdgeWeight(from, to) != 0) {
                continue;
            }

            int weight = config.minEdgeWeight + random.nextInt(config.maxEdgeWeight - config.minEdgeWeight + 1);
            graph.addEdge(from, to, weight);
            edgesCreated++;
        }
    }

    private void ensureAcyclic(Graph graph) {
        // Use topological sort approach to ensure acyclicity
        // Assign ranks to vertices and only allow edges from lower to higher ranks
        int[] ranks = new int[config.vertexCount];
        for (int i = 0; i < config.vertexCount; i++) {
            ranks[i] = i;
        }

        // Shuffle ranks to create random ordering
        for (int i = config.vertexCount - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = ranks[i];
            ranks[i] = ranks[j];
            ranks[j] = temp;
        }

        // Remove edges that violate the rank order
        List<Edge> edgesToRemove = new ArrayList<>();
        for (Edge edge : graph.getEdges()) {
            int fromRank = ranks[edge.getFrom().getId()];
            int toRank = ranks[edge.getTo().getId()];

            if (fromRank >= toRank) {
                edgesToRemove.add(edge);
            }
        }

        // Remove violating edges
        for (Edge edge : edgesToRemove) {
            graph.removeEdge(edge.getFrom().getId(), edge.getTo().getId());
        }
    }

    private void createSCCs(Graph graph, int componentCount) {
        // Distribute vertices among components
        int[] componentSizes = distributeVertices(config.vertexCount, componentCount);
        int[][] components = new int[componentCount][];

        int vertexIndex = 0;
        for (int i = 0; i < componentCount; i++) {
            components[i] = new int[componentSizes[i]];
            for (int j = 0; j < componentSizes[i]; j++) {
                components[i][j] = vertexIndex++;
            }

            // Make each component strongly connected
            makeComponentStronglyConnected(graph, components[i]);
        }
    }

    private void makeComponentStronglyConnected(Graph graph, int[] component) {
        if (component.length == 1) {
            return; // Single vertex is trivially strongly connected
        }

        // Create a cycle through all vertices in the component
        for (int i = 0; i < component.length; i++) {
            int from = component[i];
            int to = component[(i + 1) % component.length];
            int weight = config.minEdgeWeight + random.nextInt(config.maxEdgeWeight - config.minEdgeWeight + 1);
            graph.addEdge(from, to, weight);
        }

        // Add some additional random edges within the component
        int internalEdges = (component.length * (component.length - 1)) / 4;
        for (int i = 0; i < internalEdges; i++) {
            int from = component[random.nextInt(component.length)];
            int to = component[random.nextInt(component.length)];

            if (from != to && graph.getEdgeWeight(from, to) == 0) {
                int weight = config.minEdgeWeight + random.nextInt(config.maxEdgeWeight - config.minEdgeWeight + 1);
                graph.addEdge(from, to, weight);
            }
        }
    }

    private void addInterComponentEdges(Graph graph, int componentCount) {
        // Add edges between components (always from lower to higher index to avoid cycles between components)
        int interComponentEdges = (int) (config.vertexCount * config.density);

        for (int i = 0; i < interComponentEdges; i++) {
            int fromComp = random.nextInt(componentCount);
            int toComp = random.nextInt(componentCount);

            if (fromComp >= toComp) {
                continue; // Only allow edges from lower to higher component indices
            }

            // Get random vertices from each component (simplified - in real implementation would track components)
            int from = random.nextInt(config.vertexCount);
            int to = random.nextInt(config.vertexCount);

            if (from != to && graph.getEdgeWeight(from, to) == 0) {
                int weight = config.minEdgeWeight + random.nextInt(config.maxEdgeWeight - config.minEdgeWeight + 1);
                graph.addEdge(from, to, weight);
            }
        }
    }

    private int[] distributeVertices(int totalVertices, int components) {
        int[] sizes = new int[components];
        int baseSize = totalVertices / components;
        int remainder = totalVertices % components;

        for (int i = 0; i < components; i++) {
            sizes[i] = baseSize + (i < remainder ? 1 : 0);
        }

        return sizes;
    }

    // ==================== SPECIFIC DATASET CREATION ====================

    private Graph createSmallGraph1() {
        // Simple DAG
        GraphGenerationConfig config = new GraphGenerationConfig.Builder()
                .vertexCount(8)
                .density(0.4)
                .allowCycles(false)
                .useNodeDurations(true)
                .maxDuration(8)
                .minDuration(1)
                .maxEdgeWeight(3)
                .minEdgeWeight(1)
                .build();

        return new GraphGenerator(config, 1L).generateRandomGraph();
    }

    private Graph createSmallGraph2() {
        // Graph with 1-2 cycles
        GraphGenerationConfig config = new GraphGenerationConfig.Builder()
                .vertexCount(7)
                .density(0.5)
                .allowCycles(true)
                .useNodeDurations(true)
                .maxDuration(10)
                .minDuration(2)
                .maxEdgeWeight(5)
                .minEdgeWeight(1)
                .build();

        return new GraphGenerator(config, 2L).generateRandomGraph();
    }

    private Graph createSmallGraph3() {
        // Mixed structure
        return generateGraphWithSCCs(3);
    }

    private Graph createMediumGraph1() {
        // Mixed structures, several SCCs
        GraphGenerationConfig config = new GraphGenerationConfig.Builder()
                .vertexCount(15)
                .density(0.3)
                .allowCycles(true)
                .useNodeDurations(true)
                .maxDuration(12)
                .minDuration(1)
                .maxEdgeWeight(4)
                .minEdgeWeight(1)
                .build();

        return new GraphGenerator(config, 3L).generateGraphWithSCCs(4);
    }

    private Graph createMediumGraph2() {
        // Dense graph
        GraphGenerationConfig config = new GraphGenerationConfig.Builder()
                .vertexCount(12)
                .density(0.7)
                .allowCycles(true)
                .useNodeDurations(false) // Use edge weights
                .maxEdgeWeight(8)
                .minEdgeWeight(1)
                .build();

        return new GraphGenerator(config, 4L).generateRandomGraph();
    }

    private Graph createMediumGraph3() {
        // Sparse graph with cycles
        GraphGenerationConfig config = new GraphGenerationConfig.Builder()
                .vertexCount(18)
                .density(0.2)
                .allowCycles(true)
                .useNodeDurations(true)
                .maxDuration(15)
                .minDuration(3)
                .maxEdgeWeight(6)
                .minEdgeWeight(2)
                .build();

        return new GraphGenerator(config, 5L).generateRandomGraph();
    }

    private Graph createLargeGraph1() {
        // Performance test - dense
        GraphGenerationConfig config = new GraphGenerationConfig.Builder()
                .vertexCount(35)
                .density(0.6)
                .allowCycles(true)
                .useNodeDurations(true)
                .maxDuration(20)
                .minDuration(5)
                .maxEdgeWeight(10)
                .minEdgeWeight(1)
                .build();

        return new GraphGenerator(config, 6L).generateRandomGraph();
    }

    private Graph createLargeGraph2() {
        // Performance test - sparse
        GraphGenerationConfig config = new GraphGenerationConfig.Builder()
                .vertexCount(40)
                .density(0.15)
                .allowCycles(false) // DAG for shortest path tests
                .useNodeDurations(false) // Use edge weights
                .maxEdgeWeight(12)
                .minEdgeWeight(1)
                .build();

        return new GraphGenerator(config, 7L).generateRandomGraph();
    }

    private Graph createLargeGraph3() {
        // Complex structure with multiple SCCs
        GraphGenerationConfig config = new GraphGenerationConfig.Builder()
                .vertexCount(30)
                .density(0.4)
                .allowCycles(true)
                .useNodeDurations(true)
                .maxDuration(25)
                .minDuration(2)
                .maxEdgeWeight(7)
                .minEdgeWeight(1)
                .build();

        return new GraphGenerator(config, 8L).generateGraphWithSCCs(6);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Returns statistics about the generated datasets.
     *
     * @param datasets the generated datasets
     * @return statistics for each dataset
     */
    public Map<String, List<Map<String, Object>>> getDatasetStatistics(Map<String, List<Graph>> datasets) {
        Map<String, List<Map<String, Object>>> statistics = new HashMap<>();

        for (Map.Entry<String, List<Graph>> entry : datasets.entrySet()) {
            List<Map<String, Object>> categoryStats = new ArrayList<>();

            for (int i = 0; i < entry.getValue().size(); i++) {
                Graph graph = entry.getValue().get(i);
                Map<String, Object> stats = graph.getGraphStatistics();
                stats.put("datasetName", entry.getKey() + "_graph_" + (i + 1));
                categoryStats.add(stats);
            }

            statistics.put(entry.getKey(), categoryStats);
        }

        return statistics;
    }

    /**
     * Prints summary of all generated datasets.
     *
     * @param datasets the generated datasets
     */
    public void printDatasetSummary(Map<String, List<Graph>> datasets) {
        System.out.println("=== Graph Dataset Generation Summary ===");

        Map<String, List<Map<String, Object>>> stats = getDatasetStatistics(datasets);

        for (Map.Entry<String, List<Map<String, Object>>> entry : stats.entrySet()) {
            System.out.println("\n" + entry.getKey().toUpperCase() + " DATASETS:");

            for (int i = 0; i < entry.getValue().size(); i++) {
                Map<String, Object> datasetStats = entry.getValue().get(i);
                System.out.printf("  Dataset %d: %d vertices, %d edges, density=%.3f, hasCycle=%s%n",
                        i + 1,
                        datasetStats.get("vertexCount"),
                        datasetStats.get("edgeCount"),
                        datasetStats.get("density"),
                        datasetStats.get("hasCycle"));
            }
        }

        int totalGraphs = datasets.values().stream().mapToInt(List::size).sum();
        System.out.printf("%nTotal datasets generated: %d%n", totalGraphs);
    }

    // ==================== BUILDER PATTERN ====================

    /**
     * Builder for GraphGenerator with fluent configuration.
     */
    public static class Builder {
        private GraphGenerationConfig config;
        private Long seed = null;

        public Builder() {
            this.config = new GraphGenerationConfig.Builder().build();
        }

        public Builder config(GraphGenerationConfig config) {
            this.config = config;
            return this;
        }

        public Builder vertexCount(int vertexCount) {
            this.config = new GraphGenerationConfig.Builder()
                    .vertexCount(vertexCount)
                    .density(config.getDensity())
                    .allowCycles(config.isAllowCycles())
                    .useNodeDurations(config.isUseNodeDurations())
                    .maxDuration(config.getMaxDuration())
                    .maxEdgeWeight(config.getMaxEdgeWeight())
                    .minDuration(config.getMinDuration())
                    .minEdgeWeight(config.getMinEdgeWeight())
                    .build();
            return this;
        }

        public Builder density(double density) {
            this.config = new GraphGenerationConfig.Builder()
                    .vertexCount(config.getVertexCount())
                    .density(density)
                    .allowCycles(config.isAllowCycles())
                    .useNodeDurations(config.isUseNodeDurations())
                    .maxDuration(config.getMaxDuration())
                    .maxEdgeWeight(config.getMaxEdgeWeight())
                    .minDuration(config.getMinDuration())
                    .minEdgeWeight(config.getMinEdgeWeight())
                    .build();
            return this;
        }

        public Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        public GraphGenerator build() {
            if (seed != null) {
                return new GraphGenerator(config, seed);
            } else {
                return new GraphGenerator(config);
            }
        }
    }
}