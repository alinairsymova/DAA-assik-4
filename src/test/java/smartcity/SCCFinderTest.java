package smartcity;

import smartcity.algorithms.SCCFinder;
import smartcity.model.Graph;
import smartcity.model.Vertex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SCCFinderTest {
    private Graph graphWithCycles;
    private Graph dag;

    @BeforeEach
    void setUp() {
        graphWithCycles = new Graph(true);
        createGraphWithMultipleSCCs();

        dag = new Graph(true);
        createDAG();
    }

    private void createGraphWithMultipleSCCs() {
        graphWithCycles.addVertex(0, "A", 1);
        graphWithCycles.addVertex(1, "B", 2);
        graphWithCycles.addVertex(2, "C", 1);
        graphWithCycles.addVertex(3, "D", 3);
        graphWithCycles.addVertex(4, "E", 2);

        graphWithCycles.addEdge(0, 1);
        graphWithCycles.addEdge(1, 0);
        graphWithCycles.addEdge(2, 3);
        graphWithCycles.addEdge(3, 4);
        graphWithCycles.addEdge(4, 2);
        graphWithCycles.addEdge(1, 2);
    }

    private void createDAG() {
        for (int i = 0; i < 5; i++) {
            dag.addVertex(i, "Task " + i, i + 1);
        }
        dag.addEdge(0, 1);
        dag.addEdge(0, 2);
        dag.addEdge(1, 3);
        dag.addEdge(2, 3);
        dag.addEdge(3, 4);
    }

    @Test
    void testConstructor() {
        assertDoesNotThrow(() -> new SCCFinder(graphWithCycles));
    }

    @Test
    void testFindSCCsTarjan() {
        SCCFinder finder = new SCCFinder(graphWithCycles);
        List<List<Vertex>> components = finder.findSCCsTarjan();

        assertNotNull(components);
        assertTrue(components.size() >= 2);
    }

    @Test
    void testFindSCCsKosaraju() {
        SCCFinder finder = new SCCFinder(graphWithCycles);
        List<List<Vertex>> components = finder.findSCCsKosaraju();

        assertNotNull(components);
        assertTrue(components.size() >= 2);
    }

    @Test
    void testBuildCondensationGraph() {
        SCCFinder finder = new SCCFinder(graphWithCycles);
        finder.findSCCsTarjan();
        Graph condensation = finder.buildCondensationGraph();

        assertNotNull(condensation);
        assertFalse(condensation.hasCycle());
    }

    @Test
    void testDAGHasTrivialSCCs() {
        SCCFinder finder = new SCCFinder(dag);
        List<List<Vertex>> components = finder.findSCCsTarjan();

        assertEquals(5, components.size());
        for (List<Vertex> component : components) {
            assertEquals(1, component.size());
        }
    }

    @Test
    void testSCCStatistics() {
        SCCFinder finder = new SCCFinder(graphWithCycles);
        finder.findSCCsTarjan();
        Map<String, Object> stats = finder.getSCCStatistics();

        assertNotNull(stats);
        assertTrue(stats.containsKey("totalComponents"));
        assertTrue(stats.containsKey("executionTimeNs"));
    }
}