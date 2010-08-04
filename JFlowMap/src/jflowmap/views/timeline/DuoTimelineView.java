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

package jflowmap.views.timeline;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import jflowmap.AbstractCanvasView;
import jflowmap.ColorSchemes;
import jflowmap.EdgeDirection;
import jflowmap.FlowMapColorSchemes;
import jflowmap.FlowMapGraph;
import jflowmap.data.FlowMapSummaries;
import jflowmap.data.MinMax;
import jflowmap.geo.MapProjections;
import jflowmap.models.map.AreaMap;
import jflowmap.util.ColorUtils;
import jflowmap.util.Pair;
import jflowmap.util.piccolo.PNodes;
import jflowmap.util.piccolo.PTypedBasicInputEventHandler;
import jflowmap.views.ColorCodes;
import jflowmap.views.VisualCanvas;
import jflowmap.views.flowmap.ColorSchemeAware;
import jflowmap.views.flowmap.VisualArea;
import jflowmap.views.flowmap.VisualAreaMap;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.util.ColorLib;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.nodes.PLine;
import edu.umd.cs.piccolox.swing.PScrollPane;

/**
 * @author Ilya Boyandin
 */
public class DuoTimelineView extends AbstractCanvasView {

  private int maxVisibleTuples = -1;
  private static final Font NODE_MARK_FONT = new Font("Arial", Font.PLAIN, 18);
  private static final Font GRAPH_ID_MARK_FONT = new Font("Arial", Font.PLAIN, 15);

  public static Logger logger = Logger.getLogger(DuoTimelineView.class);

  static final double cellWidth = 35;
  static final double cellHeight = 35;
  private boolean interpolateColors = true;

  private final DuoTimelineStyle style = new DefaultDuoTimelineStyle();
  private final FlowMapGraph flowMapGraph;
  private final PScrollPane scrollPane;

  private final JPanel controlPanel;
  private boolean useWeightDifferences = false;

  private ColorSchemes sequentialColorScheme = ColorSchemes.OrRd;
  private ColorSchemes divergingColorScheme = ColorSchemes.RdBu5;

  private final PInputEventListener tooltipListener = createTooltipListener(DTCell.class);
  private final PInputEventListener highlightListener = createMapToMatrixLineHighlightListener();
  private final PNode heatmapLayer;
  private final PNode mapToMatrixLinesLayer;

  private Map<Edge, Pair<PLine, PLine>> edgeLines;

  private VisualAreaMap sourceVisualAreaMap;
  private VisualAreaMap targetVisualAreaMap;

  private Map<String, Pair<PPath, PPath>> countryCentroids;
  private List<Edge> visibleEdges;
  private Predicate<Edge> edgeFilter;


  public DuoTimelineView(FlowMapGraph flowMapGraph, AreaMap areaMap) {
    this(flowMapGraph, areaMap, -1);
  }

  public DuoTimelineView(FlowMapGraph flowMapGraph, AreaMap areaMap, int maxVisibleTuples) {
    this.flowMapGraph = flowMapGraph;
    this.maxVisibleTuples = maxVisibleTuples;

    VisualCanvas canvas = getVisualCanvas();
    canvas.setBackground(style.getBackgroundColor());
    canvas.setPanEventHandler(null);
    canvas.setMinZoomScale(1e-2);
    canvas.setMaxZoomScale(1.0);

    controlPanel = new DuoTimelineControlPanel(this);
    scrollPane = new PScrollPane(canvas);
    scrollPane.setHorizontalScrollBarPolicy(PScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    FlowMapSummaries.supplyNodesWithWeightSummaries(flowMapGraph);

    createAreaMaps(areaMap);
    heatmapLayer = new PNode();
    getVisualCanvas().getLayer().addChild(heatmapLayer);
    getVisualCanvas().setDefaultRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);

    mapToMatrixLinesLayer = new PNode();
    getCamera().addChild(mapToMatrixLinesLayer);

    renewHeatmap();


    getCamera().addPropertyChangeListener(new CameraListener());
  }

  @Override
  public String getName() {
    return "DuoTimeline";
  }


  public void setEdgeFilter(Predicate<Edge> edgeFilter) {
    this.edgeFilter = edgeFilter;
    visibleEdges = null;
    renewHeatmap();
    fitInView();
  }

