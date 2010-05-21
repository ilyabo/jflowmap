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

package jflowmap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jflowmap.data.FlowMapStats;
import jflowmap.data.MinMax;
import jflowmap.geom.GeomUtils;
import jflowmap.geom.Point;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Ilya Boyandin
 */

public class FlowMapGraph {

    private static Logger logger = Logger.getLogger(FlowMapGraph.class);

    public static final String GRAPH_CLIENT_PROPERTY__ID = "id";
    public static final String GRAPH_NODE_TABLE_COLUMN_NAME__ID = "_node_id";

    private static final String SUBDIVISION_POINTS_ATTR_NAME = "_subdivp";

    private final Graph graph;
    private final FlowMapStats stats;
    private final FlowMapAttrSpec attrSpec;

    public FlowMapGraph(Graph graph, FlowMapAttrSpec attrSpec) {
        this.graph = graph;
        this.attrSpec = attrSpec;
        this.stats = FlowMapStats.createFor(new FlowMapGraphWithAttrSpecs(graph, attrSpec));
        logger.info("Edge weight stats: " + stats.getEdgeWeightStats());
    }

    public String getId() {
        return getGraphId(graph);
    }

    public String getXNodeAttr() {
        return attrSpec.getXNodeAttr();
    }

    public String getYNodeAttr() {
        return attrSpec.getYNodeAttr();
    }

    public String getEdgeWeightAttr() {
        return attrSpec.getEdgeWeightAttr();
    }

    public String getNodeLabelAttr() {
        return attrSpec.getNodeLabelAttr();
    }

    public FlowMapStats getStats() {
        return stats;
    }

    public FlowMapAttrSpec getAttrSpec() {
        return attrSpec;
    }

    public Graph getGraph() {
        return graph;
    }

    // TODO: create class FlowMapGraph encapsulating Graph and move these methods there
    public static String getGraphId(Graph graph) {
        return (String) graph.getClientProperty(GRAPH_CLIENT_PROPERTY__ID);
    }

    public static void setGraphId(Graph graph, String name) {
        graph.putClientProperty(GRAPH_CLIENT_PROPERTY__ID, name);
    }

    public static String getNodeId(Node node) {
        return node.getString(GRAPH_NODE_TABLE_COLUMN_NAME__ID);
    }

    public static Node findNodeById(Graph graph, String nodeId) {
        int index = findNodeIndexById(graph, nodeId);
        if (index >= 0) {
            return graph.getNode(index);
        }
        return null;
    }

    public static int findNodeIndexById(Graph graph, String nodeId) {
        for (int i = 0, len = graph.getNodeCount(); i < len; i++) {
            Node node = graph.getNode(i);
            if (nodeId.equals(getNodeId(node))) {
                return i;
            }
        }
        return -1;
    }

    public static <T> Set<T> getNodeAttrValues(Graph graph, String attrName) {
        return getNodeAttrValues(Arrays.asList(graph), attrName);
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> getNodeAttrValues(Iterable<Graph> graphs, String attrName) {
        Set<T> values = Sets.newLinkedHashSet();
        for (Graph g : graphs) {
            for (int i = 0, len = g.getNodeCount(); i < len; i++) {
                Node node = g.getNode(i);
                T v = (T) node.get(attrName);
                if (v != null) {
                    values.add(v);
                }
            }
        }
        return values;
    }

    public MinMax getEdgeLengthStats() {
        return stats.getEdgeLengthStats();
    }

    public double getEdgeWeight(Edge edge) {
        return edge.getDouble(attrSpec.getEdgeWeightAttr());
    }

    public List<Point> getEdgePoints(Edge edge) {
        List<Point> subdiv;
        if (hasEdgeSubdivisionPoints(edge)) {
            subdiv = getEdgeSubdivisionPoints(edge);
        } else {
            subdiv = Collections.emptyList();
        }
        List<Point> points = Lists.newArrayListWithExpectedSize(subdiv.size() + 2);
        points.add(getEdgeSourcePoint(edge));
        points.addAll(subdiv);
        points.add(getEdgeTargetPoint(edge));
        return points;
    }

    public boolean isSelfLoop(Edge edge) {
        Node src = edge.getSourceNode();
        Node target = edge.getTargetNode();
        if (src == target) {
            return true;
        }
        return GeomUtils.isSelfLoopEdge(
                src.getDouble(attrSpec.getXNodeAttr()), target.getDouble(attrSpec.getXNodeAttr()),
                src.getDouble(attrSpec.getYNodeAttr()), target.getDouble(attrSpec.getYNodeAttr())
        );
    }

    public boolean hasEdgeSubdivisionPoints(Edge edge) {
        return
            edge.canGet(SUBDIVISION_POINTS_ATTR_NAME, List.class)  &&
            // the above will return true after calling removeAllEdgeSubdivisionPoints(),
            // so we need to add the following null check:
            (edge.get(SUBDIVISION_POINTS_ATTR_NAME) != null);
    }

    @SuppressWarnings("unchecked")
    public List<Point> getEdgeSubdivisionPoints(Edge edge) {
        checkContainsEdge(edge);
        return (List<Point>) edge.get(SUBDIVISION_POINTS_ATTR_NAME);
    }

    public void setEdgeSubdivisionPoints(Edge edge, List<Point> points) {
        checkContainsEdge(edge);
        if (!graph.hasSet(SUBDIVISION_POINTS_ATTR_NAME)) {
            graph.addColumn(SUBDIVISION_POINTS_ATTR_NAME, List.class);
        }
        edge.set(SUBDIVISION_POINTS_ATTR_NAME, points);
    }

    public void removeAllEdgeSubdivisionPoints() {
        int numEdges = graph.getEdgeCount();
        for (int i = 0; i < numEdges; i++) {
            Edge edge = graph.getEdge(i);
            if (hasEdgeSubdivisionPoints(edge)) {
                edge.set(SUBDIVISION_POINTS_ATTR_NAME, null);
            }
        }
    }

    private void checkContainsEdge(Edge edge) {
        if (!graph.containsTuple(edge)) {
            throw new IllegalArgumentException("Edge is not in graph");
        }
    }

    public Point getEdgeSourcePoint(Edge edge) {
        Node src = edge.getSourceNode();
        return new Point(src.getDouble(attrSpec.getXNodeAttr()), src.getDouble(attrSpec.getYNodeAttr()));
    }

    public Point getEdgeTargetPoint(Edge edge) {
        Node target = edge.getTargetNode();
        return new Point(target.getDouble(attrSpec.getXNodeAttr()), target.getDouble(attrSpec.getYNodeAttr()));
    }
}
