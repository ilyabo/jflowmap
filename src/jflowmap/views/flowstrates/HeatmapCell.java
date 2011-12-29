/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.views.flowstrates;

import java.awt.geom.Rectangle2D;

import jflowmap.FlowMapGraph;
import prefuse.data.Edge;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
class HeatmapCell extends PPath {


  private final SimpleHeatmapLayer heatmapLayer;
  private final String weightAttr;
  private final FlowMapGraph flowMapGraph;
  private final Edge edge; // can be null

  public HeatmapCell(SimpleHeatmapLayer layer, double x, double y,
      double cellWidth, double cellHeight, String weightAttr, FlowMapGraph fmg, Edge edge) {
    super(new Rectangle2D.Double(x, y, cellWidth, cellHeight), null);
    this.heatmapLayer = layer;
    this.flowMapGraph = fmg;
    this.weightAttr = weightAttr;
    this.edge = edge;
    updateColor();
  }

  public Edge getEdge() {
    return edge;
  }

  public FlowMapGraph getFlowMapGraph() {
    return flowMapGraph;
  }

  public String getWeightAttr() {
    return weightAttr;
  }

  public double getValue() {
    return heatmapLayer.getFlowstratesView().getValue(edge, weightAttr);
  }

  public void updateColor() {
    setPaint(heatmapLayer.getFlowstratesView().getColorFor(getValue()));
  }

}