  private List<Edge> getVisibleEdges() {
    if (visibleEdges == null) {
      List<Edge> edges = new ArrayList<Edge>(flowMapGraph.getGraph().getEdgeCount());
      for (Edge edge : flowMapGraph.edges()) {
        if (edgeFilter == null  ||  edgeFilter.apply(edge)) {
          edges.add(edge);
        }
      }
      Collections.sort(edges, Collections.reverseOrder(flowMapGraph.createMaxWeightComparator()));

      if (maxVisibleTuples >= 0) {
        if (edges.size() > maxVisibleTuples) {
          edges = edges.subList(0, maxVisibleTuples);
        }
      }

      visibleEdges = edges;
    }

    return visibleEdges;
  }


  public void setUseWeightDifferences(boolean value) {
    if (useWeightDifferences != value) {
      useWeightDifferences = value;
      updateHeatmapColors();
    }
  }

  public boolean getUseWeightDifferences() {
    return useWeightDifferences;
  }

  public void setDivergingColorScheme(ColorSchemes divergingColorScheme) {
    if (this.divergingColorScheme != divergingColorScheme) {
      this.divergingColorScheme = divergingColorScheme;
      updateHeatmapColors();
    }
  }

  public ColorSchemes getDivergingColorScheme() {
    return divergingColorScheme;
  }

  public void setSequentialColorScheme(ColorSchemes sequentialColorScheme) {
    if (this.sequentialColorScheme != sequentialColorScheme) {
      this.sequentialColorScheme = sequentialColorScheme;
      updateHeatmapColors();
    }
  }

  public ColorSchemes getSequentialColorScheme() {
    return sequentialColorScheme;
  }

  public void setInterpolateColors(boolean interpolateColors) {
    if (this.interpolateColors != interpolateColors) {
      this.interpolateColors = interpolateColors;
      updateHeatmapColors();
    }
  }

  public boolean getInterpolateColors() {
    return interpolateColors;
  }

  public FlowMapGraph getFlowMapGraph() {
    return flowMapGraph;
  }

  private PCamera getCamera() {
    return getVisualCanvas().getCamera();
  }

  private final ColorSchemeAware mapColorScheme = new ColorSchemeAware() {
    @Override
    public Color getColor(ColorCodes code) {
      return FlowMapColorSchemes.INVERTED.get(code);
    }
  };


  private void createCountryCentroids() {
    countryCentroids = Maps.newHashMap();

    Graph g = flowMapGraph.getGraph();

    flowMapGraph.addEdgeWeightDifferenceColumns();

//    FlowMapStats stats = flowMapGraph.getStats();
//    MinMax xstats = stats.getNodeXStats();
//    MinMax ystats = stats.getNodeYStats();
    double dotSize = .25;
//      Math.min(xstats.getMax() - xstats.getMin(), ystats.getMax() - ystats.getMin()) / 100;

    for (int i = 0, count = g.getNodeCount(); i < count; i++) {
      Node node = g.getNode(i);

      double lon = node.getDouble(flowMapGraph.getXNodeAttr());
      double lat = node.getDouble(flowMapGraph.getYNodeAttr());

      Point2D fp = sourceVisualAreaMap.getMapProjection().project(lon, lat);
      Point2D tp = targetVisualAreaMap.getMapProjection().project(lon, lat);

      PPath fromPoint = createCentroidDot(sourceVisualAreaMap, fp.getX(), fp.getY(), dotSize);
      PPath toPoint = createCentroidDot(targetVisualAreaMap, tp.getX(), tp.getY(), dotSize);

      countryCentroids.put(FlowMapGraph.getNodeId(node), Pair.of(fromPoint, toPoint));
    }
  }

  private PPath createCentroidDot(VisualAreaMap visualAreaMap, double x, double y, double dotSize) {
    PPath dot = new PPath(new Ellipse2D.Double(x - dotSize/2, y - dotSize/2, dotSize, dotSize));
    dot.setPaint(ColorLib.getColor(100, 100, 100));
    dot.setStroke(null);
    visualAreaMap.addChild(dot);
    return dot;
  }

