package jflowmap.data;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapGraph;

import org.junit.Test;

import prefuse.data.Node;

/**
 * @author Ilya Boyandin
 */
public class FlowMapGraphBuilderTest {


  public static FlowMapGraph buildTestFlowMapGraph() {
    FlowMapGraphBuilder builder =
      new FlowMapGraphBuilder("testGraph", new FlowMapAttrSpec(
          "flowSrcNodeAttr",
          "flowTargetNodeAttr",
          "legendCaption",
          Arrays.asList("value"), "label",
          "nodeLabelAttr",
          null,null));

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
    return fmg;
  }

  @Test
  public void testBuild() {
    FlowMapGraph fmg = buildTestFlowMapGraph();
    assertEquals(200.0, fmg.getEdgeWeight(fmg.getGraph().getEdge(0), "value"), 1e-10);
  }

}
