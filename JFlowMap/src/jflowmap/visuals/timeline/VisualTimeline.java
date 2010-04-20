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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jflowmap.FlowMap;
import jflowmap.FlowMapAttrsSpec;
import jflowmap.JFlowMap;
import jflowmap.JFlowTimeline;
import jflowmap.data.FlowMapStats;
import jflowmap.data.FlowMapSummaries;
import jflowmap.data.MinMax;
import jflowmap.util.piccolo.PCollapsableItemsContainer;
import jflowmap.visuals.Tooltip;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.Node;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class VisualTimeline extends PNode {
    public static Logger logger = Logger.getLogger(VisualTimeline.class);

//    enum Orientation { /* The timeline is */ VERTICAL, HORIZONTAL };
//    private static final Orientation ORIENTATION = Orientation.HORIZONTAL;
//    private static final int COLLAPSE_ANIMATION_DURATION = 200;

    enum SortMode { QTY_IN_EACH_YEAR, QTY_IN_SELECTED_YEAR, HIERARCHY }
//    private static final SortMode SORT_MODE = SortMode.HIERARCHY;

//    private static final int ROW_CAPTION_TO_CELLS_X_GAP = 85;
//    private static final int ROW_CAPTION_TO_CELLS_Y_GAP = 5;
//    private static final Font CAPTION_FONT = new Font("Arial", Font.BOLD, 13);
//    private static final Color CAPTION_COLOR = Color.black;
    private final List<Graph> graphs;
    private final FlowMapAttrsSpec attrSpec;

    private final double cellWidth = 35;
    private final double cellHeight = 35;
    private final double cellSpacingX = 0; // 4
    private final double cellSpacingY = 0;
    private final JFlowTimeline jFlowTimeline;

//    private final ColorMap sumOutgoingDiffColorMap;

//    private FloatingLabelsNode elementLabels;

//    private Graph selectedGraph;
    private final Tooltip tooltipBox;
    private final MinMax valueMinMax;
//    private List<PRow> rows;
    private final List<Graph> groupedGraphs;
    private final String columnToGroupNodesBy;

    private final FlowMapStats globalStats;
    private FlowMapStats globalGroupedStats;

    private final PCollapsableItemsContainer container;


    public VisualTimeline(JFlowTimeline jFlowTimeline, Iterable<Graph> graphs, FlowMapAttrsSpec attrSpec,
            Iterable<Graph> groupedGraphs, String columnToGroupNodesBy) {
        this.jFlowTimeline = jFlowTimeline;
        this.graphs = Lists.newArrayList(graphs);
        this.attrSpec = attrSpec;
        this.groupedGraphs = (groupedGraphs != null ? Lists.newArrayList(groupedGraphs) : null);
        this.columnToGroupNodesBy = columnToGroupNodesBy;
        this.globalStats = FlowMapStats.createFor(graphs, attrSpec);
        for (Graph graph : graphs) {
            FlowMapSummaries.supplyNodesWithSummaries(graph, attrSpec);
        }
//        FlowMapSummaries.supplyNodesWithDiffs(graphs, attrSpec);
        if (groupedGraphs == null) {
            this.valueMinMax = nodeSummaryMinMax(globalStats);
        } else {
            for (Graph graph : groupedGraphs) {
                FlowMapSummaries.supplyNodesWithSummaries(graph, attrSpec);
            }
            this.globalGroupedStats = FlowMapStats.createFor(groupedGraphs, attrSpec);
            this.valueMinMax = nodeSummaryMinMax(globalStats).mergeWith(nodeSummaryMinMax(globalGroupedStats));
        }

//        MinMax diffStats = gs.getNodeAttrStats(
//                FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING_DIFF_TO_NEXT_YEAR);
//        double diffR = Math.max(Math.abs(diffStats.getMin()), Math.abs(diffStats.getMax()));
//        this.sumOutgoingDiffColorMap = new ColorMap(VisualTimelineNodeCell.DIFF_COLORS, -diffR, diffR);


        // TODO: implement selectedGraph
//        if (this.graphs.size() > 0) {
//            this.selectedGraph = this.graphs.get(this.graphs.size() - 1);
//        }

        tooltipBox = new Tooltip();
        tooltipBox.setVisible(false);
        tooltipBox.setPickable(false);

        jFlowTimeline.getCamera().addChild(tooltipBox);


        container = new PCollapsableItemsContainer();
        addChild(container);

        buildTimeline();

    }

    private MinMax nodeSummaryMinMax(FlowMapStats stats) {
        return stats
            .getNodeAttrStats(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING)
            .mergeWith(globalStats.getNodeAttrStats(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING))
            .mergeWith(globalStats.getNodeAttrStats(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING_INTRAREG))
            .mergeWith(globalStats.getNodeAttrStats(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING_INTRAREG));
    }

    public MinMax getValueStats() {
        return valueMinMax;
    }

//    public ColorMap getSumOutgoingDiffColorMap() {
//        return sumOutgoingDiffColorMap;
//    }

//    public FlowMapStats getGlobalStats() {
//        return globalStats;
//    }

    public FlowMapAttrsSpec getAttrSpecs() {
        return attrSpec;
    }


    private void buildTimeline() {
        if (graphs.size() == 0) {
            return;
        }

        // Graph ID labels
//        int colIndex = 0;
//        for (Graph g : graphs) {
//            addChild(createCaption(colIndex, FlowMap.getGraphId(g), ORIENTATION == Orientation.HORIZONTAL));
//            colIndex++;
//        }


//        rows = Lists.newArrayList();

        if (groupedGraphs == null) {
            // Build a joint nodeset (the graphs do not necessarily have exactly the same nodes)
//            Map<String, String> nodeIdsToLabels = nodeIdsToAttrValues(graphs, attrSpec.getNodeLabelAttr());
//
//            int rowIndex = 0;
//            for (Map.Entry<String, String> e : nodeIdsToLabels.entrySet()) {
//                String nodeId = e.getKey();
//                String nodeLabel = e.getValue();
//
//                final PRow row = createRow(nodeLabel, graphs, rowIndex, nodeId);
//                rows.add(row);
//                addChild(row);
//
//                rowIndex++;
//            }

        } else {
            Map<String, String> groupIdsToLabels = nodeIdsToAttrValues(groupedGraphs, attrSpec.getNodeLabelAttr());
            Multimap<String, String> groupsToNodeIds = groupsToNodeIds(graphs, columnToGroupNodesBy);

            List<String> groupIds = Lists.newArrayList(groupIdsToLabels.keySet());
            Collections.sort(groupIds);

            int rowIndex = 0;
            for (String group: groupIds) {
                String groupLabel = groupIdsToLabels.get(group);

                PNode head = createRow(groupedGraphs, rowIndex, group);
                PNode body = new PNode();
                int i = 1;
                for (String nodeId : groupsToNodeIds.get(group)) {
                    // label: nodeId
                    body.addChild(createRow(graphs, i, nodeId));
                    i++;
                }
                container.addNewItem(groupLabel, head, body);

//                row.getLabel().addInputEventListener(new PBasicInputEventHandler() {
//                    @Override
//                    public void mouseClicked(PInputEvent event) {
//                        PText text = PNodes.getAncestorOfType(event.getPickedNode(), PText.class);
//                        if (text != null) {
//                            row.toggleCollapsed();
//                        }
//                    }
//                });

                rowIndex++;
            }
        }


        container.layoutItems();



//        elementLabels = new FloatingLabelsNode(ORIENTATION == Orientation.VERTICAL, createElementsLabelIterator());
//        jFlowTimeline.getCamera().addChild(elementLabels);
//
//        jFlowTimeline.getCamera().addPropertyChangeListener(PCamera.PROPERTY_BOUNDS, new PropertyChangeListener() {
//            @Override
//            public void propertyChange(PropertyChangeEvent evt) {
//                if (ORIENTATION == Orientation.HORIZONTAL) {
//                    elementLabels.setX(jFlowTimeline.getCamera().getWidth() - elementLabels.getWidth());
//                }
//            }
//        });

    }

    private PNode createRow(Iterable<Graph> graphs, int nodeIndex, String nodeId) {
        final PNode row = new PNode();
        int graphIndex = 0;
        for (Graph g : graphs) {
            Node node = FlowMap.findNodeById(g, nodeId);
            double x = cellSpacingX + graphIndex * (cellWidth + cellSpacingX);
            double y = cellSpacingY + nodeIndex * (cellHeight + cellSpacingY);
            row.addChild(new VisualTimelineNodeCell(this, node, x, y, cellWidth, cellHeight));
            row.setBounds(row.getFullBoundsReference());
            graphIndex++;
        }
        return row;
    }

    private static Map<String, String> nodeIdsToAttrValues(Iterable<Graph> graphs, String attr) {
        Map<String, String> nodeIdsToLabels = Maps.newLinkedHashMap();
        for (Graph g : graphs) {
            for (int i = 0, numNodes = g.getNodeCount(); i < numNodes; i++) {
                Node node = g.getNode(i);
                nodeIdsToLabels.put(FlowMap.getNodeId(node), node.getString(attr));
            }
        }
        return nodeIdsToLabels;
    }

    @SuppressWarnings("unchecked")
    private static <T> Multimap<T, String> groupsToNodeIds(Iterable<Graph> graphs, String columnToGroupNodesBy) {
        Multimap<T, String> map = LinkedHashMultimap.create();
        for (Graph g : graphs) {
            for (int i = 0, numNodes = g.getNodeCount(); i < numNodes; i++) {
                Node node = g.getNode(i);
                map.put((T)node.get(columnToGroupNodesBy), FlowMap.getNodeId(node));
            }
        }
        return map;
    }

//    private static Set<String> nodeIdsByGroup(Iterable<Graph> graphs, String groupByAtrr, String groupToFind) {
//        Set<String> nodeIds = Sets.newLinkedHashSet();
//        for (Graph g : graphs) {
//            for (int i = 0, numNodes = g.getNodeCount(); i < numNodes; i++) {
//                Node node = g.getNode(i);
//                if (groupToFind.equals(node.getString(groupByAtrr))) {
//                    nodeIds.add(FlowMap.getNodeId(node));
//                }
//            }
//        }
//        return nodeIds;
//    }


//    private PText createCaption(int i, String text, boolean horizontalNotVertical) {
//        PText t = new PText(text);
//        t.setFont(CAPTION_FONT);
//        t.setTextPaint(CAPTION_COLOR);
//        double x = 0, y = 0;
//        if (horizontalNotVertical) {
//            x = cellSpacingX + i * (cellWidth + cellSpacingX);
//            y = 0 ;
//            t.setJustification(JComponent.LEFT_ALIGNMENT);
//            t.setBounds(x + (cellWidth - t.getWidth())/2, y, t.getWidth(), t.getHeight());
//        } else {
//            x = t.getX() - t.getWidth();
//            y = cellY(0, i) + (cellHeight - CAPTION_FONT.getSize2D())/2;
//            t.setJustification(JComponent.RIGHT_ALIGNMENT);
//            t.setBounds(x, y, t.getWidth(), t.getHeight());
//        }
//        return t;
//    }
//
//
//    private double cellX(int graphIndex, int nodeIndex) {
//        switch (ORIENTATION) {
//        case VERTICAL:
//            return 10 + ROW_CAPTION_TO_CELLS_X_GAP + cellSpacingX + nodeIndex * (cellWidth + cellSpacingX);
//        case HORIZONTAL:
//            return cellSpacingX + graphIndex * (cellWidth + cellSpacingX);
//        }
//        throw new AssertionError();
//    }
//
//
//    private double cellY(int graphIndex, int nodeIndex) {
//        switch (ORIENTATION) {
//        case HORIZONTAL:
//            return CAPTION_FONT.getSize2D() + ROW_CAPTION_TO_CELLS_Y_GAP + cellSpacingY + nodeIndex * (cellHeight + cellSpacingY);
//        case VERTICAL:
//            return cellSpacingY + graphIndex * (cellHeight + cellSpacingY);
//        }
//        throw new AssertionError();
//    }
//
//    private double cellOffset(int nodeIndex) {
//        switch (ORIENTATION) {
//        case VERTICAL:
//            return cellX(0, nodeIndex);
//        case HORIZONTAL:
//            return cellY(0, nodeIndex);
//        }
//        throw new AssertionError();
//    }

//    private List<Integer> createHierarchyOrdering() {
//        List<Integer> hierarchyOrdering = Lists.newArrayList();
//        Set<Integer> usedIndices = Sets.newHashSet();
//        for (Map.Entry<String, String> e : jFlowTimeline.getRegions().entrySet()) {
//            int index = FlowMap.findNodeIndexById(selectedGraph, e.getKey());
//            if (index >= 0) {
//                hierarchyOrdering.add(index);
//                usedIndices.add(index);
//            } else {
//                // ignore: we don't need the nodes which are not in the dataset
//            }
//
//        }
//        // Sanity check: Ensure that all the nodes from the Graph were added to the list
//        for (int i = 0, numNodes = selectedGraph.getNodeCount(); i < numNodes; i++) {
//            if (!usedIndices.contains(i)) {
//                logger.warn("Node " + selectedGraph.getNode(i) + " not in the regions list");
//                throw new RuntimeException("Node " + selectedGraph.getNode(i) + " not in the regions list");
//            }
//        }
////      Collections.reverse(hierarchyOrdering);
//
//        return hierarchyOrdering;
//    }

//    @SuppressWarnings("unchecked")
//    private Iterator<Integer> nodeIterator(Graph graph) {
//        Iterator<Integer> it;
//        switch (SORT_MODE) {
//        case QTY_IN_EACH_YEAR:
//            it = graph.getNodeTable().rowsSortedBy(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING, true);
//            it = CollectionUtils.reverse(it);
//            break;
//        case QTY_IN_SELECTED_YEAR:
//            it = selectedGraph.getNodeTable().rowsSortedBy(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING, true);
//            it = CollectionUtils.reverse(it);
//            break;
//        case HIERARCHY:
//            it = createHierarchyOrdering().iterator();
//            break;
//        default:
//            throw new UnsupportedOperationException();
//        }
//        return it;
//    }


//    private LabelIterator createElementsLabelIterator() {
//        return new LabelIterator() {
//            Iterator<Integer> nodeIt = null;
//            int nodeIndex = 0;
//            double pos;
//            Node node;
//
//            private Iterator<Integer> nodeIt() {
//                if (nodeIt == null) {
//                    nodeIt = nodeIterator(selectedGraph);
//                }
//                return nodeIt;
//            }
//
//            public double getPosition() {
//                return pos;
//            }
//
//            public double getSize() {
//                return cellHeight;
//            }
//
//            public boolean hasNext() {
//                return nodeIt().hasNext();
//            }
//
//            public String next() {
//                if (!hasNext()) {
//                    throw new NoSuchElementException();
//                }
//                node = selectedGraph.getNode(nodeIt().next());
//                String label = node.getString(attrSpec.getNodeLabelAttr());
//
//                pos = cellOffset(nodeIndex);
//
//                nodeIndex++;
//                return label;
//            }
//
//            public void reset() {
//                nodeIt = null;
//                nodeIndex = 0;
//                pos = Double.NaN;
//            }
//
//            @Override
//            public Color getColor() {
//                return ColorLib.getColor(node.getInt(JFlowTimeline.NODE_COLUMN__REGION_COLOR));
//            }
//        };
//    }

    public void showTooltip(VisualTimelineNodeCell vc) {
        Node node = vc.getNode();
        double inValue = node.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING);
        double outValue = node.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING);
        double inLocalValue = node.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING_INTRAREG);
        double outLocalValue = node.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING_INTRAREG);
