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
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jflowmap.AbstractCanvasView;
import jflowmap.FlowMapColorSchemes;
import jflowmap.FlowMapGraph;
import jflowmap.FlowMapGraphSet;
import jflowmap.FlowTuple;
import jflowmap.data.FlowMapStats;
import jflowmap.data.FlowMapSummaries;
import jflowmap.data.MinMax;
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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
import edu.umd.cs.piccolox.nodes.PLine;
import edu.umd.cs.piccolox.swing.PScrollPane;

/**
 * @author Ilya Boyandin
 */
public class DuoTimelineView extends AbstractCanvasView {

  private int maxVisibleTuples = -1;
  private static final double MAP_SCALE = .65;
  private static final Font NODE_MARK_FONT = new Font("Arial", Font.PLAIN, 18);
  private static final Font GRAPH_ID_MARK_FONT = new Font("Arial", Font.PLAIN, 15);

  public static Logger logger = Logger.getLogger(DuoTimelineView.class);

  static final double cellWidth = 35;
  static final double cellHeight = 35;
  private static final boolean INTERPOLATE_COLORS = true;

  private final DuoTimelineStyle style = new DefaultDuoTimelineStyle();
  private final FlowMapGraphSet flowMapGraphs;
  private final PScrollPane scrollPane;

  private final JPanel controlPanel;
  private List<FlowTuple> tuples;

  public DuoTimelineView(FlowMapGraphSet flowMapGraphs, AreaMap areaMap) {
    this(flowMapGraphs, areaMap, -1);
  }

