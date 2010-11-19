package jflowmap.data;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import jflowmap.FlowMapGraph;
import jflowmap.FlowMapGraphAggLayers;

import org.junit.Before;
import org.junit.Test;

import prefuse.data.Edge;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Ilya Boyandin
 */
public class FlowsmapGraphAggLayersTest {

  private FlowMapGraphAggLayers flowti;

  @Before
  public void setup() {
    FlowMapGraph fmg = FlowMapGraphBuilderTest.buildTestFlowMapGraph();

    FlowMapGraphAggLayers.Builder builder = new FlowMapGraphAggLayers.Builder("base", fmg);
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

    flowti = builder.build("src-to-all");
  }

  @Test
  public void test_topLevel() {
    List<Edge> edges = flowti.getVisibleEdges();
    assertEquals(1, edges.size());

    Edge e = edges.get(0);
    FlowMapGraph fmgAll = flowti.getFlowMapGraphOf(e);
    assertEquals(1200, fmgAll.getEdgeWeight(e, "value"), 1e-10);

    assertEquals(ImmutableSet.of("1,2->1,4,2,3,4: 1200.0"), serializeVisibleEdges(flowti));
  }

  @Test
  public void test_expandSource() {
    Edge e = flowti.getVisibleEdges().get(0);

    flowti.expandSource(e);
    assertEquals(ImmutableSet.of("1->2,3,4: 900.0", "2->1,4: 300.0"), serializeVisibleEdges(flowti));
  }


  @Test
  public void test_expandSource_collapseSource_expandSource_collapseSource() {
    Edge e = flowti.getVisibleEdges().get(0);

    flowti.expandSource(e);
    assertEquals(ImmutableSet.of("1->2,3,4: 900.0", "2->1,4: 300.0"), serializeVisibleEdges(flowti));

    flowti.collapseSource(e);
    assertEquals(ImmutableSet.of("1,2->1,4,2,3,4: 1200.0"), serializeVisibleEdges(flowti));

    flowti.expandSource(e);
    assertEquals(ImmutableSet.of("1->2,3,4: 900.0", "2->1,4: 300.0"), serializeVisibleEdges(flowti));

    flowti.collapseSource(e);
    assertEquals(ImmutableSet.of("1,2->1,4,2,3,4: 1200.0"), serializeVisibleEdges(flowti));
  }

  @Test
  public void test_expandSource_expandBothTarget_collapseOneTarget_collapseSourceOverLevel() {
    Edge e_all = flowti.getVisibleEdges().get(0);

    flowti.expandSource(e_all);

    Edge e_target1_1 = flowti.findVisibleEdgeByNodeIds("2", "1,4");
    Edge e_target1_2 = flowti.findVisibleEdgeByNodeIds("1", "2,3,4");

    flowti.expandTarget(e_target1_1);
    flowti.expandTarget(e_target1_2);
    assertEquals(ImmutableSet.of(
        "1->2: 200.0", "1->3: 300.0", "1->4: 400.0", "2->1: 100.0", "2->4: 200.0"),
        serializeVisibleEdges(flowti));


    flowti.collapseTarget(e_target1_1);
    assertEquals(ImmutableSet.of(
        "1->2: 200.0", "1->3: 300.0", "1->4: 400.0", "2->1,4: 300.0"),
        serializeVisibleEdges(flowti));

//    flowti.collapseTarget(e_target1_2);

    flowti.collapseSource(e_all);
    assertEquals(ImmutableSet.of("1,2->1,4,2,3,4: 1200.0"), serializeVisibleEdges(flowti));
  }

  @Test
  public void test_collapseOverLevel() {
    Edge e_all = flowti.getVisibleEdges().get(0);

    flowti.expandSource(e_all);

    Edge e_target1_1 = flowti.findVisibleEdgeByNodeIds("2", "1,4");
    Edge e_target1_2 = flowti.findVisibleEdgeByNodeIds("1", "2,3,4");

    flowti.expandTarget(e_target1_1);
    flowti.expandTarget(e_target1_2);
    assertEquals(ImmutableSet.of(
        "1->2: 200.0", "1->3: 300.0", "1->4: 400.0", "2->1: 100.0", "2->4: 200.0"),
        serializeVisibleEdges(flowti));


    flowti.collapseSource(e_all);
    assertEquals(ImmutableSet.of("1,2->1,4,2,3,4: 1200.0"), serializeVisibleEdges(flowti));
  }

  @Test
  public void test_expandSource_collapseSource_collapseSource() {
    Edge e = flowti.getVisibleEdges().get(0);

    flowti.expandSource(e);
    assertEquals(ImmutableSet.of("1->2,3,4: 900.0", "2->1,4: 300.0"), serializeVisibleEdges(flowti));

    flowti.collapseSource(e);
    flowti.collapseSource(e);

    assertEquals(ImmutableSet.of("1,2->1,4,2,3,4: 1200.0"), serializeVisibleEdges(flowti));
  }

  @Test
  public void test_expandTarget() {
    Edge e = flowti.getVisibleEdges().get(0);

    flowti.expandTarget(e);
    assertEquals(ImmutableSet.of("1->2,3,4: 900.0", "2->1,4: 300.0"), serializeVisibleEdges(flowti));
  }

  @Test
  public void test_expandSource_expandBothTarget() {
    Edge e = flowti.getVisibleEdges().get(0);

    flowti.expandSource(e);

    Edge e2 = flowti.getVisibleEdges().get(0);
    Edge e3 = flowti.getVisibleEdges().get(1);

    flowti.expandTarget(e2);
    flowti.expandTarget(e3);

    assertEquals(ImmutableSet.of(
        "1->2: 200.0", "1->3: 300.0", "1->4: 400.0", "2->1: 100.0", "2->4: 200.0"),
        serializeVisibleEdges(flowti));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_expandUnexpandable() {
    flowti.expandSource(flowti.getVisibleEdges().get(0));
    flowti.expandSource(flowti.getVisibleEdges().get(0));
    flowti.expandSource(flowti.getVisibleEdges().get(0));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_expandOnBaseLayer() {
    Edge edge = flowti.getBaseFlowMapGraph().edges().iterator().next();
    flowti.expandSource(edge);
  }


  @Test(expected=IllegalArgumentException.class)
  public void test_collapsedOnBaseLayer() {
    Edge edge = flowti.getBaseFlowMapGraph().edges().iterator().next();
    flowti.collapseSource(edge);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_expandInvisible() {
    Edge e = flowti.getVisibleEdges().get(0);

    flowti.expandSource(e);
    flowti.expandTarget(e);  // not visible anymore
  }


  @Test(expected=IllegalArgumentException.class)
  public void test_expandNodeOfCollapsedLayer() {
    Edge e = flowti.getBaseFlowMapGraph().edges().iterator().next();
    flowti.expandSource(e);  // not visible
  }

  private static Set<String> serializeVisibleEdges(FlowMapGraphAggLayers ft) {
    Set<String> set = Sets.newHashSet();
    for (Edge edge : ft.getVisibleEdges()) {
      set.add(
          ft.getSourceNodeId(edge) + "->" + ft.getTargetNodeId(edge) + ": " +
          ft.getEdgeWeight(edge, "value")
      );
    }
    return set;
  }

}
