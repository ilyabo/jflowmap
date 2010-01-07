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

package jflowmap.visuals;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.LinearGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jflowmap.JFlowMap;
import jflowmap.aggregation.EdgeSegment;
import jflowmap.aggregation.EdgeSegmentAggregator;
import jflowmap.bundling.ForceDirectedBundlerParameters;
import jflowmap.bundling.ForceDirectedEdgeBundler;
import jflowmap.clustering.NodeDistanceMeasure;
import jflowmap.data.FlowMapStats;
import jflowmap.data.MinMax;
import jflowmap.geom.FPoint;
import jflowmap.models.FlowMapModel;

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

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class VisualFlowMap extends PNode {

    private static final boolean SHOW_SPLINE_POINTS = false;
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(VisualFlowMap.class);
    public enum Attributes {
        NODE_SELECTION
    }
//    private static final Color SINGLE_ELEMENT_CLUSTER_COLOR = new Color(100, 100, 100, 150);
    private final Tooltip tooltipBox;
    private PBounds nodeBounds;

    private final PNode edgeLayer;
    private final PNode nodeLayer;

    private final FlowMapModel flowMapModel;
    private List<VisualNode> visualNodes;
    private List<VisualEdge> visualEdges;
    private Map<Node, VisualNode> nodesToVisuals;
    private Map<Edge, VisualEdge> edgesToVisuals;
    private final JFlowMap jFlowMap;

    // clustering fields
    private ClusterNode<VisualNode> rootCluster = null;
    private ClusterNode<VisualNode> euclideanRootCluster = null;
    private List<VisualNodeDistance> nodeDistanceList;
    private double maxNodeDistance;
    private double clusterDistanceThreshold;
    private double euclideanClusterDistanceThreshold;
    private List<VisualNodeCluster> visualNodeClusters;
    private Map<VisualNode, VisualNodeCluster> nodesToClusters;
    private VisualAreaMap areaMap;
    private double euclideanMaxNodeDistance;
    // endOf clustering fields
    private boolean bundled;

    public VisualFlowMap(JFlowMap jFlowMap, Graph graph, FlowMapStats stats,
    		FlowMapModel model) {
        this.jFlowMap = jFlowMap;
    	this.flowMapModel = model;

        nodeLayer = new PNode();
        createNodeVisuals();

        edgeLayer = new PNode();
        createEdgeVisuals();

        addChild(edgeLayer);
        addChild(nodeLayer);

        tooltipBox = new Tooltip();
        tooltipBox.setVisible(false);
        tooltipBox.setPickable(false);
        PCamera camera = getCamera();
	    camera.addChild(tooltipBox);

        initModelChangeListeners(model);
//        fitInCameraView();
//        fitInCameraView(false);
    }

    public boolean isBundled() {
        return bundled;
    }


    private void createNodeVisuals() {
        nodeLayer.removeAllChildren();

        Graph graph = flowMapModel.getGraph();

        final int numNodes = graph.getNodeCount();
        visualNodes = new ArrayList<VisualNode>();
        nodesToVisuals = new LinkedHashMap<Node, VisualNode>();

        for (int i = 0; i < numNodes; i++) {
            Node node = graph.getNode(i);

            VisualNode vnode = new VisualNode(this, node,
                    node.getDouble(flowMapModel.getXNodeAttr()),// - xStats.min,
                    node.getDouble(flowMapModel.getYNodeAttr())// - yStats.min,
                    );
            nodeLayer.addChild(vnode);
            visualNodes.add(vnode);
            nodesToVisuals.put(node, vnode);
        }
    }

    public void setAreaMap(VisualAreaMap areaMap) {
        this.areaMap = areaMap;
        addChild(areaMap);
        areaMap.moveToBack();
    }

    public List<VisualNode> getVisualNodes() {
        return Collections.unmodifiableList(visualNodes);
    }

    public List<VisualEdge> getVisualEdges() {
        return Collections.unmodifiableList(visualEdges);
    }

    public String getLabel(Edge edge) {
        String labelAttr = flowMapModel.getNodeLabelAttr();
        Node src = edge.getSourceNode();
        Node target = edge.getTargetNode();
        if (labelAttr == null) {
            return src.toString() + " -> " + target.toString();
        } else {
            return src.getString(labelAttr) + " -> " + target.getString(labelAttr);
        }
    }

    public String getLabel(Node node) {
        String labelAttr = flowMapModel.getNodeLabelAttr();
        if (labelAttr == null) {
            return node.toString();
        } else {
            return node.getString(labelAttr);
        }
    }

    private void createEdgeVisuals() {
        edgeLayer.removeAllChildren();
        clearAggregatedEdgesLayer();

//      for (int i = 0; i < graph.getEdgeTable().getColumnCount(); i++) {
//      if (logger.isDebugEnabled()) logger.debug("Field: " + graph.getEdgeTable().getColumnName(i));
//  }

        visualEdges = new ArrayList<VisualEdge>();
        edgesToVisuals = new LinkedHashMap<Edge, VisualEdge>();

        Graph graph = flowMapModel.getGraph();

//        Iterator<Integer> it = graph.getEdgeTable().rows();
        @SuppressWarnings("unchecked")
        Iterator<Integer> it = graph.getEdgeTable().rowsSortedBy(flowMapModel.getEdgeWeightAttr(), true);

        while (it.hasNext()) {
            Edge edge = graph.getEdge(it.next());

            if (edge.getSourceNode().equals(edge.getTargetNode())) {
                logger.warn(
                        "Self-loop edge: " +
                        " [" + edge + "]"
                );
            }

            double value = edge.getDouble(flowMapModel.getEdgeWeightAttr());
            if (Double.isNaN(value)) {
                logger.warn(
                    "Omitting NaN value for edge: " +
                    edge.getSourceNode().getString(flowMapModel.getNodeLabelAttr()) + " -> " +
                    edge.getTargetNode().getString(flowMapModel.getNodeLabelAttr()) +
                    " [" + edge + "]"
                );
            } else {
                VisualNode fromNode = nodesToVisuals.get(edge.getSourceNode());
                VisualNode toNode = nodesToVisuals.get(edge.getTargetNode());

                VisualEdge visualEdge;
                if (flowMapModel.hasEdgeSubdivisionPoints(edge)) {
                    visualEdge = new BSplineVisualEdge(
                            this, edge, fromNode, toNode, flowMapModel.getEdgePoints(edge), SHOW_SPLINE_POINTS);
                } else {
                    visualEdge = new LineVisualEdge(this, edge, fromNode, toNode);
                }
                visualEdge.update();
                edgeLayer.addChild(visualEdge);

                visualEdges.add(visualEdge);
                edgesToVisuals.put(edge, visualEdge);
            }
        }
    }

    private PBounds getNodesBounds() {
        if (nodeBounds == null) {
//            nodeBounds = new PBounds(
//                    0, 0, (xStats.max - xStats.min) / 2, (yStats.max - yStats.min) / 2
//            );
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
		return flowMapModel.getStats();
	}

    public FlowMapModel getModel() {
        return flowMapModel;
    }

//    private static final Insets contentInsets = new Insets(10, 10, 10, 10);

//    private Insets getContentInsets() {
//        return contentInsets;
//    }

//    public void fitInCameraView(boolean animate) {
//        if (nodeBounds != null) {
//            Insets insets = getContentInsets();
//            insets.left += 5;
//            insets.top += 5;
//            insets.bottom += 5;
//            insets.right += 5;
//            if (animate) {
//                PiccoloUtils.animateViewToPaddedBounds(getCamera(), nodeBounds, insets, SHORT_ANIMATION_DURATION);
//            } else {
//                PiccoloUtils.setViewPaddedBounds(getCamera(), nodeBounds, insets);
//            }
//        }
//    }

    public void fitInCameraView() {
        PBounds boundRect = getNodesBounds();
//        PPath boundRectPath = new PPath(boundRect);
//        addChild(boundRectPath);
//        boundRectPath.setStrokePaint(Color.red);
        logger.info("Fit in camera view: Bounding box: " + boundRect);
        boundRect = (PBounds)getCamera().globalToLocal(boundRect);
//        PiccoloUtils.setViewPaddedBounds(getCamera(), boundRect, new Insets(10, 10, 10, 10));
        getCamera().animateViewToCenterBounds(boundRect, true, 0);
    }

    public String getLabelAttr() {
        return flowMapModel.getNodeLabelAttr();
    }

    public void showTooltip(PNode component, Point2D pos) {
        final PBounds cameraBounds = getCamera().getBoundsReference();
        double maxLabelWidth = cameraBounds.getWidth() - pos.getX();
        if (component instanceof VisualNode) {
            VisualNode vnode = (VisualNode) component;
            tooltipBox.setText(
                    wordWrapLabel(vnode.getFullLabel(), maxLabelWidth),
                    ""
//			        "Outgoing " + selectedFlowAttrName + ": " + graph.getOutgoingTotal(fnode.getId(), selectedFlowAttrName) + "\n" +
//			        "Incoming " + selectedFlowAttrName + ": " + graph.getIncomingTotal(fnode.getId(), selectedFlowAttrName)
                    ,
                    "");
        } else if (component instanceof VisualEdge) {
            VisualEdge edge = (VisualEdge) component;
            tooltipBox.setText(
                    wordWrapLabel(edge.getLabel(), maxLabelWidth),
                    flowMapModel.getEdgeWeightAttr() + ": ", Double.toString(edge.getEdgeWeight()));
        } else {
            return;
        }
        final PBounds tooltipBounds = tooltipBox.getBoundsReference();
        double x = pos.getX();
        double y = pos.getY();
        pos = new Point2D.Double(x, y);
        getCamera().viewToLocal(pos);
        x = pos.getX();
        y = pos.getY();
        if (x + tooltipBounds.getWidth() > cameraBounds.getWidth()) {
            final double _x = pos.getX() - tooltipBounds.getWidth() - 8;
            if (cameraBounds.getX() - _x < x + tooltipBounds.getWidth() - cameraBounds.getMaxX()) {
                x = _x;
            }
        }
        if (y + tooltipBounds.getHeight() > cameraBounds.getHeight()) {
            final double _y = pos.getY() - tooltipBounds.getHeight() - 8;
            if (cameraBounds.getY() - _y < y + tooltipBounds.getHeight() - cameraBounds.getMaxY()) {
                y = _y;
            }
        }
        pos.setLocation(x + 8, y + 8);
        tooltipBox.setPosition(pos.getX(), pos.getY());
        tooltipBox.setVisible(true);
    }

    private String wordWrapLabel(String label, double maxWidth) {
        FontMetrics fm = jFlowMap.getGraphics().getFontMetrics();
        int width = SwingUtilities.computeStringWidth(fm, label);
        if (width > maxWidth) {
            StringBuilder sb = new StringBuilder();
            StringBuilder line = new StringBuilder();
            for (String word : label.split(" ")) {
                line.append(word);
                int w = SwingUtilities.computeStringWidth(fm, line.toString());
                if (w > maxWidth) {
                    int newLength = line.length() - word.length();
                    if (newLength > 0) {        // wrap line only if there are more than one words
                        line.setLength(newLength);  // remove last word
                        sb.append(line).append("\n");
                        line.setLength(0);
                        line.append(word);
                    } else {
                        sb.append(line);        // TODO: wordWrapLabel: implement hyphenation
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

    private void initModelChangeListeners(FlowMapModel model) {
    	model.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				String prop = evt.getPropertyName();
				if (prop.equals(FlowMapModel.PROPERTY_AUTO_ADJUST_COLOR_SCALE)  ||
					prop.equals(FlowMapModel.PROPERTY_EDGE_ALPHA)  ||
					prop.equals(FlowMapModel.PROPERTY_DIRECTION_MARKER_ALPHA)  ||
					prop.equals(FlowMapModel.PROPERTY_FILL_EDGES_WITH_GRADIENT) ||
					prop.equals(FlowMapModel.PROPERTY_SHOW_DIRECTION_MARKERS)  ||
					prop.equals(FlowMapModel.PROPERTY_USE_PROPORTIONAL_DIRECTION_MARKERS)  ||
					prop.equals(FlowMapModel.PROPERTY_DIRECTION_MARKER_SIZE)
				) {
					updateEdgeColors();
				} else if (prop.equals(FlowMapModel.PROPERTY_MAX_EDGE_WIDTH)) {
					updateEdgeWidths();
				} else if (prop.equals(FlowMapModel.PROPERTY_VALUE_FILTER_MIN) ||
						prop.equals(FlowMapModel.PROPERTY_VALUE_FILTER_MAX))
				{
	                updateEdgeVisibility();
	                updateEdgeColors();
				} else if (prop.equals(FlowMapModel.PROPERTY_EDGE_LENGTH_FILTER_MIN) ||
						prop.equals(FlowMapModel.PROPERTY_EDGE_LENGTH_FILTER_MAX))
				{
	                updateEdgeVisibility();
				} else if (prop.equals(FlowMapModel.PROPERTY_SHOW_NODES)) {
				    updateNodeVisibility();
				} else if (prop.equals(FlowMapModel.PROPERTY_NODE_SIZE)) {
				    updateNodeSizes();
				}
			}
		});
    }

    @SuppressWarnings("unchecked")
	protected void updateEdgeColors() {
        for (PNode node : (List<PNode>) edgeLayer.getChildrenReference()) {
            if (node instanceof VisualEdge) {
                ((VisualEdge) node).updateEdgeColors();
            }
        }
    }

//    private void updateEdgeMarkerColors() {
//        for (PNode node : (List<PNode>) edgeLayer.getChildrenReference()) {
//            if (node instanceof VisualEdge) {
//                ((VisualEdge) node).updateEdgeMarkerColors();
//            }
//        }
//    }

    protected void updateEdgeVisibility() {
        for (VisualEdge ve : visualEdges) {
            ve.updateVisibility();
        }
//        for (VisualNode vn : visualNodes) {
//            vn.updatePickability();
//        }
    }

    protected void updateNodeVisibility() {
        for (VisualNode vn : visualNodes) {
            vn.updateVisibility();
        }
    }

    protected void updateNodeSizes() {
        for (VisualNode vn : visualNodes) {
            vn.updateSize();
        }
    }

    protected void updateEdgeWidths() {
        for (VisualEdge ve : visualEdges) {
            ve.updateEdgeWidth();
        }
    }

    public PCamera getCamera() {
        return jFlowMap.getCanvas().getCamera();
    }

    public void resetBundling() {
        bundled = false;
        flowMapModel.removeAllEdgeSubdivisionPoints();
        createEdgeVisuals();
        repaint();
    }

    public void bundleEdges(ForceDirectedBundlerParameters bundlerParams) {
        final ProgressTracker pt = new ProgressTracker();
        final ForceDirectedEdgeBundler bundler =
                new ForceDirectedEdgeBundler(flowMapModel, bundlerParams);
        ProgressWorker worker = new ProgressWorker(pt) {
            @Override
            public Object construct() {
                try {
                    bundler.bundle(getProgressTracker());
                } catch (Throwable th) {
                    logger.error("Bundling error", th);
                    JOptionPane.showMessageDialog(jFlowMap,
                            "Bundling error: [" + th.getClass().getSimpleName()+ "] " + th.getMessage()
                    );
                }
                return null;
            }
        };
        ProgressDialog dialog = new ProgressDialog(jFlowMap.getApp(), "Edge Bundling", worker, true);
        pt.addProgressListener(dialog);
        pt.addTaskCompletionListener(new TaskCompletionListener() {
            public void taskCompleted(int taskId) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        createEdgeVisuals();
                        bundled = true;
                        repaint();
                    }
                });
            }
        });
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

    public void aggregateBundledEdges() {
        if (!isBundled()) {
            return;
        }
        final EdgeSegmentAggregator aggregator = new EdgeSegmentAggregator(getModel());
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
                } catch (Throwable th) {
                    logger.error("Aggregation error", th);
                    JOptionPane.showMessageDialog(jFlowMap,
                            "Aggregation error: [" + th.getClass().getSimpleName()+ "] " + th.getMessage()
                    );
                }
                return null;
            }
        };
        ProgressDialog dialog = new ProgressDialog(
                jFlowMap.getApp(), "Edge Segment Aggregation", worker, true);
        pt.addProgressListener(dialog);
        worker.start();
        dialog.setVisible(true);
    }

    private void createAggregatedSegmentsVisuals(
            final List<EdgeSegment> segments) {
//        edgeLayer.removeAllChildren();
        clearAggregatedEdgesLayer();

        // create aggregated edge weight stats to normalize on them
        MinMax stats = MinMax.createFor(Iterators.transform(segments.iterator(), EdgeSegment.TRANSFORM_TO_WEIGHT));

        PNode parentNode = getAggregatedEdgesLayer();

        for (EdgeSegment seg : segments) {
//          if (seg.length() == 0) {
//              logger.warn("Zero-length segment: " + seg);
//          }

          double nv = stats.normalizeLog(seg.getWeight());
//          double nv = stats.normalize(seg.getWeight());
//          double nv = seg.getWeight();
          double width = 1 + nv * getModel().getMaxEdgeWidth();

          parentNode.addChild(new PSegment(seg, width));
      }
    }

    private static class PSegment extends PNode {
        private static final long serialVersionUID = 1L;
//        private static final Color JOINT_PT_COLOR = new Color(0, 0, 255, 150);
//        private static final Color SEGMENT_COLOR = new Color(255, 255, 255, 200);
        private static final float[] GRADIENT_FRACTIONS = new float[] { 0.0f, 1.0f };
//        private static final Color[] GRADIENT_COLORS = new Color[] { new Color(100, 100, 200, 200), new Color(200, 200, 100, 200) };
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
//            linep.setPaint(SEGMENT_COLOR);
//            linep.setStrokePaint(SEGMENT_COLOR);
            linep.setStrokePaint( new LinearGradientPaint(
                    (float)src.x(), (float)src.y(), (float)dest.x(), (float)dest.y(),
                    GRADIENT_FRACTIONS,
                    GRADIENT_COLORS
            ));
            addChild(linep);

//            PSegmentPoint srcp = new PSegmentPoint(src);
//            PSegmentPoint dstp = new PSegmentPoint(dest);
//            srcp.setPaint(JOINT_PT_COLOR);
//            dstp.setPaint(JOINT_PT_COLOR);
//            srcp.setStroke(null);
//            dstp.setStroke(null);
//            addChild(srcp);
//            addChild(dstp);

//            setPickable(true);
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

//                    PSegment pseg = PiccoloUtils.getParentNodeOfType(picked, PSegment.class);
//                    if (pseg != null) {
//                        logger.debug("MouseEntered: " + pseg.getSegment());
//                    }
                }
            }

            @Override
            public void mouseExited(PInputEvent event) {
            }
        };
    }


    public ClusterNode<VisualNode> getRootCluster() {
        return rootCluster;
    }

    public List<VisualNodeDistance> getNodeDistanceList() {
        return nodeDistanceList;
    }

    public double getMaxNodeDistance() {
//        if (Double.isNaN(maxNodeDistance)) {
//            maxNodeDistance = VisualNodeDistance.findMaxDistance(nodeDistanceList);
//        }
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

    public void setOriginalVisualFlowMap(VisualFlowMap originalVisualFlowMap) {
        this.flowMapBeforeJoining = originalVisualFlowMap;
    }

    public void joinClusterEdges() {
//        for (VisualNodeCluster cluster : visualNodeClusters) {
//            Point2D centroid = GeomUtils.centroid(
//                    Iterators.transform(
//                        cluster.iterator(), VisualNode.TRANSFORM_NODE_TO_POSITION
//                    )
//            );
//            PPath marker = new PPath(new Rectangle2D.Double(centroid.getX() - 4, centroid.getY() - 4, 8, 8));
//            marker.setPaint(Color.BLUE);
//            addChild(marker);
//        }

        Graph clusteredGraph = VisualNodeCluster.createClusteredFlowMap(visualNodeClusters);
        VisualFlowMap clusteredFlowMap = jFlowMap.createVisualFlowMap(
                FlowMapModel.DEFAULT_EDGE_WEIGHT_ATTR_NAME,
                FlowMapModel.DEFAULT_NODE_LABEL_ATTR_NAME,
                FlowMapModel.DEFAULT_NODE_X_ATTR_NAME,
                FlowMapModel.DEFAULT_NODE_Y_ATTR_NAME,
                0,
                clusteredGraph,
                areaMap == null ? null : (VisualAreaMap)areaMap.clone()
        );
        clusteredFlowMap.setOriginalVisualFlowMap(this);
        jFlowMap.setVisualFlowMap(clusteredFlowMap);
    }

    public void resetClusters() {
        removeClusterTags();
        rootCluster = null;
        euclideanRootCluster = null;
        visualNodeClusters = null;
    }

    public void resetJoinedNodes() {
        if (flowMapBeforeJoining != null) {
            jFlowMap.setVisualFlowMap(flowMapBeforeJoining);
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

}
