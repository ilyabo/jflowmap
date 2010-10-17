package jflowmap;

import java.util.List;
import java.util.Map;

import jflowmap.data.FlowMapGraphEdgeAggregator;
import jflowmap.data.MinMax;
import prefuse.data.Edge;

import com.google.common.base.Function;
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

      FlowMapGraph newFmg =
        new FlowMapGraphEdgeAggregator(prevLayer.getFlowMapGraph(), aggFunc)
        .aggregate();
      layersByName.put(layerName, new AggLayer(layerName, prevLayer, newFmg));

      return this;
    }

    public FlowtiGraph build(String initialLayer) {
      return new FlowtiGraph(layersByName, initialLayer);
    }
  }


  private final Map<String, AggLayer> layersByName;
  private final List<Edge> visibleEdges;  // possibly of different graphs

  private FlowtiGraph(Map<String, AggLayer> layers, String initialLayer) {
    layersByName = layers;
    visibleEdges = Lists.newArrayList(layersByName.get(initialLayer).getFlowMapGraph().edges());
  }

  public List<Edge> getVisibleEdges() {
    return visibleEdges;
  }

  public void expand(Edge edge) {

  }

  public void collapse(Edge edge) {

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

  public FlowMapGraph getFlowMapGraphOf(Edge e) {
    for (AggLayer layer : layersByName.values()) {
      if (e.getGraph() == layer.getFlowMapGraph().getGraph()) {
        return layer.getFlowMapGraph();
      }
    }
    return null;
  }
}
