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
import java.awt.Font;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import jflowmap.geom.GeomUtils;
import jflowmap.util.Pair;
import jflowmap.util.piccolo.PLabel;
import jflowmap.util.piccolo.PNodes;
import jflowmap.util.piccolo.PPaths;
import jflowmap.util.piccolo.PTypedBasicInputEventHandler;
import prefuse.data.Edge;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class SimpleHeatmapLayer extends AbstractHeatmapLayer {

  static final double cellWidth = 40;
  static final double cellHeight = 40;
  private static final Font HEATMAP_ROW_LABELS_FONT = new Font("Arial", Font.PLAIN, 22 /*18*/);
  private static final Font HEATMAP_COLUMN_LABELS_FONT = new Font("Arial", Font.PLAIN, 25 /*19*/);

  private final PInputEventListener heatmapCellTooltipListener;
  private final PInputEventListener heatmapCellHoverListener;

  private final PPath columnHighlightRect;
  private final PNode heatmapNode;
  private final Map<Edge, Pair<PText, PText>> edgesToLabels = Maps.newHashMap();

  public SimpleHeatmapLayer(FlowstratesView flowstratesView) {
    super(flowstratesView);

    heatmapNode = new PNode();
    addChild(heatmapNode);

    columnHighlightRect = PPaths.rect(0, 0, 1, 1);
    columnHighlightRect.setPaint(null);
    FlowstratesStyle style = getFlowstratesView().getStyle();
    columnHighlightRect.setStrokePaint(style.getHeatmapSelectedCellStrokeColor());
    columnHighlightRect.setStroke(style.getHeatmapSelectedCellStroke());
    columnHighlightRect.setVisible(false);
    addChild(columnHighlightRect);

    heatmapCellTooltipListener = getFlowstratesView().createTooltipListener(HeatmapCell.class);
    heatmapCellHoverListener = createHeatMapCellHoverListener();
  }

  @Override
  public Dimension2D getEdgeLabelBounds(Edge edge, FlowEndpoint ep) {
    Pair<PText, PText> labels = edgesToLabels.get(edge);
    switch (ep) {
    case ORIGIN: return labels.first().getBounds().getSize();
    case DEST: return labels.second().getBounds().getSize();
    default: throw new AssertionError();
    }
  }

  private void createColumnLabels() {
    List<String> attrNames = getFlowMapGraph().getEdgeWeightAttrs();
    // String cp = StringUtils.getCommonPrefix(attrNames.toArray(new String[attrNames.size()]));
    int col = 0;
    for (String attr : attrNames) {
      PLabel label = createColumnLabelNode(col, attr);
      PBounds b = label.getFullBoundsReference();
      label.setX(col * cellWidth);
      label.setY(-b.getHeight() / 1.5);
      label.rotateAboutPoint(-Math.PI * .65 / 2, label.getX(), label.getY());
      heatmapNode.addChild(label);

      label.addInputEventListener(new PBasicInputEventHandler() {
        @Override
        public void mouseEntered(PInputEvent event) {
          PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
          label.moveToFront();
          final String attr = label.getName();

          Iterable<HeatmapCell> cells = getHeatMapColumnCells(attr);
          updateMapsOnHeatmapColumnHover(attr, true);

          columnHighlightRect.setBounds(
              GeomUtils.growRect(PNodes.fullBoundsOf(cells), 2));
          columnHighlightRect.moveToFront();
          columnHighlightRect.setVisible(true);
          columnHighlightRect.repaint();

          getFlowstratesView().getFlowLinesLayerNode().hideAllFlowLines();
        }

        @Override
        public void mouseExited(PInputEvent event) {
          PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
          columnHighlightRect.setVisible(false);
          updateMapsOnHeatmapColumnHover(label.getName(), false);

          getFlowstratesView().getFlowLinesLayerNode().updateFlowLines();
        }
      });
      col++;
    }
  }

  private PLabel createColumnLabelNode(int col, String weightAttr) {
    PLabel label = new PLabel(weightAttr);
    label.setName(weightAttr);
    label.setFont(HEATMAP_COLUMN_LABELS_FONT);
    label.setPaint(Color.white);
    return label;
  }

  /**
   * @return The point in the heatmap camera view coords.
   */
  @Override
  public Point2D.Double getFlowLineInPoint(int row, FlowEndpoint ep) {
    switch (ep) {

    case ORIGIN:
      return new Point2D.Double(-10, getTupleY(row) + SimpleHeatmapLayer.cellHeight / 2);

    case DEST:
      int numCols = getFlowMapGraph().getEdgeWeightAttrsCount();
      return new Point2D.Double(
          10 + SimpleHeatmapLayer.cellWidth * numCols, getTupleY(row) + SimpleHeatmapLayer.cellHeight / 2);

    default:
      throw new AssertionError();
    }
  }

  double getTupleY(int row) {
    return row * SimpleHeatmapLayer.cellHeight;
  }

  @Override
  public void renew() {
    super.renew();

    heatmapNode.removeAllChildren();

    int row = 0, maxCol = 0;

    edgesToLabels.clear();

    for (Edge edge : getFlowstratesView().getVisibleEdges()) {
      int col = 0;

      double y = getTupleY(row);

      // "from" label
      PText srcLabel = new PText(getFlowMapGraph().getNodeLabel(edge.getSourceNode()));
      srcLabel.setFont(HEATMAP_ROW_LABELS_FONT);
      srcLabel.setX(-srcLabel.getFullBoundsReference().getWidth() - 6);
      srcLabel.setY(y + (cellHeight - srcLabel.getFullBoundsReference().getHeight()) / 2);
      heatmapNode.addChild(srcLabel);

      // "value" box node
      for (String weightAttr : getFlowMapGraph().getEdgeWeightAttrs()) {
        double x = col * cellWidth;

        HeatmapCell cell = new HeatmapCell(
            this, x, y, cellWidth, cellHeight, weightAttr,
            getFlowstratesView().getAggLayers().getFlowMapGraphOf(edge), edge);

        cell.addInputEventListener(heatmapCellHoverListener);
        // if (!Double.isNaN(cell.getWeight())) {
        cell.addInputEventListener(heatmapCellTooltipListener);
        // }
        heatmapNode.addChild(cell);

        col++;
        if (col > maxCol)
          maxCol = col;
      }

      // "to" label
      PText targetLabel = new PText(getFlowMapGraph().getNodeLabel(edge.getTargetNode()));
      targetLabel.setFont(HEATMAP_ROW_LABELS_FONT);
      targetLabel.setX(cellWidth * maxCol + 6);
      targetLabel.setY(y + (cellHeight - targetLabel.getFullBoundsReference().getHeight()) / 2);
      heatmapNode.addChild(targetLabel);

      edgesToLabels.put(edge, Pair.of(srcLabel, targetLabel));

      row++;
    }

    createColumnLabels();

    repaint();
  }

  private Iterable<HeatmapCell> getHeatMapColumnCells(final String attr) {
    return
      Iterables.filter(
        PNodes.childrenOfType(heatmapNode, HeatmapCell.class),
        new Predicate<HeatmapCell>() {
          @Override
          public boolean apply(HeatmapCell cell) {
            return attr.equals(cell.getWeightAttr());
          }
        });
  }

  @Override
  public void updateColors() {
    for (HeatmapCell cell : PNodes.childrenOfType(heatmapNode, HeatmapCell.class)) {
      cell.updateColor();
    }
  }

  @Override
  public void fitInView(boolean animate, boolean whole) {
    Rectangle2D bounds = heatmapNode.getFullBounds();
    PCamera camera = getCamera();
    if (!whole  &&  bounds.getHeight() > bounds.getWidth() * 10) {
      PBounds camb = camera.getViewBounds();
      bounds = new Rectangle2D.Double(
          bounds.getX(), bounds.getY(), bounds.getWidth(),
          bounds.getWidth() * (camb.height / camb.width));
    }
    camera.animateViewToCenterBounds(
        GeomUtils.growRectByRelativeSize(bounds, .025, .1, .025, .1), true,
        FlowstratesView.fitInViewDuration(animate));
  }


  void updateMapsOnHeatmapCellHover(HeatmapCell cell, boolean hover) {
    updateMapsOnHeatmapCellHover(cell.getEdge(), cell.getWeightAttr(), hover);
  }

  void updateMapsOnHeatmapCellHover(Edge edge, String weightAttr, boolean hover) {
    FlowstratesView fs = getFlowstratesView();
    fs.getMapLayer(FlowEndpoint.ORIGIN).updateOnHeatmapCellHover(edge, weightAttr, hover);
    fs.getMapLayer(FlowEndpoint.DEST).updateOnHeatmapCellHover(edge, weightAttr, hover);
  }

  PTypedBasicInputEventHandler<HeatmapCell> createHeatMapCellHoverListener() {
    return new PTypedBasicInputEventHandler<HeatmapCell>(HeatmapCell.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        HeatmapCell cell = node(event);

        FlowstratesStyle style = getFlowstratesView().getStyle();

        // highlight cell
        cell.moveToFront();
        cell.setStroke(style.getHeatmapSelectedCellStroke());
        cell.setStrokePaint(style.getHeatmapSelectedCellStrokeColor());

        getFlowstratesView().getFlowLinesLayerNode().setFlowLinesOfEdgeHighlighted(cell.getEdge(), true);

        updateMapsOnHeatmapCellHover(cell, true);
      }

      @Override
      public void mouseExited(PInputEvent event) {
        HeatmapCell cell = node(event);
        FlowstratesStyle style = getFlowstratesView().getStyle();

        cell.setStroke(style.getTimelineCellStroke());
        cell.setStrokePaint(style.getTimelineCellStrokeColor());

        getFlowstratesView().getFlowLinesLayerNode().setFlowLinesOfEdgeHighlighted(cell.getEdge(), false);

        updateMapsOnHeatmapCellHover(cell, false);
      }


      @Override
      public void mouseClicked(PInputEvent event) {
//        if (event.isControlDown()) {
//          getFlowstratesView().setEgdeForSimilaritySorting(node(event).getEdge());
//        }
        HeatmapCell cell = node(event);
        Edge edge = cell.getEdge();
        FlowMapGraph fmg = cell.getFlowMapGraph();

        String srcId = fmg.getSourceNodeId(edge);
        String targetId = fmg.getTargetNodeId(edge);

        getFlowstratesView().getMapLayer(FlowEndpoint.ORIGIN).focusOnNode(srcId);
        getFlowstratesView().getMapLayer(FlowEndpoint.DEST).focusOnNode(targetId);
      }
    };
  }

}
