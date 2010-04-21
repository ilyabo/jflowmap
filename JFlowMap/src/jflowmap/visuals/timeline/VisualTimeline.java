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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

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
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class VisualTimeline extends PNode {

    public static Logger logger = Logger.getLogger(VisualTimeline.class);

    enum SortMode { QTY_IN_EACH_YEAR, QTY_IN_SELECTED_YEAR, HIERARCHY }
//    private static final SortMode SORT_MODE = SortMode.HIERARCHY;

    private static final Font CAPTION_FONT = new Font("Arial", Font.BOLD, 13);
    private static final Color CAPTION_COLOR = Color.black;
    private final List<Graph> graphs;
    private final FlowMapAttrsSpec attrSpec;

    private final double cellWidth = 35;
    private final double cellHeight = 35;
    private final double cellSpacingX = 0; // 4
    private final double cellSpacingY = 0;
    private final JFlowTimeline jFlowTimeline;

//    private final ColorMap sumOutgoingDiffColorMap;

//    private Graph selectedGraph;
    private final Tooltip tooltipBox;
    private final MinMax valueMinMax;
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


    public FlowMapAttrsSpec getAttrSpecs() {
        return attrSpec;
    }


    private void buildTimeline() {
        if (graphs.size() == 0) {
            return;
        }


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


        // Graph ID labels
        int colIndex = 0;
        for (Graph g : graphs) {
            addChild(createCaption(colIndex, FlowMap.getGraphId(g)));
            colIndex++;
        }


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


    private PText createCaption(int i, String text) {
        PText t = new PText(text);
        t.setFont(CAPTION_FONT);
        t.setTextPaint(CAPTION_COLOR);
        double x = 0, y = 0;
        x = cellSpacingX + i * (cellWidth + cellSpacingX);
        y = 0 ;
        t.setJustification(JComponent.LEFT_ALIGNMENT);
        t.setBounds(x + container.getItemsOffsetX() + (cellWidth - t.getWidth())/2, y - t.getHeight()*1.5, t.getWidth(), t.getHeight());
        return t;
    }

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

        PBounds b = vc.getGlobalBounds();
        tooltipBox.showTooltipAt(b.getMaxX(), b.getMaxY(), 0, 0);
    }

    public void hideTooltip() {
        tooltipBox.setVisible(false);
    }

}

