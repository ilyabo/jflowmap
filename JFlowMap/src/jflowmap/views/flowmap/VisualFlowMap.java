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

import java.awt.Color;
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
import jflowmap.clustering.NodeDistanceMeasure;
import jflowmap.data.FlowMapStats;
import jflowmap.data.SeqStat;
import jflowmap.geo.MapProjection;
import jflowmap.geo.MapProjections;
import jflowmap.geom.GeomUtils;
import jflowmap.geom.Point;
import jflowmap.views.ColorCodes;
import jflowmap.views.IFlowMapColorScheme;
import jflowmap.views.Legend;
import jflowmap.views.MapBackgroundImage;
import jflowmap.views.PTooltip;
import jflowmap.views.map.PGeoMap;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import at.fhj.utils.misc.ProgressTracker;
import at.fhj.utils.misc.TaskCompletionListener;
import at.fhj.utils.swing.ProgressDialog;
import at.fhj.utils.swing.ProgressWorker;
import ch.unifr.dmlib.cluster.ClusterNode;
import ch.unifr.dmlib.cluster.ClusterSetBuilder;
import ch.unifr.dmlib.cluster.ClusterVisitor;
import ch.unifr.dmlib.cluster.DistanceMatrix;
import ch.unifr.dmlib.cluster.HierarchicalClusterer;
import ch.unifr.dmlib.cluster.Linkage;
import ch.unifr.dmlib.cluster.Linkages;

import com.google.common.collect.Maps;

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

  private static final boolean SHOW_SPLINE_POINTS = false;

  public static final int PROPERTY_CODE_FLOW_WEIGHT_ATTR = 1 << 11;
  public static final String PROPERTY_FLOW_WEIGHT_ATTR = "flowWeightAttr";

  public enum Attributes {
    NODE_SELECTION
  }
