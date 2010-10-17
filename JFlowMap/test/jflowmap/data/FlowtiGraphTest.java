package jflowmap.data;

import static org.junit.Assert.assertEquals;

import java.util.List;

import jflowmap.FlowMapGraph;
import jflowmap.FlowtiGraph;

import org.junit.Test;

import prefuse.data.Edge;

/**
 * @author Ilya Boyandin
 */
public class FlowtiGraphTest {

  @Test
  public void testBuild() {
    FlowMapGraph fmg = FlowMapGraphBuilderTest.buildTestFlowMapGraph();

    FlowtiGraph.Builder builder = new FlowtiGraph.Builder(fmg);
    builder.addAggregationLayer(
        "src-node", null,
        FlowMapGraphEdgeAggregator.GroupFunctions.SRC_NODE);
    builder.addAggregationLayer(
        "target-node", null,
        FlowMapGraphEdgeAggregator.GroupFunctions.TARGET_NODE);

    builder.addAggregationLayer(
        "src-to-all", "src-node",
        FlowMapGraphEdgeAggregator.GroupFunctions.MERGE_ALL);
    builder.addAggregationLayer(
        "target-to-all", "target-node",
        FlowMapGraphEdgeAggregator.GroupFunctions.MERGE_ALL);


    FlowtiGraph flowti = builder.build("src-to-all");

    List<Edge> edges = flowti.getVisibleEdges();
    assertEquals(1, edges.size());

    Edge e = edges.get(0);
    FlowMapGraph fmgAll = flowti.getFlowMapGraphOf(e);
    assertEquals(1200, fmgAll.getEdgeWeight(e, "value"), 1e-10);

//    flowti.expand(e, );
  }
}