  private void renewMapToMatrixLines() {
    mapToMatrixLinesLayer.removeAllChildren();

    edgeLines = Maps.newHashMap();
    BasicStroke lineStroke = new BasicStroke(2);
    for (Edge edge : getVisibleEdges()) {
      edgeLines.put(edge, Pair.of(createMapToMatrixLine(lineStroke),
          createMapToMatrixLine(lineStroke)));
    }

    updateMapToMatrixLines();
  }

  private PLine createMapToMatrixLine(BasicStroke lineStroke) {
    PLine lineIn = new PLine();
    for (int i = 0; i < 3; i++) {
      lineIn.addPoint(i, 0, 0);
    }
    lineIn.setStrokePaint(style.getMapToMatrixLineLinesColor());
    lineIn.setStroke(lineStroke);
    mapToMatrixLinesLayer.addChild(lineIn);
    return lineIn;
  }

  private void updateMapToMatrixLines() {
    PCamera camera = getCamera();
    PBounds viewBounds = camera.getViewBounds();
    int row = 0;
    for (Edge edge : getVisibleEdges()) {
      Pair<PLine, PLine> lines = edgeLines.get(edge);

      PNode srcMapPoint = countryCentroids.get(
          FlowMapGraph.getNodeId(edge.getSourceNode())).first();
      PBounds srcB = srcMapPoint.getFullBounds();
      srcMapPoint.localToGlobal(srcB);


      PNode targetMapPoint = countryCentroids.get(
          FlowMapGraph.getNodeId(edge.getTargetNode())).second();
      PBounds targetB = targetMapPoint.getFullBounds();
      targetMapPoint.localToGlobal(targetB);

      Point2D.Double matrixIn = getMatrixInPoint(row);
      boolean inVis = viewBounds.contains(matrixIn);
      camera.viewToLocal(matrixIn);
      PLine lineIn = lines.first();
      lineIn.setVisible(inVis);
      lineIn.setPickable(inVis);
      lineIn.setPoint(0, srcB.getCenterX(), srcB.getCenterY());
      lineIn.setPoint(1, matrixIn.x - 10, matrixIn.y);
      lineIn.setPoint(2, matrixIn.x, matrixIn.y);


      Point2D.Double matrixOut = getMatrixOutPoint(row);
      boolean outVis = viewBounds.contains(matrixOut);
      camera.viewToLocal(matrixOut);
      PLine lineOut = lines.second();
      lineOut.setVisible(outVis);
      lineOut.setPickable(outVis);
      lineOut.setPoint(0, targetB.getCenterX(), targetB.getCenterY());
      lineOut.setPoint(1, matrixOut.x + 10, matrixOut.y);
      lineOut.setPoint(2, matrixOut.x, matrixOut.y);

      row++;
    }

    mapToMatrixLinesLayer.repaint();
  }

  private Point2D.Double getMatrixInPoint(int row) {
    return new Point2D.Double(-20, getTupleY(row) + cellHeight/2);
  }

  private Point2D.Double getMatrixOutPoint(int row) {
    return new Point2D.Double(20 + cellWidth * flowMapGraph.getEdgeWeightAttrsCount(),
        getTupleY(row) + cellHeight/2);
  }

  private class CameraListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
        final String prop = evt.getPropertyName();
        if (prop == PCamera.PROPERTY_VIEW_TRANSFORM) {
          updateMapToMatrixLines();
        } else if (prop == PCamera.PROPERTY_BOUNDS) {
          fitInView();
        }
    }
  }

  private void createAreaMaps(AreaMap areaMap) {
    sourceVisualAreaMap = new VisualAreaMap(mapColorScheme, areaMap, MapProjections.MERCATOR);
    targetVisualAreaMap = new VisualAreaMap(mapColorScheme, areaMap, MapProjections.MERCATOR);

//    sourcesVisualAreaMap.translate(800, 0);

    addMouseOverListenersToMaps(sourceVisualAreaMap);
    addMouseOverListenersToMaps(targetVisualAreaMap);

//    updateMapColors(null);

    createCountryCentroids();

    getCamera().addChild(sourceVisualAreaMap);
    getCamera().addChild(targetVisualAreaMap);

//    anchorRightVisualAreaMap();
  }
//
//  private void anchorRightVisualAreaMap() {
//    PNodes.moveTo(sourcesVisualAreaMap, getCamera().getViewBounds().getMaxX() -
//        sourcesVisualAreaMap.getFullBoundsReference().getWidth(), 0);
//  }

