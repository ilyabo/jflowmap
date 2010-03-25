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

package jflowmap.visuals.timeline;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;

import jflowmap.FlowMapAttrsSpec;
import jflowmap.JFlowMap;
import jflowmap.JFlowTimeline;
import jflowmap.data.FlowMapLoader;
import jflowmap.data.FlowMapStats;
import jflowmap.data.FlowMapSummaries;
import jflowmap.data.MinMax;
import jflowmap.util.CollectionUtils;
import jflowmap.visuals.Tooltip;
import jflowmap.visuals.timeline.FloatingLabelsNode.LabelIterator;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.util.ColorLib;
import prefuse.util.ColorMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class VisualTimeline extends PNode {
    public static Logger logger = Logger.getLogger(VisualTimeline.class);

    enum Orientation { /* The timeline is */ VERTICAL, HORIZONTAL };
    private static final Orientation ORIENTATION = Orientation.HORIZONTAL;

    enum SortMode { QTY_IN_EACH_YEAR, QTY_IN_SELECTED_YEAR, HIERARCHY }
    private static final SortMode SORT_MODE = SortMode.HIERARCHY;

    private static final int ROW_CAPTION_TO_CELLS_X_GAP = 85;
    private static final int ROW_CAPTION_TO_CELLS_Y_GAP = 5;
    private static final Font CAPTION_FONT = new Font("Arial", Font.BOLD, 13);
    private static final Color CAPTION_COLOR = Color.black;
    private final List<Graph> graphs;
    private final FlowMapAttrsSpec attrSpecs;

    private final double cellWidth = 35;
    private final double cellHeight = 35;
    private final double cellSpacingX = 0; // 4
    private final double cellSpacingY = 0;
    private final JFlowTimeline jFlowTimeline;

    private final ColorMap sumOutgoingDiffColorMap;

    private FloatingLabelsNode elementLabels;

    private Graph selectedGraph;

    private final Tooltip tooltipBox;

    private final MinMax valueStats;


    public VisualTimeline(JFlowTimeline jFlowTimeline, Iterable<Graph> graphs, FlowMapAttrsSpec attrSpecs) {
        this.jFlowTimeline = jFlowTimeline;
        this.graphs = Lists.newArrayList(graphs);
        this.attrSpecs = attrSpecs;

        FlowMapStats gs = getGlobalStats();
        valueStats = gs
            .getNodeAttrStats(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING)
            .mergeWith(gs.getNodeAttrStats(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING))
            .mergeWith(gs.getNodeAttrStats(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING_INTRAREG))
            .mergeWith(gs.getNodeAttrStats(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING_INTRAREG))
        ;

        MinMax diffStats = gs.getNodeAttrStats(
                FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING_DIFF_TO_NEXT_YEAR);
        double diffR = Math.max(Math.abs(diffStats.getMin()), Math.abs(diffStats.getMax()));
        this.sumOutgoingDiffColorMap = new ColorMap(VisualTimelineNodeCell.DIFF_COLORS, -diffR, diffR);

        // TODO: implement selectedGraph
        if (this.graphs.size() > 0) {
            this.selectedGraph = this.graphs.get(this.graphs.size() - 1);
        }
        buildTimeline();

        tooltipBox = new Tooltip();
        tooltipBox.setVisible(false);
        tooltipBox.setPickable(false);


        jFlowTimeline.getCamera().addChild(tooltipBox);
    }

    public MinMax getValueStats() {
        return valueStats;
    }

    public ColorMap getSumOutgoingDiffColorMap() {
        return sumOutgoingDiffColorMap;
    }

    public FlowMapStats getGlobalStats() {
        return jFlowTimeline.getGlobalStats();
    }

    public FlowMapAttrsSpec getAttrSpecs() {
        return attrSpecs;
    }


    private void buildTimeline() {
        if (graphs.size() == 0) {
            return;
        }

        // TODO: build a joint nodeset (now the graphs are supposed to have exactly the same nodes)

        int i = 0;
        for (Graph g : graphs) {

            // Graph ID labels
            PText t = new PText(FlowMapLoader.getGraphId(g));
            t.setFont(CAPTION_FONT);
            t.setTextPaint(CAPTION_COLOR);
            double x = 0, y = 0;
            switch (ORIENTATION) {
                case VERTICAL:
                    x = ROW_CAPTION_TO_CELLS_X_GAP + t.getX() - t.getWidth();
                    y = cellSpacingY + i * (cellHeight + cellSpacingY) + (cellHeight - CAPTION_FONT.getSize2D())/2;
                    t.setJustification(JComponent.RIGHT_ALIGNMENT);
                    t.setBounds(x , y, t.getWidth(), t.getHeight());
                    break;
                case HORIZONTAL:
                    x = cellSpacingX + i * (cellWidth + cellSpacingX);
                    y = 0 ;
                    t.setJustification(JComponent.LEFT_ALIGNMENT);
                    t.setBounds(x + (cellWidth - t.getWidth())/2, y, t.getWidth(), t.getHeight());
                break;
            }
            addChild(t);

            // Cells
            Iterator<Integer> it = nodeIterator(g);
            int j = 0;
            while (it.hasNext()) {
                Node n = g.getNode(it.next());
                addChild(new VisualTimelineNodeCell(this, n, cellX(i, j), cellY(i, j), cellWidth, cellHeight));
                j++;
            }
            i++;
        }

        elementLabels = new FloatingLabelsNode(ORIENTATION == Orientation.VERTICAL, createElementsLabelIterator());
        jFlowTimeline.getCamera().addChild(elementLabels);

        jFlowTimeline.getCamera().addPropertyChangeListener(PCamera.PROPERTY_BOUNDS, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (ORIENTATION == Orientation.HORIZONTAL) {
                    elementLabels.setX(jFlowTimeline.getCamera().getWidth() - elementLabels.getWidth());
                }
            }
        });

    }

    private double cellX(int graphIndex, int nodeIndex) {
        switch (ORIENTATION) {
        case VERTICAL:
            return 10 + ROW_CAPTION_TO_CELLS_X_GAP + cellSpacingX + nodeIndex * (cellWidth + cellSpacingX);
        case HORIZONTAL:
            return cellSpacingX + graphIndex * (cellWidth + cellSpacingX);
        }
        throw new AssertionError();
    }


    private double cellY(int graphIndex, int nodeIndex) {
        switch (ORIENTATION) {
        case HORIZONTAL:
            return CAPTION_FONT.getSize2D() + ROW_CAPTION_TO_CELLS_Y_GAP + cellSpacingY + nodeIndex * (cellHeight + cellSpacingY);
        case VERTICAL:
            return cellSpacingY + graphIndex * (cellHeight + cellSpacingY);
        }
        throw new AssertionError();
    }

    private double cellOffset(int nodeIndex) {
        switch (ORIENTATION) {
        case VERTICAL:
            return cellX(0, nodeIndex);
        case HORIZONTAL:
            return cellY(0, nodeIndex);
        }
        throw new AssertionError();
    }

    private List<Integer> createHierarchyOrdering() {
        List<Integer> hierarchyOrdering = Lists.newArrayList();
        if (SORT_MODE == SortMode.HIERARCHY) {
            Set<Integer> usedIndices = Sets.newHashSet();
            for (Map.Entry<String, String> e : jFlowTimeline.getRegions().entrySet()) {
                int index = FlowMapLoader.findNodeIndexById(selectedGraph, e.getKey());
                if (index >= 0) {
                    hierarchyOrdering.add(index);
                    usedIndices.add(index);
                } else {
                    // ignore: we don't need the nodes which are not in the dataset
                }

            }
            // Ensure that all the nodes from the Graph were added to the list
            for (int i = 0, numNodes = selectedGraph.getNodeCount(); i < numNodes; i++) {
                if (!usedIndices.contains(i)) {
                    logger.warn("Node " + selectedGraph.getNode(i) + " not in the regions list");
                    throw new RuntimeException("Node " + selectedGraph.getNode(i) + " not in the regions list");
                }
            }
//          Collections.reverse(hierarchyOrdering);

        }
        return hierarchyOrdering;
    }

    @SuppressWarnings("unchecked")
    private Iterator<Integer> nodeIterator(Graph graph) {
        Iterator<Integer> it;
        switch (SORT_MODE) {
        case QTY_IN_EACH_YEAR:
            it = graph.getNodeTable().rowsSortedBy(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING, true);
            it = CollectionUtils.reverse(it);
            break;
        case QTY_IN_SELECTED_YEAR:
            it = selectedGraph.getNodeTable().rowsSortedBy(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING, true);
            it = CollectionUtils.reverse(it);
            break;
        case HIERARCHY:
            it = createHierarchyOrdering().iterator();
            break;
        default:
            throw new UnsupportedOperationException();
        }
        return it;
    }


    private LabelIterator createElementsLabelIterator() {
        return new LabelIterator() {
            Iterator<Integer> nodeIt = null;
            int nodeIndex = 0;
            double pos;
            Node node;

            private Iterator<Integer> nodeIt() {
                if (nodeIt == null) {
                    nodeIt = nodeIterator(selectedGraph);
                }
                return nodeIt;
            }

            public double getPosition() {
                return pos;
            }

            public double getSize() {
                return cellHeight;
            }

            public boolean hasNext() {
                return nodeIt().hasNext();
            }

            public String next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                node = selectedGraph.getNode(nodeIt().next());
                String label = node.getString(attrSpecs.getNodeLabelAttr());

                pos = cellOffset(nodeIndex);

                nodeIndex++;
                return label;
            }

            public void reset() {
                nodeIt = null;
                nodeIndex = 0;
                pos = Double.NaN;
            }

            @Override
            public Color getColor() {
                return ColorLib.getColor(node.getInt(JFlowTimeline.NODE_COLUMN__REGION_COLOR));
            }
        };
    }

    public void showTooltip(VisualTimelineNodeCell vc) {

        Node node = vc.getNode();
        double inValue = node.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING);
        double outValue = node.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING);
        double inLocalValue = node.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING_INTRAREG);
        double outLocalValue = node.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING_INTRAREG);
        double inDiffValue = node.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING_DIFF_TO_NEXT_YEAR);
        double outDiffValue = node.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING_DIFF_TO_NEXT_YEAR);

        tooltipBox.setText(
                FlowMapLoader.getGraphId(node.getGraph()) + "\n" +
                node.getString(attrSpecs.getNodeLabelAttr()),
                "Incoming: " + JFlowMap.NUMBER_FORMAT.format(inValue) + "\n" +
                "Outgoing: " + JFlowMap.NUMBER_FORMAT.format(outValue) + "\n" +
                "Incoming intrareg: " + JFlowMap.NUMBER_FORMAT.format(inLocalValue) + "\n" +
                "Outgoing intrareg: " + JFlowMap.NUMBER_FORMAT.format(outLocalValue)
//                 + "\n" + "Incoming diff to next: " + JFlowMap.NUMBER_FORMAT.format(inDiffValue) + "\n" +
//                "Outgoing diff to next: " + JFlowMap.NUMBER_FORMAT.format(outDiffValue)
                , null
        );

        PBounds b = vc.getBoundsReference();
        tooltipBox.showTooltipAt(b.getMaxX(), b.getMaxY(), 0, 0);
    }

    public void hideTooltip() {
        tooltipBox.setVisible(false);
    }
}

