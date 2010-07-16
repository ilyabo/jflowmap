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
import jflowmap.data.FlowMapSummaries;
import jflowmap.data.MinMax;
import jflowmap.models.map.AreaMap;
import jflowmap.util.ColorUtils;
import jflowmap.util.piccolo.PNodes;
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

  public static Logger logger = Logger.getLogger(DuoTimelineView.class);

  private static final double cellWidth = 35;
  private static final double cellHeight = 35;

  private final DuoTimelineStyle style = new DefaultDuoTimelineStyle();
  private final FlowMapGraphSet flowMapGraphs;
  private final PScrollPane scrollPane;

  private final JPanel controlPanel;
  private List<FlowTuple> tuples;

  public DuoTimelineView(FlowMapGraphSet flowMapGraphs, AreaMap areaMap) {
    this.flowMapGraphs = flowMapGraphs;

    VisualCanvas canvas = getVisualCanvas();
    canvas.setBackground(style.getBackgroundColor());

//    canvas.setPanEventHandler(null);

    controlPanel = new JPanel();
    controlPanel.add(new JTextField());  // FROM filter
    scrollPane = new PScrollPane(canvas);


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


  private Map<FlowTuple, PLine> tupleLines;

  private VisualAreaMap leftVisualAreaMap;
  private VisualAreaMap rightVisualAreaMap;

  private void createMapToMatrixLines() {
    tupleLines = Maps.newHashMap();

    int row = 0;
    Color lineColor = new Color(0f, 0f, 0f, .1f);
    PCamera camera = getCamera();
    for (FlowTuple tuple : tuples) {
      Point2D centroid = countryCentroids.get(tuple.getSrcNodeId());

      Point2D.Double lineTo = getTupleLineTo(row);
      camera.viewToLocal(lineTo);

      PLine line = new PLine();
      line.addPoint(0, centroid.getX(), centroid.getY());
      line.addPoint(1, lineTo.x - 10, lineTo.y);
      line.addPoint(2, lineTo.x, lineTo.y);
      line.setStrokePaint(lineColor);
      line.setVisible(false);
      line.setPickable(false);
//      line.setScale(.7);
      leftVisualAreaMap.addChild(line);

      tupleLines.put(tuple, line);

      row++;
    }
  }

  private void updateMapToMatrixLines() {
    PCamera camera = getCamera();
    PBounds viewBounds = camera.getViewBounds();
    int row = 0;
    BasicStroke lineStroke = new BasicStroke(2);
    for (FlowTuple tuple : tuples) {
      Point2D.Double lineTo = getTupleLineTo(row);
      PLine line = tupleLines.get(tuple);
      boolean vis = viewBounds.contains(lineTo);
      line.setVisible(vis);
      line.setPickable(vis);
      camera.viewToLocal(lineTo);
      line.setPoint(1, lineTo.x - 10, lineTo.y);
      line.setPoint(2, lineTo.x, lineTo.y);
      line.setStroke(lineStroke);

      row++;
    }
  }

  private Point2D.Double getTupleLineTo(int row) {
    return new Point2D.Double(-20, getTupleY(row) + cellHeight/2);
  }

  private class CameraListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
        final String prop = evt.getPropertyName();
        if (prop == PCamera.PROPERTY_VIEW_TRANSFORM) {
          updateMapToMatrixLines();
        } else if (prop == PCamera.PROPERTY_BOUNDS) {
//          anchorRightVisualAreaMap();
          updateMapToMatrixLines();
        }
    }
}

  private void createAreaMaps(AreaMap areaMap) {
    leftVisualAreaMap = new VisualAreaMap(mapColorScheme, areaMap);
    rightVisualAreaMap = new VisualAreaMap(mapColorScheme, areaMap);
    rightVisualAreaMap.translate(1150, 0);
//    leftVisualAreaMap.setScale(.7);

    addMouseOverListenersToMaps(leftVisualAreaMap);
    addMouseOverListenersToMaps(rightVisualAreaMap);

    FlowMapSummaries.supplyNodesWithWeightSummaries(flowMapGraphs);
    colorizeMap(leftVisualAreaMap, false);
    colorizeMap(rightVisualAreaMap, true);

    createCountryCentroids(leftVisualAreaMap);
    createCountryCentroids(rightVisualAreaMap);

    getCamera().addChild(leftVisualAreaMap);
    getCamera().addChild(rightVisualAreaMap);

//    anchorRightVisualAreaMap();
  }