//  private void updateMapColors(FlowMapGraph fmg) {
//    colorizeMap(sourceVisualAreaMap, fmg, EdgeDirection.INCOMING);
//    colorizeMap(targetVisualAreaMap, fmg, EdgeDirection.OUTGOING);
//  }

  private void addMouseOverListenersToMaps(VisualAreaMap visualAreaMap) {
    PBasicInputEventHandler listener = new PBasicInputEventHandler() {
      @Override
      public void mouseEntered(PInputEvent event) {
        VisualArea va = PNodes.getAncestorOfType(event.getPickedNode(), VisualArea.class);
        if (va != null) {
//          va.setStrokePaint(ColorLib.getColor(254, 99, 95, 200));
          va.setStrokePaint(ColorLib.getColor(150, 150, 150));
//          va.setStroke(new BasicStroke(1));
        }
      }
      @Override
      public void mouseExited(PInputEvent event) {
        VisualArea va = PNodes.getAncestorOfType(event.getPickedNode(), VisualArea.class);
        if (va != null) {
          va.setStrokePaint(mapColorScheme.getColor(ColorCodes.AREA_STROKE));
//          va.setStroke(new BasicStroke(1));
        }
      }
    };
    for (VisualArea va : PNodes.childrenOfType(visualAreaMap, VisualArea.class)) {
      va.addInputEventListener(listener);
    }
  }

  private void colorizeMap(VisualAreaMap visualAreaMap, FlowMapGraph fmg,
      String weightAttrName, EdgeDirection dir) {
    if (fmg == null) {
      for (VisualArea va : PNodes.childrenOfType(visualAreaMap, VisualArea.class)) {
        va.setPaint(style.getMissingValueColor());
      }
    } else {
      String weightSumAttr = FlowMapSummaries.getWeightSummaryNodeAttr(weightAttrName, dir);
      MinMax mm = fmg.getStats().getNodeAttrStats(weightSumAttr);

      for (VisualArea va : PNodes.childrenOfType(visualAreaMap, VisualArea.class)) {
        Node node = FlowMapGraph.findNodeById(fmg.getGraph(), va.getArea().getId());
        if (node != null) {
          Double w = node.getDouble(weightSumAttr);
          if (w == null) w = Double.NaN;
          va.setPaint(getColorForWeight(w, mm));
        }
      }
    }
  }

  public Color getColorForWeight(double weight, MinMax wstats) {
    DuoTimelineStyle style = getStyle();
    if (Double.isNaN(weight)) {
      return style.getMissingValueColor();
    }
    if (wstats.getMin() < 0  &&  wstats.getMax() > 0) {
      // use diverging color scheme
      return ColorLib.getColor(ColorUtils.colorFromMap(divergingColorScheme.getColors(),
          wstats.normalizeLogAroundZero(weight), -1.0, 1.0, 255, interpolateColors));
    } else {
      // use sequential color scheme
      return ColorLib.getColor(ColorUtils.colorFromMap(sequentialColorScheme.getColors(),
          wstats.normalizeLog(weight), 0.0, 1.0, 255, interpolateColors));
    }
  }

  @Override
  public JComponent getViewComponent() {
    return scrollPane;
  }

  @Override
  public JComponent getControls() {
    return controlPanel;
  }


  private void renewHeatmap() {
    heatmapLayer.removeAllChildren();

    int row = 0, maxCol = 0;

    for (Edge edge : getVisibleEdges()) {
      int col = 0;

      double y = getTupleY(row);

      // "from" label
      PText srcLabel = new PText(flowMapGraph.getNodeLabel(edge.getSourceNode()));
      srcLabel.setFont(NODE_MARK_FONT);
      srcLabel.setX(-srcLabel.getFullBoundsReference().getWidth() - 6);
      srcLabel.setY(y + (cellHeight - srcLabel.getFullBoundsReference().getHeight())/ 2);
      heatmapLayer.addChild(srcLabel);

      // "value" box node
      for (String weightAttr : flowMapGraph.getEdgeWeightAttrNames()) {
        double x = col * cellWidth;

        DTCell cell = new DTCell(this, x, y, weightAttr, flowMapGraph, edge);

        cell.addInputEventListener(highlightListener);
//        if (!Double.isNaN(cell.getWeight())) {
          cell.addInputEventListener(tooltipListener);
//        }
          heatmapLayer.addChild(cell);

        col++;
        if (col > maxCol) maxCol = col;
      }

      // "to" label
      PText targetLabel = new PText(flowMapGraph.getNodeLabel(edge.getTargetNode()));
      targetLabel.setFont(NODE_MARK_FONT);
      targetLabel.setX(cellWidth * maxCol + 6);
      targetLabel.setY(y + (cellHeight - targetLabel.getFullBoundsReference().getHeight())/ 2);
      heatmapLayer.addChild(targetLabel);

      row++;
    }

    createYearMarks();
    renewMapToMatrixLines();

//    layer.addChild(new PPath(new Rectangle2D.Double(0, 0, cellWidth * maxCol, cellHeight * row)));
  }

  private void createYearMarks() {
    // Year marks
    int col = 0;
    for (String attr : flowMapGraph.getEdgeWeightAttrNames()) {
      PText graphIdMark = new PText(attr);
      graphIdMark.setFont(GRAPH_ID_MARK_FONT);
      graphIdMark.setX(col * cellWidth +
          (cellWidth - graphIdMark.getFullBoundsReference().getWidth())/2);
      graphIdMark.setY(-graphIdMark.getFont().getSize2D() - 2);
      heatmapLayer.addChild(graphIdMark);
      col++;
    }
  }


  private void updateHeatmapColors() {
    for (DTCell cell : PNodes.childrenOfType(heatmapLayer, DTCell.class)) {
      cell.updateColor();
    }
    getVisualCanvas().repaint();
  }


  private PTypedBasicInputEventHandler<DTCell> createMapToMatrixLineHighlightListener() {
    return new PTypedBasicInputEventHandler<DTCell>(DTCell.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        DTCell node = node(event);
//        updateMapColors(node.getFlowMapGraph());
        node.moveToFront();
        node.setStroke(style.getSelectedTimelineCellStroke());
        node.setStrokePaint(style.getSelectedTimelineCellStrokeColor());
        Pair<PLine, PLine> lines = lines(event);
        lines.first().setStrokePaint(style.getMapToMatrixLineHighlightedColor());
        lines.second().setStrokePaint(style.getMapToMatrixLineHighlightedColor());
      }

      @Override
      public void mouseExited(PInputEvent event) {
//        updateMapColors(null);
        DTCell node = node(event);
        node.setStroke(style.getTimelineCellStroke());
        node.setStrokePaint(style.getTimelineCellStrokeColor());
        Pair<PLine, PLine> lines = lines(event);
        lines.first().setStrokePaint(style.getMapToMatrixLineLinesColor());
        lines.second().setStrokePaint(style.getMapToMatrixLineLinesColor());
      }

      private Pair<PLine, PLine> lines(PInputEvent event) {
        return edgeLines.get(node(event).getEdge());
      }
    };
  }

  public DuoTimelineStyle getStyle() {
    return style;
  }

  private double getTupleY(int row) {
    return row * cellHeight;
  }

  @Override
  protected String getTooltipHeaderFor(PNode node) {
    return ((DTCell)node).getTooltipHeader();
  }

  @Override
  protected String getTooltipLabelsFor(PNode node) {
    return ((DTCell)node).getTooltipLabels();
  }

  @Override
  protected String getTooltipValuesFor(PNode node) {
    return ((DTCell)node).getTooltipValues();
  }

  enum Align {

  }

  @Override
  public void fitInView() {
    PNodes.adjustStickyNodeToCameraSize(getCamera(), sourceVisualAreaMap, -1, 0, .3, .9);
    PNodes.adjustStickyNodeToCameraSize(getCamera(), targetVisualAreaMap, +1, 0, .3, .9);

    updateMapToMatrixLines();

    PCamera camera = getCamera();
    PBounds viewBounds = camera.getViewBounds();
    PBounds boundRect = getVisualCanvas().getLayer().getFullBounds();
    double middleGap = camera.getViewBounds().getWidth() * .3;
    boundRect.height = boundRect.width * viewBounds.height / middleGap;

    camera.animateViewToCenterBounds(boundRect, true, 0);

    getVisualCanvas().setViewZoomPoint(viewBounds.getCenter2D());
  }

}