//        double inDiffValue = node.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING_DIFF_TO_NEXT_YEAR);
//        double outDiffValue = node.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING_DIFF_TO_NEXT_YEAR);

        tooltipBox.setText(
                FlowMap.getGraphId(node.getGraph()) + "\n" +
                node.getString(attrSpec.getNodeLabelAttr()),
                "Incoming: " + JFlowMap.NUMBER_FORMAT.format(inValue) + "\n" +
                "Outgoing: " + JFlowMap.NUMBER_FORMAT.format(outValue) + "\n" +
                "Incoming intrareg: " + JFlowMap.NUMBER_FORMAT.format(inLocalValue) + "\n" +
                "Outgoing intrareg: " + JFlowMap.NUMBER_FORMAT.format(outLocalValue)
//                 + "\n" + "Incoming diff to next: " + JFlowMap.NUMBER_FORMAT.format(inDiffValue) + "\n" +
//                "Outgoing diff to next: " + JFlowMap.NUMBER_FORMAT.format(outDiffValue)
                , null
        );

        PBounds b = vc.getGlobalBounds(); // getBoundsReference();
//        Point2D off = vc.getOffset();
        tooltipBox.showTooltipAt(b.getMaxX(), b.getMaxY(), 0, 0);
//        tooltipBox.showTooltipAt(off.getX() + b.getMaxX(), off.getY() + b.getMaxY(), 0, 0);
//        PAffineTransform t = vc.getTransformReference(true);
//        off = t.transform(off, new Point2D.Double());
//        tooltipBox.showTooltipAt(b.getMaxX(), b.getMaxY(), 0, 0);
    }

    public void hideTooltip() {
        tooltipBox.setVisible(false);
    }



