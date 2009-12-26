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
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.Arrays;

import jflowmap.data.MinMax;
import jflowmap.geom.BSplinePath;
import jflowmap.geom.GeomUtils;
import jflowmap.geom.Point;
import jflowmap.models.FlowMapModel;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public abstract class VisualEdge extends PNode {

    private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(VisualEdge.class);

	private static final int MIN_COLOR_INTENSITY = 25;
	private static final float[] DEFAULT_GRADIENT_FRACTIONS = new float[] { 0.0f, 1.0f };
    private static final float MIN_FRACTION_DIFF = 1e-5f;

    private static final Color STROKE_HIGHLIGHTED_PAINT = new Color(0, 0, 255, 200);
    private static final Color STROKE_HIGHLIGHTED_INCOMING_PAINT = new Color(255, 0, 0, 200);
    private static final Color STROKE_HIGHLIGHTED_OUTGOING_PAINT = new Color(0, 255, 0, 200);

    private final VisualFlowMap visualFlowMap;

    private final VisualNode sourceNode;
    private final VisualNode targetNode;
    private final Edge edge;
    private final boolean isSelfLoop;

    private final double edgeLength;

    private PPath edgePPath;

    public VisualEdge(VisualFlowMap visualFlowMap, Edge edge, VisualNode sourceNode, VisualNode targetNode) {
        this.edge = edge;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.visualFlowMap = visualFlowMap;
        this.isSelfLoop = visualFlowMap.getModel().isSelfLoop(edge);
        if (isSelfLoop) {
            this.edgeLength = 0;
        } else {
            final double x1 = sourceNode.getValueX();
            final double y1 = sourceNode.getValueY();
            final double x2 = targetNode.getValueX();
            final double y2 = targetNode.getValueY();
            this.edgeLength = GeomUtils.distance(x1, y1, x2, y2);
        }

        addInputEventListener(visualEdgeListener);
    }

    public boolean isSelfLoop() {
        return isSelfLoop;
    }

    protected Shape createSelfLoopShape() {
        Shape shape;
        final double x1 = sourceNode.getValueX();
        final double y1 = sourceNode.getValueY();

//        shape = new Ellipse2D.Double(
//                x1 - SELF_LOOP_CIRCLE_SIZE/2, y1,
//                SELF_LOOP_CIRCLE_SIZE, SELF_LOOP_CIRCLE_SIZE);

//        final double size = SELF_LOOP_CIRCLE_SIZE;
        final double size = visualFlowMap.getStats().getEdgeLengthStats().getAvg() / 5;
//        MinMax xstats = visualFlowMap.getGraphStats().getNodeXStats();
//        MinMax ystats = visualFlowMap.getGraphStats().getNodeYStats();
//
//        final double xsize = (xstats.getMax() - xstats.getMin()) / 20;
//        final double ysize = (ystats.getMax() - ystats.getMin()) / 20;
        shape = new BSplinePath(Arrays.asList(new Point[] {
                new Point(x1, y1),
                new Point(x1 - size/2, y1 + size/2),
                new Point(x1, y1 + size),
                new Point(x1 + size/2, y1 + size/2),
                new Point(x1, y1)
        }));


        return shape;
    }



    public double getSourceX() {
        return sourceNode.getValueX();
    }

    public double getSourceY() {
        return sourceNode.getValueY();
    }

    public double getTargetX() {
        return targetNode.getValueX();
    }

    public double getTargetY() {
        return targetNode.getValueY();
    }

    protected void setEdgePPath(PPath ppath) {
        this.edgePPath = ppath;
    }

    protected PPath getEdgePPath() {
        return edgePPath;
    }

    public void updateEdgeWidth() {
        PPath ppath = getEdgePPath();
        if (ppath != null) {
            ppath.setStroke(createStroke());
        }
    }

//    public abstract void updateEdgeMarkerColors();

    public void updateVisibility() {
        final FlowMapModel model = visualFlowMap.getModel();
        double weightFilterMin = model.getEdgeWeightFilterMin();
        double weightFilterMax = model.getEdgeWeightFilterMax();

        double edgeLengthFilterMin = model.getEdgeLengthFilterMin();
        double edgeLengthFilterMax = model.getEdgeLengthFilterMax();
        final double weight = getEdgeWeight();
        double length = getEdgeLength();

        boolean visible =
                weightFilterMin <= weight && weight <= weightFilterMax    &&
                edgeLengthFilterMin <= length && length <= edgeLengthFilterMax
        ;

        if (visible) {
            if (visualFlowMap.hasClusters()) {
                VisualNodeCluster srcCluster = visualFlowMap.getNodeCluster(getSourceNode());
                VisualNodeCluster targetCluster = visualFlowMap.getNodeCluster(getTargetNode());

                // TODO: why do we need these null checks here? i.e. why some countries don't have a cluster
                visible = (srcCluster != null  &&  srcCluster.getTag().isVisible())  ||
                          (targetCluster != null  &&  targetCluster.getTag().isVisible());
            }
        }
        setVisible(visible);
        setPickable(visible);
        setChildrenPickable(visible);
    }

    public Edge getEdge() {
        return edge;
    }

    public VisualFlowMap getVisualFlowMap() {
        return visualFlowMap;
    }

    public String getLabel() {
        return sourceNode.getLabel() + "  -->  " +
               targetNode.getLabel();
    }

    public double getEdgeWeight() {
        return edge.getDouble(visualFlowMap.getModel().getEdgeWeightAttr());
    }

    public double getEdgeLength() {
        return edgeLength;
    }

    public VisualNode getSourceNode() {
        return sourceNode;
    }

    public VisualNode getTargetNode() {
        return targetNode;
    }

    /**
     * Returns source node if source is true or target node otherwise.
     */
    public VisualNode getNode(boolean source) {
        if (source) {
            return getSourceNode();
        } else {
            return getTargetNode();
        }
    }

    public VisualNode getOppositeNode(VisualNode node) {
        if (targetNode == node) {
            return sourceNode;
        }
        if (sourceNode == node) {
            return targetNode;
        }
        throw new IllegalArgumentException(
                "Node '" + node.getLabel() + "' is neither the source nor the target node of the edge '" + getLabel() + "'");
    }

    @Override
    public String toString() {
        return "VisualEdge{" +
                "label='" + getLabel() + "', " +
                "value=" + getEdgeWeight() +
        '}';
    }

    public double getNormalizedLogValue() {
        FlowMapModel model = getVisualFlowMap().getModel();
        double value = getEdgeWeight();
        double nv;
        if (model.getAutoAdjustEdgeColorScale()) {
            double minLog = 1.0;
            double maxLog = Math.log(model.getEdgeWeightFilterMax() - model.getEdgeWeightFilterMin());
            if (maxLog == minLog) {
                nv = 1.0;
            } else {
                nv = (Math.log(value - model.getEdgeWeightFilterMin()) - minLog) / (maxLog - minLog);
            }
        } else {
            MinMax stats = visualFlowMap.getStats().getEdgeWeightStats();
            nv = stats.normalizeLog(value);
        }
        if (Double.isNaN(nv)) {
            logger.error("NaN normalized log value for edge: " + this);
        }
        return nv;
    }

    public double getNormalizedValue() {
        double nv;

        MinMax stats = visualFlowMap.getStats().getEdgeWeightStats();
        nv = stats.normalize(getEdgeWeight());

        if (Double.isNaN(nv)) {
            logger.error("NaN normalized value for edge: " + this);
        }

        return nv;
    }

    protected Paint createPaint() {
		// TODO: use colors from color scheme
        FlowMapModel model = getVisualFlowMap().getModel();
        final double normalizedValue = getNormalizedLogValue();
        int intensity = (int)Math.round(MIN_COLOR_INTENSITY + (255 - MIN_COLOR_INTENSITY) * normalizedValue);
        int alpha = model.getEdgeAlpha();
        if (isSelfLoop) {
            return new Color(intensity, intensity, 0, alpha);	// mix of red and green
        } else {
        	if (!model.getShowDirectionMarkers()  &&  !model.getFillEdgesWithGradient()) {
        		return new Color(intensity, intensity, intensity, alpha);	// white
        	} else {
        		Color startEdgeColor, endEdgeColor;
        		if (model.getFillEdgesWithGradient()) {
    				startEdgeColor = new Color(intensity, 0, 0, alpha);
    				endEdgeColor = new Color(0, intensity, 0, alpha);
        		} else {
        			// TODO: use a special paint (not gradient) for this case
					startEdgeColor = new Color(intensity, intensity, intensity, alpha);
					endEdgeColor = startEdgeColor;
        		}

				float[] fractions = null;
        		Color[] colors = null;
	        	if (model.getShowDirectionMarkers()) {
	        		float markerSize;
		        	if (model.getUseProportionalDirectionMarkers()) {
		        		markerSize = (float)model.getDirectionMarkerSize();
		        	} else {
		        		MinMax lstats = visualFlowMap.getStats().getEdgeLengthStats();
						markerSize = (float)Math.min(
								.5 - MIN_FRACTION_DIFF,	 // the markers must not be longer than half of an edge
								((lstats.getMin() + model.getDirectionMarkerSize() * (lstats.getMax() - lstats.getMin()))
								/ 2)
								/ edgeLength	// the markers must be of equal length for every edge
												// (excepting the short ones)
						);
		        	}
		        	if (markerSize - MIN_FRACTION_DIFF < 0) {
		        		markerSize = MIN_FRACTION_DIFF;
		        	}
		        	if (markerSize > 0.5f - MIN_FRACTION_DIFF) {
		        		markerSize = 0.5f - MIN_FRACTION_DIFF;
		        	}
		            int markerAlpha = model.getDirectionMarkerAlpha();
		        	Color startMarkerColor = new Color(intensity, 0, 0, markerAlpha);
		        	Color endMarkerColor = new Color(0, intensity, 0, markerAlpha);
					fractions = new float[] {
							markerSize - MIN_FRACTION_DIFF,			// start marker
							markerSize, 1.0f - markerSize,			// line
							1.0f - markerSize + MIN_FRACTION_DIFF	// end marker
					};
					colors = new Color[] {
							startMarkerColor,
							startEdgeColor,
							endEdgeColor,
							endMarkerColor,
					};
	        	} else {
	        		fractions = DEFAULT_GRADIENT_FRACTIONS;
	        		colors = new Color[] { startEdgeColor, endEdgeColor };
	        	}
				return new LinearGradientPaint(
	                    (float)getSourceX(), (float)getSourceY(),
	                    (float)getTargetX(), (float)getTargetY(),
	                    fractions,
	                    colors
	            );
        	}

        }
    }

    public void updateEdgeColors() {
        PPath ppath = getEdgePPath();
        if (ppath != null) {
            ppath.setStrokePaint(createPaint());
        }
    }

    public void setHighlighted(boolean value, boolean showDirection, boolean asOutgoing) {
        PPath ppath = getEdgePPath();
        if (ppath != null) {
            Paint paint;
            if (value) {
                Color color;
                if (showDirection) {
                    color = (asOutgoing ? STROKE_HIGHLIGHTED_OUTGOING_PAINT : STROKE_HIGHLIGHTED_INCOMING_PAINT);
                } else {
                    color = STROKE_HIGHLIGHTED_PAINT;
                }
//                paint = getValueColor(color, false);
                paint = color;
            } else {
                paint = createPaint();
            }
            ppath.setStrokePaint(paint);
//            getSourceNode().setVisible(value);
//            getTargetNode().setVisible(value);
        }
    }

    protected Stroke createStroke() {
        double nv = getNormalizedValue();
        float width = (float)(1 + nv * getVisualFlowMap().getModel().getMaxEdgeWidth());
        return new PFixedWidthStroke(width);
//        return new BasicStroke(width);
    }

    public void update() {
        updateEdgeColors();
//        updateEdgeMarkerColors();
        updateEdgeWidth();
        updateVisibility();
    }

    private static final PInputEventListener visualEdgeListener = new PBasicInputEventHandler() {
        @Override
        public void mouseEntered(PInputEvent event) {
            VisualEdge ve = getParentVisualEdge(event.getPickedNode());
            if (ve != null) {
                ve.setHighlighted(true, false, false);
            }
            ve.getVisualFlowMap().showTooltip(ve, event.getPosition());
//            node.moveToFront();
        }

        @Override
        public void mouseExited(PInputEvent event) {
            VisualEdge ve = getParentVisualEdge(event.getPickedNode());
            if (!ve.getVisible()) {
                return;
            }
            if (ve != null) {
                ve.setHighlighted(false, false, false);
            }
            ve.getVisualFlowMap().hideTooltip();
        }
    };

    private static final VisualEdge getParentVisualEdge(PNode node) {
        PNode parent = node;
        while (parent != null && !(parent instanceof VisualEdge)) {
            parent = parent.getParent();
        }
        return (VisualEdge) parent;
    }

}
