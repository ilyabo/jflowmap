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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import jflowmap.util.ColorUtils;
import jflowmap.util.Pair;
import prefuse.data.Edge;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class FlowLinesLayerNode extends PNode {

  private final float FLOW_LINES_ALPHA = .1f; // .3f;

  private Map<Edge, Pair<FlowLine, FlowLine>> edgesToLines;
  private final FlowstratesView flowstratesView;
  private FlowLinesColoringMode flowLinesColoringMode = FlowLinesColoringMode.ORIGIN;
  private Map<String, Color> flowLinesPalette;

  private boolean showFlowLines = true;

  public FlowLinesLayerNode(FlowstratesView flowstratesView) {
    this.flowstratesView = flowstratesView;
  }

  public FlowLinesColoringMode getFlowLinesColoringMode() {
    return flowLinesColoringMode;
  }

  public void setFlowLinesColoringMode(FlowLinesColoringMode flowLinesColoringMode) {
    if (this.flowLinesColoringMode != flowLinesColoringMode) {
      this.flowLinesColoringMode = flowLinesColoringMode;
      updateFlowLinesPalette();
      updateFlowLineColors();
    }
  }

  public void setShowFlowLines(boolean showFlowLines) {
    if (this.showFlowLines != showFlowLines) {
      this.showFlowLines = showFlowLines;
      renewFlowLines();
    }
  }

  void renewFlowLines() {
    removeAllChildren();

    edgesToLines = Maps.newHashMap();

    if (showFlowLines) {
      for (Edge edge : flowstratesView.getVisibleEdges()) {
        FlowLine origin = createFlowLine(edge);
        FlowLine dest = createFlowLine(edge);
        edgesToLines.put(edge, Pair.of(origin, dest));
      }

      updateFlowLineColors();
      updateFlowLinePositionsAndVisibility();
    }
  }

  private FlowLine createFlowLine(Edge edge) {
    FlowLine line = new FlowLine();
    addChild(line);
    return line;
  }

  private void updateFlowLineColors() {
    if (showFlowLines) {
      for (Map.Entry<Edge, Pair<FlowLine, FlowLine>> e : edgesToLines.entrySet()) {
        Pair<FlowLine, FlowLine> p = e.getValue();
        Pair<Color, Color> colors = getFlowLineColors(e.getKey());

        FlowLine originLine = p.first();
        originLine.setColor(colors.first());
        originLine.setHighlightedColor(colors.second());

        FlowLine destLine = p.second();
        destLine.setColor(colors.first());
        destLine.setHighlightedColor(colors.second());
      }
    }
  }

  Pair<FlowLine, FlowLine> getFlowLinesOf(Edge edge) {
    return edgesToLines.get(edge);
  }


  private Pair<Color, Color> getFlowLineColors(Edge edge) {

    FlowstratesStyle style = flowstratesView.getStyle();
    FlowMapGraph flowMapGraph = flowstratesView.getFlowMapGraph();

    Color c;
    switch (flowLinesColoringMode) {
    case SAME_COLOR:
      return Pair.of(style.getFlowLineColor(), style.getFlowLineHighlightedColor());

    case ORIGIN:
      c = flowLinesPalette.get(flowMapGraph.getSourceNodeId(edge));
      return Pair.of(c, ColorUtils.setAlpha(c, 255));

    case DEST:
      c = flowLinesPalette.get(flowMapGraph.getTargetNodeId(edge));
      return Pair.of(c, ColorUtils.setAlpha(c, 255));

    }
    throw new AssertionError();
  }

  void hideAllFlowLines() {
    for (Edge edge : flowstratesView.getVisibleEdges()) {
      Pair<FlowLine, FlowLine> lines = edgesToLines.get(edge);
      if (lines != null) {
        lines.first().setVisible(false);
        lines.second().setVisible(false);
      }
    }
  }

  void updateFlowLinePositionsAndVisibility() {
    if (showFlowLines) {
      int row = 0;

      for (Edge edge : flowstratesView.getVisibleEdges()) {
        Pair<FlowLine, FlowLine> lines = edgesToLines.get(edge);
        Pair<PText, PText> labels = flowstratesView.getHeatmapLayer().getEdgeLabels(edge);

        if (lines != null  &&  labels != null) {
          updateFlowLine(row, edge, lines.first(), labels.first(), FlowEndpoint.ORIGIN);
          updateFlowLine(row, edge, lines.second(), labels.second(), FlowEndpoint.DEST);
        }

        row++;
      }

//      repaint();
    }
  }

  private void updateFlowLine(int row, Edge edge, FlowLine line, PText label, FlowEndpoint ep) {
    PCamera hmcam = flowstratesView.getHeatmapLayer().getHeatmapCamera();
    PBounds viewBounds = hmcam.getViewBounds();

    MapLayer mapLayer = flowstratesView.getMapLayer(ep);

    Point2D p0 = mapLayer.getCentroidPoint(edge);
    boolean visible = (p0 != null  &&  mapLayer.getMapLayerCamera().getViewBounds().contains(p0));

    if (visible) {
      Point2D.Double p = flowstratesView.getHeatmapLayer().getHeatmapFlowLineInPoint(row, ep);
      visible = (visible  &&  viewBounds.contains(p));
      if (visible) {
        mapLayer.getMapLayerCamera().viewToLocal(p0);
        hmcam.viewToLocal(p);
        Rectangle2D lb = hmcam.viewToLocal(label.getBounds());

        line.setPoint(0, p0.getX(), p0.getY());

        double x1 = p.x;
        double y1 = p.y + lb.getHeight() / 2;
        if (ep == FlowEndpoint.ORIGIN) {
          x1 -= lb.getWidth();
        } else {
          x1 += lb.getWidth();
        }
        line.setPoint(1, x1, y1);

        double x2 = p.x;
        double y2 = y1;
        line.setPoint(2, x2, y2);
      }
    }
    line.setVisible(visible);
    line.setPickable(false);
  }

  void updateFlowLinesPalette() {
    if (flowLinesColoringMode == FlowLinesColoringMode.SAME_COLOR) {
      if (flowLinesPalette != null)
        flowLinesPalette.clear();
      return;
    }

    FlowMapGraph flowMapGraph = flowstratesView.getFlowMapGraph();

    Set<String> ids = Sets.newHashSet();
    for (Edge e : flowstratesView.getVisibleEdges()) {

      switch (flowLinesColoringMode) {
        case ORIGIN:
          ids.add(flowMapGraph.getSourceNodeId(e));
          break;

        case DEST:
          ids.add(flowMapGraph.getTargetNodeId(e));
          break;
      }
    }
    flowLinesPalette = new HashMap<String, Color>(ids.size());
    Color[] palette = ColorUtils.createCategoryColors(ids.size(), FLOW_LINES_ALPHA);
    int i = 0;
    for (String origin : ids) {
      flowLinesPalette.put(origin, palette[i++]);
    }
  }

}
