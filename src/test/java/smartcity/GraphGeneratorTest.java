package smartcity;

import smartcity.generator.GraphGenerator;
import smartcity.model.Graph;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GraphGeneratorTest {

    @Test
    void testDefaultConstructor() {
        GraphGenerator generator = new GraphGenerator();
        assertNotNull(generator);
    }

    @Test
    void testGenerateRandomGraph() {
        GraphGenerator generator = new GraphGenerator();
        Graph graph = generator.generateRandomGraph();

        assertNotNull(graph);
        assertTrue(graph.getVertexCount() > 0);
    }

    @Test
    void testGenerateDAG() {
        GraphGenerator generator = new GraphGenerator();
        Graph dag = generator.generateDAG();

        assertNotNull(dag);
        assertFalse(dag.hasCycle());
    }

    @Test
    void testGenerateGraphWithSCCs() {
        GraphGenerator generator = new GraphGenerator();
        Graph graph = generator.generateGraphWithSCCs(3);

        assertNotNull(graph);
        // The graph should have multiple components
        assertTrue(graph.getVertexCount() >= 3);
    }

    @Test
    void testGenerateAssignmentDatasets() {
        GraphGenerator generator = new GraphGenerator();
        Map<String, List<Graph>> datasets = generator.generateAssignmentDatasets();

        assertNotNull(datasets);
        assertTrue(datasets.containsKey("small"));
        assertTrue(datasets.containsKey("medium"));
        assertTrue(datasets.containsKey("large"));

        assertEquals(3, datasets.get("small").size());
        assertEquals(3, datasets.get("medium").size());
        assertEquals(3, datasets.get("large").size());
    }

    @Test
    void testGraphGenerationConfig() {
        GraphGenerator.GraphGenerationConfig config =
                new GraphGenerator.GraphGenerationConfig.Builder()
                        .vertexCount(20)
                        .density(0.7)
                        .allowCycles(false)
                        .build();

        assertEquals(20, config.getVertexCount());
        assertEquals(0.7, config.getDensity(), 0.001);
        assertFalse(config.isAllowCycles());
    }
}