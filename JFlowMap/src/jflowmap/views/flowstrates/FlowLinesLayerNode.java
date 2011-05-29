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
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import jflowmap.util.ColorUtils;
import prefuse.data.Edge;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class FlowLinesLayerNode extends PNode {

  private final float FLOW_LINES_ALPHA = .1f; // .3f;

  private final Map<Edge, FlowLine> edgesToOriginLines = Maps.newHashMap();
  private final Map<Edge, FlowLine> edgesToDestLines = Maps.newHashMap();
  private final FlowstratesView flowstratesView;
  private FlowLinesColoringMode flowLinesColoringMode = FlowLinesColoringMode.ORIGIN;
  private Map<String, Color> flowLinesPalette;
  private final List<FlowLine> flowLinePool = Lists.newArrayList();
  private final Set<Edge> highlightedEdges = Sets.newLinkedHashSet();

  private boolean showAllFlowLines = false;

  public FlowLinesLayerNode(FlowstratesView flowstratesView) {
    this.flowstratesView = flowstratesView;
  }

  public FlowLinesColoringMode getFlowLinesColoringMode() {
    return flowLinesColoringMode;
  }

  public void setFlowLinesColoringMode(FlowLinesColoringMode flowLinesColoringMode) {
    if (this.flowLinesColoringMode != flowLinesColoringMode) {
      this.flowLinesColoringMode = flowLinesColoringMode;
      updatePalette();
      updateFlowLineColorsOnly();
    }
  }

  public boolean getShowAllFlowLines() {
    return showAllFlowLines;
  }

  public void setShowAllFlowLines(boolean showFlowLines) {
    if (this.showAllFlowLines != showFlowLines) {
      this.showAllFlowLines = showFlowLines;
      renewFlowLines();
    }
  }

  void renewFlowLines() {
    removeAllChildren();

    edgesToOriginLines.clear();
    edgesToDestLines.clear();

    updateFlowLines();
  }

  private void updateFlowLineColorsOnly() {
    Iterable<Edge> edges;
    if (showAllFlowLines) {
      edges = flowstratesView.getVisibleEdges();
    } else {
      edges = highlightedEdges;
    }
    for (Edge e : edges) {
      updateFlowLineColors(e, FlowEndpoint.ORIGIN);
      updateFlowLineColors(e, FlowEndpoint.DEST);
    }
  }

  private void updateFlowLineColors(Edge e, FlowEndpoint ep) {
    FlowLine line = getEdgesToFlowLinesMap(ep).get(e);
    if (line != null) {
      line.setColor(getFlowLineColor(e));
      line.setHighlightedColor(getFlowLineHighlightedColor(e));
      line.setHighlighted((highlightedEdges.contains(e)));
    }
  }

  Map<Edge, FlowLine> getEdgesToFlowLinesMap(FlowEndpoint ep) {
    switch (ep) {
    case ORIGIN: return edgesToOriginLines;
    case DEST: return edgesToDestLines;
    default: throw new AssertionError();
    }
  }

  public void setFlowLinesOfEdgeHighlighted(Edge edge, boolean highlighted) {
    if (highlighted) {
      if (!highlightedEdges.contains(edge)) {
        highlightedEdges.add(edge);
      }
    } else {
      highlightedEdges.remove(edge);
    }

    FlowLine originLine = edgesToOriginLines.get(edge);
    if (originLine != null) {
      originLine.setHighlighted(highlighted);
    }

    FlowLine destLine = edgesToDestLines.get(edge);
    if (destLine != null) {
      destLine.setHighlighted(highlighted);
    }

    updateFlowLinesOf(edge);
  }

  private Color getFlowLineColor(Edge edge) {
    FlowstratesStyle style = flowstratesView.getStyle();
    if (showAllFlowLines) {
      FlowMapGraph fmg = flowstratesView.getFlowMapGraph();
      switch (flowLinesColoringMode) {
        case SAME_COLOR: return style.getFlowLineColor();
        case ORIGIN: return flowLinesPalette.get(fmg.getSourceNodeId(edge));
        case DEST: return flowLinesPalette.get(fmg.getTargetNodeId(edge));
      }
      throw new AssertionError();
    } else {
      return style.getFlowLineColor();
    }
  }

  private Color getFlowLineHighlightedColor(Edge edge) {
    FlowstratesStyle style = flowstratesView.getStyle();
      if (showAllFlowLines) {
      FlowMapGraph fmg = flowstratesView.getFlowMapGraph();

      switch (flowLinesColoringMode) {
        case SAME_COLOR: return style.getFlowLineHighlightedColor();
        case ORIGIN: return ColorUtils.setAlpha(flowLinesPalette.get(fmg.getSourceNodeId(edge)), 255);
        case DEST: return ColorUtils.setAlpha(flowLinesPalette.get(fmg.getTargetNodeId(edge)), 255);
      }
      throw new AssertionError();
    } else {
      return style.getFlowLineHighlightedColor();
    }
  }

  void hideAllFlowLines() {
    for (Edge edge : flowstratesView.getVisibleEdges()) {
      FlowLine originLine = edgesToOriginLines.get(edge);
      if (originLine != null) {
        originLine.setVisible(false);
      }

      FlowLine destLine = edgesToDestLines.get(edge);
      if (destLine != null) {
        destLine.setVisible(false);
      }
    }
  }

  void updateFlowLines() {
    if (showAllFlowLines) {
      List<Edge> visibleEdges = flowstratesView.getVisibleEdges();
      for (int row = 0, size = visibleEdges.size(); row < size; row++) {
        updateFlowLinesOf(row, visibleEdges.get(row));
      }
    } else {
      for (Edge e : highlightedEdges) {
        updateFlowLinesOf(e);
      }
    }
  }

  private void updateFlowLinesOf(Edge edge) {
    updateFlowLinesOf(flowstratesView.getVisibleEdgeIndex(edge), edge);
  }

  private void updateFlowLinesOf(int row, Edge edge) {
    updateFlowLine(row, edge, FlowEndpoint.ORIGIN);
    updateFlowLine(row, edge, FlowEndpoint.DEST);
  }

  private void updateFlowLine(int row, Edge edge, FlowEndpoint ep) {
    boolean visible = (showAllFlowLines  ||  highlightedEdges.contains(edge));
    if (visible) {
      PCamera temporalViewCamera = flowstratesView.getTemporalLayer().getCamera();
      PBounds temporalViewBounds = flowstratesView.getTemporalLayer().getActualViewBounds();

      MapLayer mapLayer = flowstratesView.getMapLayer(ep);

      Point2D centrp = mapLayer.getCentroidPoint(edge);
      visible = (centrp != null  &&  mapLayer.isPointVisible(centrp));

      if (visible) {
        Point2D p = flowstratesView.getTemporalLayer().getFlowLineInPoint(row, ep);
        visible = (p != null  &&  temporalViewBounds.contains(p));

        if (visible) {
          mapLayer.getCamera().viewToLocal(centrp);
          temporalViewCamera.viewToLocal(p);

          Dimension2D lb = temporalViewCamera.viewToLocal(
              flowstratesView.getTemporalLayer().getEdgeLabelBounds(edge, ep));

          FlowLine line = getOrCreateFlowLine(edge, ep);
          line.setPoint(0, centrp.getX(), centrp.getY());

          double x1 = p.getX();
          double y1 = p.getY() + lb.getHeight() / 2;
          if (ep == FlowEndpoint.ORIGIN) {
            x1 -= lb.getWidth();
          } else {
            x1 += lb.getWidth();
          }
          line.setPoint(1, x1, y1);

          double x2 = p.getX();
          double y2 = y1;
          line.setPoint(2, x2, y2);

          line.setVisible(true);
        }
      }
    }

    if (!visible) {
      releaseFlowLine(edge, ep);
    }
  }

  private void releaseFlowLine(Edge edge, FlowEndpoint ep) {
    FlowLine line = getEdgesToFlowLinesMap(ep).remove(edge);
    if (line != null) {
      removeChild(line);
      flowLinePool.add(line);
      // TODO: clean if there are too many objects
    }
  }

  private FlowLine getOrCreateFlowLine(Edge edge, FlowEndpoint ep) {
    Map<Edge, FlowLine> map = getEdgesToFlowLinesMap(ep);
    FlowLine line = map.get(edge);
    if (line == null) {
      if (!flowLinePool.isEmpty()) {
        line = flowLinePool.remove(flowLinePool.size() - 1);
        line.reset();
      } else {
        line = new FlowLine();
      }
      addChild(line);
      map.put(edge, line);
    }
    updateFlowLineColors(edge, ep);
    return line;
  }


  void updatePalette() {
    if (flowLinesColoringMode == FlowLinesColoringMode.SAME_COLOR) {
      if (flowLinesPalette != null)
        flowLinesPalette.clear();
      return;
    }
    createPaletteForNodes(getNodeIdsToColorBy());
  }

  private void createPaletteForNodes(Set<String> nodeIds) {
    flowLinesPalette = new HashMap<String, Color>(nodeIds.size());
    Color[] palette = ColorUtils.createCategoryColors(nodeIds.size(), FLOW_LINES_ALPHA);
    int i = 0;
    for (String nodeId : nodeIds) {
      flowLinesPalette.put(nodeId, palette[i++]);
    }
  }

  private Set<String> getNodeIdsToColorBy() {
    FlowMapGraph fmg = flowstratesView.getFlowMapGraph();

    Set<String> ids = Sets.newHashSet();
    for (Edge e : flowstratesView.getVisibleEdges()) {
      switch (flowLinesColoringMode) {
        case ORIGIN:
          ids.add(fmg.getSourceNodeId(e));
          break;

        case DEST:
          ids.add(fmg.getTargetNodeId(e));
          break;
      }
    }
    return ids;
  }

}