//  private static final Color SINGLE_ELEMENT_CLUSTER_COLOR = new Color(100, 100, 100, 150);
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

  // clustering fields
  private ClusterNode<VisualNode> rootCluster = null;
  private ClusterNode<VisualNode> euclideanRootCluster = null;
  private List<VisualNodeDistance> nodeDistanceList;
  private double maxNodeDistance;
  private double clusterDistanceThreshold;
  private double euclideanClusterDistanceThreshold;
  private List<VisualNodeCluster> visualNodeClusters;
  private Map<VisualNode, VisualNodeCluster> nodesToClusters;
  private PGeoMap areaMap;
  private double euclideanMaxNodeDistance;
  // endOf clustering fields
  private boolean bundled;

  private final VisualEdgePaintFactory visualEdgePaintFactory;
  private final VisualEdgeStrokeFactory visualEdgeStrokeFactory;
  private final Legend visualLegend;

  private String flowWeightAttr;
  private final MapProjection mapProjection;
  private IFlowMapColorScheme colorScheme;
  private PNode mapBackgroundImage;


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

    tooltipBox = new PTooltip();
    tooltipBox.setVisible(false);
    tooltipBox.setPickable(false);

    visualLegend = new Legend(
        getColor(ColorCodes.LEDGEND_BOX_PAINT),
        getColor(ColorCodes.LEDGEND_TEXT), new FlowMapLegendItemProducer(this, 5));
    setLegendVisible(showLegend);

    initModelChangeListeners(model);

    getCamera().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == PCamera.PROPERTY_BOUNDS) {
          fitInCameraView();
        } else
        if (evt.getPropertyName() == PCamera.PROPERTY_VIEW_TRANSFORM) {
          hideTooltip();
          updateNodePositions();
        }
      }
    });
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
    String oldValue = this.flowWeightAttr;
    if (!oldValue.equals(attr)) {
      this.flowWeightAttr = attr;
      resetClusters();
      resetBundling();
      if (doUpdate) {
        updateVisualEdges();
      }
      updateVisualEdgeOrdering();
      firePropertyChange(PROPERTY_CODE_FLOW_WEIGHT_ATTR, PROPERTY_FLOW_WEIGHT_ATTR, oldValue, attr);
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

    Graph graph = fmg.getGraph();

    final int numNodes = graph.getNodeCount();
    visualNodes = new ArrayList<VisualNode>();
    nodesToVisuals = new LinkedHashMap<Node, VisualNode>();

    for (int i = 0; i < numNodes; i++) {
      Node node = graph.getNode(i);

      if (!fmg.hasCoords(node)) {
        // TODO: create rectangles for flowmap nodes with missing coords
        //       See FlowMapGraph.haveCoordsPredicate() and
        //           PGeoMap.createAreasForNodesWithoutCoords(nodesWithoutCoords)
        logger.warn("NaN coordinates passed in for node: " + node);
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

  private void createEdgeVisuals() {
    edgeLayer.removeAllChildren();
    clearAggregatedEdgesLayer();

    visualEdges = new ArrayList<VisualEdge>();
    edgesToVisuals = new LinkedHashMap<Edge, VisualEdge>();

    for (Edge edge : getFlowMapGraph().getEdgesSortedBy(getValueAttr())) {

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
    for (Edge edge : getFlowMapGraph().getEdgesSortedBy(getValueAttr())) {
      VisualEdge ve = edgesToVisuals.get(edge);
      if (hasCoordinates(edge)) {
        ve.update();
//        ve.moveToFront();  // order by attr value
      }
    }
  }

  private void updateVisualEdgeOrdering() {
    for (Edge edge : getFlowMapGraph().getEdgesSortedBy(getValueAttr())) {
      VisualEdge ve = edgesToVisuals.get(edge);
      if (hasCoordinates(edge)) {
        ve.moveToFront();  // order by attr value
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

  private PBounds getVisualNodesBounds() {
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
          wordWrapLabel(vnode.getFullLabel(), maxLabelWidth),
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


  private PNode aggregatedEdgesLayer;

  private PNode getAggregatedEdgesLayer() {
    if (aggregatedEdgesLayer == null) {
      aggregatedEdgesLayer = new PNode();
      addChild(aggregatedEdgesLayer);
    }
    return aggregatedEdgesLayer;
  }

  private void clearAggregatedEdgesLayer() {
    if (aggregatedEdgesLayer != null) {
      aggregatedEdgesLayer.removeAllChildren();
    }
  }

  /*
  public void aggregateBundledEdges() {
    if (!isBundled()) {
      return;
    }
    final EdgeSegmentAggregator aggregator = new EdgeSegmentAggregator(getFlowMapGraph());
    final ProgressTracker pt = new ProgressTracker();
    ProgressWorker worker = new ProgressWorker(pt) {
      @Override
      public Object construct() {
        try {
          aggregator.aggregate(pt);
          if (pt.isCancelled()) {
            return null;
          }

          final List<EdgeSegment> segments = aggregator.getAggregatedSegments();
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              createAggregatedSegmentsVisuals(segments);
              // remove edges
            }
          });
          repaint();
        } catch (Exception ex) {
          logger.error("Aggregation error", ex);
          JOptionPane.showMessageDialog(view.getVisualCanvas(),
              "Aggregation error: [" + ex.getClass().getSimpleName()+ "] " + ex.getMessage()
          );
        } catch (Error err) {
          logger.error(err);
          System.exit(1);
        }
        return null;
      }
    };
    ProgressDialog dialog = new ProgressDialog(
        SwingUtilities.getWindowAncestor(view.getVisualCanvas()), "Edge Segment Aggregation", worker, true);
    pt.addProgressListener(dialog);
    worker.start();
    dialog.setVisible(true);
  }
  */

  /*
  private void createAggregatedSegmentsVisuals(
      final List<EdgeSegment> segments) {
//    edgeLayer.removeAllChildren();
    clearAggregatedEdgesLayer();

    // create aggregated edge weight stats to normalize on them
    SeqStat stats = SeqStat.createFor(Iterators.transform(segments.iterator(), EdgeSegment.TRANSFORM_TO_WEIGHT));

    PNode parentNode = getAggregatedEdgesLayer();

    for (EdgeSegment seg : segments) {
//      if (seg.length() == 0) {
//        logger.warn("Zero-length segment: " + seg);
//      }

      double nv = stats.normalizer().normalizeLog(seg.getWeight());
//      double nv = stats.normalize(seg.getWeight());
//      double nv = seg.getWeight();
      double width = 1 + nv * getModel().getMaxEdgeWidth();

      parentNode.addChild(new PSegment(seg, width));
    }
  }

  private static class PSegment extends PNode {
    private static final long serialVersionUID = 1L;
//    private static final Color JOINT_PT_COLOR = new Color(0, 0, 255, 150);
//    private static final Color SEGMENT_COLOR = new Color(255, 255, 255, 200);
    private static final float[] GRADIENT_FRACTIONS = new float[] { 0.0f, 1.0f };
//    private static final Color[] GRADIENT_COLORS = new Color[] { new Color(100, 100, 200, 200), new Color(200, 200, 100, 200) };
    private static final Color[] GRADIENT_COLORS = new Color[] { new Color(255, 0, 0, 150), new Color(0, 255, 0, 150) };
    private final EdgeSegment segment;

    public PSegment(EdgeSegment seg, double width) {
      this.segment = seg;

      FPoint src = seg.getA();
      FPoint dest = seg.getB();
      if (src.getPoint().equals(dest.getPoint())) {
        throw new IllegalStateException();
      }
      PPath linep = new PPath(new Line2D.Double(src.asPoint2D(), dest.asPoint2D()), new PFixedWidthStroke((float)width));
//      linep.setPaint(SEGMENT_COLOR);
//      linep.setStrokePaint(SEGMENT_COLOR);
      linep.setStrokePaint( new LinearGradientPaint(
          (float)src.x(), (float)src.y(), (float)dest.x(), (float)dest.y(),
          GRADIENT_FRACTIONS,
          GRADIENT_COLORS
      ));
      addChild(linep);

//      PSegmentPoint srcp = new PSegmentPoint(src);
//      PSegmentPoint dstp = new PSegmentPoint(dest);
//      srcp.setPaint(JOINT_PT_COLOR);
//      dstp.setPaint(JOINT_PT_COLOR);
//      srcp.setStroke(null);
//      dstp.setStroke(null);
//      addChild(srcp);
//      addChild(dstp);

//      setPickable(true);
      addInputEventListener(MOUSE_HANDLER);
      setPickable(false);
    }

    private static class PSegmentPoint extends PPath {
      private static final long serialVersionUID = 1L;
      private static final double DIAMETER = .5;
      private final FPoint point;

      public PSegmentPoint(FPoint p) {
        super(new Ellipse2D.Double(p.x() - DIAMETER/2, p.y() - DIAMETER/2, DIAMETER, DIAMETER));
        this.point = p;
      }

      public FPoint getPoint() {
        return point;
      }
    }

    public EdgeSegment getSegment() {
      return segment;
    }

    private static final PInputEventListener MOUSE_HANDLER = new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
      }

      @Override
      public void mouseEntered(PInputEvent event) {
        if (logger.isDebugEnabled()) {
          PNode picked = event.getPickedNode();
          if (picked instanceof PSegmentPoint) {
            PSegmentPoint psp = (PSegmentPoint)picked;
            logger.debug("Segment point: " + psp.getPoint());
          }

//          PSegment pseg = PiccoloUtils.getParentNodeOfType(picked, PSegment.class);
//          if (pseg != null) {
//            logger.debug("MouseEntered: " + pseg.getSegment());
//          }
        }
      }

      @Override
      public void mouseExited(PInputEvent event) {
      }
    };
  }
  */


  public ClusterNode<VisualNode> getRootCluster() {
    return rootCluster;
  }

  public List<VisualNodeDistance> getNodeDistanceList() {
    return nodeDistanceList;
  }

  public double getMaxNodeDistance() {
//    if (Double.isNaN(maxNodeDistance)) {
//      maxNodeDistance = VisualNodeDistance.findMaxDistance(nodeDistanceList);
//    }
    return maxNodeDistance;
  }

  public double getEuclideanMaxNodeDistance() {
    return euclideanMaxNodeDistance;
  }

  public double getClusterDistanceThreshold() {
    return clusterDistanceThreshold;
  }

  public void setClusterDistanceThreshold(double value) {
    this.clusterDistanceThreshold = value;
    updateClusters();
  }

  public double getEuclideanClusterDistanceThreshold() {
    return euclideanClusterDistanceThreshold;
  }

  public void setEuclideanClusterDistanceThreshold(double value) {
    this.euclideanClusterDistanceThreshold = value;
    updateClusters();
  }

  public boolean hasClusters() {
    return rootCluster != null;
  }

  public boolean hasEuclideanClusters() {
    return euclideanRootCluster != null;
  }

  public boolean hasJoinedEdges() {
    return flowMapBeforeJoining != null;
  }

  public VisualNodeCluster getNodeCluster(VisualNode node) {
    return nodesToClusters.get(node);
  }

  public void updateClusters() {
    removeClusterTags();  // to remove cluster tags for those nodes which were excluded from clustering

    List<VisualNodeCluster> clusters;
    if (rootCluster == null) {
      clusters = Collections.emptyList();
    } else {
      List<List<VisualNode>> nodeClusterLists =
        ClusterSetBuilder.getClusters(rootCluster, clusterDistanceThreshold);
      if (euclideanRootCluster != null) {
        nodeClusterLists = VisualNodeCluster.combineClusters(
            nodeClusterLists,
            ClusterSetBuilder.getClusters(euclideanRootCluster, euclideanClusterDistanceThreshold)
        );
      }
      clusters = VisualNodeCluster.createClusters(nodeClusterLists, clusterDistanceThreshold);
    }
    this.visualNodeClusters = clusters;

    this.nodesToClusters = Maps.newHashMap();
    for (VisualNodeCluster cluster : clusters) {
      for (VisualNode node : cluster) {
        nodesToClusters.put(node, cluster);
      }
    }
  }

  public List<VisualNodeCluster> getVisualNodeClusters() {
    if (!hasClusters()) {
      throw new IllegalStateException("The flow map is not clustered");
    }
    return Collections.unmodifiableList(visualNodeClusters);
  }

  public int getNumberOfClusters() {
    if (visualNodeClusters == null) {
      return 0;
    }
    return visualNodeClusters.size();
  }

  private void removeClusterTags() {
    for (VisualNode node : visualNodes) {
      node.setClusterTag(null);
    }
  }

  public void clusterNodes(NodeDistanceMeasure distanceMeasure, Linkage<VisualNode> linkage,
      boolean combineWithEuclideanClusters) {
    logger.info("Clustering nodes");
    HierarchicalClusterer<VisualNode> clusterer =
      HierarchicalClusterer.createWith(distanceMeasure, linkage).build();

    List<VisualNode> items = distanceMeasure.filterNodes(visualNodes);

    ProgressTracker tracker = new ProgressTracker();
    DistanceMatrix<VisualNode> distances = clusterer.makeDistanceMatrix(items, tracker);
    nodeDistanceList = VisualNodeDistance.makeDistanceList(items, distances);
    rootCluster = clusterer.clusterToRoot(items, distances, tracker);
    maxNodeDistance = findMaxClusterDist(rootCluster);
    clusterDistanceThreshold = maxNodeDistance / 2;
    if (combineWithEuclideanClusters) {
      euclideanRootCluster = HierarchicalClusterer
        .createWith(NodeDistanceMeasure.EUCLIDEAN, Linkages.<VisualNode>complete())
        .build()
        .clusterToRoot(items, new ProgressTracker());

      euclideanMaxNodeDistance = findMaxClusterDist(euclideanRootCluster);
      euclideanClusterDistanceThreshold = euclideanMaxNodeDistance / 2;
    } else {
      euclideanRootCluster = null;
      euclideanMaxNodeDistance = Double.NaN;
      euclideanClusterDistanceThreshold = 0;
    }
    updateClusters();
  }

  private static <T> double findMaxClusterDist(ClusterNode<T> root) {
    class Finder extends ClusterVisitor.Adapter<T> {
      double maxDist = Double.NaN;
      @Override
      public void betweenChildren(ClusterNode<T> cn) {
        if (Double.isNaN(maxDist)  || cn.getDistance() > maxDist) {
          maxDist = cn.getDistance();
        }
      }
    }
    Finder finder = new Finder();
    ClusterNode.traverseClusters(root, finder);
    return finder.maxDist;
  }

  private VisualFlowMap flowMapBeforeJoining = null;
  private PInterpolatingActivity valueAnimation;

  public void setOriginalVisualFlowMap(VisualFlowMap originalVisualFlowMap) {
    this.flowMapBeforeJoining = originalVisualFlowMap;
  }

  public void joinClusterEdges() {
    if (view instanceof FlowMapView) {
      FlowMapView fmview = (FlowMapView)view;
      FlowMapGraph fmg = VisualNodeCluster.createClusteredFlowMap(
          getFlowMapGraph().getAttrSpec(), visualNodeClusters);
      VisualFlowMap clusteredFlowMap = fmview.createVisualFlowMap(
          new VisualFlowMapModel(fmg, model.getViewConfig()), mapProjection,
          getValueAttr(), getColorScheme());
      if (areaMap != null) {
        clusteredFlowMap.setAreaMap(new PGeoMap(areaMap));
      }
      clusteredFlowMap.setOriginalVisualFlowMap(this);
      fmview.setVisualFlowMap(clusteredFlowMap);
    }
  }

  public void resetClusters() {
    if (rootCluster != null) {
      removeClusterTags();
      rootCluster = null;
      euclideanRootCluster = null;
      visualNodeClusters = null;
    }
  }

  public void resetJoinedNodes() {
    if (view instanceof FlowMapView) {
      if (flowMapBeforeJoining != null) {
        ((FlowMapView)view).setVisualFlowMap(flowMapBeforeJoining);
      }
    }
  }

  public void setNodeClustersToShow(List<VisualNodeCluster> nodeClustersToShow) {
    if (visualNodeClusters == null) {
      return;
    }
    for (VisualNodeCluster cluster : visualNodeClusters) {
      final boolean visible;
      if (nodeClustersToShow.isEmpty()) {
        visible = true;
      } else {
        visible = nodeClustersToShow.contains(cluster);
      }
      cluster.setVisible(visible);
    }
    updateEdgeVisibility();
  }

  public VisualNode getVisualNodeByLabel(String label) {
    for (VisualNode node : visualNodes) {
      if (node.getLabel().equals(label)) {
        return node;
      }
    }
    return null;
  }

  public void startValueAnimation(final Runnable runWhenFinished) {
    if (valueAnimation != null   &&   valueAnimation.isStepping()) {
      return;
    }

    final List<String> attrs = getFlowMapGraph().getEdgeWeightAttrs();

    final int numAttrs = attrs.size();
    if (numAttrs == 0) {
      return;
    }

    valueAnimation = new PInterpolatingActivity(20000) {
      @Override
      public void setRelativeTargetValue(float zeroToOne) {
        for (VisualEdge ve : edgesToVisuals.values()) {

          double value;

          double alpha = (numAttrs - 1) * zeroToOne;
          int lowi = (int)Math.floor(alpha);
          int highi = (int)Math.ceil(alpha);

          if (lowi == highi) {
            value = getValueOf(ve, attrs.get(lowi));
          } else {
            double low = getValueOf(ve, attrs.get(lowi));
            double high = getValueOf(ve, attrs.get(highi));
            if (Double.isNaN(low)) low = 0;
            if (Double.isNaN(high)) high = 0;
            value = low + (high - low) * (alpha - lowi);
          }

          setFlowWeightAttr(attrs.get(lowi), false);

          ve.updateEdgeWidthTo(value);
          ve.updateEdgeColorsTo(value);
        }
      }

      @Override
      protected void activityFinished() {
        super.activityFinished();
        if (runWhenFinished != null) {
          runWhenFinished.run();
          valueAnimation = null;
        }
      }
    };
    valueAnimation.setSlowInSlowOut(false);
    addActivity(valueAnimation);
  }


  public void stopValueAnimation() {
    if (valueAnimation != null  &&  valueAnimation.isStepping()) {
      valueAnimation.getActivityScheduler().removeActivity(valueAnimation);
      valueAnimation = null;
    }
  }

  public ForceDirectedBundlerParameters createForceDirectedBundlerParameters() {
    return model.createForceDirectedBundlerParameters(getValueAttr());
  }


}
