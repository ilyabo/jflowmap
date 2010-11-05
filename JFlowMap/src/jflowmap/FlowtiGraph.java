package jflowmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import jflowmap.data.FlowMapGraphEdgeAggregator;
import jflowmap.data.MinMax;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Ilya Boyandin
 */
public class FlowtiGraph {

  public static class Builder {

    private final Map<String, AggLayer> layersByName = Maps.newLinkedHashMap();
    private final AggLayer baseLayer;

    public Builder(FlowMapGraph fmg) {
      baseLayer = new AggLayer(null, null, fmg);
    }

    public Builder addAggregationLayer(String layerName, String prevLayerName,
        Function<Edge, Object> aggFunc) {

      if (layersByName.containsKey("layerName")) {
        throw new IllegalArgumentException("Agg layer '" + layerName + "' exists");
      }
      AggLayer prevLayer;
      if (prevLayerName == null) {
        prevLayer = baseLayer;
      } else {
        prevLayer = layersByName.get(prevLayerName);
        if (prevLayer == null) {
          throw new IllegalArgumentException("Base agg layer '"+ prevLayerName + "' not found");
        }
      }

      FlowMapGraph prevFmg = prevLayer.getFlowMapGraph();
      FlowMapGraph newFmg =
        new FlowMapGraphEdgeAggregator(prevFmg, aggFunc).aggregate();

      layersByName.put(layerName, new AggLayer(layerName, prevLayer, newFmg));

      return this;
    }

    public FlowtiGraph build(String initialLayer) {
      return new FlowtiGraph(baseLayer, layersByName.values(), layersByName.get(initialLayer));
    }
  }


  private final List<AggLayer> aggLayers;
  private List<Edge> visibleEdges;  // possibly of different graphs
  private final AggLayer baseLayer;

  private FlowtiGraph(AggLayer base, Iterable<AggLayer> layers, AggLayer initialLayer) {
    baseLayer = base;
    aggLayers = ImmutableList.copyOf(Iterables.concat(ImmutableList.of(base), layers));
    visibleEdges = Lists.newArrayList(initialLayer.getFlowMapGraph().edges());
  }

  public FlowMapGraph getBaseFlowMapGraph() {
    return baseLayer.getFlowMapGraph();
  }

  public List<Edge> getVisibleEdges() {
    return visibleEdges;
  }

//  public boolean isEdgeAggregatedBy(Edge edge, Edge aggEdge) {
//    List<Edge> aggList = FlowMapGraphEdgeAggregator.getAggregateList(aggEdge);
//    if (aggList.contains(edge)) {
//      return true;
//    }
//
//
//
//    return false;
//  }

  public void expandSource(Edge e) {
    expand(e.getSourceNode(), NodeEdgePos.SOURCE);
  }

  public void expandTarget(Edge e) {
    expand(e.getTargetNode(), NodeEdgePos.TARGET);
  }

  private void expand(Node node, NodeEdgePos as) {
    AggLayer layer = requireAggLayer(node);
    Edge aggEdge = requireVisibleEdgeWith(node, as);
    if (layer == baseLayer) {
      throw new IllegalArgumentException("Base layer node cannot be deaggregated. Node id:" +
          layer.getFlowMapGraph().getNodeId(node));
    }

    // replace aggEdge with all edges it aggregates
    List<Edge> deagg = FlowMapGraphEdgeAggregator.getAggregateList(aggEdge);
    List<Edge> newVisEdges = replaceEdgeWithListOfEdges(visibleEdges, aggEdge, deagg);

    visibleEdges = Collections.unmodifiableList(newVisEdges);
  }

  public void collapseSource(Edge e) {
    collapse(e.getSourceNode(), NodeEdgePos.SOURCE);
  }

  public void collapseTarget(Edge e) {
    collapse(e.getTargetNode(), NodeEdgePos.TARGET);
  }

  private void collapse(Node node, NodeEdgePos as) {
    requireAggLayer(node);

    Edge aggEdge = getEdgeWith(node, as);
    if (aggEdge == null) {
      throw new IllegalArgumentException("Edge not found with node " + node + " as " + as);
    }
//    requireExpanded(aggEdge);

//    if (isExpanded(aggEdge)) {
    List<Edge> deagg = FlowMapGraphEdgeAggregator.getAggregateList(aggEdge);
    List<Edge> newVisEdges = replaceListOfEdgesWithEdge(visibleEdges, deagg, aggEdge, false);

    visibleEdges = Collections.unmodifiableList(newVisEdges);
//    }
  }

//  public boolean isExpanded(Edge aggEdge) {
//    for (Edge edge : FlowMapGraphEdgeAggregator.getAggregateList(aggEdge)) {
//      if (!visibleEdges.contains(edge)) {
//        return false;
//      }
//    }
//    return true;
//  }

  private static List<Edge> replaceEdgeWithListOfEdges(List<Edge> edges, Edge what, List<Edge> replaceWith) {
    int index = edges.indexOf(what);
    if (index < 0) {
      throw new IllegalArgumentException("Edge not found in list: " + edges);
    }

    List<Edge> newEdges = new ArrayList<Edge>(edges.size() - 1 + replaceWith.size());
    newEdges.addAll(edges.subList(0, index));
    newEdges.addAll(replaceWith);
    newEdges.addAll(edges.subList(index + 1, edges.size()));

    return newEdges;
  }

