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

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import jflowmap.FlowMapGraph;
import jflowmap.data.MinMax;
import prefuse.data.Edge;
import prefuse.data.Node;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
class HeatMapCell extends PPath {

  private static final NumberFormat NUMBER_FORMAT = DecimalFormat.getNumberInstance();

  public enum ValueType {
    VALUE("original value") {
      @Override
      public MinMax getMinMax(HeatMapCell cell) {
        return cell.getView().getStats().getEdgeWeightStats();
      }

      @Override
      public String getAttr(HeatMapCell cell) {
        return cell.getWeightAttr();
      }
    }, DIFF("difference") {
      @Override
      public MinMax getMinMax(HeatMapCell cell) {
        return cell.getView().getStats().getEdgeWeightDiffStats();
      }

      @Override
      public String getAttr(HeatMapCell cell) {
        return cell.getFlowMapGraph().getAttrSpec().getEdgeWeightDiffAttr(cell.getWeightAttr());
      }
    }, DIFF_REL("relative diff") {
      @Override
      public MinMax getMinMax(HeatMapCell cell) {
        return cell.getView().getStats().getEdgeWeightRelativeDiffStats();
      }

      @Override
      public String getAttr(HeatMapCell cell) {
        return cell.getFlowMapGraph().getAttrSpec().getEdgeWeightRelativeDiffAttr(cell.getWeightAttr());
      }
    };

    private String name;

    private ValueType(String name) {
      this.name = name;
    }

    public abstract MinMax getMinMax(HeatMapCell cell);

    public abstract String getAttr(HeatMapCell cell);

    public Color getColorFor(HeatMapCell cell) {
      return getColorFor(cell, cell.getEdge().getDouble(getAttr(cell)));
    }

    public Color getColorFor(HeatMapCell cell, double value) {
      return cell.getView().getColorForWeight(value, getMinMax(cell));
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private final FlowstratesView view;
  private final String weightAttr;
  private final FlowMapGraph flowMapGraph;
  private final Edge edge; // can be null

  public HeatMapCell(FlowstratesView view, double x, double y,
      double cellWidth, double cellHeight, String weightAttr, FlowMapGraph fmg, Edge edge) {
    super(new Rectangle2D.Double(x, y, cellWidth, cellHeight), null);
    this.view = view;
    this.flowMapGraph = fmg;
    this.weightAttr = weightAttr;
    this.edge = edge;
    updateColor();
  }

  public FlowstratesView getView() {
    return view;
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

  private double getWeight() {
    return edge.getDouble(weightAttr);
  }


  public String getTooltipHeader() {
    // return tuple.getSrcNodeId() + "->" + tuple.getTargetNodeId() +
    // " " + FlowMapGraph.getGraphId(edge.getGraph());
    Node src = edge.getSourceNode();
    Node target = edge.getTargetNode();
    String nodeLabelAttr = flowMapGraph.getNodeLabelAttr();

    String origin = src.getString(nodeLabelAttr);
    String dest = target.getString(nodeLabelAttr);

    if (origin.length() > 75) origin = origin.substring(0, 75) + "...";
    if (dest.length() > 75) dest = dest.substring(0, 75) + "...";

    return
      //FlowMapGraph.getGraphId(edge.getGraph()) + ": " +
        origin + " -> " + dest;
  }

  public String getTooltipLabels() {
    return
      weightAttr + ":" + "\n" +
      flowMapGraph.getAttrSpec().getEdgeWeightDiffAttr(weightAttr) + ":" + "\n" +
      flowMapGraph.getAttrSpec().getEdgeWeightRelativeDiffAttr(weightAttr) + ":";
  }

  public String getTooltipValues() {
    double weight = edge.getDouble(weightAttr);
    double weightDiff = edge.getDouble(flowMapGraph.getAttrSpec().getEdgeWeightDiffAttr(weightAttr));
    double weightRelDiff = edge.getDouble(
        flowMapGraph.getAttrSpec().getEdgeWeightRelativeDiffAttr(weightAttr));
    return
      NUMBER_FORMAT.format(weight) + "\n" +
      NUMBER_FORMAT.format(weightDiff) + "\n" +
      NUMBER_FORMAT.format(weightRelDiff);
  }

  public void updateColor() {
    setPaint(view.getHeatMapCellValueType().getColorFor(this));
  }

}
