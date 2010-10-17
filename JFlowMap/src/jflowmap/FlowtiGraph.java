package jflowmap;

import java.util.List;

import jflowmap.data.FlowMapGraphEdgeAggregator;
import jflowmap.data.MinMax;
import prefuse.data.Edge;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class FlowtiGraph {

  public static class Builder {

    private final List<Function<Edge, Object>> aggLayers = Lists.newArrayList();
    private final FlowMapGraph flowMapGraph;

    public Builder(FlowMapGraph fmg) {
      this.flowMapGraph = fmg;
    }

    public Builder addAggregationLayer(Function<Edge, Object> aggFunction) {
      aggLayers.add(aggFunction);
      return this;
    }

    public FlowtiGraph build() {
      List<FlowMapGraph> list = Lists.newArrayList();
      list.add(flowMapGraph);
      FlowMapGraph fmg = flowMapGraph;
      for (Function<Edge, Object> aggFunc : aggLayers) {
        fmg = new FlowMapGraphEdgeAggregator(fmg, aggFunc).aggregate();
        list.add(fmg);
      }
      return new FlowtiGraph(list);
    }
  }

  private final List<FlowMapGraph> flowMapGraphs;
  private List<Edge> visibleEdges;  // possibly of different graphs

  private FlowtiGraph(List<FlowMapGraph> fmgs) {
    this.flowMapGraphs = fmgs;
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
}