  public DuoTimelineView(FlowMapGraphSet flowMapGraphs, AreaMap areaMap, int maxVisibleTuples) {
    this.flowMapGraphs = flowMapGraphs;
    this.maxVisibleTuples = maxVisibleTuples;

    VisualCanvas canvas = getVisualCanvas();
    canvas.setBackground(style.getBackgroundColor());

//    canvas.setPanEventHandler(null);

    controlPanel = new JPanel();
    controlPanel.add(new JTextField());  // FROM filter
    scrollPane = new PScrollPane(canvas);
    scrollPane.setHorizontalScrollBarPolicy(PScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

//    getCamera().scale(.5);

    createAreaMaps(areaMap);
    createTupleRows();

    getCamera().addPropertyChangeListener(new CameraListener());
    createMapToMatrixLines();
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


  private Map<FlowTuple, Pair<PLine, PLine>> tupleLines;

  private VisualAreaMap sourceVisualAreaMap;
  private VisualAreaMap targetVisualAreaMap;

  private Map<String, Pair<PPath, PPath>> countryCentroids;

  private void createCountryCentroids() {
    countryCentroids = Maps.newHashMap();

    FlowMapGraph fmg = Iterables.getLast(flowMapGraphs.asList());
    Graph g = fmg.getGraph();

    FlowMapStats stats = fmg.getStats();
    MinMax xstats = stats.getNodeXStats();
    MinMax ystats = stats.getNodeYStats();
    double dotSize =
      Math.min(xstats.getMax() - xstats.getMin(), ystats.getMax() - ystats.getMin()) / 100;

    for (int i = 0, count = g.getNodeCount(); i < count; i++) {
      Node node = g.getNode(i);

      double x = node.getDouble(fmg.getXNodeAttr());
      double y = node.getDouble(fmg.getYNodeAttr());

      PPath fromPoint = createCentroidDot(sourceVisualAreaMap, x, y, dotSize);
      PPath toPoint = createCentroidDot(targetVisualAreaMap, x, y, dotSize);

      countryCentroids.put(FlowMapGraph.getNodeId(node), Pair.of(fromPoint, toPoint));
    }
  }

  private PPath createCentroidDot(VisualAreaMap visualAreaMap, double x, double y, double dotSize) {
    Point2D.Double centroid = new Point2D.Double(x, y);
    PPath dot = new PPath(new Ellipse2D.Double(-dotSize/2, -dotSize/2, dotSize, dotSize));
    dot.setX(visualAreaMap.getX() + centroid.x);
    dot.setY(visualAreaMap.getY() + centroid.y);
    dot.setPaint(ColorLib.getColor(100, 100, 100));
    dot.setStroke(null);
    visualAreaMap.addChild(dot);
    return dot;
  }

  private void createMapToMatrixLines() {
    tupleLines = Maps.newHashMap();
    BasicStroke lineStroke = new BasicStroke(2);
    for (FlowTuple tuple : tuples) {
      tupleLines.put(tuple, Pair.of(createMapToMatrixLine(lineStroke),
          createMapToMatrixLine(lineStroke)));
    }
  }

  private PLine createMapToMatrixLine(BasicStroke lineStroke) {
    PLine lineIn = new PLine();
    for (int i = 0; i < 3; i++) {
      lineIn.addPoint(i, 0, 0);
    }
    lineIn.setStrokePaint(style.getMapToMatrixLineLinesColor());
    lineIn.setStroke(lineStroke);
    getCamera().addChild(lineIn);
    return lineIn;
  }

  private void updateMapToMatrixLines() {
    PCamera camera = getCamera();
    PBounds viewBounds = camera.getViewBounds();
    int row = 0;
    for (FlowTuple tuple : tuples) {
      Pair<PLine, PLine> lines = tupleLines.get(tuple);

      PNode srcMapPoint = countryCentroids.get(tuple.getSrcNodeId()).first();
      PNode targetMapPoint = countryCentroids.get(tuple.getTargetNodeId()).second();

      Point2D.Double matrixIn = getMatrixInPoint(row);
      boolean inVis = viewBounds.contains(matrixIn);
      camera.viewToLocal(matrixIn);
      PLine lineIn = lines.first();
      lineIn.setVisible(inVis);
      lineIn.setPickable(inVis);
      lineIn.setPoint(0,
          srcMapPoint.getX() * MAP_SCALE + sourceVisualAreaMap.getXOffset(),
          srcMapPoint.getY() * MAP_SCALE + sourceVisualAreaMap.getYOffset());
      lineIn.setPoint(1, matrixIn.x - 10, matrixIn.y);
      lineIn.setPoint(2, matrixIn.x, matrixIn.y);


      Point2D.Double matrixOut = getMatrixOutPoint(row);
      boolean outVis = viewBounds.contains(matrixOut);
      camera.viewToLocal(matrixOut);
      PLine lineOut = lines.second();
      lineOut.setVisible(outVis);
      lineOut.setPickable(outVis);
      lineOut.setPoint(0,
          targetMapPoint.getX() * MAP_SCALE + targetVisualAreaMap.getXOffset(),
          targetMapPoint.getY() * MAP_SCALE + targetVisualAreaMap.getYOffset());
      lineOut.setPoint(1, matrixOut.x + 10, matrixOut.y);
      lineOut.setPoint(2, matrixOut.x, matrixOut.y);

      row++;
    }
  }

  private Point2D.Double getMatrixInPoint(int row) {
    return new Point2D.Double(-20, getTupleY(row) + cellHeight/2);
  }

  private Point2D.Double getMatrixOutPoint(int row) {
    return new Point2D.Double(20 + cellWidth * flowMapGraphs.size(), getTupleY(row) + cellHeight/2);
  }

  private class CameraListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
        final String prop = evt.getPropertyName();
        if (prop == PCamera.PROPERTY_VIEW_TRANSFORM) {
          updateMapToMatrixLines();
        } else if (prop == PCamera.PROPERTY_BOUNDS) {
//          anchorRightVisualAreaMap();
//          updateMapToMatrixLines();
//          fitInView();
        }
    }
}

