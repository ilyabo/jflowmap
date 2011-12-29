package jflowmap.views.flowstrates;

import jflowmap.FlowMapGraph;
import jflowmap.FlowMapGraphAggLayers;

/**
 * @author Ilya Boyandin
 */
public interface AggLayersBuilder {

  FlowMapGraphAggLayers build(FlowMapGraph flowMapGraph);

}
