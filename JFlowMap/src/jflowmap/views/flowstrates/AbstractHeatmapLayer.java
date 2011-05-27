/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.views.flowstrates;

import java.util.List;

import jflowmap.FlowEndpoint;
import jflowmap.data.SeqStat;
import prefuse.data.Edge;

/**
 * @author Ilya Boyandin
 */
public abstract class AbstractHeatmapLayer extends TemporalViewLayer {

  private SeqStat weightAttrTotalsStat = null;

  public AbstractHeatmapLayer(FlowstratesView flowstratesView) {
    super(flowstratesView);
  }

  protected SeqStat getWeightAttrTotalsStat() {
    return weightAttrTotalsStat;
  }

  @Override
  public void resetWeightAttrTotals() {
    weightAttrTotalsStat = null;
  }

  @Override
  public void renew() {
    resetWeightAttrTotals();
  }

  void updateMapsOnHeatmapColumnHover(String columnAttr, boolean hover) {
    MapLayer originMap = getFlowstratesView().getMapLayer(FlowEndpoint.ORIGIN);
    MapLayer destMap = getFlowstratesView().getMapLayer(FlowEndpoint.DEST);

    List<Edge> edges = getFlowstratesView().getVisibleEdges();

    SeqStat wstat = getFlowstratesView().getValueStat();

    if (hover) {
      if (weightAttrTotalsStat == null) {
         for (String attr : getFlowMapGraph().getEdgeWeightAttrs()) {
          // "merge" the value stats with the max value of the sums, to construct a color
          // scale in which we can represent the totals for the nodes
          wstat = wstat
              .mergeWith(originMap.calcNodeTotalsFor(edges, attr).values())
              .mergeWith(destMap.calcNodeTotalsFor(edges, attr).values());
        }
        weightAttrTotalsStat = wstat;
      }
      getFlowstratesView().setValueStat(weightAttrTotalsStat);
    } else {
      getFlowstratesView().resetValueStat();
    }

    originMap.updateOnHeatmapColumnHover(columnAttr, hover);
    destMap.updateOnHeatmapColumnHover(columnAttr, hover);

    //updateHeatmapColors();
  }

}
