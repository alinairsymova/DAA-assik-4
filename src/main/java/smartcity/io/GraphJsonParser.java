package smartcity.io;

import smartcity.model.Graph;
import smartcity.model.Vertex;
import smartcity.model.Edge;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Handles JSON serialization and deserialization for Graph objects.
 * Supports reading/writing graphs to JSON files with proper error handling.
 */
public class GraphJsonParser {
    private final ObjectMapper objectMapper;

    /**
     * Creates a GraphJsonParser with default configuration.
     */
    public GraphJsonParser() {
        this.objectMapper = new ObjectMapper();
        // Configure ObjectMapper if needed
        // objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Creates a GraphJsonParser with custom ObjectMapper.
     *
     * @param objectMapper custom ObjectMapper instance
     */
    public GraphJsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ==================== JSON SERIALIZATION (GRAPH TO JSON) ====================

    /**
     * Converts a Graph object to JSON string.
     *
     * @param graph the graph to convert
     * @return JSON string representation
     * @throws JsonProcessingException if serialization fails
     */
    public String graphToJson(Graph graph) throws JsonProcessingException {
        Map<String, Object> graphData = new HashMap<>();

        // Basic graph properties
        graphData.put("useNodeDurations", graph.useNodeDurations());
        graphData.put("vertexCount", graph.getVertexCount());
        graphData.put("edgeCount", graph.getEdgeCount());

        // Serialize vertices
        graphData.put("vertices", serializeVertices(graph));

        // Serialize edges
        graphData.put("edges", serializeEdges(graph));

        // Add statistics
        graphData.put("statistics", graph.getGraphStatistics());

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(graphData);
    }

    /**
     * Writes a Graph object to a JSON file.
     *
     * @param graph the graph to write
     * @param filePath the output file path
     * @throws IOException if file operations fail
     */
    public void writeGraphToFile(Graph graph, String filePath) throws IOException {
        String json = graphToJson(graph);
        Files.write(Paths.get(filePath), json.getBytes());
    }

    /**
     * Writes multiple graphs to JSON files in a directory.
     *
     * @param graphs list of graphs to write
     * @param directoryPath the output directory path
     * @param baseFileName base name for files
     * @throws IOException if file operations fail
     */
    public void writeGraphsToFiles(List<Graph> graphs, String directoryPath, String baseFileName) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        for (int i = 0; i < graphs.size(); i++) {
            String filePath = directoryPath + File.separator + baseFileName + "_" + (i + 1) + ".json";
            writeGraphToFile(graphs.get(i), filePath);
        }
    }

    /**
     * Writes dataset categories to organized directory structure.
     *
     * @param datasets map of dataset categories to graphs
     * @param baseDirectory the base output directory
     * @throws IOException if file operations fail
     */
    public void writeDatasetsToFiles(Map<String, List<Graph>> datasets, String baseDirectory) throws IOException {
        for (Map.Entry<String, List<Graph>> entry : datasets.entrySet()) {
            String categoryDir = baseDirectory + File.separator + entry.getKey();
            writeGraphsToFiles(entry.getValue(), categoryDir, entry.getKey() + "_graph");
        }
    }

    // ==================== JSON DESERIALIZATION (JSON TO GRAPH) ====================

    /**
     * Converts JSON string to Graph object.
     *
     * @param json the JSON string
     * @return parsed Graph object
     * @throws IOException if parsing fails
     */
    public Graph jsonToGraph(String json) throws IOException {
        JsonNode rootNode = objectMapper.readTree(json);

        // Read graph properties
        boolean useNodeDurations = rootNode.has("useNodeDurations") ?
                rootNode.get("useNodeDurations").asBoolean() : true;

        Graph graph = new Graph(useNodeDurations);

        // Parse vertices
        parseVertices(rootNode, graph);

        // Parse edges
        parseEdges(rootNode, graph);

        return graph;
    }

    /**
     * Reads a Graph object from a JSON file.
     *
     * @param filePath the input file path
     * @return parsed Graph object
     * @throws IOException if file operations or parsing fail
     */
    public Graph readGraphFromFile(String filePath) throws IOException {
        String json = new String(Files.readAllBytes(Paths.get(filePath)));
        return jsonToGraph(json);
    }

    /**
     * Reads multiple graphs from JSON files in a directory.
     *
     * @param directoryPath the input directory path
     * @return list of parsed Graph objects
     * @throws IOException if file operations or parsing fail
     */
    public List<Graph> readGraphsFromDirectory(String directoryPath) throws IOException {
        List<Graph> graphs = new ArrayList<>();
        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Directory does not exist: " + directoryPath);
        }

        File[] jsonFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (jsonFiles != null) {
            Arrays.sort(jsonFiles);
            for (File file : jsonFiles) {
                graphs.add(readGraphFromFile(file.getAbsolutePath()));
            }
        }

