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
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

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
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class HeatmapLayer extends PLayer {

  static final double cellWidth = 40;
  static final double cellHeight = 40;
  private static final Font HEATMAP_ROW_LABELS_FONT = new Font("Arial", Font.PLAIN, 22 /*18*/);
  private static final Font HEATMAP_COLUMN_LABELS_FONT = new Font("Arial", Font.PLAIN, 25 /*19*/);

  private final PInputEventListener heatmapCellTooltipListener;
  private final PInputEventListener heatmapCellHoverListener;

  private final PCamera heatmapCamera;
  private final PPath columnHighlightRect;
  private final PNode heatmapNode;
  private final FlowstratesView flowstratesView;
  private Map<Edge, Pair<PText, PText>> edgesToLabels;

  public HeatmapLayer(FlowstratesView flowstratesView) {
    this.flowstratesView = flowstratesView;
    heatmapCamera = new PCamera();
    heatmapCamera.addLayer(this);

    heatmapNode = new PNode();
    addChild(heatmapNode);

    columnHighlightRect = PPaths.rect(0, 0, 1, 1);
    columnHighlightRect.setStroke(new PFixedWidthStroke(3));
    columnHighlightRect.setPaint(null);
//    columnHighlightRect.setStrokePaint(style.getHeatmapSelectedCellStrokeColor());
    columnHighlightRect.setStrokePaint(Color.cyan);
    columnHighlightRect.setVisible(false);
    addChild(columnHighlightRect);

    heatmapCellTooltipListener = flowstratesView.createTooltipListener(HeatmapCell.class);
    heatmapCellHoverListener = createHeatMapCellHoverListener();

  }

  public FlowMapGraph getFlowMapGraph() {
    return flowstratesView.getFlowMapGraph();
  }

  public PCamera getHeatmapCamera() {
    return heatmapCamera;
  }

  public Pair<PText, PText> getEdgeLabels(Edge edge) {
    return edgesToLabels.get(edge);
  }

  private void createColumnLabels() {
    List<String> attrNames = getFlowMapGraph().getEdgeWeightAttrs();
    // String cp = StringUtils.getCommonPrefix(attrNames.toArray(new String[attrNames.size()]));
    int col = 0;
    for (String attr : attrNames) {
      // attr = attr.substring(cp.length());
      PLabel label = new PLabel(attr);
      label.setName(attr);
      label.setFont(HEATMAP_COLUMN_LABELS_FONT);
      PBounds b = label.getFullBoundsReference();
      double x = col * cellWidth; // + (cellWidth - b.getWidth()) / 2;
      double y = -b.getHeight() / 1.5;
      label.setPaint(Color.white);
      label.rotateAboutPoint(-Math.PI * .65 / 2, x, y);
      label.setX(x);
      label.setY(y);
//      label.setX(5 + col * 6.3);
//      label.setY(col * cellWidth + (cellWidth - b.getWidth()) / 2);
//      label.translate(5, col * cellWidth + (cellWidth - b.getWidth()) / 2);
      heatmapNode.addChild(label);

      label.addInputEventListener(new PBasicInputEventHandler() {
        @Override
        public void mouseEntered(PInputEvent event) {
          PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
          label.moveToFront();
          final String attr = label.getName();

          Iterable<HeatmapCell> cells = getHeatMapColumnCells(attr);
          flowstratesView.updateMapsOnHeatmapColumnHover(attr, true);

          columnHighlightRect.setBounds(
              GeomUtils.growRect(PNodes.fullBoundsOf(cells), 2));
          columnHighlightRect.moveToFront();
          columnHighlightRect.setVisible(true);
          columnHighlightRect.repaint();
        }

        @Override
        public void mouseExited(PInputEvent event) {
          PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
          columnHighlightRect.setVisible(false);
          flowstratesView.updateMapsOnHeatmapColumnHover(label.getName(), false);
        }
      });
      col++;
    }
  }

  Point2D.Double getMatrixInPoint(int row) {
    return new Point2D.Double(-10, getTupleY(row) + HeatmapLayer.cellHeight / 2);
  }

  Point2D.Double getMatrixOutPoint(int row) {
    return new Point2D.Double(10 + HeatmapLayer.cellWidth * getFlowMapGraph().getEdgeWeightAttrsCount(), getTupleY(row)
        + HeatmapLayer.cellHeight / 2);
  }

  double getTupleY(int row) {
    return row * HeatmapLayer.cellHeight;
  }

  public void renewHeatmap() {
    heatmapNode.removeAllChildren();

    int row = 0, maxCol = 0;

    edgesToLabels = Maps.newHashMap();

    for (Edge edge : flowstratesView.getVisibleEdges()) {
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
            flowstratesView.getAggLayers().getFlowMapGraphOf(edge), edge);

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

    // heatmapLayer.getHeatmapCamera().setViewBounds(heatmapNode.getFullBounds());

    createColumnLabels();
    flowstratesView.renewFlowtiLines();
    flowstratesView.updateLegend();

    repaint();
    // layer.addChild(new PPath(new Rectangle2D.Double(0, 0, cellWidth * maxCol, cellHeight *
    // row)));
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

  void updateHeatmapColors() {
    for (HeatmapCell cell : PNodes.childrenOfType(heatmapNode, HeatmapCell.class)) {
      cell.updateColor();
    }
    flowstratesView.updateLegend();
    flowstratesView.getVisualCanvas().repaint();
  }


  public Color getColorFor(HeatmapCell cell) {
    ValueType valueType = flowstratesView.getValueType();

    String attr = valueType.getColumnValueAttr(
        cell.getFlowMapGraph().getAttrSpec(),
        cell.getWeightAttr());

    double value = cell.getEdge().getDouble(attr);
    return flowstratesView.getColorFor(value);
  }

  public void fitHeatMapInView() {
    PBounds heatmapBounds = getFullBounds();
    if (heatmapBounds.height > heatmapBounds.width * 10) {
      heatmapBounds.height = heatmapBounds.width * heatmapCamera.getViewBounds().height
          / heatmapCamera.getWidth();
    }
    heatmapCamera.setViewBounds(GeomUtils.growRectByPercent(heatmapBounds, .025, .1, .025, .1));
  }


  PTypedBasicInputEventHandler<HeatmapCell> createHeatMapCellHoverListener() {
    return new PTypedBasicInputEventHandler<HeatmapCell>(HeatmapCell.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        HeatmapCell cell = node(event);

        FlowstratesStyle style = flowstratesView.getStyle();

        // highlight cell
        cell.moveToFront();
        cell.setStroke(style.getSelectedTimelineCellStroke());
        cell.setStrokePaint(style.getHeatmapSelectedCellStrokeColor());

        // highlight flow lines
        Pair<FlowLine, FlowLine> lines = lines(event);
        if (lines != null) {
          lines.first().setHighlighted(true);
          lines.second().setHighlighted(true);
        }

        flowstratesView.updateMapsOnHeatmapCellHover(cell, true);
      }

      @Override
      public void mouseExited(PInputEvent event) {
        HeatmapCell cell = node(event);
        FlowstratesStyle style = flowstratesView.getStyle();

        cell.setStroke(style.getTimelineCellStroke());
        cell.setStrokePaint(style.getTimelineCellStrokeColor());

        Pair<FlowLine, FlowLine> lines = lines(event);
        if (lines != null) {
          lines.first().setHighlighted(false);
          lines.second().setHighlighted(false);
        }

        flowstratesView.updateMapsOnHeatmapCellHover(cell, false);
      }

      private Pair<FlowLine, FlowLine> lines(PInputEvent event) {
        return flowstratesView.getFlowLinesOf(node(event).getEdge());
      }


      @Override
      public void mouseClicked(PInputEvent event) {
        if (event.isControlDown()) {
          flowstratesView.setEgdeForSimilaritySorting(node(event).getEdge());
        }
      }
    };
  }

}