  private void createAreaMaps(AreaMap areaMap) {
    sourceVisualAreaMap = new VisualAreaMap(mapColorScheme, areaMap);
    targetVisualAreaMap = new VisualAreaMap(mapColorScheme, areaMap);

    sourceVisualAreaMap.setScale(MAP_SCALE);
    targetVisualAreaMap.setScale(MAP_SCALE);

//    sourcesVisualAreaMap.translate(800, 0);

    addMouseOverListenersToMaps(sourceVisualAreaMap);
    addMouseOverListenersToMaps(targetVisualAreaMap);

    FlowMapSummaries.supplyNodesWithWeightSummaries(flowMapGraphs);
    updateMapColors(null);

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

  private void updateMapColors(FlowMapGraph fmg) {
    colorizeMap(sourceVisualAreaMap, fmg, false);
    colorizeMap(targetVisualAreaMap, fmg, true);
  }

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
      boolean incomingNotOutgoing) {
    if (fmg == null) {
      for (VisualArea va : PNodes.childrenOfType(visualAreaMap, VisualArea.class)) {
        va.setPaint(style.getMissingValueColor());
      }
    } else {
      String summaryNodeAttr = (incomingNotOutgoing ?
              FlowMapSummaries.NODE_COLUMN__SUM_INCOMING :
                FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING);
      MinMax mm = fmg.getStats().getNodeAttrStats(summaryNodeAttr);
      for (VisualArea va : PNodes.childrenOfType(visualAreaMap, VisualArea.class)) {
        Node node = FlowMapGraph.findNodeById(fmg.getGraph(), va.getArea().getId());
        if (node != null) {
          Double w = node.getDouble(summaryNodeAttr);
          if (w == null) w = Double.NaN;
          va.setPaint(getColorForWeight(w, mm));
        }
      }
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

  @Override
  public String getName() {
    return "DuoTimeline";
  }


  private void createTupleRows() {
    updateTuples();

//    ColorMap cm = new ColorMap(colors, /*-1*/0, 1);

    PInputEventListener tooltipListener = createTooltipListener(DTNode.class);
    PInputEventListener highlightListener = createMapToMatrixLineHighlightListener();

//    Iterable<FlowMapGraph> reversedFmgList = Iterables.reverse(flowMapGraphs.asList());

    List<FlowMapGraph> fmgList = Lists.newArrayList(flowMapGraphs.asList());
    Collections.sort(fmgList, FlowMapGraph.COMPARE_BY_GRAPH_IDS);

    PLayer layer = getVisualCanvas().getLayer();
    int row = 0, maxCol = 0;
    for (FlowTuple tuple : tuples) {
      MinMax wstats = flowMapGraphs.getStats().getEdgeWeightStats();
      int col = 0;

      double y = getTupleY(row);

      PText srcLabel = new PText(tuple.getSrcNodeLabel());
      srcLabel.setFont(NODE_MARK_FONT);
      srcLabel.setX(-srcLabel.getFullBoundsReference().getWidth() - 6);
      srcLabel.setY(y + (cellHeight - srcLabel.getFullBoundsReference().getHeight())/ 2);
      layer.addChild(srcLabel);

      for (FlowMapGraph fmg : fmgList) {
        Edge edge = tuple.getElementFor(fmg.getGraph());

        double x = col * cellWidth;

        DTNode rect = new DTNode(x, y, tuple, fmg, edge);
        double weight;
        if (edge == null) {
          weight = Double.NaN;
        } else {
          weight = fmg.getEdgeWeight(edge);
        }
        rect.setPaint(getColorForWeight(weight, wstats));
        rect.addInputEventListener(highlightListener);
        if (!Double.isNaN(weight)) {
          rect.addInputEventListener(tooltipListener);
        }
        layer.addChild(rect);

        col++;
        if (col > maxCol) maxCol = col;
      }

      PText targetLabel = new PText(tuple.getTargetNodeLabel());
      targetLabel.setFont(NODE_MARK_FONT);
      targetLabel.setX(cellWidth * maxCol + 6);
      targetLabel.setY(y + (cellHeight - targetLabel.getFullBoundsReference().getHeight())/ 2);
      layer.addChild(targetLabel);

      row++;
    }

    // Year marks
    int col = 0;
    for (FlowMapGraph fmg : fmgList) {
      PText graphIdMark = new PText(fmg.getId());
      graphIdMark.setFont(GRAPH_ID_MARK_FONT);
      graphIdMark.setX(col * cellWidth +
          (cellWidth - graphIdMark.getFullBoundsReference().getWidth())/2);
      graphIdMark.setY(-graphIdMark.getFont().getSize2D() - 2);
      layer.addChild(graphIdMark);
      col++;
    }

    layer.addChild(new PPath(new Rectangle2D.Double(0, 0, cellWidth * maxCol, cellHeight * row)));
  }

  private PTypedBasicInputEventHandler<DTNode> createMapToMatrixLineHighlightListener() {
    return new PTypedBasicInputEventHandler<DTNode>(DTNode.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        DTNode node = node(event);
        updateMapColors(node.getFlowMapGraph());
        node.moveToFront();
        node.setStroke(style.getSelectedTimelineCellStroke());
        node.setStrokePaint(style.getSelectedTimelineCellStrokeColor());
        Pair<PLine, PLine> lines = lines(event);
        lines.first().setStrokePaint(style.getMapToMatrixLineHighlightedColor());
        lines.second().setStrokePaint(style.getMapToMatrixLineHighlightedColor());
      }

      @Override
      public void mouseExited(PInputEvent event) {
        updateMapColors(null);
        DTNode node = node(event);
        node.setStroke(style.getTimelineCellStroke());
        node.setStrokePaint(style.getTimelineCellStrokeColor());
        Pair<PLine, PLine> lines = lines(event);
        lines.first().setStrokePaint(style.getMapToMatrixLineLinesColor());
        lines.second().setStrokePaint(style.getMapToMatrixLineLinesColor());
      }

      private Pair<PLine, PLine> lines(PInputEvent event) {
        return tupleLines.get(node(event).getTuple());
      }
    };
  }

  private Color getColorForWeight(double weight, MinMax wstats) {
    if (Double.isNaN(weight)) {
      return style.getMissingValueColor();
    }
    if (wstats.getMin() < 0  &&  wstats.getMax() > 0) {
      // use diverging color scheme
      return ColorLib.getColor(ColorUtils.colorFromMap(style.getDivergingValueColors(),
          wstats.normalizeLogAroundZero(weight), -1.0, 1.0, 255, INTERPOLATE_COLORS));
    } else {
      // use sequential color scheme
      return ColorLib.getColor(ColorUtils.colorFromMap(style.getSequentialValueColors(),
          wstats.normalizeLog(weight), 0.0, 1.0, 255, INTERPOLATE_COLORS));
    }
  }

  private double getTupleY(int row) {
    return row * cellHeight;
  }

  private void updateTuples() {
    tuples = flowMapGraphs.listFlowTuples(new Predicate<Edge>() {
      @Override
      public boolean apply(Edge e) {
        return true;
      }
    });
    Collections.sort(tuples, FlowTuple.COMPARE_MAX_WEIGHT);
    if (maxVisibleTuples >= 0) {
      if (tuples.size() > maxVisibleTuples) {
        tuples = tuples.subList(0, maxVisibleTuples);
      }
    }
  }

  @Override
  protected String getTooltipHeaderFor(PNode node) {
    return ((DTNode)node).getTooltipHeader();
  }

  @Override
  protected String getTooltipLabelsFor(PNode node) {
    return ((DTNode)node).getTooltipLabels();
  }

  @Override
  protected String getTooltipValuesFor(PNode node) {
    return ((DTNode)node).getTooltipValues();
  }

  @Override
  public void fitInView() {
    PBounds srcBounds = sourceVisualAreaMap.getFullBoundsReference();
    PBounds trgBounds = targetVisualAreaMap.getFullBoundsReference();

    PCamera camera = getCamera();
    PBounds viewBounds = camera.getViewBounds();
//    PBounds viewBounds = new PBounds(0, 0, getViewComponent().getWidth(), getViewComponent().getHeight());

    sourceVisualAreaMap.setOffset(
        0, (viewBounds.getHeight() - srcBounds.getHeight())/2

    );
    targetVisualAreaMap.setOffset(
        viewBounds.getMaxX() - srcBounds.getWidth()
        - scrollPane.getVerticalScrollBar().getWidth()
        ,  (viewBounds.getHeight() - trgBounds.getHeight())/2
        );

    srcBounds = sourceVisualAreaMap.getFullBoundsReference();
    trgBounds = targetVisualAreaMap.getFullBoundsReference();

    double middleGap = (trgBounds.getMinX() - srcBounds.getMaxX()) * .75;

    updateMapToMatrixLines();
    PBounds boundRect = getVisualCanvas().getLayer().getFullBounds();
    boundRect.height = boundRect.width * viewBounds.height / middleGap;
    camera.animateViewToCenterBounds(boundRect, true, 0);
  }

}
