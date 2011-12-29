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

import static jflowmap.data.FlowMapGraphEdgeAggregator.getAggregateList;
import static jflowmap.data.FlowMapGraphEdgeAggregator.isAggregate;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import jflowmap.data.Nodes;
import jflowmap.util.piccolo.PTypedBasicInputEventHandler;
import prefuse.data.Edge;
import at.fhjoanneum.cgvis.plots.mosaic.MosaicPlotNode;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Ilya Boyandin
 */
public class FastHeatmapCursor extends PNode {

  private Point highlightedCell;
  private int highlightedColumn = -1;
  private final FastHeatmapLayer heatmapLayer;

  public FastHeatmapCursor(FastHeatmapLayer layer) {
    this.heatmapLayer = layer;
    setPickable(false);

    final PCamera camera = heatmapLayer.getCamera();
    camera.addPropertyChangeListener(PCamera.PROPERTY_BOUNDS, new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        // adjust cursor bounds
        setBounds(camera.getBoundsReference());
      }
    });
    camera.addInputEventListener(new PTypedBasicInputEventHandler<MosaicPlotNode>(MosaicPlotNode.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        setHighlightedCell(cell(event));
      }

      @Override
      public void mouseMoved(PInputEvent event) {
        setHighlightedCell(cell(event));
      }

      @Override
      public void mouseExited(PInputEvent event) {
        setHighlightedCell(null);
      }

      @Override
      public void mouseClicked(PInputEvent event) {
        focusOnCell(cell(event));
      }

      private Point cell(PInputEvent event) {
        Point2D pos = event.getCanvasPosition();
        heatmapLayer.getCamera().localToView(pos);
        return heatmapLayer.getHeatmapNode().pointToCell(pos);
      }
    });

    moveToFront();
  }

  private void focusOnCell(Point cell) {
    if (cell != null) {
      FlowstratesView fs = getFlowstratesView();
      FlowMapGraph fmg = fs.getFlowMapGraph();
      Edge edge = edgeOf(cell);

      if (isAggregate(edge)) {
        fs.getMapLayer(FlowEndpoint.ORIGIN).focusOnNodes(
            Nodes.nodeIdsOfEdges(getAggregateList(edge), FlowEndpoint.ORIGIN));

        fs.getMapLayer(FlowEndpoint.DEST).focusOnNodes(
            Nodes.nodeIdsOfEdges(getAggregateList(edge), FlowEndpoint.DEST));
      } else {
        fs.getMapLayer(FlowEndpoint.ORIGIN).focusOnNode(fmg.getSourceNodeId(edge));
        fs.getMapLayer(FlowEndpoint.DEST).focusOnNode(fmg.getTargetNodeId(edge));
      }

      showTooltipFor(cell);
    }
  }

  void setHighlightedCell(Point newCell) {
    if (newCell != highlightedCell) {
      if (highlightedCell != null) {
        setEdgeLinesVisible(highlightedCell, false); // hide previously shown line
      }

      if (newCell != null) {
        setEdgeLinesVisible(newCell, true);
        setVisible(true);
        showTooltipFor(newCell);
      } else {
        setVisible(false);
        getFlowstratesView().hideTooltip();
      }
      highlightedCell = newCell;
      repaint();
    }
  }

  void setHighlightedColumn(int columnIndex) {
    if (highlightedColumn != columnIndex) {
      highlightedColumn = columnIndex;

//      repaint();
      setVisible(false); setVisible(true);  // repaint() alone does not work for some reason
    }
  }

  public void clearHighlighted() {
    setHighlightedColumn(-1);
    setHighlightedCell(null);
  }

  private void showTooltipFor(Point cell) {
    Rectangle2D r = cellToRect(cell);
    Point2D pos = new Point2D.Double(r.getMaxX(), r.getMaxY());
    heatmapLayer.getCamera().viewToLocal(pos);

    FlowstratesView fs = getFlowstratesView();
    fs.showTooltip(pos, new TooltipText(fs.getFlowMapGraph(),
        fs.getVisibleEdge(cell.y), fs.getEdgeWeightAttr(cell.x)));
  }

  private void setEdgeLinesVisible(Point cell, boolean visible) {
    FlowstratesView fs = getFlowstratesView();
    Edge edge = edgeOf(cell);
    String weightAttr = weightAttr(cell);
    fs.getFlowLinesLayerNode().setFlowLinesOfEdgeHighlighted(edge, visible);
    fs.getMapLayer(FlowEndpoint.ORIGIN).updateOnHeatmapCellHover(edge, weightAttr, visible);
    fs.getMapLayer(FlowEndpoint.DEST).updateOnHeatmapCellHover(edge, weightAttr, visible);
  }

  private String weightAttr(Point cell) {
    int index = cell.x;
    return getFlowstratesView().getEdgeWeightAttr(index);
  }

  private Edge edgeOf(Point cell) {
    return getFlowstratesView().getVisibleEdge(cell.y);
  }

  private FlowstratesView getFlowstratesView() {
    return heatmapLayer.getFlowstratesView();
  }

  private Rectangle2D cellToRect(Point cell) {
    if (cell == null) {
      return null;
    }

    MosaicPlotNode heatmap = heatmapLayer.getHeatmapNode();
    int spacing = heatmap.getCellSpacing();
    int cw = heatmap.getCellWidth();
    int ch = heatmap.getCellHeight();

    final int x = ((int) heatmap.getX()) + spacing + (cell.x * (cw + spacing));
    final int y = ((int) heatmap.getY()) + spacing + (cell.y * (ch + spacing));
    return new Rectangle(x, y, cw, ch);
  }

  private Rectangle2D columnToRect(int column) {
    if (column < 0) {
      return null;
    }

    /*

    MosaicPlotNode heatmap = heatmapLayer.getHeatmapNode();
    int spacing = heatmap.getCellSpacing();
    int cw = heatmap.getCellWidth();
//    int ch = heatmap.getCellHeight();
    PBounds b = getVisiblePartOfHeatmapBounds(heatmap);

//    final int x = ((int) heatmap.getX()) + spacing + (column * (cw + spacing));
//    Rectangle r = new Rectangle(x, (int)b.y, cw, (int)b.height);

    b.x = b.x + spacing + (column * (cw + spacing));
    b.width = cw;

    return b;
    */

    MosaicPlotNode heatmap = heatmapLayer.getHeatmapNode();
    int spacing = heatmap.getCellSpacing();
    int cw = heatmap.getCellWidth();
    int ch = heatmap.getCellHeight();

    final int x = ((int) heatmap.getX()) + spacing + (column * (cw + spacing));
    final int y = ((int) heatmap.getY()) + spacing + 0;
    final int h = spacing + (getFlowstratesView().getVisibleEdges().size() * (ch + spacing));

    return new Rectangle(x, y, cw, h);

  }

  private PBounds getVisiblePartOfHeatmapBounds(MosaicPlotNode heatmap) {
    PBounds b = new PBounds();
    Rectangle.intersect(heatmap.getFullBoundsReference(), heatmapLayer.getActualViewBounds(), b);
    return b;
  }

  @Override
  protected void paint(PPaintContext paintContext) {
    Graphics2D g2 = paintContext.getGraphics();
    PCamera camera = heatmapLayer.getCamera();
    FlowstratesStyle style = getFlowstratesView().getStyle();

    if (highlightedCell != null) {
      Rectangle2D r = cellToRect(highlightedCell);
      camera.viewToLocal(r);

      int y = (int) r.getY();
      int x = (int) r.getX();
      int w = (int) Math.max(r.getWidth(), 1);
      int h = (int) Math.max(r.getHeight(), 1);

      g2.setStroke(style.getHeatmapSelectedCellStroke());
      g2.setColor(style.getHeatmapSelectedCellStrokeColor());
      g2.drawRect(x, y, w - 1, h - 1);
    }

    if (highlightedColumn != -1) {

      Rectangle2D r = columnToRect(highlightedColumn);

      camera.viewToLocal(r);

      Shape oldClip = g2.getClip();
      g2.setClip(camera.getBounds());

      g2.setStroke(style.getHeatmapSelectedCellStroke());
      g2.setColor(style.getHeatmapSelectedCellStrokeColor());
      g2.drawRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());

      g2.setClip(oldClip);
    }
  }


}
