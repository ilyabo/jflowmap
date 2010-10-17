package jflowmap.data;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import jflowmap.FlowMapGraph;

import org.junit.Test;

import prefuse.data.Edge;
import prefuse.data.Node;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;


public class FlowMapGraphEdgeAggregatorTest {

  @Test
  public void testAggregateBySource() {
    final FlowMapGraph fmg = FlowMapGraphBuilderTest.buildTestFlowMapGraph();

    // aggregate by source node
    FlowMapGraph aggregated = FlowMapGraphEdgeAggregator.aggregate(fmg,
        FlowMapGraphEdgeAggregator.GroupFunctions.SRC_NODE);

    assertEquals(4, Iterables.size(aggregated.nodes()));
    assertEquals(2, Iterables.size(aggregated.edges()));

    assertEquals(
        ImmutableSet.of("2->1,4: 300.0", "1->2,3,4: 900.0"),
        serializeEdges(aggregated));
    assertEquals(
        ImmutableSet.of("2","1","2,3,4","1,4"),
        serializeNodes(aggregated));


    List<Edge> aggList = FlowMapGraphEdgeAggregator.getAggregateList(
        aggregated.edges().iterator().next());
    Edge e1 = aggList.get(0);
    Edge e2 = aggList.get(1);
    assertEquals(fmg.getGraph(), e1.getGraph());
    assertEquals(fmg.getGraph(), e2.getGraph());

    assertEquals("2", fmg.getSourceNodeId(e1));
    assertEquals("1", fmg.getTargetNodeId(e1));
    assertEquals("2", fmg.getSourceNodeId(e2));
    assertEquals("4", fmg.getTargetNodeId(e2));
  }


  private Set<String> serializeNodes(FlowMapGraph fmg) {
    Set<String> set = Sets.newHashSet();
    for (Node node : fmg.nodes()) {
      set.add(fmg.getNodeId(node));
    }
    return set;
  }

  private Set<String> serializeEdges(FlowMapGraph fmg) {
    Set<String> set = Sets.newHashSet();
    for (Edge edge : fmg.edges()) {
      set.add(
          fmg.getSourceNodeId(edge) + "->" + fmg.getTargetNodeId(edge) + ": " +
          fmg.getEdgeWeight(edge, "value")
      );
    }
    return set;
  }
}
