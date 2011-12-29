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

  private FlowMapGraphAggLayers layers;

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

    layers = builder.build("src-to-all");
  }

  @Test
  public void test_topLevel() {
    List<Edge> edges = layers.getEdges();
    assertEquals(1, edges.size());

    Edge e = edges.get(0);
    FlowMapGraph fmgAll = layers.getFlowMapGraphOf(e);
    assertEquals(1200, fmgAll.getEdgeWeight(e, "value"), 1e-10);

    assertEquals(ImmutableSet.of("1,2->1,4,2,3,4: 1200.0"), serializeVisibleEdges(layers));
  }

  @Test
  public void test_expandSource() {
    Edge e = layers.getEdges().get(0);

    layers.expandSource(e);
    assertEquals(ImmutableSet.of("1->2,3,4: 900.0", "2->1,4: 300.0"), serializeVisibleEdges(layers));
  }


  @Test
  public void test_expandSource_collapseSource_expandSource_collapseSource() {
    Edge e = layers.getEdges().get(0);

    layers.expandSource(e);
    assertEquals(ImmutableSet.of("1->2,3,4: 900.0", "2->1,4: 300.0"), serializeVisibleEdges(layers));

    layers.collapseSource(e);
    assertEquals(ImmutableSet.of("1,2->1,4,2,3,4: 1200.0"), serializeVisibleEdges(layers));

    layers.expandSource(e);
    assertEquals(ImmutableSet.of("1->2,3,4: 900.0", "2->1,4: 300.0"), serializeVisibleEdges(layers));

    layers.collapseSource(e);
    assertEquals(ImmutableSet.of("1,2->1,4,2,3,4: 1200.0"), serializeVisibleEdges(layers));
  }

  @Test
  public void test_expandSource_expandBothTarget_collapseOneTarget_collapseSourceOverLevel() {
    Edge e_all = layers.getEdges().get(0);

    layers.expandSource(e_all);

    Edge e_target1_1 = layers.findEdgeByNodeIds("2", "1,4");
    Edge e_target1_2 = layers.findEdgeByNodeIds("1", "2,3,4");

    layers.expandTarget(e_target1_1);
    layers.expandTarget(e_target1_2);
    assertEquals(ImmutableSet.of(
        "1->2: 200.0", "1->3: 300.0", "1->4: 400.0", "2->1: 100.0", "2->4: 200.0"),
        serializeVisibleEdges(layers));


    layers.collapseTarget(e_target1_1);
    assertEquals(ImmutableSet.of(
        "1->2: 200.0", "1->3: 300.0", "1->4: 400.0", "2->1,4: 300.0"),
        serializeVisibleEdges(layers));

//    layers.collapseTarget(e_target1_2);

    layers.collapseSource(e_all);
    assertEquals(ImmutableSet.of("1,2->1,4,2,3,4: 1200.0"), serializeVisibleEdges(layers));
  }

  @Test
  public void test_collapseOverLevel() {
    Edge e_all = layers.getEdges().get(0);

    layers.expandSource(e_all);

    Edge e_target1_1 = layers.findEdgeByNodeIds("2", "1,4");
    Edge e_target1_2 = layers.findEdgeByNodeIds("1", "2,3,4");

    layers.expandTarget(e_target1_1);
    layers.expandTarget(e_target1_2);
    assertEquals(ImmutableSet.of(
        "1->2: 200.0", "1->3: 300.0", "1->4: 400.0", "2->1: 100.0", "2->4: 200.0"),
        serializeVisibleEdges(layers));


    layers.collapseSource(e_all);
    assertEquals(ImmutableSet.of("1,2->1,4,2,3,4: 1200.0"), serializeVisibleEdges(layers));
  }

  @Test
  public void test_expandSource_collapseSource_collapseSource() {
    Edge e = layers.getEdges().get(0);

    layers.expandSource(e);
    assertEquals(ImmutableSet.of("1->2,3,4: 900.0", "2->1,4: 300.0"), serializeVisibleEdges(layers));

    layers.collapseSource(e);
    layers.collapseSource(e);

    assertEquals(ImmutableSet.of("1,2->1,4,2,3,4: 1200.0"), serializeVisibleEdges(layers));
  }

  @Test
  public void test_expandTarget() {
    Edge e = layers.getEdges().get(0);

    layers.expandTarget(e);
    assertEquals(ImmutableSet.of("1->2,3,4: 900.0", "2->1,4: 300.0"), serializeVisibleEdges(layers));
  }

  @Test
  public void test_expandSource_expandBothTarget() {
    Edge e = layers.getEdges().get(0);

    layers.expandSource(e);

    Edge e2 = layers.getEdges().get(0);
    Edge e3 = layers.getEdges().get(1);

    layers.expandTarget(e2);
    layers.expandTarget(e3);

    assertEquals(ImmutableSet.of(
        "1->2: 200.0", "1->3: 300.0", "1->4: 400.0", "2->1: 100.0", "2->4: 200.0"),
        serializeVisibleEdges(layers));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_expandUnexpandable() {
    layers.expandSource(layers.getEdges().get(0));
    layers.expandSource(layers.getEdges().get(0));
    layers.expandSource(layers.getEdges().get(0));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_expandOnBaseLayer() {
    Edge edge = layers.getBaseFlowMapGraph().edges().iterator().next();
    layers.expandSource(edge);
  }


  @Test(expected=IllegalArgumentException.class)
  public void test_collapsedOnBaseLayer() {
    Edge edge = layers.getBaseFlowMapGraph().edges().iterator().next();
    layers.collapseSource(edge);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_expandInvisible() {
    Edge e = layers.getEdges().get(0);

    layers.expandSource(e);
    layers.expandTarget(e);  // not visible anymore
  }


  @Test(expected=IllegalArgumentException.class)
  public void test_expandNodeOfCollapsedLayer() {
    Edge e = layers.getBaseFlowMapGraph().edges().iterator().next();
    layers.expandSource(e);  // not visible
  }

  private static Set<String> serializeVisibleEdges(FlowMapGraphAggLayers ft) {
    Set<String> set = Sets.newHashSet();
    for (Edge edge : ft.getEdges()) {
      set.add(
          ft.getSourceNodeId(edge) + "->" + ft.getTargetNodeId(edge) + ": " +
          ft.getEdgeWeight(edge, "value")
      );
    }
    return set;
  }

}