//    class PRow extends PNode {
//        private boolean collapsed = true;
//        private final int index;
//        private PActivity lastActivity;
//        private int numSubRows = 0;
//        private final PText labelNode;
//
//        public PRow(String label, int index) {
//            this.index = index;
//
//            labelNode = createCaption(index, label, (ORIENTATION == Orientation.VERTICAL));
//            addChild(labelNode);
//        }
//
//        public PNode getLabel() {
//            return labelNode;
//        }
//
//        public void addSubRow(PRow sub) {
//            addChild(sub);
////            sub.setVisible(false);
////            sub.offset(0, getY() + getHeight());
////            sub.offset(getX() + getWidth(), 0);
//            numSubRows++;
//        }
//
//        private PAffineTransform shift(PAffineTransform t) {
//            PAffineTransform st = new PAffineTransform();
//            switch (ORIENTATION) {
//            case HORIZONTAL:
//                st.setOffset(0, (collapsed ? +1 : -1) * getSubRowsUnionBounds().getHeight());
//                break;
//            case VERTICAL:
//                st.setOffset((collapsed ? +1 : -1) * getSubRowsUnionBounds().getWidth(), 0);
//                break;
//            }
//            st.concatenate(t);
//            return st;
//        }
//
//        @Override
//        public boolean addActivity(PActivity activity) {
//            if (super.addActivity(activity)) {
//                lastActivity = activity;
//                return true;
//            } else {
//                return false;
//            }
//        }
//
//        public void toggleCollapsed() {
//            for (int i = index + 1, numRows = rows.size(); i < numRows; i++) {
//                PRow row = rows.get(i);
//                row.terminateIfStepping();
//                row.animateToTransform(shift(row.getTransform()), COLLAPSE_ANIMATION_DURATION);
//            }
//
//            collapsed = !collapsed;
////            setSubRowsVisibile(!collapsed);
//        }
//
//        private void setSubRowsVisibile(boolean v) {
//            for (int i = 0, numChildren = getChildrenCount(); i < numChildren; i++) {
//                PNode ch = getChild(i);
//                if (ch instanceof PRow) {
//                    ch.setVisible(v);
//                }
//            }
//        }
//
//        private PBounds getSubRowsUnionBounds() {
//            PBounds b = new PBounds();
//            for (int i = 0, numChildren = getChildrenCount(); i < numChildren; i++) {
//                PNode row = getChild(i);
//                if (row instanceof PRow) {
//                    b.add(row.getFullBoundsReference());
//                }
//            }
//            return b;
//        }
//
//        private void terminateIfStepping() {
//            if (lastActivity != null && lastActivity.isStepping()) {
//                lastActivity.terminate(PActivity.TERMINATE_AND_FINISH_IF_STEPPING);
//                lastActivity = null;
//            }
//        }
//
//    }
}

