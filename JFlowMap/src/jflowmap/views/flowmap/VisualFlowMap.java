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

package jflowmap.views.flowmap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Window;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jflowmap.FlowMapGraph;
import jflowmap.IView;
import jflowmap.bundling.ForceDirectedBundlerParameters;
import jflowmap.bundling.ForceDirectedEdgeBundler;
import jflowmap.data.FlowMapStats;
import jflowmap.data.SeqStat;
import jflowmap.geo.MapProjection;
import jflowmap.geo.MapProjections;
import jflowmap.geom.GeomUtils;
import jflowmap.geom.Point;
import jflowmap.util.piccolo.POutlinedText;
import jflowmap.views.ColorCodes;
import jflowmap.views.IFlowMapColorScheme;
import jflowmap.views.Legend;
import jflowmap.views.MapBackgroundImage;
import jflowmap.views.PTooltip;
import jflowmap.views.VisualCanvas;
import jflowmap.views.flowstrates.ValueType;
import jflowmap.views.map.PGeoMap;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Node;
import at.fhj.utils.misc.ProgressTracker;
import at.fhj.utils.misc.TaskCompletionListener;
import at.fhj.utils.swing.ProgressDialog;
import at.fhj.utils.swing.ProgressWorker;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PInterpolatingActivity;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class VisualFlowMap extends PNode implements ColorSchemeAware {

  private static final long serialVersionUID = 1L;
  private static Logger logger = Logger.getLogger(VisualFlowMap.class);
  private final Font CAPTION_FONT = new Font("Arial", Font.BOLD, 20);

  private static final boolean SHOW_SPLINE_POINTS = false;

  public static final int PROPERTY_CODE_FLOW_WEIGHT_ATTR = 1 << 11;
  public static final String PROPERTY_FLOW_WEIGHT_ATTR = "flowWeightAttr";

  public enum Attributes {
    NODE_SELECTION
  }
  private final PTooltip tooltipBox;
  private PBounds nodeBounds;

  private final PNode edgeLayer;
//  private final PNode nodeLayer;

  private final VisualFlowMapModel model;
  private List<VisualNode> visualNodes;
  private List<VisualEdge> visualEdges;
  private Map<Node, VisualNode> nodesToVisuals;
  private Map<Edge, VisualEdge> edgesToVisuals;
  private final IView view;

  private PGeoMap areaMap;
  private boolean bundled;

  private final VisualEdgePaintFactory visualEdgePaintFactory;
  private final VisualEdgeStrokeFactory visualEdgeStrokeFactory;
  private final Legend visualLegend;

  private String flowWeightAttr;
  private final MapProjection mapProjection;
  private IFlowMapColorScheme colorScheme;
  private PNode mapBackgroundImage;
  private final POutlinedText flowWeightAttrLabel;


  public VisualFlowMap(final IView view, VisualFlowMapModel model, boolean showLegend,
      MapProjection proj, String flowWeightAttr, IFlowMapColorScheme colorScheme) {
    this.view = view;
    this.mapProjection = proj;
    this.flowWeightAttr = flowWeightAttr;
    this.colorScheme = colorScheme;

    this.model = model;

//    double minWeight = flowMapGraph.getAttrSpec().getWeightFilterMin();
//    if (!Double.isNaN(minWeight)) {
//      visualFlowMapModel.setEdgeWeightFilterMin(minWeight);
//    }

    visualEdgePaintFactory = new VisualEdgePaintFactory(this);
    visualEdgeStrokeFactory = new VisualEdgeStrokeFactory(this);

//    nodeLayer = new PNode();

    edgeLayer = new PNode();
    addChild(edgeLayer);

    createVisuals();

//    addChild(nodeLayer);

    tooltipBox = createTooltip();
    tooltipBox.setVisible(false);

    visualLegend = new Legend(
        getColor(ColorCodes.LEDGEND_BOX_PAINT),
        getColor(ColorCodes.LEDGEND_TEXT), new FlowMapLegendItemProducer(this, 5));
    setLegendVisible(showLegend);

    initModelChangeListeners(model);

    getCamera().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == PCamera.PROPERTY_BOUNDS) {
          fitInCameraView();
          updateFlowWeightAttrLabel();
        } else
        if (evt.getPropertyName() == PCamera.PROPERTY_VIEW_TRANSFORM) {
          hideTooltip();
          updateNodePositions();
        }
      }
    });

    flowWeightAttrLabel = new POutlinedText(getValueAttr());