//
//  private void anchorRightVisualAreaMap() {
//    PNodes.moveTo(rightVisualAreaMap, getCamera().getViewBounds().getMaxX() -
//        rightVisualAreaMap.getFullBoundsReference().getWidth(), 0);
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

  private void colorizeMap(VisualAreaMap visualAreaMap, boolean incomingNotOutgoing) {
    Map<String, Double> maxWeightMap = FlowMapSummaries.findMaxWeightForEachNode(
        flowMapGraphs, incomingNotOutgoing);
    MinMax mm = MinMax.createFor(maxWeightMap.values());

    for (VisualArea va : PNodes.childrenOfType(visualAreaMap, VisualArea.class)) {
      Double maxW = maxWeightMap.get(va.getArea().getId());
      if (maxW == null) maxW = Double.NaN;
      va.setPaint(getColorForWeight(maxW, mm));
    }
  }

  private Map<String, Point2D> countryCentroids;

  private void createCountryCentroids(VisualAreaMap visualAreaMap) {
    countryCentroids = Maps.newHashMap();

    FlowMapGraph fmg = Iterables.getLast(flowMapGraphs.asList());
    Graph g = fmg.getGraph();
    for (int i = 0, count = g.getNodeCount(); i < count; i++) {
      Node node = g.getNode(i);

      double x = node.getDouble(fmg.getXNodeAttr());
      double y = node.getDouble(fmg.getYNodeAttr());

      Point2D.Double centroid = new Point2D.Double(x, y);
      PPath dot = new PPath(new Ellipse2D.Double(centroid.x-1, centroid.y-1, 2, 2));
      dot.setPaint(ColorLib.getColor(100, 100, 100));
      dot.setStroke(null);
      visualAreaMap.addChild(dot);

      countryCentroids.put(FlowMapGraph.getNodeId(node), centroid);
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

  private static final int[] colors = new int[] {
      // ColorBrewer  OrRd (sequential)
      ColorLib.rgb(255, 247, 236), ColorLib.rgb(254, 232, 200),
      ColorLib.rgb(253, 212, 158), ColorLib.rgb(253, 187, 132),
      ColorLib.rgb(252, 141, 89), ColorLib.rgb(239, 101, 72),
      ColorLib.rgb(215, 48, 31), ColorLib.rgb(179, 0, 0),
      ColorLib.rgb(127, 0, 0)

      // ColorBrewer RdYlGn (diverging)
//      ColorLib.rgb(0, 104, 55),
//      ColorLib.rgb(26, 152, 80),
//      ColorLib.rgb(102, 189, 99),
//      ColorLib.rgb(166, 217, 106),
//      ColorLib.rgb(217, 239, 139),
//      ColorLib.rgb(255, 255, 191),
//      ColorLib.rgb(254, 224, 139),
//      ColorLib.rgb(253, 174, 97),
//      ColorLib.rgb(244, 109, 67),
//      ColorLib.rgb(215, 48, 39),
//      ColorLib.rgb(165, 0, 38)
  };

  private void createTupleRows() {
    updateTuples();

//    ColorMap cm = new ColorMap(colors, /*-1*/0, 1);

    PInputEventListener tooltipListener = createTooltipListener(DTNode.class);

    Iterable<FlowMapGraph> reversedFmgList = Iterables.reverse(flowMapGraphs.asList());
    PLayer layer = getVisualCanvas().getLayer();
    int row = 0, maxCol = 0;
    for (FlowTuple tuple : tuples) {
      MinMax wstats = flowMapGraphs.getStats().getEdgeWeightStats();
      int col = 0;

      double y = getTupleY(row);

      PText srcLabel = new PText(tuple.getSrcNodeId());
      srcLabel.setX(-srcLabel.getFullBoundsReference().getWidth() - 3);
      srcLabel.setY(y + (cellHeight - srcLabel.getFullBoundsReference().getHeight())/ 2);
      layer.addChild(srcLabel);

      for (FlowMapGraph fmg : reversedFmgList) {
        Edge edge = tuple.getElementFor(fmg.getGraph());

        double x = col * cellWidth;

        PNode rect = null;
        if (edge != null) {
          double weight = fmg.getEdgeWeight(edge);
          if (!Double.isNaN(weight)) {

  //          row.addChild(new HalfMoonsNode(cellWidth, cellHeight, wstats.normalize(weight), rightNormalizedValue));
  //          PPath circle = new PPath(new Ellipse2D.Double(x, y, cellWidth * nw, cellHeight * nw), null);


  //          double wh = Math.min(cellWidth, cellHeight);
  //          double r = Math.sqrt(Math.abs(nw)) * wh;
  //          PPath circle = new PPath(new Ellipse2D.Double(x + (cellWidth - r)/2, y + (cellHeight - r)/2, r, r), null);
  //          circle.setPaint(style.getFlowCircleColor());

            rect = new DTNode(x, y, weight, tuple, edge);
//            rect.setPaint(ColorLib.getColor(cm.getColor(nw)));
            rect.setPaint(getColorForWeight(weight, wstats));
            rect.addInputEventListener(tooltipListener);
          }
        }
        if (rect == null) {
          rect = createEmptyCellNode(x, y);
        }
        layer.addChild(rect);
        col++;
        if (col > maxCol) maxCol = col;
      }

      PText targetLabel = new PText(tuple.getTargetNodeId());
      targetLabel.setX(cellWidth * maxCol + 3);
      targetLabel.setY(y + (cellHeight - targetLabel.getFullBoundsReference().getHeight())/ 2);
      layer.addChild(targetLabel);

      row++;
    }

    // Year marks
    int col = 0;
    for (FlowMapGraph fmg : reversedFmgList) {
      PText graphIdMark = new PText(fmg.getId());
      graphIdMark.setX(col * cellWidth + (cellWidth - 20)/2);
      graphIdMark.setY(-graphIdMark.getFont().getSize2D() - 2);
      layer.addChild(graphIdMark);
      col++;
    }

    layer.addChild(new PPath(new Rectangle2D.Double(0, 0, cellWidth * maxCol, cellHeight * row)));
  }

  private Color getColorForWeight(double weight, MinMax wstats) {
    if (Double.isNaN(weight)) {
      return Color.lightGray;
    }
    return ColorLib.getColor(ColorUtils.colorFromMap(colors, wstats.normalizeLog(weight), 255));
  }

  private double getTupleY(int row) {
    return row * cellHeight;
  }

  private void updateTuples() {
    tuples = flowMapGraphs.listFlowTuples(new Predicate<Edge>() {
      @Override
      public boolean apply(Edge e) {
//        String nodeId = FlowMapGraph.getNodeId(e.getTargetNode());
//        return (nodeId.equals("CHE") || nodeId.equals("AUT"));

//        String nodeId = FlowMapGraph.getNodeId(e.getSourceNode());
//        return (nodeId.equals("IRQ") || nodeId.equals("AFG"));

        return true;
      }
    });
    Collections.sort(tuples, FlowTuple.COMPARE_MAX_WEIGHT);
    tuples = tuples.subList(0, 1000);
  }

  private static class DTNode extends PPath {
    private final double weight;
    private final FlowTuple tuple;
    private final Edge edge;

    public DTNode(double x, double y, double weight, FlowTuple tuple, Edge edge) {
      super(new Rectangle2D.Double(x, y, cellWidth, cellHeight), null);
      this.weight = weight;
      this.tuple = tuple;
      this.edge = edge;
    }

    public String getTooltipHeader() {
//      return tuple.getSrcNodeId() + "->" + tuple.getTargetNodeId() +
//             " " + FlowMapGraph.getGraphId(edge.getGraph());
      FlowMapGraph fmg = tuple.getFlowMapGraphSet().findFlowMapGraphFor(edge.getGraph());
      Node src = edge.getSourceNode();
      Node target = edge.getTargetNode();
      String nodeLabelAttr = fmg.getNodeLabelAttr();
      return src.getString(nodeLabelAttr) + " -> " + target.getString(nodeLabelAttr) +
            " " + FlowMapGraph.getGraphId(edge.getGraph());
    }

    public String getTooltipLabels() {
      return tuple.getFlowMapGraphSet().getAttrSpec().getEdgeWeightAttr() + ":";
    }

    public String getTooltipValues() {
      return Double.toString(
          tuple.getFlowMapGraphSet().findFlowMapGraphFor(edge.getGraph()).getEdgeWeight(edge));
    }

  }


  private PNode createEmptyCellNode(double x, double y) {
    PNode emptyNode = new PNode();
    emptyNode.setBounds(x, y, cellWidth, cellHeight);
    emptyNode.setPaint(Color.lightGray);
    return emptyNode;
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
    PCamera camera = getCamera();
    PBounds boundRect = getVisualCanvas().getLayer().getFullBounds();
    boundRect.height = boundRect.width * 1.5;
    camera.globalToLocal(boundRect);
    camera.animateViewToCenterBounds(boundRect, true, 0);
  }

}
