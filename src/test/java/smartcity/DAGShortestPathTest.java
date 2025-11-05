package smartcity;

import smartcity.algorithms.DAGShortestPath;
import smartcity.model.Graph;
import smartcity.model.Vertex;
import smartcity.model.Edge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DAGShortestPathTest {
    private Graph dag;

    @BeforeEach
    void setUp() {
        dag = new Graph(true);

        dag.addVertex(0, "Task A", 2);
        dag.addVertex(1, "Task B", 3);
        dag.addVertex(2, "Task C", 1);
        dag.addVertex(3, "Task D", 4);
        dag.addVertex(4, "Task E", 2);

        dag.addEdge(0, 1, 1);
        dag.addEdge(0, 2, 2);
        dag.addEdge(1, 3, 1);
        dag.addEdge(2, 3, 3);
        dag.addEdge(3, 4, 1);
    }

    @Test
    void testConstructorWithValidDAG() {
        assertDoesNotThrow(() -> new DAGShortestPath(dag));
    }

    @Test
    void testTopologicalSortKahn() {
        DAGShortestPath solver = new DAGShortestPath(dag);
        List<Vertex> topoOrder = solver.topologicalSortKahn();

        assertNotNull(topoOrder);
        assertEquals(dag.getVertexCount(), topoOrder.size());
        assertValidTopologicalOrder(topoOrder);
    }

    @Test
    void testSingleSourceShortestPath() {
        DAGShortestPath solver = new DAGShortestPath(dag);
        Map<Integer, Integer> distances = solver.singleSourceShortestPath(0);

        assertNotNull(distances);
        assertEquals(0, distances.get(0).intValue());
        assertTrue(distances.get(4) > 0);
    }

    @Test
    void testFindCriticalPath() {
        DAGShortestPath solver = new DAGShortestPath(dag);
        DAGShortestPath.CriticalPathResult result = solver.findCriticalPath();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.getLength() >= 0);
        assertTrue(result.getPathSize() > 0);
    }

    private void assertValidTopologicalOrder(List<Vertex> order) {
        Map<Integer, Integer> position = new HashMap<>();
        for (int i = 0; i < order.size(); i++) {
            position.put(order.get(i).getId(), i);
        }

        for (Edge edge : dag.getEdges()) {
            int fromPos = position.get(edge.getFrom().getId());
            int toPos = position.get(edge.getTo().getId());
            assertTrue(fromPos < toPos);
        }
    }
}