//    flowWeightAttrLabel.setTextPaint(new Color(150, 150, 150));
    flowWeightAttrLabel.setTextPaint(colorScheme.get(ColorCodes.FLOW_ATTR_LABEL));
    flowWeightAttrLabel.setOutlinePaint(new Color(80, 80, 80));
    flowWeightAttrLabel.setFont(CAPTION_FONT);
//    flowWeightAttrLabel.setTransparency(0.9f);
    getCamera().addChild(flowWeightAttrLabel);
    flowWeightAttrLabel.setVisible(false);
  }

  public VisualCanvas getVisualCanvas() {
    return view.getVisualCanvas();
  }

  protected PTooltip createTooltip() {
    return new PTooltip();
  }

  private void createVisuals() {
    createNodeVisuals();
    createEdgeVisuals();
    MapBackgroundImage mapImage = model.getMapBackgroundImage();
    if (mapImage != null) {
      mapBackgroundImage = mapImage.createImageNode();
      addChild(mapBackgroundImage);
      mapBackgroundImage.moveToBack();
    }
  }

  public void setFlowWeightAttr(String attr) {
    setFlowWeightAttr(attr, true);
  }

  private void setFlowWeightAttr(String attr, boolean doUpdate) {
    String oldValue = flowWeightAttr;
    if (!oldValue.equals(attr)) {
      logger.info("Setting flow weight attr to '" + attr + "'");
      flowWeightAttr = attr;
      resetBundling();
      updateFlowWeightAttrLabel();
      if (doUpdate) {
        updateVisualEdges();
        updateVisualEdgeOrdering();
      }
      firePropertyChange(PROPERTY_CODE_FLOW_WEIGHT_ATTR, PROPERTY_FLOW_WEIGHT_ATTR, oldValue, attr);
    }
  }

  public void setFlowWeightAttrLabelVisibile(boolean visible) {
    flowWeightAttrLabel.setVisible(visible);
  }

  public void updateFlowWeightAttrLabel() {
    POutlinedText label = flowWeightAttrLabel;
    if (label.getVisible()) {
      PBounds cb = getCamera().getBoundsReference();
      label.setText(flowWeightAttr);

      float height = (float)(cb.height * 0.17);
      float fontSize = label.getFont().getSize2D();
      if (fontSize != height) {
        label.setFont(label.getFont().deriveFont(height));
        flowWeightAttrLabel.setOutlineStroke(new BasicStroke(height * 0.02f));
      }

//      flowWeightAttrLabel.setTextPaint(colorScheme.get(ColorCodes.FLOW_ATTR_LABEL));
      label.setBounds(
//          cb.getMaxX() - label.getWidth() - 3,
          cb.getX() + 3,
          cb.y + (cb.height - height - 2), label.getWidth(), height);
    }
  }

  /**
   * This method returns the selected attr without taking <code>valueType</code>
   * into account. Use {@link #getFlowWeightAttr()} if you need the attribute of
   * the selected valueType.
   */
  public String getFlowWeightAttr() {
    return flowWeightAttr;
  }

  public String getValueAttr() {
    return getValueAttrFor(flowWeightAttr);
  }

  private String getValueAttrFor(String attr) {
    return model.getValueType().getColumnValueAttr(
        model.getFlowMapGraph().getAttrSpec(), attr);
  }

  private double getValueOf(VisualEdge ve, String attr) {
    return ve.getEdge().getDouble(getValueAttrFor(attr));
  }

  public PGeoMap getAreaMap() {
    return areaMap;
  }

  @Override
  public String getName() {
    return getFlowMapGraph().getId();
  }

  public FlowMapGraph getFlowMapGraph() {
    return model.getFlowMapGraph();
  }

  public void setLegendVisible(boolean visible) {
    visualLegend.setVisible(visible);
  }

  public Legend getVisualLegend() {
    return visualLegend;
  }

  public PTooltip getTooltipBox() {
    return tooltipBox;
  }

  public void addNodesToCamera() {
    getCamera().addChild(tooltipBox);
    getCamera().addChild(visualLegend);
  }

  public void removeNodesFromCamera() {
    getCamera().removeChild(tooltipBox);
    getCamera().removeChild(visualLegend);
  }

  public boolean isBundled() {
    return bundled;
  }

  public Color getColor(ColorCodes code) {
    return colorScheme.get(code);
  }

  public void setColorScheme(IFlowMapColorScheme cs) {
    if (!this.colorScheme.equals(cs)) {
      this.colorScheme = cs;
      updateColors();
    }
  }

  public IFlowMapColorScheme getColorScheme() {
    return colorScheme;
  }

  public MapProjection getMapProjection() {
    return mapProjection;
  }

  public VisualEdgePaintFactory getVisualEdgePaintFactory() {
    return visualEdgePaintFactory;
  }

  public VisualEdgeStrokeFactory getVisualEdgeStrokeFactory() {
    return visualEdgeStrokeFactory;
  }

  private void createNodeVisuals() {
//    nodeLayer.removeAllChildren();
    if (visualNodes != null) {
      for (VisualNode vn : visualNodes) {
        getCamera().removeChild(vn);
      }
    }

    FlowMapGraph fmg = getFlowMapGraph();

    visualNodes = new ArrayList<VisualNode>();
    nodesToVisuals = new LinkedHashMap<Node, VisualNode>();

    for (Node node : fmg.nodes()) {
      if (!fmg.hasCoords(node)) {
        // TODO: !!! create rectangles for flowmap nodes with missing coords
        //       See FlowMapGraph.haveCoordsPredicate() and
        //           PGeoMap.createAreasForNodesWithoutCoords(nodesWithoutCoords)
//        logger.warn("NaN coordinates passed in for node: " + node);
      } else {
        double lon = node.getDouble(fmg.getNodeLonAttr());
        double lat = node.getDouble(fmg.getNodeLatAttr());

        Point2D p = getMapProjection().project(lon, lat);

        VisualNode vnode = new VisualNode(this, node, p.getX(), p.getY());
        getCamera().addChild(vnode);
        visualNodes.add(vnode);
        nodesToVisuals.put(node, vnode);
      }
    }

    updateNodePositions();
  }

  public void setAreaMap(PGeoMap areaMap) {
    if (this.areaMap != null) {
      removeChild(this.areaMap);
    }
    this.areaMap = areaMap;
    if (areaMap != null) {
      addChild(areaMap);
      areaMap.moveToBack();
      if (mapBackgroundImage != null) {
        mapBackgroundImage.moveToBack();
      }
    }
  }

  public List<VisualNode> getVisualNodes() {
    return Collections.unmodifiableList(visualNodes);
  }

  public List<VisualEdge> getVisualEdges() {
    return Collections.unmodifiableList(visualEdges);
  }

  public String getLabel(Edge edge) {
    String labelAttr = getFlowMapGraph().getNodeLabelAttr();
    Node src = edge.getSourceNode();
    Node target = edge.getTargetNode();
    if (labelAttr == null) {
      return src.toString() + " -> " + target.toString();
    } else {
      return src.getString(labelAttr) + " -> " + target.getString(labelAttr);
    }
  }

  public String getLabel(Node node) {
    String labelAttr = getFlowMapGraph().getNodeLabelAttr();
    if (labelAttr == null) {
      return node.toString();
    } else {
      return node.getString(labelAttr);
    }
  }

  public Iterable<Edge> getEdgesSortedByValue(boolean ascending) {
    return getFlowMapGraph().getEdgesSortedBy(getValueAttr(), true, ascending);
  }

  private void createEdgeVisuals() {
    edgeLayer.removeAllChildren();

    visualEdges = new ArrayList<VisualEdge>();
    edgesToVisuals = new LinkedHashMap<Edge, VisualEdge>();

    for (Edge edge : getEdgesSortedByValue(true)) {

      if (!hasCoordinates(edge)) {
        // TODO: create rectangles for flowmap nodes with missing coords
        //       See FlowMapGraph.haveCoordsPredicate() and
        //           PGeoMap.createAreasForNodesWithoutCoords(nodesWithoutCoords)
//        logger.warn("NaN coordinates passed in for edge: " + edge);

      } else {
          VisualEdge visualEdge = createVisualEdgeFor(edge);
          edgeLayer.addChild(visualEdge);
          visualEdges.add(visualEdge);
          edgesToVisuals.put(edge, visualEdge);
      }
    }
  }

  private boolean hasCoordinates(Edge edge) {
    FlowMapGraph fmg = getFlowMapGraph();
    Node srcNode = edge.getSourceNode();
    Node targetNode = edge.getTargetNode();
    return (fmg.hasCoords(srcNode)  &&  fmg.hasCoords(targetNode));
  }

  private void updateVisualEdges() {
    for (Edge edge : getEdgesSortedByValue(false)) {
      VisualEdge ve = edgesToVisuals.get(edge);
      if (hasCoordinates(edge)) {
        ve.update();
//        ve.moveToFront();  // order by attr value
      }
    }
  }

  private void updateVisualEdgeOrdering() {
    for (Edge edge : getEdgesSortedByValue(false)) {
      VisualEdge ve = edgesToVisuals.get(edge);
      if (hasCoordinates(edge)) {
        ve.moveToBack();  // order by attr value
      }
    }
  }

  private VisualEdge createVisualEdgeFor(Edge edge) {
    FlowMapGraph fmg = getFlowMapGraph();

    VisualNode fromNode = nodesToVisuals.get(edge.getSourceNode());
    VisualNode toNode = nodesToVisuals.get(edge.getTargetNode());

    VisualEdge visualEdge;
    if (fmg.hasEdgeSubdivisionPoints(edge)) {

      Iterable<Point> points = MapProjections.projectAll(
          fmg.getEdgePoints(edge), getMapProjection());

      visualEdge = new BSplineVisualEdge(this, edge, fromNode, toNode, points, SHOW_SPLINE_POINTS);
    } else {
      visualEdge = new LineVisualEdge(this, edge, fromNode, toNode);
    }
    visualEdge.update();
    return visualEdge;
  }

  PBounds getVisualNodesBounds() {
    if (nodeBounds == null) {
      PBounds b = null;
      for (VisualNode node : visualNodes) {
        if (b == null) {
          b = node.getBounds();
        } else {
          Rectangle2D.union(b, node.getBoundsReference(), b);
        }
      }
      nodeBounds = b;
    }
    return nodeBounds;
  }

  public FlowMapStats getStats() {
    return getFlowMapGraph().getStats();
  }

  public SeqStat getValueStat() {
    return model.getValueStat();
  }

  public VisualFlowMapModel getModel() {
    return model;
  }

