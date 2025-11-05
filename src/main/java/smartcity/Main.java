package smartcity;

import smartcity.model.Graph;
import smartcity.model.Vertex;
import smartcity.model.Edge;
import smartcity.generator.GraphGenerator;
import smartcity.io.GraphJsonParser;
import smartcity.algorithms.SCCFinder;
import smartcity.algorithms.TopologicalSort;
import smartcity.algorithms.DAGShortestPath;
import smartcity.metrics.Metrics;
import java.util.*;
import java.io.IOException;

/**
 * Main application class for Smart City Scheduling System.
 * Demonstrates all graph algorithms on generated datasets.
 *
 * @author Your Name
 * @version 1.0
 * @since 2024
 */
public class Main {
    private static final Metrics metrics = new Metrics();

    public static void main(String[] args) {
        System.out.println("=== Smart City Scheduling System ===");
        System.out.println("Starting comprehensive graph algorithms demonstration...\n");

        try {
            if (args.length > 0) {
                // Process file if provided
                processFile(args[0]);
            } else {
                // Generate and analyze datasets
                demonstrateCompleteWorkflow();
            }

            System.out.println("\n=== Application Completed Successfully ===");

        } catch (Exception e) {
            System.err.println("Error in application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== COMPLETE WORKFLOW DEMONSTRATION ====================

    private static void demonstrateCompleteWorkflow() {
        System.out.println("Generating and analyzing all datasets...\n");

        // Step 1: Generate datasets
        Map<String, List<Graph>> datasets = generateDatasets();

        // Step 2: Analyze each dataset category
        for (Map.Entry<String, List<Graph>> entry : datasets.entrySet()) {
            String category = entry.getKey();
            List<Graph> graphs = entry.getValue();

            System.out.println("=== ANALYZING " + category.toUpperCase() + " DATASETS ===");

            for (int i = 0; i < graphs.size(); i++) {
                System.out.printf("\n--- %s Graph %d ---%n", category, i + 1);
                analyzeGraph(graphs.get(i), category + "_graph_" + (i + 1));
            }
        }

        // Step 3: Print final metrics report
        printFinalReport();
    }

    // ==================== DATASET GENERATION ====================

    private static Map<String, List<Graph>> generateDatasets() {
        System.out.println("Step 1: Generating datasets...");

        GraphGenerator generator = new GraphGenerator();
        Map<String, List<Graph>> datasets = generator.generateAssignmentDatasets();

        // Save datasets to files
        try {
            GraphJsonParser parser = new GraphJsonParser();
            parser.writeDatasetsToFiles(datasets, "data");
            parser.createDatasetManifest(datasets, "data/manifest.json");
            System.out.println("✓ Datasets saved to 'data' directory");
        } catch (IOException e) {
            System.err.println("Warning: Could not save datasets to files: " + e.getMessage());
        }

        // Print dataset summary
        generator.printDatasetSummary(datasets);

        return datasets;
    }

    // ==================== GRAPH ANALYSIS ====================

    private static void analyzeGraph(Graph graph, String graphName) {
        metrics.incrementCounter("graphsAnalyzed");

        System.out.println("Graph: " + graphName);
        System.out.println("Vertices: " + graph.getVertexCount() + ", Edges: " + graph.getEdgeCount());

        // 1. Basic graph analysis
        analyzeGraphProperties(graph);

        // 2. SCC Analysis (if graph has cycles)
        if (graph.hasCycle()) {
            analyzeSCC(graph);
        } else {
            System.out.println("✓ Graph is acyclic (DAG)");
        }

        // 3. Topological Sort (if acyclic or after SCC condensation)
        analyzeTopologicalSort(graph);

        // 4. DAG Shortest Paths (if acyclic or using condensation graph)
        analyzeDAGShortestPaths(graph);

        System.out.println(); // Empty line for readability
    }

    private static void analyzeGraphProperties(Graph graph) {
        Map<String, Object> stats = graph.getGraphStatistics();
        System.out.printf("Properties: density=%.3f, connected=%s, hasCycle=%s%n",
                stats.get("density"), stats.get("isConnected"), stats.get("hasCycle"));
    }

    private static void analyzeSCC(Graph graph) {
        System.out.println("\n1. STRONGLY CONNECTED COMPONENTS ANALYSIS:");

        try {
            metrics.startTimer("sccAnalysis");

            SCCFinder sccFinder = new SCCFinder(graph);

            // Use Tarjan's algorithm
            List<List<Vertex>> components = sccFinder.findSCCsTarjan();
            Map<String, Object> sccStats = sccFinder.getSCCStatistics();

            metrics.stopTimer("sccAnalysis");
            metrics.recordSCCMetrics(
                    (Long) sccStats.get("dfsVisits"),
                    (Long) sccStats.get("edgesTraversed"),
                    components.size(),
                    metrics.getElapsedTime("sccAnalysis")
            );

            System.out.printf("✓ Found %d SCC components%n", components.size());
            System.out.printf("  Largest: %d, Smallest: %d, Average: %.1f%n",
                    sccStats.get("largestComponent"),
                    sccStats.get("smallestComponent"),
                    sccStats.get("averageComponentSize"));
            System.out.printf("  Execution: %.3f ms, DFS visits: %d%n",
                    metrics.getElapsedTimeMillis("sccAnalysis"),
                    sccStats.get("dfsVisits"));

            // Build and analyze condensation graph
            if (components.size() > 1) {
                Graph condensationGraph = sccFinder.buildCondensationGraph();
                System.out.printf("✓ Condensation graph: %d vertices, %d edges%n",
                        condensationGraph.getVertexCount(),
                        condensationGraph.getEdgeCount());

                // Store for later use
                metrics.setCustomMetric("condensationGraph", condensationGraph);
                metrics.setCustomMetric("sccFinder", sccFinder);
            }

        } catch (Exception e) {
            System.err.println("✗ SCC analysis failed: " + e.getMessage());
        }
    }

    private static void analyzeTopologicalSort(Graph graph) {
        System.out.println("\n2. TOPOLOGICAL SORT ANALYSIS:");

        try {
            metrics.startTimer("topologicalAnalysis");

            TopologicalSort topoSorter = new TopologicalSort(graph);

            if (graph.hasCycle()) {
                // Use condensation graph for cyclic graphs
                SCCFinder sccFinder = (SCCFinder) metrics.getCustomMetric("sccFinder");
                if (sccFinder != null) {
                    TopologicalSort.TopologicalResult result =
                            topoSorter.completeTopologicalProcessing(sccFinder);

                    metrics.stopTimer("topologicalAnalysis");
                    metrics.recordTopoMetrics(
                            (long) result.getOperationsCount(),                            0, 0, // pops/pushes not tracked in this method
                            result.getExecutionTime()
                    );

                    System.out.printf("✓ Derived topological order from SCC condensation%n");
                    System.out.printf("  Components: %d, Original vertices: %d%n",
                            result.getComponentCount(), result.getOriginalVertexCount());
                    System.out.printf("  Execution: %.3f ms%n",
                            result.getExecutionTime() / 1_000_000.0);
                }
            } else {
                // Direct topological sort for DAGs
                List<Vertex> topoOrder = topoSorter.topologicalSortKahn();

                metrics.stopTimer("topologicalAnalysis");
                metrics.recordTopoMetrics(
                        ((Number) topoSorter.getMetrics().get("operationsCount")).longValue(),  // Fixed: get long value
                        metrics.getCounter(Metrics.KAHN_POPS),
                        metrics.getCounter(Metrics.KAHN_PUSHES),
                        metrics.getElapsedTime("topologicalAnalysis")
                );

                System.out.printf("✓ Valid topological order found (%d vertices)%n", topoOrder.size());
                System.out.printf("  Execution: %.3f ms%n",
                        metrics.getElapsedTimeMillis("topologicalAnalysis"));

                // Analyze levels
                Map<Integer, Integer> levels = topoSorter.computeLevels(topoOrder);
                int maxLevel = levels.values().stream().max(Integer::compareTo).orElse(0);
                System.out.printf("  Maximum level: %d%n", maxLevel);
            }

        } catch (Exception e) {
            System.err.println("✗ Topological sort failed: " + e.getMessage());
        }
    }

    private static void analyzeDAGShortestPaths(Graph graph) {
        System.out.println("\n3. DAG SHORTEST PATHS ANALYSIS:");

        try {
            Graph dagToAnalyze = graph;

            // If original graph has cycles, use condensation graph
            if (graph.hasCycle()) {
                Graph condensationGraph = (Graph) metrics.getCustomMetric("condensationGraph");
                if (condensationGraph != null) {
                    dagToAnalyze = condensationGraph;
                    System.out.println("  Using condensation graph (DAG) for analysis");
                } else {
                    System.out.println("  Skipping - graph has cycles and no condensation available");
                    return;
                }
            }

            metrics.startTimer("dagspAnalysis");

            DAGShortestPath dagsp = new DAGShortestPath(dagToAnalyze);

            // Find critical path (longest path)
            DAGShortestPath.CriticalPathResult criticalPath = dagsp.findCriticalPath();

            metrics.stopTimer("dagspAnalysis");
            metrics.recordDAGSPMetrics(
                    criticalPath.getRelaxations(),
                    criticalPath.getTopologicalOperations(),
                    1, // path reconstruction
                    criticalPath.getExecutionTime()
            );

            System.out.printf("✓ Critical path analysis completed%n");
            System.out.printf("  Critical path length: %d, Path size: %d vertices%n",
                    criticalPath.getLength(), criticalPath.getPathSize());
            System.out.printf("  Source: %d, Sink: %d%n",
                    criticalPath.getSourceId(), criticalPath.getSinkId());
            System.out.printf("  Execution: %.3f ms, Relaxations: %d%n",
                    criticalPath.getExecutionTime() / 1_000_000.0,
                    criticalPath.getRelaxations());

            // Additional: Single-source shortest paths from a source
            if (dagToAnalyze.getVertexCount() > 0) {
                List<Vertex> sources = new TopologicalSort(dagToAnalyze).findSourceVertices();
                if (!sources.isEmpty()) {
                    int sourceId = sources.get(0).getId();
                    DAGShortestPath.ShortestPathResult sssp =
                            dagsp.singleSourceShortestPathWithInfo(sourceId);

                    System.out.printf("  Single-source shortest paths from vertex %d%n", sourceId);
                    System.out.printf("  Reachable vertices: %d/%d%n",
                            countReachableVertices(sssp), dagToAnalyze.getVertexCount());
                }
            }

        } catch (Exception e) {
            System.err.println("✗ DAG shortest paths analysis failed: " + e.getMessage());
        }
    }

    // ==================== FILE PROCESSING ====================

    private static void processFile(String filePath) {
        System.out.println("Processing file: " + filePath);

        try {
            GraphJsonParser parser = new GraphJsonParser();

            if (parser.isValidGraphFile(filePath)) {
                Graph graph = parser.readGraphFromFile(filePath);
                System.out.printf("✓ Successfully loaded graph: %d vertices, %d edges%n",
                        graph.getVertexCount(), graph.getEdgeCount());

                // Analyze the loaded graph
                analyzeGraph(graph, "loaded_from_file");

            } else {
                System.err.println("✗ Invalid graph file: " + filePath);
            }

        } catch (IOException e) {
            System.err.println("✗ Error processing file: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================

    private static int countReachableVertices(DAGShortestPath.ShortestPathResult result) {
        int reachable = 0;
        for (int vertexId : result.getDistances().keySet()) {
            if (result.isReachable(vertexId)) {
                reachable++;
            }
        }
        return reachable;
    }

    private static void printFinalReport() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("FINAL PERFORMANCE REPORT");
        System.out.println("=".repeat(50));

        metrics.printMetricsReport();

        // Additional summary
        System.out.println("\nSUMMARY:");
        System.out.printf("Total graphs analyzed: %d%n", metrics.getCounter("graphsAnalyzed"));
        System.out.printf("Average SCC time: %.3f ms%n",
                metrics.getElapsedTimeMillis("sccAnalysis"));
        System.out.printf("Average topological sort time: %.3f ms%n",
                metrics.getElapsedTimeMillis("topologicalAnalysis"));
        System.out.printf("Average DAG SP time: %.3f ms%n",
                metrics.getElapsedTimeMillis("dagspAnalysis"));

        // Memory usage
        Map<String, Object> memoryStats = metrics.getMemoryStats();
        System.out.printf("Peak memory usage: %.1f MB%n", memoryStats.get("usedMemoryMB"));
    }

    // ==================== DEMONSTRATION METHODS ====================

    /**
     * Runs a quick demonstration on a small generated graph.
     * Useful for testing and debugging.
     */
    public static void runQuickDemo() {
        System.out.println("=== QUICK DEMONSTRATION ===");

        // Generate a small test graph
        GraphGenerator generator = new GraphGenerator();
        Graph testGraph = generator.generateGraphWithSCCs(2);

        System.out.printf("Test graph: %d vertices, %d edges%n",
                testGraph.getVertexCount(), testGraph.getEdgeCount());

        // Run all analyses
        analyzeGraph(testGraph, "quick_demo");
    }

    /**
     * Runs performance comparison between algorithms.
     */
    public static void runPerformanceComparison() {
        System.out.println("=== PERFORMANCE COMPARISON ===");

        GraphGenerator generator = new GraphGenerator();
        Metrics comparisonMetrics = new Metrics();

        // Test on different graph sizes
        int[] sizes = {10, 20, 30};

        for (int size : sizes) {
            System.out.printf("\nTesting with %d vertices:%n", size);

            Graph graph = new GraphGenerator.Builder()
                    .vertexCount(size)
                    .density(0.3)
                    .build()
                    .generateRandomGraph();

            comparisonMetrics.startTimer("scc_" + size);
            SCCFinder sccFinder = new SCCFinder(graph);
            sccFinder.findSCCsTarjan();
            comparisonMetrics.stopTimer("scc_" + size);

            comparisonMetrics.startTimer("topo_" + size);
            TopologicalSort topo = new TopologicalSort(graph);
            if (!graph.hasCycle()) {
                topo.topologicalSortKahn();
            }
            comparisonMetrics.stopTimer("topo_" + size);

            System.out.printf("  SCC: %.3f ms, Topo: %.3f ms%n",
                    comparisonMetrics.getElapsedTimeMillis("scc_" + size),
                    comparisonMetrics.getElapsedTimeMillis("topo_" + size));
        }
    }
}