package jflowmap.data;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapGraph;

import org.junit.Test;

import prefuse.data.Edge;
import prefuse.data.Node;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;


public class FlowMapGraphEdgeAggregatorTest {

  @Test
  public void testAggregateBySource() {
    FlowMapGraphBuilder builder =
      new FlowMapGraphBuilder("testGraph", new FlowMapAttrSpec("value", "label"));

    Node node1 = builder.addNode("1", "Node1");
    Node node2 = builder.addNode("2", "Node2");
    Node node3 = builder.addNode("3", "Node3");
    Node node4 = builder.addNode("4", "Node4");

    builder.addEdge(node1, node2, 200);
    builder.addEdge(node1, node3, 300);
    builder.addEdge(node1, node4, 400);

    builder.addEdge(node2, node1, 100);
    builder.addEdge(node2, node4, 200);

    final FlowMapGraph fmg = builder.build();

    // aggregate by source node
    FlowMapGraph aggregated = FlowMapGraphEdgeAggregator.aggregate(fmg,
        new Function<Edge, Object>() {
          @Override
          public Object apply(Edge edge) {
            return fmg.getNodeId(edge.getSourceNode());
          }
        });

    assertEquals(4, Iterables.size(aggregated.nodes()));
    assertEquals(2, Iterables.size(aggregated.edges()));

    assertEquals(
        ImmutableSet.of("2->1,4: 300.0", "1->2,3,4: 900.0"),
        serializeEdges(aggregated));
    assertEquals(
        ImmutableSet.of("2","1","2,3,4","1,4"),
        serializeNodes(aggregated));
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