//  private static final Insets contentInsets = new Insets(10, 10, 10, 10);

//  private Insets getContentInsets() {
//    return contentInsets;
//  }

//  public void fitInCameraView(boolean animate) {
//    if (nodeBounds != null) {
//      Insets insets = getContentInsets();
//      insets.left += 5;
//      insets.top += 5;
//      insets.bottom += 5;
//      insets.right += 5;
//      if (animate) {
//        PiccoloUtils.animateViewToPaddedBounds(getCamera(), nodeBounds, insets, SHORT_ANIMATION_DURATION);
//      } else {
//        PiccoloUtils.setViewPaddedBounds(getCamera(), nodeBounds, insets);
//      }
//    }
//  }

  public void fitInCameraView() {
    PBounds boundRect = getVisualNodesBounds();
    boundRect = (PBounds)getCamera().globalToLocal(boundRect);
    getCamera().animateViewToCenterBounds(GeomUtils.growRectByRelativeSize(boundRect, .05, .05, .05, .05), true, 0);
  }

  public String getLabelAttr() {
    return getFlowMapGraph().getNodeLabelAttr();
  }

  public void showTooltip(PNode component, Point2D pos) {
    Point2D localPos = getCamera().viewToLocal(new Point2D.Double(pos.getX(), pos.getY()));
    double maxLabelWidth = Math.abs(getCamera().getBoundsReference().getWidth() - localPos.getX());
    if (component instanceof VisualNode) {
      VisualNode vnode = (VisualNode) component;
      tooltipBox.setText(
          wordWrapLabel(vnode.getLabel(), maxLabelWidth),
          ""
//			    "Outgoing " + selectedFlowAttrName + ": " + graph.getOutgoingTotal(fnode.getId(), selectedFlowAttrName) + "\n" +
//			    "Incoming " + selectedFlowAttrName + ": " + graph.getIncomingTotal(fnode.getId(), selectedFlowAttrName)
          ,
          "");
    } else if (component instanceof VisualEdge) {
      VisualEdge edge = (VisualEdge) component;
      tooltipBox.setText(
          wordWrapLabel(edge.getLabel(), maxLabelWidth),
          getValueAttr() + ": ", Double.toString(edge.getEdgeWeight())
          );
    } else {
      return;
    }
    tooltipBox.showTooltipAt(pos.getX(), pos.getY(), 8, 8);
  }

  private String wordWrapLabel(String label, double maxWidth) {
    FontMetrics fm = view.getVisualCanvas().getGraphics().getFontMetrics();
    int width = SwingUtilities.computeStringWidth(fm, label);
    if (width > maxWidth) {
      StringBuilder sb = new StringBuilder();
      StringBuilder line = new StringBuilder();
      for (String word : label.split(" ")) {
        line.append(word);
        int w = SwingUtilities.computeStringWidth(fm, line.toString());
        if (w > maxWidth) {
          int newLength = line.length() - word.length();
          if (newLength > 0) {    // wrap line only if there are more than one words
            line.setLength(newLength);  // remove last word
            sb.append(line).append("\n");
            line.setLength(0);
            line.append(word);
          } else {
            sb.append(line);    // TODO: wordWrapLabel: implement hyphenation
          }
        }
        line.append(" ");
      }
      label = sb.toString();
    }
    return label;
  }

  public void hideTooltip() {
    tooltipBox.setVisible(false);
  }

  public VisualNode getSelectedNode() {
    return (VisualNode) getAttribute(Attributes.NODE_SELECTION.name());
  }

  public void setSelectedNode(VisualNode visualNode) {
    VisualNode old = getSelectedNode();
    if (old != null) {
      old.setSelected(false);
    }
    if (visualNode != null) {
      visualNode.setSelected(true);
    }
    addAttribute(Attributes.NODE_SELECTION.name(), visualNode);  // will fire a propertyChange event
  }

  public ValueType getValueType() {
    return model.getValueType();
  }

  public void setValueType(ValueType valueType) {
    model.setValueType(valueType);
    updateColors();
//    updateEdgeWidths();
//    updateEdgeVisibility();
    updateVisualEdges();
    updateVisualEdgeOrdering();
    getVisualLegend().update();
  }

  private void initModelChangeListeners(VisualFlowMapModel model) {
    model.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if (prop.equals(VisualFlowMapModel.PROPERTY_AUTO_ADJUST_COLOR_SCALE)
            || prop.equals(VisualFlowMapModel.PROPERTY_EDGE_ALPHA)
            || prop.equals(VisualFlowMapModel.PROPERTY_DIRECTION_MARKER_ALPHA)
            || prop.equals(VisualFlowMapModel.PROPERTY_USE_LOG_COLOR_SCALE)
            || prop.equals(VisualFlowMapModel.PROPERTY_FILL_EDGES_WITH_GRADIENT)
            || prop.equals(VisualFlowMapModel.PROPERTY_SHOW_DIRECTION_MARKERS)
            || prop.equals(VisualFlowMapModel.PROPERTY_USE_PROPORTIONAL_DIRECTION_MARKERS)
            || prop.equals(VisualFlowMapModel.PROPERTY_DIRECTION_MARKER_SIZE)) {
          updateEdgeColors();
          visualLegend.update();
        } else if (prop.equals(VisualFlowMapModel.PROPERTY_MAX_EDGE_WIDTH)
            || prop.equals(VisualFlowMapModel.PROPERTY_USE_LOG_WIDTH_SCALE)) {
          updateEdgeWidths();
          visualLegend.update();
        } else if (prop.equals(VisualFlowMapModel.PROPERTY_VALUE_FILTER_MIN)
            || prop.equals(VisualFlowMapModel.PROPERTY_VALUE_FILTER_MAX)
            || prop.equals(VisualFlowMapModel.PROPERTY_SHOW_SELF_LOOPS)) {
          updateEdgeVisibility();
          // updateEdgeColors();
        } else if (prop.equals(VisualFlowMapModel.PROPERTY_EDGE_LENGTH_FILTER_MIN)
            || prop.equals(VisualFlowMapModel.PROPERTY_EDGE_LENGTH_FILTER_MAX)) {
          updateEdgeVisibility();
        } else if (prop.equals(VisualFlowMapModel.PROPERTY_SHOW_NODES)) {
          updateNodeVisibility();
        } else if (prop.equals(VisualFlowMapModel.PROPERTY_NODE_SIZE)) {
          updateNodeSizes();
        } else if (prop.equals(VisualFlowMapModel.PROPERTY_AUTO_VISUAL_LEGEND_SCALE)) {
          visualLegend.setScale(getModel().getVisualLegendScale());
        }
      }
    });
  }

  public void updateColors() {
    updateEdgeColors();
    updateNodeColors();
    if (areaMap != null) {
      areaMap.updateColors();
    }
    visualLegend.update();
  }


  protected void updateEdgeColors() {
    for (VisualEdge ve : visualEdges) {
      ve.updateEdgeColors();
    }
  }

  protected void updateEdgeVisibility() {
    for (VisualEdge ve : visualEdges) {
      ve.updateVisibility();
    }
  }

  protected void updateNodeVisibility() {
    for (VisualNode vn : visualNodes) {
      vn.updateVisibility();
    }
  }

  protected void updateNodeColors() {
    for (VisualNode vn : visualNodes) {
      vn.updateColorsAndStroke();
    }
  }

  protected void updateNodeSizes() {
    for (VisualNode vn : visualNodes) {
      vn.updateSize();
    }
  }

  private void updateNodePositions() {
    for (VisualNode vn : visualNodes) {
      vn.updatePositionInCamera(getCamera());
    }
  }

  protected void updateEdgeWidths() {
    for (VisualEdge ve : visualEdges) {
      ve.updateEdgeWidth();
    }
  }

  public PCamera getCamera() {
    return view.getVisualCanvas().getCamera();
  }

  public void resetBundling() {
    if (bundled) {
      bundled = false;
      getFlowMapGraph().removeAllEdgeSubdivisionPoints();
      createEdgeVisuals();
      repaint();
    }
  }

  private void createBundledEdgeVisuals() {
    createEdgeVisuals();
    bundled = true;
    repaint();
  }

  public void bundleEdges(final ForceDirectedBundlerParameters params) {
    final ProgressTracker pt = new ProgressTracker();
    final ForceDirectedEdgeBundler bundler = new ForceDirectedEdgeBundler(
        getFlowMapGraph(), params);
    ProgressWorker worker = new ProgressWorker(pt) {
      @Override
      public Object construct() {
        try {
          bundler.bundle(getProgressTracker());
          if (!params.getUpdateViewAfterEachStep()) {
            createBundledEdgeVisuals();
          }
        } catch (Exception ex) {
          logger.error("Bundling error", ex);
          JOptionPane.showMessageDialog(view.getVisualCanvas(),
              "Bundling error: [" + ex.getClass().getSimpleName()+ "] " + ex.getMessage()
          );
        } catch (Error err) {
          logger.error(err);
          System.exit(1);
        }
        return null;
      }
    };
    Window window = SwingUtilities.getWindowAncestor(view.getVisualCanvas());
    ProgressDialog dialog = new ProgressDialog(window, "Edge Bundling", worker, true);
    pt.addProgressListener(dialog);
    if (params.getUpdateViewAfterEachStep()) {
      pt.addTaskCompletionListener(new TaskCompletionListener() {
        public void taskCompleted(final int taskId) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              createBundledEdgeVisuals();
            }
          });
        }
      });
    }
    worker.start();
    dialog.setVisible(true);
  }


  private ValueAnimationActivity valueAnimation;

  public VisualNode getVisualNodeByLabel(String label) {
    for (VisualNode node : visualNodes) {
      if (node.getLabel().equals(label)) {
        return node;
      }
    }
    return null;
  }

  /**
   * @param attrsPerSecond Animation speed
   * @param i
   */
  public void startValueAnimation(Runnable runWhenFinished, int startAttrIndex,
      double attrsPerSecond) {
    if (valueAnimation != null   &&   valueAnimation.isStepping()) {
      return;
    }

    logger.info("Start flow value animation starting from attr " +
        getFlowMapGraph().getEdgeWeightAttrs().get(startAttrIndex) +
        " with the speed of " + attrsPerSecond + " attrs per second");

    setFlowWeightAttrLabelVisibile(true);

    valueAnimation = new ValueAnimationActivity(startAttrIndex, runWhenFinished, attrsPerSecond);
    if (valueAnimation.numAttrs > 1) {
      valueAnimation.start();
    } else {
      if (runWhenFinished != null) {
        runWhenFinished.run();
      }
    }
  }


  class ValueAnimationActivity extends PInterpolatingActivity {

    final List<String> attrs = getFlowMapGraph().getEdgeWeightAttrs();
    private final int startAttrIndex;
    private final Runnable runWhenFinished;
    private final int numAttrs;
    private int lastAttrIndex;

    public ValueAnimationActivity(int startAttrIndex, Runnable runWhenFinished,
        double attrsPerSecond) {
      super(0, 20);
      this.startAttrIndex = startAttrIndex;
      this.runWhenFinished = runWhenFinished;
      this.numAttrs = (attrs.size() - startAttrIndex);
      setSlowInSlowOut(false);
      setDuration(Math.round(numAttrs * 1000 / attrsPerSecond));
      for (VisualEdge ve : edgesToVisuals.values()) {
        setAbsVal(ve, ve.getEdgeWeight());
      }
    }

    public void start() {
      addActivity(valueAnimation);
    }

    public void stop() {
      getActivityScheduler().removeActivity(this);
      setFlowWeightAttr(attrs.get(lastAttrIndex), true);
    }

    @Override
    public void setRelativeTargetValue(float zeroToOne) {
      double alpha = alpha(zeroToOne);
      int lowi = lowi(zeroToOne);
      int highi = highi(zeroToOne);

      this.lastAttrIndex = closesti(zeroToOne);

      setFlowWeightAttr(attrs.get(lowi), false);

      for (VisualEdge ve : edgesToVisuals.values()) {
        double value;

        if (lowi == highi) {
          value = getValueOf(ve, attrs.get(lowi));
        } else {
          double low = getValueOf(ve, attrs.get(lowi));
          double high = getValueOf(ve, attrs.get(highi));
          if (Double.isNaN(low)) low = 0;
          if (Double.isNaN(high)) high = 0;
          value = low + (high - low) * (alpha - (lowi - startAttrIndex));
        }

        orderByValue(ve, value);
        setAbsVal(ve, value);

        ve.updateEdgeWidthTo(value);
        ve.updateEdgeColorsTo(value);
      }
    }

    public int highi(float zeroToOne) {
      return startAttrIndex + (int)Math.ceil(alpha(zeroToOne));
    }

    public int lowi(float zeroToOne) {
      return startAttrIndex + (int)Math.floor(alpha(zeroToOne));
    }

    public int closesti(float zeroToOne) {
      return startAttrIndex + (int)Math.round(alpha(zeroToOne));
    }

    public double alpha(float zeroToOne) {
      return (numAttrs - 1) * zeroToOne;
    }

    public void orderByValue(VisualEdge ve, double value) {
      if (Double.isNaN(value)) {
        return;
      }
      double absValue = Math.abs(value);
      int numChildren = edgeLayer.getChildrenCount();
      int index = edgeLayer.indexOfChild(ve);

      while (index > 0   &&   absValue < getVal(index - 1)) index--;
      while (index < numChildren - 1   &&   absValue > getVal(index + 1)) index++;

      int change = edgeLayer.indexOfChild(ve) - index;
      if (change != 0) {
        VisualEdge nve = getVE(index);
        double nv = getVal(nve);
        if (nv > absValue) {
          ve.moveInBackOf(nve);
        } else {
          ve.moveInFrontOf(nve);
        }
      }
    }

    public void setAbsVal(VisualEdge ve, double value) {
      ve.addAttribute(VisualEdge.ATTR_ANIMATION_ABS_EDGE_WEIGHT, Math.abs(value));
    }

    public double getVal(int i) {
      return getVal(getVE(i));
    }

    public VisualEdge getVE(int i) {
      return (VisualEdge)edgeLayer.getChild(i);
    }

    public double getVal(VisualEdge ve) {
      return (Double) ve.getAttribute(VisualEdge.ATTR_ANIMATION_ABS_EDGE_WEIGHT);
    }

    @Override
    protected void activityFinished() {
      super.activityFinished();
      if (runWhenFinished != null) {
        runWhenFinished.run();
        valueAnimation = null;
      }
    }

  }


  public boolean isValueAnimationRunning() {
    return (valueAnimation != null  &&   valueAnimation.isStepping());
  }

  public void stopValueAnimation() {
    if (valueAnimation != null  &&  valueAnimation.isStepping()) {
      logger.info("Stop flow value animation");
      valueAnimation.stop();
      valueAnimation = null;
    }
  }

  public ForceDirectedBundlerParameters createForceDirectedBundlerParameters() {
    return model.createForceDirectedBundlerParameters(getValueAttr());
  }


}
