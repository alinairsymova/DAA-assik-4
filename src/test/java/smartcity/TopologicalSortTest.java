package smartcity;

import smartcity.algorithms.TopologicalSort;
import smartcity.model.Graph;
import smartcity.model.Vertex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TopologicalSortTest {
    private Graph dag;
    private Graph cyclicGraph;

    @BeforeEach
    void setUp() {
        dag = new Graph(true);
        createDAG();

        cyclicGraph = new Graph(true);
        createCyclicGraph();
    }

    private void createDAG() {
        dag.addVertex(0, "Task A", 2);
        dag.addVertex(1, "Task B", 3);
        dag.addVertex(2, "Task C", 1);
        dag.addVertex(3, "Task D", 4);
        dag.addVertex(4, "Task E", 2);

        dag.addEdge(0, 1);
        dag.addEdge(0, 2);
        dag.addEdge(1, 3);
        dag.addEdge(2, 3);
        dag.addEdge(3, 4);
    }

    private void createCyclicGraph() {
        cyclicGraph.addVertex(0, "X", 1);
        cyclicGraph.addVertex(1, "Y", 2);
        cyclicGraph.addVertex(2, "Z", 1);

        cyclicGraph.addEdge(0, 1);
        cyclicGraph.addEdge(1, 2);
        cyclicGraph.addEdge(2, 0);
    }

    @Test
    void testConstructor() {
        assertDoesNotThrow(() -> new TopologicalSort(dag));
    }

    @Test
    void testTopologicalSortKahn() {
        TopologicalSort sorter = new TopologicalSort(dag);
        List<Vertex> order = sorter.topologicalSortKahn();

        assertNotNull(order);
        assertEquals(dag.getVertexCount(), order.size());
        assertValidTopologicalOrder(order, dag);
    }

    @Test
    void testTopologicalSortDFS() {
        TopologicalSort sorter = new TopologicalSort(dag);
        List<Vertex> order = sorter.topologicalSortDFS();

        assertNotNull(order);
        assertEquals(dag.getVertexCount(), order.size());
        assertValidTopologicalOrder(order, dag);
    }

    @Test
    void testTopologicalSortOnCyclicGraph() {
        TopologicalSort sorter = new TopologicalSort(cyclicGraph);
        assertThrows(IllegalStateException.class, () -> sorter.topologicalSortKahn());
    }

    @Test
    void testIsDAG() {
        TopologicalSort dagSorter = new TopologicalSort(dag);
        assertTrue(dagSorter.isDAG());

        TopologicalSort cyclicSorter = new TopologicalSort(cyclicGraph);
        assertFalse(cyclicSorter.isDAG());
    }

    @Test
    void testFindSourceVertices() {
        TopologicalSort sorter = new TopologicalSort(dag);
        List<Vertex> sources = sorter.findSourceVertices();

        assertNotNull(sources);
        assertEquals(1, sources.size());
        assertEquals(0, sources.get(0).getId());
    }

    @Test
    void testFindSinkVertices() {
        TopologicalSort sorter = new TopologicalSort(dag);
        List<Vertex> sinks = sorter.findSinkVertices();

        assertNotNull(sinks);
        assertEquals(1, sinks.size());
        assertEquals(4, sinks.get(0).getId());
    }

    @Test
    void testComputeLevels() {
        TopologicalSort sorter = new TopologicalSort(dag);
        List<Vertex> order = sorter.topologicalSortKahn();
        Map<Integer, Integer> levels = sorter.computeLevels(order);

        assertNotNull(levels);
        assertEquals(dag.getVertexCount(), levels.size());
        assertEquals(0, levels.get(0).intValue());
        assertTrue(levels.get(4) > 0);
    }

    @Test
    void testMetricsCollection() {
        TopologicalSort sorter = new TopologicalSort(dag);
        sorter.topologicalSortKahn();

        Map<String, Object> metrics = sorter.getMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("executionTimeNs"));
        assertTrue(metrics.containsKey("operationsCount"));
    }

    private void assertValidTopologicalOrder(List<Vertex> order, Graph graph) {
        Map<Integer, Integer> position = new HashMap<>();
        for (int i = 0; i < order.size(); i++) {
            position.put(order.get(i).getId(), i);
        }

        for (smartcity.model.Edge edge : graph.getEdges()) {
            int fromPos = position.get(edge.getFrom().getId());
            int toPos = position.get(edge.getTo().getId());
            assertTrue(fromPos < toPos);
        }
    }
}