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

import jflowmap.FlowMapGraph;
import jflowmap.data.MinMax;
import prefuse.data.Edge;
import prefuse.data.Node;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
class HeatMapCell extends PPath {

  public enum ValueType {
    VALUE("original value") {
      @Override
      protected MinMax getStats(HeatMapCell cell) {
        return cell.getFlowMapGraph().getStats().getEdgeWeightStats();
      }

      @Override
      protected String getAttr(HeatMapCell cell) {
        return cell.getWeightAttr();
      }
    }, DIFF("difference") {
      @Override
      protected MinMax getStats(HeatMapCell cell) {
        return cell.getFlowMapGraph().getStats().getEdgeWeightDiffStats();
      }

      @Override
      protected String getAttr(HeatMapCell cell) {
        return cell.getFlowMapGraph().getEdgeWeightDiffAttr(cell.getWeightAttr());
      }
    }, DIFF_REL("relative diff") {
      @Override
      protected MinMax getStats(HeatMapCell cell) {
        return cell.getFlowMapGraph().getStats().getEdgeWeightRelativeDiffStats();
      }

      @Override
      protected String getAttr(HeatMapCell cell) {
        return cell.getFlowMapGraph().getEdgeWeightRelativeDiffAttr(cell.getWeightAttr());
      }
    };

    private String name;

    private ValueType(String name) {
      this.name = name;
    }

    protected abstract MinMax getStats(HeatMapCell cell);

    protected abstract String getAttr(HeatMapCell cell);

    public Color getColorFor(HeatMapCell cell) {
      double diff = cell.getEdge().getDouble(getAttr(cell));
      return cell.getView().getColorForWeight(diff, getStats(cell));
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
    return
      FlowMapGraph.getGraphId(edge.getGraph()) + ": " + src.getString(nodeLabelAttr) + " -> " +
        target.getString(nodeLabelAttr);
  }

  public String getTooltipLabels() {
    return
      weightAttr + ":" + "\n" +
      flowMapGraph.getEdgeWeightDiffAttr(weightAttr) + ":" + "\n" +
      flowMapGraph.getEdgeWeightRelativeDiffAttr(weightAttr) + ":";
  }

  public String getTooltipValues() {
    return
      Double.toString(edge.getDouble(weightAttr)) + "\n" +
      Double.toString(edge.getDouble(flowMapGraph.getEdgeWeightDiffAttr(weightAttr))) + "\n" +
      Double.toString(edge.getDouble(flowMapGraph.getEdgeWeightRelativeDiffAttr(weightAttr)));
  }

  public void updateColor() {
    setPaint(view.getHeatMapCellValueType().getColorFor(this));
  }

}
