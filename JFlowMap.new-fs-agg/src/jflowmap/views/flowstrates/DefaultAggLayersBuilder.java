package jflowmap.views.flowstrates;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import jflowmap.FlowMapGraphAggLayers;
import jflowmap.FlowMapGraphAggLayers.Builder;
import jflowmap.data.FlowMapGraphEdgeAggregator;
import jflowmap.data.FlowMapGraphEdgeAggregator.AggEntity;
import jflowmap.data.FlowMapGraphEdgeAggregator.GroupFunctions;
import jflowmap.data.FlowMapGraphEdgeAggregator.ValueAggregator;
import prefuse.data.Tuple;

/**
 * @author Ilya Boyandin
 */
public class DefaultAggLayersBuilder implements AggLayersBuilder {

  public static final String BY_DEST_LAYER = "Dest";
  public static final String BY_ORIGIN_LAYER = "Origin";
  public static final String ALL_TO_ALL_LAYER = "All-to-all";

  @Override
  public FlowMapGraphAggLayers build(FlowMapGraph flowMapGraph) {
    return createBuilder(flowMapGraph).build(null);
  }

  protected Builder createBuilder(FlowMapGraph flowMapGraph) {
    FlowMapGraphAggLayers.Builder builder = new FlowMapGraphAggLayers.Builder(
        "<No grouping>", flowMapGraph);

//    builder.addAggregationLayer("Origin", null, FlowMapGraphEdgeAggregator.GroupFunctions.SRC_NODE);
//    builder.addAggregationLayer("Dest", null, FlowMapGraphEdgeAggregator.GroupFunctions.TARGET_NODE);

    String labelAttr = flowMapGraph.getNodeLabelAttr();
    builder.addAggregationLayer(BY_ORIGIN_LAYER, null,
        builder.edgeAggregatorFor(GroupFunctions.SRC_NODE, null)
          .withCustomValueAggregator(
              labelAttr,
              oneSideNodeLabelsAggregator(FlowEndpoint.ORIGIN, labelAttr, "ALL"))
          );

    builder.addAggregationLayer(BY_DEST_LAYER, null,
        builder.edgeAggregatorFor(GroupFunctions.TARGET_NODE, null)
          .withCustomValueAggregator(
              labelAttr,
              oneSideNodeLabelsAggregator(FlowEndpoint.DEST, labelAttr, "ALL"))
          );

    builder.addAggregationLayer(ALL_TO_ALL_LAYER, "Origin",
        builder.edgeAggregatorFor(FlowMapGraphEdgeAggregator.GroupFunctions.MERGE_ALL, "Origin")
            .withCustomValueAggregator(
                labelAttr,
                createAllForAllLabelsAggregator()));

    return builder;
  }

  protected static ValueAggregator createAllForAllLabelsAggregator() {
    return new ValueAggregator() {
      @Override
      public Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity) {
        return "ALL";
      }
    };
  }

  protected static ValueAggregator oneSideNodeLabelsAggregator(
      final FlowEndpoint s, final String aggAttr, final String aggLabel) {
    return new ValueAggregator() {
      @Override
      public Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity) {
        if (entity == getAggEntityFor(s)) {
          // the value of this aggAttr is supposed to be the same for all tuples,
          // becase we aggregated on it, so we can use the label of the first item
          // for the whole group
          return tuples.iterator().next().get(aggAttr);
        } else {
          return aggLabel;
        }
      }
    };
  }

  private static AggEntity getAggEntityFor(FlowEndpoint s) throws AssertionError {
    final AggEntity ae;
    switch (s) {
      case ORIGIN: ae = AggEntity.SOURCE_NODE; break;
      case DEST: ae = AggEntity.TARGET_NODE; break;
      default: throw new AssertionError();
    }
    return ae;
  }


}
