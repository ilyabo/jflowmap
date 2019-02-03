package jflowmap.data;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jflowmap.FlowMapGraph;

import org.junit.Before;
import org.junit.Test;

import prefuse.data.Edge;
import prefuse.data.Node;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;


public class FlowMapGraphEdgeAggregatorTest {

  private FlowMapGraph fmg;

  @Before
  public void setup() {
    fmg = FlowMapGraphBuilderTest.buildTestFlowMapGraph();
  }

  @Test
  public void testAggregateBySource() {
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
  }

  public void testAggregateBySource_2() {
    // aggregate by source node
    FlowMapGraph aggregated = FlowMapGraphEdgeAggregator.aggregate(fmg,
        FlowMapGraphEdgeAggregator.GroupFunctions.SRC_NODE);


    List<Edge> aggEdgeList = FlowMapGraphEdgeAggregator.getAggregateList(
        aggregated.edges().iterator().next());
    Edge e1 = aggEdgeList.get(0);
    Edge e2 = aggEdgeList.get(1);
    assertEquals(fmg.getGraph(), e1.getGraph());
    assertEquals(fmg.getGraph(), e2.getGraph());

    assertEquals("2", fmg.getSourceNodeId(e1));
    assertEquals("1", fmg.getTargetNodeId(e1));
    assertEquals("2", fmg.getSourceNodeId(e2));
    assertEquals("4", fmg.getTargetNodeId(e2));
  }


  public void testAggregateBySource_3() {
    // aggregate by source node
    FlowMapGraph aggregated = FlowMapGraphEdgeAggregator.aggregate(fmg,
        FlowMapGraphEdgeAggregator.GroupFunctions.SRC_NODE);

    Iterator<Node> aggNodeIt = aggregated.nodes().iterator();
    Node aggn1 = aggNodeIt.next();
    Node aggn2 = aggNodeIt.next();
    assertEquals("2", aggregated.getNodeId(aggn1));
    assertEquals("1,4", aggregated.getNodeId(aggn2));

    List<Node> aggNodeList1 = FlowMapGraphEdgeAggregator.getAggregateList(aggn1);
    assertEquals(1, aggNodeList1.size());
    assertEquals("2", aggregated.getNodeId(aggNodeList1.get(0)));

    List<Node> aggNodeList2 = FlowMapGraphEdgeAggregator.getAggregateList(aggn2);
    assertEquals(2, aggNodeList2.size());
    assertEquals("1", aggregated.getNodeId(aggNodeList2.get(0)));
    assertEquals("4", aggregated.getNodeId(aggNodeList2.get(1)));
  }


  @Test
  public void testAggregateBySource_aggOfAgg() {
    FlowMapGraph aggAgg = FlowMapGraphEdgeAggregator.aggregate(
        FlowMapGraphEdgeAggregator.aggregate(fmg, FlowMapGraphEdgeAggregator.GroupFunctions.SRC_NODE),
        FlowMapGraphEdgeAggregator.GroupFunctions.MERGE_ALL);

    assertEquals(
        ImmutableSet.of("1,2->1,4,2,3,4: 1200.0"),
        serializeEdges(aggAgg));

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