        return graphs;
    }

    /**
     * Reads all datasets from organized directory structure.
     *
     * @param baseDirectory the base input directory
     * @return map of dataset categories to graphs
     * @throws IOException if file operations or parsing fail
     */
    public Map<String, List<Graph>> readDatasetsFromDirectory(String baseDirectory) throws IOException {
        Map<String, List<Graph>> datasets = new HashMap<>();
        File baseDir = new File(baseDirectory);

        if (!baseDir.exists() || !baseDir.isDirectory()) {
            throw new IOException("Base directory does not exist: " + baseDirectory);
        }

        File[] categoryDirs = baseDir.listFiles(File::isDirectory);

        if (categoryDirs != null) {
            for (File categoryDir : categoryDirs) {
                String categoryName = categoryDir.getName();
                List<Graph> graphs = readGraphsFromDirectory(categoryDir.getAbsolutePath());
                datasets.put(categoryName, graphs);
            }
        }

        return datasets;
    }

    // ==================== PRIVATE SERIALIZATION METHODS ====================

    private List<Map<String, Object>> serializeVertices(Graph graph) {
        List<Map<String, Object>> verticesJson = new ArrayList<>();

        for (Vertex vertex : graph.getVertices()) {
            Map<String, Object> vertexData = new HashMap<>();
            vertexData.put("id", vertex.getId());
            vertexData.put("name", vertex.getName());
            vertexData.put("duration", vertex.getDuration());
            vertexData.put("type", vertex.getType().name());
            vertexData.put("description", vertex.getDescription());

            verticesJson.add(vertexData);
        }

        return verticesJson;
    }

    private List<Map<String, Object>> serializeEdges(Graph graph) {
        List<Map<String, Object>> edgesJson = new ArrayList<>();

        for (Edge edge : graph.getEdges()) {
            Map<String, Object> edgeData = new HashMap<>();
            edgeData.put("from", edge.getFrom().getId());
            edgeData.put("to", edge.getTo().getId());
            edgeData.put("weight", edge.getWeight());
            edgeData.put("type", edge.getType().name());
            edgeData.put("description", edge.getDescription());

            edgesJson.add(edgeData);
        }

        return edgesJson;
    }

    // ==================== PRIVATE DESERIALIZATION METHODS ====================

    private void parseVertices(JsonNode rootNode, Graph graph) throws IOException {
        JsonNode verticesNode = rootNode.get("vertices");

        if (verticesNode == null || !verticesNode.isArray()) {
            throw new IOException("Invalid JSON: 'vertices' array not found");
        }

        for (JsonNode vertexNode : verticesNode) {
            int id = vertexNode.get("id").asInt();
            String name = vertexNode.get("name").asText();
            int duration = vertexNode.has("duration") ? vertexNode.get("duration").asInt() : 0;

            Vertex.VertexType type = Vertex.VertexType.OTHER;
            if (vertexNode.has("type")) {
                try {
                    type = Vertex.VertexType.valueOf(vertexNode.get("type").asText());
                } catch (IllegalArgumentException e) {
                    // Use default type if unknown
                }
            }

            String description = vertexNode.has("description") ?
                    vertexNode.get("description").asText() : "";

            Vertex vertex = new Vertex(id, name, duration, type, description);
            graph.addVertex(vertex);
        }
    }

    private void parseEdges(JsonNode rootNode, Graph graph) throws IOException {
        JsonNode edgesNode = rootNode.get("edges");

        if (edgesNode == null || !edgesNode.isArray()) {
            throw new IOException("Invalid JSON: 'edges' array not found");
        }

        for (JsonNode edgeNode : edgesNode) {
            int fromId = edgeNode.get("from").asInt();
            int toId = edgeNode.get("to").asInt();
            int weight = edgeNode.has("weight") ? edgeNode.get("weight").asInt() : 0;

            Edge.EdgeType type = Edge.EdgeType.OTHER;
            if (edgeNode.has("type")) {
                try {
                    type = Edge.EdgeType.valueOf(edgeNode.get("type").asText());
                } catch (IllegalArgumentException e) {
                    // Use default type if unknown
                }
            }

            String description = edgeNode.has("description") ?
                    edgeNode.get("description").asText() : "";

            // Create edge using builder pattern
            Edge edge = new Edge.Builder(fromId, toId, graph)
                    .weight(weight)
                    .type(type)
                    .description(description)
                    .build();

            graph.addEdge(edge);
        }
    }

    // ==================== VALIDATION AND UTILITY METHODS ====================

    /**
     * Validates if a string contains valid JSON for graph data.
     *
     * @param json the JSON string to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidGraphJson(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            return rootNode.has("vertices") && rootNode.has("edges");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates if a file contains valid graph JSON.
     *
     * @param filePath the file to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidGraphFile(String filePath) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(filePath)));
            return isValidGraphJson(json);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Creates a sample JSON template for graph data.
     *
     * @return sample JSON template
     * @throws JsonProcessingException if serialization fails
     */
    public String createJsonTemplate() throws JsonProcessingException {
        Map<String, Object> template = new HashMap<>();
        template.put("useNodeDurations", true);

        // Sample vertices
        List<Map<String, Object>> vertices = new ArrayList<>();
        vertices.add(createVertexTemplate(0, "Street Cleaning: Downtown", 5));
        vertices.add(createVertexTemplate(1, "Repairs: City Center", 3));
        template.put("vertices", vertices);

        // Sample edges
        List<Map<String, Object>> edges = new ArrayList<>();
        edges.add(createEdgeTemplate(0, 1, 2));
        template.put("edges", edges);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(template);
    }

    private Map<String, Object> createVertexTemplate(int id, String name, int duration) {
        Map<String, Object> vertex = new HashMap<>();
        vertex.put("id", id);
        vertex.put("name", name);
        vertex.put("duration", duration);
        vertex.put("type", "STREET_CLEANING");
        vertex.put("description", "Sample task description");
        return vertex;
    }

    private Map<String, Object> createEdgeTemplate(int from, int to, int weight) {
        Map<String, Object> edge = new HashMap<>();
        edge.put("from", from);
        edge.put("to", to);
        edge.put("weight", weight);
        edge.put("type", "TASK_DEPENDENCY");
        edge.put("description", "Sample dependency");
        return edge;
    }

    // ==================== BATCH OPERATIONS ====================

    /**
     * Converts multiple graphs to JSON strings.
     *
     * @param graphs list of graphs to convert
     * @return list of JSON strings
     */
    public List<String> graphsToJson(List<Graph> graphs) {
        List<String> jsonStrings = new ArrayList<>();

        for (Graph graph : graphs) {
            try {
                jsonStrings.add(graphToJson(graph));
            } catch (JsonProcessingException e) {
                // Add error message for failed conversion
                jsonStrings.add("{\"error\": \"Failed to serialize graph: " + e.getMessage() + "\"}");
            }
        }

        return jsonStrings;
    }

    /**
     * Creates a dataset manifest file with information about all generated graphs.
     *
     * @param datasets the datasets to document
     * @param manifestFilePath the output manifest file path
     * @throws IOException if file operations fail
     */
    public void createDatasetManifest(Map<String, List<Graph>> datasets, String manifestFilePath) throws IOException {
        Map<String, Object> manifest = new HashMap<>();
        manifest.put("generatedAt", new Date().toString());
        manifest.put("totalCategories", datasets.size());

        Map<String, Object> categories = new HashMap<>();

        for (Map.Entry<String, List<Graph>> entry : datasets.entrySet()) {
            List<Map<String, Object>> categoryInfo = new ArrayList<>();

            for (int i = 0; i < entry.getValue().size(); i++) {
                Graph graph = entry.getValue().get(i);
                Map<String, Object> graphInfo = graph.getGraphStatistics();
                graphInfo.put("fileName", entry.getKey() + "_graph_" + (i + 1) + ".json");
                categoryInfo.add(graphInfo);
            }

            categories.put(entry.getKey(), categoryInfo);
        }

        manifest.put("categories", categories);

        String manifestJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(manifest);
        Files.write(Paths.get(manifestFilePath), manifestJson.getBytes());
    }

    // ==================== ERROR HANDLING AND LOGGING ====================

    /**
     * Exception class for graph JSON parsing errors.
     */
    public static class GraphJsonException extends IOException {
        public GraphJsonException(String message) {
            super(message);
        }

        public GraphJsonException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Attempts to read a graph file with detailed error reporting.
     *
     * @param filePath the file to read
     * @return ParseResult containing the graph or error information
     */
    public ParseResult readGraphWithDiagnostics(String filePath) {
        try {
            Graph graph = readGraphFromFile(filePath);
            return new ParseResult(graph, null, filePath);
        } catch (Exception e) {
            return new ParseResult(null, e.getMessage(), filePath);
        }
    }

    /**
     * Result container for parse operations with diagnostics.
     */
    public static class ParseResult {
        private final Graph graph;
        private final String error;
        private final String filePath;

        public ParseResult(Graph graph, String error, String filePath) {
            this.graph = graph;
            this.error = error;
            this.filePath = filePath;
        }

        public boolean isSuccess() { return graph != null; }
        public Graph getGraph() { return graph; }
        public String getError() { return error; }
        public String getFilePath() { return filePath; }
    }

    // ==================== BUILDER PATTERN ====================

    /**
     * Builder for GraphJsonParser with optional configuration.
     */
    public static class Builder {
        private ObjectMapper objectMapper;
        private boolean prettyPrint = true;

        public Builder() {
            this.objectMapper = new ObjectMapper();
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public Builder prettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
            return this;
        }

        public GraphJsonParser build() {
            if (prettyPrint) {
                objectMapper.writerWithDefaultPrettyPrinter();
            }
            return new GraphJsonParser(objectMapper);
        }
    }
}