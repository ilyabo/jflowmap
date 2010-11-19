package jflowmap.views.flowstrates;

import jflowmap.FlowMapGraph;
import jflowmap.FlowMapGraphAggLayers;
import jflowmap.NodeEdgePos;
import jflowmap.data.FlowMapGraphEdgeAggregator;
import jflowmap.data.FlowMapGraphEdgeAggregator.AggEntity;
import jflowmap.data.FlowMapGraphEdgeAggregator.GroupFunctions;
import jflowmap.data.FlowMapGraphEdgeAggregator.ValueAggregator;
import prefuse.data.Edge;
import prefuse.data.Tuple;

import com.google.common.base.Function;

/**
 * @author Ilya Boyandin
 */
public class RefugeeAggLayers {

  private RefugeeAggLayers() {
  }

  static FlowMapGraphAggLayers createAggLayers(FlowMapGraph flowMapGraph) {
      // aggregate by source node
      // flowMapGraph = new FlowMapGraphEdgeAggregator(flowMapGraph,
      // // FlowMapGraphEdgeAggregator.GroupFunctions.SRC_NODE)
      // FlowMapGraphEdgeAggregator.GroupFunctions.TARGET_NODE)
      // .withCustomValueAggregator("lat", ValueAggregators.DOUBLE_AVERAGE)
      // .withCustomValueAggregator("lon", ValueAggregators.DOUBLE_AVERAGE)
      // .withCustomValueAggregator("name", ValueAggregators.STRING_ONE_OR_NONE)
      // .aggregate();

      FlowMapGraphAggLayers.Builder builder = new FlowMapGraphAggLayers.Builder(
          "<No grouping>", flowMapGraph);

//      builder.addAggregationLayer("Origin", null, FlowMapGraphEdgeAggregator.GroupFunctions.SRC_NODE);
//      builder.addAggregationLayer("Dest", null, FlowMapGraphEdgeAggregator.GroupFunctions.TARGET_NODE);

      String labelAttr = flowMapGraph.getNodeLabelAttr();
      builder.addAggregationLayer("Origin", null,
          builder.edgeAggregatorFor(GroupFunctions.SRC_NODE, null)
            .withCustomValueAggregator(
                labelAttr, oneSideNodeLabelsAggregator(NodeEdgePos.SOURCE, labelAttr, "ALL")));

      builder.addAggregationLayer("Dest", null,
          builder.edgeAggregatorFor(GroupFunctions.TARGET_NODE, null));



      builder.addAggregationLayer("Origin/Subregion", null,
          builder.edgeAggregatorFor(new Function<Edge, Object>() {
            @Override
            public Object apply(Edge edge) {
              return edge.getSourceNode().getString("region2");
            }
          }, null)
          .withCustomValueAggregator(
              labelAttr,
              oneSideNodeLabelsAggregator(NodeEdgePos.SOURCE, "region2", "ALL"))
          );

      builder.addAggregationLayer("Origin/Region", null,
          builder.edgeAggregatorFor(new Function<Edge, Object>() {
            @Override
            public Object apply(Edge edge) {
              return edge.getSourceNode().getString("region1");
            }
          }, null)
          .withCustomValueAggregator(
              labelAttr,
              oneSideNodeLabelsAggregator(NodeEdgePos.SOURCE, "region1", "ALL"))
          );

      builder.addAggregationLayer("Origin/All", "Origin",
          builder.edgeAggregatorFor(FlowMapGraphEdgeAggregator.GroupFunctions.MERGE_ALL, "Origin")
              .withCustomValueAggregator(
                  labelAttr,
                  createAllForAllLabelsAggregator()));

  //    builder.addAggregationLayer("Dest/All", "Dest",
  //        builder.edgeAggregatorFor(FlowMapGraphEdgeAggregator.GroupFunctions.MERGE_ALL, "Dest")
  //            .withCustomValueAggregator(flowMapGraph.getNodeLabelAttr(), labelForAll));

      FlowMapGraphAggLayers layers =
        builder.build(null);
  //      builder.build("Origin/All");
  //      builder.build("Origin/Subregion");


      for (FlowMapGraph fmg : layers.getFlowMapGraphs()) {
        fmg.addEdgeWeightDifferenceColumns();
        fmg.addEdgeWeightRelativeDifferenceColumns();
      }
      return layers;
    }

  private static ValueAggregator createAllForAllLabelsAggregator() {
    return new ValueAggregator() {
      @Override
      public Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity) {
        return "ALL";
      }
    };
  }

  private static ValueAggregator oneSideNodeLabelsAggregator(NodeEdgePos s, final String attr,
      final String aggLabel) {
    final AggEntity ae;
    switch (s) {
      case SOURCE: ae = AggEntity.SOURCE_NODE; break;
      case TARGET: ae = AggEntity.TARGET_NODE; break;
      default: throw new AssertionError();
    }
    return new ValueAggregator() {
      @Override
      public Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity) {
        if (entity == ae) {
          return tuples.iterator().next().get(attr);
        } else {
          return aggLabel;
        }
      }
    };
  }


}
