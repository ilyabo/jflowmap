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

import java.awt.Color;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.util.List;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import prefuse.data.Edge;
import at.fhjoanneum.cgvis.data.IColorForValue;
import at.fhjoanneum.cgvis.data.IDataValues;
import at.fhjoanneum.cgvis.plots.mosaic.MosaicPlotNode;

/**
 * @author Ilya Boyandin
 */
public class FastHeatmapLayer extends TemporalViewLayer {

  private MosaicPlotNode mosaicPlotNode;
  private final IColorForValue colorForValue;

  public FastHeatmapLayer(FlowstratesView flowstratesView) {
    super(flowstratesView);
    colorForValue = createColorForValue();
    renew();
  }

  @Override
  public void renew() {
    removeAllChildren();

    IDataValues data = getDataValues();
    FlowstratesView fs = getFlowstratesView();
    mosaicPlotNode = new MosaicPlotNode(data, fs.getVisualCanvas(), colorForValue);

    Color missingColor = fs.getStyle().getMissingValueColor();
    mosaicPlotNode.setMissingValueColor1(missingColor);
    mosaicPlotNode.setMissingValueColor2(missingColor);

    addChild(mosaicPlotNode);
  }

  private IColorForValue createColorForValue() {
    return new IColorForValue() {
      @Override
      public Color getColorForValue(double value) {
        return getFlowstratesView().getColorFor(value);
      }
    };
  }

  private IDataValues getDataValues() {
    return new IDataValues() {
      FlowstratesView fs = getFlowstratesView();
      FlowMapGraph fmg = fs.getFlowMapGraph();
      List<String> attrs = fmg.getEdgeWeightAttrs();
      List<Edge> edges = fs.getVisibleEdges();
      @Override
      public int getSize() {
        return edges.size();
      }
      @Override
      public int getDimension() {
        return fmg.getEdgeWeightAttrsCount();
      }
      @Override
      public double getValue(int element, int attribute) {
        return getFlowstratesView().getValue(edges.get(element), attrs.get(attribute));
      }
    };
  }


  @Override
  public void updateColors() {
    renew();
  }

  @Override
  public void resetWeightAttrTotals() {
  }

  @Override
  public void fitInView() {
    HeatmapLayer.fitBoundsInCameraView(mosaicPlotNode.getFullBounds(), getCamera());
  }

  @Override
  public Rectangle2D getEdgeLabelBounds(Edge edge, FlowEndpoint ep) {
    return null;
  }

  @Override
  public Double getFlowLineInPoint(int row, FlowEndpoint ep) {
    return null;
  }

}