  /*
  private static List<Edge> replaceListOfEdgesWithEdge(List<Edge> edges, List<Edge> what, Edge replaceWith) {
    List<Edge> newEdges = new ArrayList<Edge>(edges.size() - what.size() + 1);
    int numFound = 0;
    for (Edge edge : edges) {
      if (what.contains(edge)) {
        if (numFound == 0) {
          newEdges.add(replaceWith);
        }
        numFound++;
      } else {
        newEdges.add(edge);
      }
    }
    if (numFound != what.size()) {
      throw new IllegalArgumentException("Not all edges were found: " + numFound + " of " + what.size());
    }
    return newEdges;
  }
  */

  private static List<Edge> replaceListOfEdgesWithEdge(
      List<Edge> edges, List<Edge> what, Edge replaceWith, boolean requireAllToBeFound) {
    List<Edge> newEdges = Lists.newArrayList(edges);
    List<Edge> toReplace = Lists.newArrayList(what);

    // Remove from newEdges the 'what' edges or edges they aggregate
    int min = 0;
    while (!toReplace.isEmpty()) {
      for (ListIterator<Edge> it = newEdges.listIterator(); it.hasNext(); ) {
        Edge e = it.next();
        if (toReplace.contains(e)) {
          int i = (it.nextIndex() - 1);
          if (i < min) min = i;
          it.remove();
          toReplace.remove(e);
        }
      }

      if (!toReplace.isEmpty()) {
        // Replace nodes which were not found in 'what' with the nodes they aggregate
        List<Edge> newToReplace = Lists.newArrayList();
        for (Edge edge : toReplace) {
          if (FlowMapGraphEdgeAggregator.isAggregate(edge)) {
            newToReplace.addAll(FlowMapGraphEdgeAggregator.getAggregateList(edge));
          } else {
            if (requireAllToBeFound) {
              throw new IllegalStateException("Edge to be replaced was not found: " + edge);
            }
          }
        }
        toReplace = newToReplace;
      }
    }

    newEdges.add(min, replaceWith);

    return newEdges;
  }

  private Edge getEdgeWith(Node node, NodeEdgePos as) {
    FlowMapGraph fmg = getFlowMapGraphOf(node);
    for (Edge e : fmg.edges()) {
      if (as.nodeOf(e) == node) {
        return e;
      }
    }
    return null;
  }

  private Edge getVisibleEdgeWith(Node node, NodeEdgePos as) {
    for (Edge e : getVisibleEdges()) {
      if (as.nodeOf(e) == node) {
        return e;
      }
    }
    return null;
  }

  private Edge requireVisibleEdgeWith(Node node, NodeEdgePos as) {
    Edge edge = getVisibleEdgeWith(node, as);
    if (edge == null) {
      throw new IllegalArgumentException("Node '" + getNodeLabel(node) + "' is not visible as " + as);
    }
    return edge;
  }

  private AggLayer requireAggLayer(Node node) {
    AggLayer layer = getLayerOf(node.getGraph());
    if (layer == null) {
      throw new IllegalArgumentException("Layer not found for node: " + node);
    }
    return layer;
  }

  public String getNodeLabel(Node node) {
    return getFlowMapGraphOf(node).getNodeLabel(node);
  }

  public MinMax getStatsForVisibleEdges() {
    return null;
  }

  private static class AggLayer {
    private final String name;
    private final AggLayer prevLayer;
    private final FlowMapGraph flowMapGraph;
    public AggLayer(String name, AggLayer prevLayer, FlowMapGraph flowMapGraph) {
      this.name = name;
      this.prevLayer = prevLayer;
      this.flowMapGraph = flowMapGraph;
    }
    public String getName() {
      return name;
    }
    public AggLayer getPrevLayerName() {
      return prevLayer;
    }
    public FlowMapGraph getFlowMapGraph() {
      return flowMapGraph;
    }
  }

  public FlowMapGraph getFlowMapGraphOf(Node n) {
    return getLayerOf(n.getGraph()).getFlowMapGraph();
  }

  public FlowMapGraph getFlowMapGraphOf(Edge e) {
    return getLayerOf(e.getGraph()).getFlowMapGraph();
  }

  private AggLayer getLayerOf(Graph graph) {
    for (AggLayer layer : aggLayers) {
      if (graph == layer.getFlowMapGraph().getGraph()) {
        return layer;
      }
    }
    return null;
  }

  public String getSourceNodeId(Edge edge) {
    return getFlowMapGraphOf(edge).getSourceNodeId(edge);
  }

  public String getTargetNodeId(Edge edge) {
    return getFlowMapGraphOf(edge).getTargetNodeId(edge);
  }

  public double getEdgeWeight(Edge edge, String weightAttr) {
    return getFlowMapGraphOf(edge).getEdgeWeight(edge, weightAttr);
  }

  public Edge findVisibleEdgeByNodeIds(String srcId, String targetId) {
    for (Edge e : visibleEdges) {
      if (srcId.equals(getSourceNodeId(e))  &&  targetId.equals(getTargetNodeId(e))) {
        return e;
      }
    }
    return null;
  }

}